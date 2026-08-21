package com.tillzo.pos.data.sync.options.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.tillzo.pos.data.local.AppDatabase
import com.tillzo.pos.data.local.SyncLogEntity
import com.tillzo.pos.data.local.entity.WastageEntity
import com.tillzo.pos.data.local.entity.toSheetRow
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.domain.sync.DataSyncInterface
import com.tillzo.pos.domain.sync.SyncPayload
import com.tillzo.pos.domain.sync.SyncResult
import com.tillzo.pos.domain.sync.usecase.CategoryUpsertUseCase
import com.tillzo.pos.domain.sync.usecase.InventoryUpsertUseCase
import com.tillzo.pos.domain.sync.usecase.KhataEventUseCase
import com.tillzo.pos.domain.sync.usecase.SalesUploadUseCase
import com.tillzo.pos.domain.sync.usecase.SchemaGuardUseCase
import com.tillzo.pos.domain.sync.usecase.ProductUnitUpsertUseCase
import com.tillzo.pos.domain.sync.usecase.VendorUpsertUseCase
import com.tillzo.pos.domain.sync.usecase.CustomerUpsertUseCase
import com.tillzo.pos.domain.sync.usecase.ExpenseUpsertUseCase
import com.tillzo.pos.utils.AppLogger
import com.tillzo.pos.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SyncWorker — M2.1, M2.3, M2.4
 *
 * Scheduled by WorkerModule as TWO separate PeriodicWorkRequests (OR constraint pattern):
 *   SyncWorker_charging → requiresCharging(true)
 *   SyncWorker_idle     → requiresDeviceIdle(true)
 *
 * Flow per execution:
 *   1. Read all pending tables from SyncLogDao
 *   2. For each table: delegate to specific UseCases (Sales, Inventory, Khata)
 *   3. HTTP 200 → marked as synced inside the UseCase
 *   4. After sales sync: deduct stock for each synced sale item (Blind Selling rule)
 *   5. After all tables: run SchemaGuardUseCase to verify tabs exist and are hidden
 *   6. Upload any pending wastage records
 *   7. Any failure → Result.retry() — WorkManager applies exponential backoff (5s→15s→1m→5m)
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val appDatabase: AppDatabase,
    private val salesUploadUseCase: SalesUploadUseCase,
    private val inventoryUpsertUseCase: InventoryUpsertUseCase,
    private val khataEventUseCase: KhataEventUseCase,
    private val categoryUpsertUseCase: CategoryUpsertUseCase,
    private val productUnitUpsertUseCase: ProductUnitUpsertUseCase,
    private val vendorUpsertUseCase: VendorUpsertUseCase,
    private val customerUpsertUseCase: CustomerUpsertUseCase,
    private val expenseUpsertUseCase: ExpenseUpsertUseCase,
    private val schemaGuardUseCase: SchemaGuardUseCase,
    private val sheetsRepository: SheetsRepository,
    private val sheetsRemoteDataSource: SheetsRemoteDataSource,
    private val notificationHelper: NotificationHelper,
    private val appSetupPrefs: AppSetupPrefs,
    private val appLogger: AppLogger
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "SyncWorker"
    }

    private val gson = Gson()

    private suspend fun ensureCoreTables(syncLogDao: com.tillzo.pos.data.local.dao.SyncLogDao) {
        syncLogDao.ensureTableRegistered("Sales")
        syncLogDao.ensureTableRegistered("Inventory")
        syncLogDao.ensureTableRegistered("KhataEvents")
        syncLogDao.ensureTableRegistered("Categories")
        syncLogDao.ensureTableRegistered("Product_Units")
        syncLogDao.ensureTableRegistered("Vendors")
        syncLogDao.ensureTableRegistered("Customers")
        syncLogDao.ensureTableRegistered("Expenses")
        // FIX (2026-08-06): Users_Permissions was never registered → local user
        // changes (roles, new users) never uploaded to the Sheet. Now tracked.
        syncLogDao.ensureTableRegistered("Users")
        syncLogDao.ensureTableRegistered("Time_Clock")
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "SyncWorker started (attempt #${runAttemptCount + 1})")
        appLogger.logInfo("SYNC_PROCESS", "SyncWorker started (attempt #${runAttemptCount + 1})")

        if (appSetupPrefs.spreadsheetId.isBlank()) {
            Log.w(TAG, "spreadsheetId not configured — skipping sync. Setup not completed.")
            appLogger.logWarn("SYNC_PROCESS", "spreadsheetId not configured — skipping sync")
            return@withContext Result.success()
        }

        try {
            val syncLogDao = appDatabase.syncLogDao()

            // CL — Rolling retention cleanup: delete logs older than 48 hours
            try {
                val cutoff = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L)
                appDatabase.logDao().deleteLogsOlderThan(cutoff)
            } catch (e: Exception) {
                Log.w(TAG, "Log retention cleanup failed: ${e.message}")
            }

            // ── Step 0: Ensure core tables are tracked before fetching ────────
            ensureCoreTables(syncLogDao)

            // ── Step 1: Get all tracked tables ──────────────────────────────
            val tables = syncLogDao.getAllTrackedTables()

            if (tables.isEmpty()) {
                Log.d(TAG, "No tables registered yet — skipping upload.")
                appLogger.logInfo("SYNC_PROCESS", "No tables registered — skipping upload")
                verifyAndHideSysDbTab()
                return@withContext Result.success()
            }

            var anyFailure = false

            // M11 Explicit Sync Methods
            uploadPendingPurchaseOrders()
            uploadPendingGRNs()
            if (!uploadPendingProductBatches()) anyFailure = true
            uploadPendingStockAdjustments()
            uploadPendingTillSessions()
            uploadPendingWastage()

            // ── Step 2: Upload pending rows per table ────────────────────────
            for (tableName in tables) {
                val uploadResult = uploadTable(tableName)
                if (uploadResult) {
                    syncLogDao.markTableSynced(tableName, System.currentTimeMillis())
                    Log.d(TAG, "Table $tableName synced successfully")
                    appLogger.logInfo("SYNC_PROCESS", "Table $tableName synced successfully")
                } else {
                    syncLogDao.markTableFailed(tableName)
                    Log.w(TAG, "Table $tableName sync failed — will retry")
                    appLogger.logWarn("SYNC_PROCESS", "Table $tableName sync failed — will retry")
                    anyFailure = true
                }
            }

            // M6.6 Log low stock count
            val lowStockCount = appDatabase.inventoryDao().getLowStockItemsAsList().size
            if (lowStockCount > 0) {
                Log.w(TAG, "M6.6 ALERT: $lowStockCount items are currently below their low stock threshold!")
                appLogger.logWarn("SYNC_PROCESS", "Low stock alert: $lowStockCount items below threshold")
            }

            // ── Step 5: M2.3 schema maintenance ─────────────────────────────
            verifyAndHideSysDbTab()

            if (anyFailure) {
                Log.w(TAG, "Some tables failed — scheduling retry with exponential backoff")
                appLogger.logWarn("SYNC_PROCESS", "Some tables failed — scheduling retry")
                Result.retry()
            } else {
                Log.d(TAG, "SyncWorker completed successfully")
                appLogger.logInfo("SYNC_PROCESS", "SyncWorker completed successfully")
                try {
                    sheetsRepository.updateLastUpdatedTimestamp(System.currentTimeMillis())
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to update Settings timestamp: ${e.message}")
                }
                Result.success()
            }

        } catch (e: Exception) {
            Log.e(TAG, "SyncWorker unexpected error: ${e.message}", e)
            appLogger.logError("SYNC_PROCESS", "SyncWorker unexpected error: ${e.message}", e)
            if (runAttemptCount < 4) Result.retry() else Result.failure()
        }
    }

    /**
     * Fallback stock deduction for sales that were NOT deducted at sale time
     * (e.g. legacy pending sales from before the local-first refactor).
     * Runs under an atomic Room transaction so partial failures roll back.
     * Handles JsonSyntaxException safely without skipping DB rollbacks.
     */
    private suspend fun deductStockForSyncedSales(itemsJsonList: List<String>) {
        val inventoryDao   = appDatabase.inventoryDao()
        val productBatchDao = appDatabase.productBatchDao()
        val listType = object : TypeToken<List<Map<String, Any>>>() {}.type

        try {
            appDatabase.withTransaction {
                for (itemsJson in itemsJsonList) {
                    val items: List<Map<String, Any>>
                    try {
                        items = gson.fromJson(itemsJson, listType) ?: continue
                    } catch (e: JsonSyntaxException) {
                        Log.w(TAG, "Skipping sale with unparseable items_json: ${e.message}")
                        continue
                    }

                    for (cartItem in items) {
                        val productId = cartItem["itemId"] as? String ?: continue
                        val qtySold = (cartItem["quantity"] as? Double)
                            ?: (cartItem["quantity"] as? Number)?.toDouble()
                            ?: continue

                        val item = inventoryDao.getItemById(productId)
                        if (item == null) {
                            Log.w(TAG, "Product not found in local inventory for stock deduction: $productId — skipping")
                            continue
                        }

                        val newStock = maxOf(0.0, item.current_stock - qtySold)
                        inventoryDao.updateStockAndSyncStatus(productId, newStock)

                        // Fire notifications if thresholds crossed
                        if (newStock <= 0.0) {
                            notificationHelper.outOfStockAlert(item.item_name)
                        } else if (newStock <= item.low_stock_threshold) {
                            notificationHelper.lowStockAlert(item.item_name, newStock, item.unit)
                        }

                        // FIFO batch deduction if product has batches
                        if (item.hasBatches) {
                            var remaining = qtySold
                            while (remaining > 0.0) {
                                val oldestBatch = productBatchDao.getOldestActiveBatch(productId) ?: break
                                val deductFromBatch = minOf(remaining, oldestBatch.stockQty)
                                val newBatchQty = oldestBatch.stockQty - deductFromBatch
                                val now = System.currentTimeMillis()
                                if (newBatchQty <= 0.0) {
                                    productBatchDao.deactivateBatch(oldestBatch.batchId, now)
                                } else {
                                    productBatchDao.updateBatchStock(oldestBatch.batchId, newBatchQty, now)
                                }
                                remaining -= deductFromBatch
                            }

                            val allBatches = productBatchDao.getAllBatchesForProduct(productId)
                            val total = allBatches.filter { it.isActive && !it.isDeleted }.sumOf { it.stockQty }
                            inventoryDao.updateTotalStockAndSyncStatus(productId, total, System.currentTimeMillis())
                        }

                        Log.d(TAG, "Fallback stock deducted: $productId → ${item.current_stock} - $qtySold = $newStock")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback stock deduction transaction failed — will retry: ${e.message}", e)
        }
    }

    /**
     * Uploads pending rows for a given table by delegating to specific UseCases.
     */
    private suspend fun uploadTable(tableName: String): Boolean {
        val posTerminalId = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }
        return when (tableName) {
            "Sales"       -> salesUploadUseCase(tableName, posTerminalId)
            "Inventory"   -> inventoryUpsertUseCase(posTerminalId)
            "KhataEvents" -> khataEventUseCase(tableName, posTerminalId)
            "Categories"  -> categoryUpsertUseCase(posTerminalId)
            "Product_Units" -> productUnitUpsertUseCase(posTerminalId)
            "Customers"   -> customerUpsertUseCase(posTerminalId)
            "Expenses"    -> expenseUpsertUseCase(posTerminalId)
            "Vendors"     -> vendorUpsertUseCase.invoke()
            // FIX (2026-08-06): Users_Permissions upload — previously fell into the
            // `else` branch (silently "passed" without uploading anything). Now
            // pending users (new/role-changed) are appended to the Users_Permissions tab.
            "Users", "Users_Permissions" -> uploadPendingUsers()
            "Time_Clock" -> uploadPendingTimeClock()
            else -> {
                Log.w(TAG, "Unknown table name for sync: $tableName")
                true
            }
        }
    }

    private suspend fun uploadPendingUsers(): Boolean {
        return try {
            val pending = appDatabase.userDao().getPendingSyncUsers()
            if (pending.isEmpty()) return true
            val rows = pending.map { u ->
                listOf(
                    u.system_row_id, u.email, u.name, u.role,
                    u.password_hash ?: "", u.permissions_json ?: "",
                    if (u.is_deleted) 1 else 0, u.deleted_at ?: "",
                    u.sync_status, u.pos_terminal_id, u.created_at, u.updated_at
                )
            }
            val result = sheetsRepository.uploadBatch("Users_Permissions", rows)
            if (result is SyncResult.Success) {
                pending.forEach { u ->
                    appDatabase.userDao().updateUser(u.copy(sync_status = "synced"))
                }
                Log.d(TAG, "Users_Permissions uploaded: ${pending.size}")
                true
            } else {
                Log.w(TAG, "Users_Permissions upload failed")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Users_Permissions upload error", e)
            false
        }
    }

    // FIX (2026-08-06): employee time-tracking cloud sync
    private suspend fun uploadPendingTimeClock(): Boolean {
        return try {
            val pending = appDatabase.timeClockDao().getPendingPunches()
            if (pending.isEmpty()) return true
            val rows = pending.map { p ->
                listOf(
                    p.system_row_id, p.employee_email, p.employee_name, p.event_type,
                    p.timestamp, p.note ?: "", p.pos_terminal_id, p.created_at,
                    p.updated_at, "synced"
                )
            }
            val result = sheetsRepository.uploadBatch("Time_Clock", rows)
            if (result is SyncResult.Success) {
                pending.forEach { p -> appDatabase.timeClockDao().markSynced(p.system_row_id) }
                Log.d(TAG, "Time_Clock uploaded: ${pending.size}")
                true
            } else {
                Log.w(TAG, "Time_Clock upload failed")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Time_Clock upload error", e)
            false
        }
    }

    /**
     * M2.3 — Validates that all required tabs exist in the remote sheet
     * and that SYS_DB_DO_NOT_TOUCH is hidden. Self-heals if missing.
     */
    private suspend fun verifyAndHideSysDbTab() {
        try {
            schemaGuardUseCase()
            Log.d(TAG, "M2.3: Schema checked and verified")
        } catch (e: Exception) {
            Log.w(TAG, "M2.3: Failed to verify schema: ${e.message}")
        }
    }

    private suspend fun uploadTableIfNeeded(tableName: String, newRows: List<List<Any>>): SyncResult? {
        if (newRows.isNotEmpty()) {
            return sheetsRepository.uploadBatch(tableName, newRows)
        }
        return null
    }

    private suspend fun uploadPendingPurchaseOrders() {
        try {
            val poDao = appDatabase.purchaseOrderDao()
            val pendingPOs = poDao.getPendingPOs()
            if (pendingPOs.isEmpty()) return

            val existingHeaderIds = sheetsRepository.getExistingUuids("Purchase_Orders").toHashSet()
            val existingItemIds = sheetsRepository.getExistingUuids("PO_Items").toHashSet()

            for (po in pendingPOs) {
                // 1. Upload items first
                val items = poDao.getPOItems(po.poId)
                var itemsSucceeded = true
                if (items.isNotEmpty()) {
                    val newItems = items.filter { it.poItemId !in existingItemIds }
                    if (newItems.isNotEmpty()) {
                        val itemsResult = sheetsRepository.uploadBatch("PO_Items", newItems.map { it.toSheetRow() })
                        if (itemsResult !is SyncResult.Success) {
                            Log.w(TAG, "PO_Items upload failed for PO ${po.poId} — keeping pending")
                            itemsSucceeded = false
                        }
                    }
                }
                if (!itemsSucceeded) continue

                // 2. Upload header (only if not already on sheet)
                if (po.poId !in existingHeaderIds) {
                    val headerResult = sheetsRepository.uploadBatch("Purchase_Orders", listOf(po.toSheetRow()))
                    if (headerResult !is SyncResult.Success) {
                        Log.w(TAG, "PO header upload failed for ${po.poId} — keeping pending")
                        continue
                    }
                }

                // 3. Both items and header succeeded
                poDao.markSynced(po.poId)
            }
        } catch (e: Exception) { Log.e(TAG, "PO Upload failed", e) }
    }

    private suspend fun uploadPendingGRNs() {
        try {
            val grnDao = appDatabase.grnDao()
            val pendingHeaders = grnDao.getPendingGrns()
            if (pendingHeaders.isEmpty()) return

            val existingHeaderIds = sheetsRepository.getExistingUuids("GRN_Headers").toHashSet()
            val existingItemIds = sheetsRepository.getExistingUuids("GRN_Items").toHashSet()

            for (grn in pendingHeaders) {
                // 1. Upload items first
                val items = grnDao.getGrnItems(grn.grnId)
                var itemsSucceeded = true
                if (items.isNotEmpty()) {
                    val newItems = items.filter { it.grnItemId !in existingItemIds }
                    if (newItems.isNotEmpty()) {
                        val itemsResult = sheetsRepository.uploadBatch("GRN_Items", newItems.map { it.toSheetRow() })
                        if (itemsResult !is SyncResult.Success) {
                            Log.w(TAG, "GRN_Items upload failed for GRN ${grn.grnId} — keeping pending")
                            itemsSucceeded = false
                        }
                    }
                }
                if (!itemsSucceeded) continue

                // 2. Upload header (only if not already on sheet)
                if (grn.grnId !in existingHeaderIds) {
                    val headerResult = sheetsRepository.uploadBatch("GRN_Headers", listOf(grn.toSheetRow()))
                    if (headerResult !is SyncResult.Success) {
                        Log.w(TAG, "GRN header upload failed for ${grn.grnId} — keeping pending")
                        continue
                    }
                }

                // 3. Both items and header succeeded
                grnDao.markGrnSynced(grn.grnId, System.currentTimeMillis())
            }
        } catch (e: Exception) { Log.e(TAG, "GRN Upload failed", e) }
    }

    private suspend fun uploadPendingProductBatches(): Boolean {
        return try {
            val productBatchDao = appDatabase.productBatchDao()
            val pendingBatches = productBatchDao.getPendingBatches()
            if (pendingBatches.isEmpty()) return true
            
            val existingIds = sheetsRepository.getExistingUuids("Product_Batches")
            val newBatches = pendingBatches.filter { it.batchId !in existingIds }
            
            val result = if (newBatches.isNotEmpty()) {
                sheetsRepository.uploadBatch("Product_Batches", newBatches.map { it.toSheetRow() })
            } else {
                SyncResult.Success(0)
            }
            
            if (result is SyncResult.Success) {
                pendingBatches.forEach { productBatchDao.markSynced(it.batchId) }
                true
            } else {
                Log.w(TAG, "Product Batches upload failed — keeping pending")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Batch Upload failed", e)
            false
        }
    }

    private suspend fun uploadPendingStockAdjustments() {
        try {
            val dao = appDatabase.stockAdjustmentDao()
            val inventoryDao = appDatabase.inventoryDao()
            val pendingAdj = dao.getPendingAdjustments()
            if (pendingAdj.isEmpty()) return
            
            val existingAdjIds = sheetsRepository.getExistingUuids("Stock_Adjustments")
            val newAdj = pendingAdj.filter { it.adjustmentId !in existingAdjIds }
            val termId = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }
            
            val result = if (newAdj.isNotEmpty()) {
                uploadTableIfNeeded("Stock_Adjustments", newAdj.map { it.toSheetRow(termId) })
            } else {
                SyncResult.Success(0)
            }
            
            if (result is SyncResult.Success) {
                val ids = pendingAdj.map { it.adjustmentId }
                dao.markAsSynced(ids)
                // FIX (2026-08-05, TillzoTest Bug #3): REMOVED the duplicate
                // stock-apply loop that ran here. Stock is already applied at
                // save time in StockAdjustmentViewModel.saveAdjustment() /
                // ManualStockAdjustmentUseCase — applying it again on every sync
                // caused quantity double-counting (+2 entered → +4 in stock).
                Log.d(TAG, "Stock adjustments marked synced: ${ids.size} (no re-apply)")
            } else if (result != null) {
                Log.w(TAG, "Stock Adjustments upload failed — keeping pending")
            }
        } catch (e: Exception) { Log.e(TAG, "Stock Adjustment Upload failed", e) }
    }

    private suspend fun uploadPendingTillSessions() {
        try {
            val tillDao = appDatabase.tillSessionDao()
            val pendingSessions = tillDao.getPendingSessions()
            if (pendingSessions.isEmpty()) return
            
            val existingIds = sheetsRepository.getExistingUuids("Till_Sessions")
            val newSessions = pendingSessions.filter { it.sessionId !in existingIds }
            val updateSessions = pendingSessions.filter { it.sessionId in existingIds }

            fun sessionRow(s: com.tillzo.pos.data.local.entity.TillSessionEntity) = listOf(
                s.sessionId, s.cashierId, s.cashierName,
                s.posTerminalId, s.openingCash, s.closingCash,
                s.expectedCash, s.totalCashSales,
                s.totalCardSales, s.totalWalletSales,
                s.totalUdhaarSales, s.totalSalesCount,
                s.totalRefunds, s.netCash, s.status,
                s.notes, s.shiftDate, s.openedAt,
                s.closedAt ?: "", s.syncStatus,
                s.createdAt, s.updatedAt
            )

            // FIX (2026-08-06): sessions that already exist in the Sheet (closed /
            // reconciled → sync_status back to pending) were previously filtered
            // out and their closingCash/netCash/variance/payIn/payOut NEVER synced.
            // Now they are PUT-updated in place.
            var allOk = true
            if (newSessions.isNotEmpty()) {
                val result = sheetsRepository.uploadBatch("Till_Sessions", newSessions.map { sessionRow(it) })
                if (result !is SyncResult.Success) allOk = false
            }
            for (s in updateSessions) {
                val ok = sheetsRepository.updateRowByUuid("Till_Sessions", s.sessionId, sessionRow(s))
                if (!ok) allOk = false
            }
            
            if (allOk) {
                pendingSessions.forEach { tillDao.markSynced(it.sessionId) }
                Log.d(TAG, "Till sessions synced: ${newSessions.size} new, ${updateSessions.size} updated")
            }
        } catch (e: Exception) { Log.e(TAG, "Till Sessions Upload failed", e) }
    }

    private suspend fun uploadPendingWastage() {
        try {
            val wastageDao = appDatabase.wastageDao()
            val pendingWastage = wastageDao.getPendingWastage()
            if (pendingWastage.isEmpty()) return
            
            val existingIds = sheetsRepository.getExistingUuids("Wastage_Ledger")
            val newWastage = pendingWastage.filter { it.wastageId !in existingIds }
            
            val result = if (newWastage.isNotEmpty()) {
                sheetsRepository.uploadBatch("Wastage_Ledger", newWastage.map { it.toSheetRow() })
            } else {
                SyncResult.Success(0)
            }
            
            if (result is SyncResult.Success) {
                pendingWastage.forEach { wastageDao.markSynced(it.wastageId) }
                Log.d(TAG, "Wastage records synced: ${pendingWastage.size}")
            }
        } catch (e: Exception) { Log.e(TAG, "Wastage Upload failed", e) }
    }
} // end SyncWorker class

// ── Extension functions for toSheetRow() ────────────────────────────────────
fun com.tillzo.pos.data.local.entity.PurchaseOrderEntity.toSheetRow() = listOf(
    poId, poNumber, vendorId, vendorName, status, notes, totalAmount, currency,
    expectedDeliveryDate, createdBy, syncStatus, posTerminalId, createdAt, updatedAt
)
fun com.tillzo.pos.data.local.entity.PurchaseOrderItemEntity.toSheetRow() = listOf(
    poItemId, poId, productId, productName, sku, barcodeId,
    orderedQty, receivedQty, unitCostPrice, totalCost, unit,
    syncStatus, createdAt, updatedAt
)
fun com.tillzo.pos.data.local.entity.GrnHeaderEntity.toSheetRow() = listOf(
    grnId, grnNumber, poId, vendorId, vendorName, status, notes,
    receivedBy, totalAmount, syncStatus, posTerminalId,
    attachedFileId, attachedFileUrl, createdAt, updatedAt
)
fun com.tillzo.pos.data.local.entity.GrnItemEntity.toSheetRow() = listOf(
    grnItemId, grnId, poItemId, productId, productName, barcodeId, sku,
    receivedQty, unitCostPrice, totalCost, unit, batchNumber,
    manufacturingDate, expiryDate, inventoryAction, if (isNewProduct) 1 else 0,
    syncStatus, createdAt, updatedAt
)
fun com.tillzo.pos.data.local.entity.VendorEntity.toSheetRow() = listOf(
    vendorId, name, phone, whatsapp, email, address,
    city, creditLimit,
    if (isActive) 1 else 0,
    if (isDeleted) 1 else 0, syncStatus, createdAt, updatedAt
)
fun com.tillzo.pos.data.local.entity.ProductBatchEntity.toSheetRow() = listOf(
    batchId, productId, barcodeId, batchNumber, manufacturingDate, expiryDate,
    stockQty, costPrice, sellingPrice, if (isActive) 1 else 0,
    if (isDeleted) 1 else 0, deletedAt ?: "", syncStatus, posTerminalId, createdAt, updatedAt
)
fun com.tillzo.pos.data.local.entity.StockAdjustmentEntity.toSheetRow(posTerminalId: String = "TERM_1") = listOf(
    adjustmentId, productId, adjustmentType, quantityChanged, reason, adjustedBy, syncStatus,
    posTerminalId, createdAt, updatedAt
)


