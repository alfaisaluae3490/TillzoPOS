package com.tillzo.pos.data.sync.options.delta

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.tillzo.pos.data.local.AppDatabase
import com.tillzo.pos.data.local.SyncLogEntity
import com.tillzo.pos.data.sync.options.worker.RestoreWorker
import com.tillzo.pos.domain.sync.DataSyncInterface
import com.tillzo.pos.utils.AppLogger
import com.tillzo.pos.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DeltaSyncManager — M2.6 + M2.8
 *
 * Background polling: every 60 seconds, checks for remote updates from ALL terminals.
 *
 * M2.6 — Delta Sync:
 *   1. GET Settings tab → read last_updated_timestamp
 *   2. Compare with local SyncLogDao.getLastSyncedAt("delta_cursor")
 *   3. If remote > local → GET updated rows → UPSERT via system_row_id in Room
 *
 * M2.8 — Multi-POS Sync:
 *   Delta fetch includes ALL terminals' data (no terminal filter).
 *   Every device maintains a complete replica of all POS terminals.
 *   Terminal A can search/refund Terminal B's sale offline.
 *
 * Lifecycle:
 *   startPolling() → called from SignInViewModel after successful sign-in
 *   stopPolling()  → called on sign-out or app destruction
 */
@Singleton
class DeltaSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncInterface: DataSyncInterface,
    private val appDatabase: AppDatabase,
    private val appLogger: AppLogger
) {
    companion object {
        private const val TAG = "DeltaSyncManager"
        /** Special "table name" used in SyncLogDao to track the delta cursor position. */
        private const val DELTA_CURSOR_KEY = "delta_cursor"
    }

    sealed class RestoreState {
        object Idle : RestoreState()
        data class Running(val progress: Float, val status: String) : RestoreState()
        object Success : RestoreState()
        data class Failed(val error: String) : RestoreState()
    }

    private val _restoreState = MutableStateFlow<RestoreState>(RestoreState.Idle)
    val restoreState: StateFlow<RestoreState> = _restoreState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null

    /**
     * Starts the 60-second delta sync poll loop.
     * Safe to call multiple times — existing loop is cancelled before starting new one.
     */
    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            appLogger.logInfo(TAG, "Delta sync polling started (every ${Constants.DELTA_SYNC_INTERVAL_MS / 1000}s)")

            // Register delta cursor in SyncLog if not already tracked
            appDatabase.syncLogDao().ensureTableRegistered(DELTA_CURSOR_KEY)

            while (isActive) {
                try {
                    pollOnce()
                } catch (e: Exception) {
                    appLogger.logError(TAG, "Delta sync poll error: ${e.message}", e)
                }
                delay(Constants.DELTA_SYNC_INTERVAL_MS)
            }
        }
    }

    /**
     * Triggers a single immediate delta sync poll outside the regular interval.
     * Called when a sheet is selected in SheetPickerViewModel.
     */
    fun triggerImmediatePoll() {
        scope.launch {
            try {
                pollOnce()
            } catch (e: Exception) {
                appLogger.logError(TAG, "Immediate delta sync poll error: ${e.message}", e)
            }
        }
    }

    /**
     * Stops the polling loop. Called on sign-out or app destruction.
     */
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        appLogger.logInfo(TAG, "Delta sync polling stopped")
    }

    /**
     * Performs a single delta sync cycle.
     */
    private suspend fun pollOnce() {
        val localTimestamp = appDatabase.syncLogDao()
            .getLastSyncedAt(DELTA_CURSOR_KEY) ?: 0L

        val settings = syncInterface.getSettings()
        val remoteTimestamp = settings.lastUpdatedTimestamp

        // Only skip if we have local data and remote hasn't changed
        if (localTimestamp > 0L && remoteTimestamp <= localTimestamp) {
            appLogger.logInfo(TAG, "No remote updates (remote=$remoteTimestamp, local=$localTimestamp)")
            return
        }

        appLogger.logInfo(TAG, "Remote updates detected (remote=$remoteTimestamp, local=$localTimestamp)")

        val delta = syncInterface.fetchDelta(lastTimestamp = localTimestamp)

        // FIX (2026-08-06): only advance the cursor on success. Previously the
        // cursor was set unconditionally — if a per-tab upsert failed, the
        // cursor moved past it and those rows were never retried (silent loss).
        val upsertOk = if (delta.rows.isEmpty()) {
            appLogger.logInfo(TAG, "Delta fetch returned 0 rows")
            true
        } else {
            appLogger.logInfo(TAG, "Fetched ${delta.rows.size} delta rows from all terminals")
            upsertDeltaRows(delta.rows)
        }

        if (upsertOk) {
            appDatabase.syncLogDao().upsertSyncLog(
                SyncLogEntity(
                    table_name     = DELTA_CURSOR_KEY,
                    lastSyncedAt   = remoteTimestamp,
                    lastSyncStatus = "synced"
                )
            )
        } else {
            appLogger.logWarn(TAG, "Delta upsert had failures — cursor NOT advanced, will retry")
        }
    }

    /**
     * Performs the initial cloud restore (Reverse Sync) for a new device.
     * Fetches ALL data from the cloud, upserts into Room, and tracks progress
     * via [restoreState]. Also enqueues a WorkManager [RestoreWorker] for
     * durability in case the process is killed mid-restore.
     */
    suspend fun performInitialRestore() {
        _restoreState.value = RestoreState.Running(0f, "Starting cloud restore...")

        try {
            _restoreState.value = RestoreState.Running(0.1f, "Fetching cloud data...")

            val delta = syncInterface.fetchDelta(lastTimestamp = 0L)

            if (delta.rows.isEmpty()) {
                appLogger.logInfo(TAG, "Initial restore — no rows returned from cloud")
            } else {
                _restoreState.value = RestoreState.Running(0.6f, "Processing ${delta.rows.size} records...")
                upsertDeltaRows(delta.rows)
            }

            _restoreState.value = RestoreState.Running(0.9f, "Finalizing...")

            appDatabase.syncLogDao().upsertSyncLog(
                SyncLogEntity(
                    table_name     = DELTA_CURSOR_KEY,
                    lastSyncedAt   = System.currentTimeMillis(),
                    lastSyncStatus = "synced"
                )
            )

            _restoreState.value = RestoreState.Success
            appLogger.logInfo(TAG, "Initial restore completed successfully (${delta.rows.size} rows)")
        } catch (e: Exception) {
            appLogger.logError(TAG, "Initial restore failed: ${e.message}", e)
            _restoreState.value = RestoreState.Failed(e.message ?: "Unknown error")
        }
    }

    /**
     * Enqueues a durable WorkManager [RestoreWorker] so that if the process
     * is killed mid-restore, it will be resumed by WorkManager.
     */
    fun scheduleRestoreWorker() {
        val workManager = WorkManager.getInstance(context)
        val restoreRequest = OneTimeWorkRequestBuilder<RestoreWorker>()
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(
            RestoreWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            restoreRequest
        )
        appLogger.logInfo(TAG, "RestoreWorker enqueued for durable execution")
    }

    /**
     * Resets [restoreState] back to Idle so the user can retry.
     */
    fun resetRestoreState() {
        _restoreState.value = RestoreState.Idle
    }

    /**
     * UPSERT delta rows into Room.
     *
     * Blueprint M2.7 — Immutable UUID PK:
     *   Upsert keyed on system_row_id (UUID, never changes).
     *   barcode_id / product_name changes do NOT create new rows — they update existing.
     *
     * Blueprint M2.8 — Multi-POS:
     *   Rows from ALL terminals are merged — every device becomes a complete replica.
     *
     * NOTE: M3+ modules inject their DAOs here.
     * For M2 with no entity DAOs yet, this is a framework with log output only.
     */
    suspend fun upsertDeltaRows(rows: List<Map<String, Any>>): Boolean {
        val grouped = rows.groupBy { it["_sheet"] as? String }
        var anyFailed = false
        for ((tabName, tabRows) in grouped) {
            if (tabName == null) continue
            try {
                when {
                    tabName == "Inventory" -> {
                        val items = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.InventoryEntity(
                                system_row_id = row["system_row_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                sync_status = "synced",
                                created_at = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updated_at = (row["updated_at"] as? String)?.toLongOrNull() ?: (row["last_updated"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                pos_terminal_id = row["pos_terminal_id"] as? String ?: "terminal_1",
                                item_name = row["name"] as? String ?: "",
                                item_number = (row["item_number"] as? String)?.toIntOrNull() ?: 0,
                                category = row["category"] as? String ?: "",
                                barcode_id = row["barcode_id"] as? String ?: "",
                                unit = row["unit"] as? String ?: "Pieces",
                                price_per_unit = (row["selling_price"] as? String)?.toDoubleOrNull() ?: (row["price"] as? String)?.toDoubleOrNull() ?: 0.0,
                                current_stock = (row["stock_qty"] as? String)?.toDoubleOrNull() ?: 0.0,
                                low_stock_threshold = (row["low_threshold"] as? String)?.toDoubleOrNull() ?: 0.0,
                                sku = row["sku"] as? String ?: "",
                                brand = row["brand"] as? String ?: "",
                                description = row["description"] as? String ?: "",
                                cost_price = (row["cost_price"] as? String)?.toDoubleOrNull() ?: 0.0,
                                tax_percent = (row["tax_percent"] as? String)?.toDoubleOrNull() ?: 0.0,
                                batch_number = row["batch_number"] as? String ?: "",
                                expiry_date = row["expiry_date"] as? String ?: "",
                                manufacturing_date = row["manufacturing_date"] as? String ?: "",
                                expiry_alert_days = (row["expiry_alert_days"] as? String)?.toIntOrNull() ?: 30,
                                is_damaged_stock = (row["is_damaged"] as? String)?.toIntOrNull() == 1 || (row["is_damaged"] as? String)?.toBoolean() ?: false,
                                damaged_qty = (row["damaged_qty"] as? String)?.toDoubleOrNull() ?: 0.0,
                                totalStock = (row["total_stock"] as? String)?.toDoubleOrNull() ?: 0.0,
                                hasBatches = (row["has_batches"] as? String)?.toIntOrNull() == 1 || (row["has_batches"] as? String)?.toBoolean() ?: false,
                                isPinned = (row["isPinned"] as? String)?.toIntOrNull() == 1 || (row["isPinned"] as? String)?.toBoolean() ?: false,
                                pinnedOrder = (row["pinnedOrder"] as? String)?.toIntOrNull() ?: 0,
                                is_deleted = (row["is_deleted"] as? String)?.toIntOrNull() == 1 || (row["is_deleted"] as? String)?.toBoolean() ?: false,
                                deleted_at = (row["deleted_at"] as? String)?.toLongOrNull()
                            )
                        }
                        // FIX (2026-08-06) echo-clobber: a locally PENDING item (edited
                        // on this device, awaiting upload) must not be overwritten by a
                        // stale remote copy fetched by delta — that cleared its pending
                        // flag and silently lost the local change. Only insert rows that
                        // are not locally pending.
                        val invDao = appDatabase.inventoryDao()
                        items.forEach { item ->
                            val local = invDao.getItemById(item.system_row_id)
                            if (local == null || local.sync_status != "pending") {
                                invDao.insertItem(item)
                            }
                        }
                    }
                    tabName == "Categories" -> {
                        val categories = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.CategoryEntity(
                                system_row_id = row["system_row_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                sync_status = "synced",
                                created_at = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updated_at = (row["updated_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                pos_terminal_id = row["pos_terminal_id"] as? String ?: "terminal_1",
                                category_name = row["category_name"] as? String ?: "",
                                parent_category_id = row["parent_category_id"] as? String,
                                is_deleted = (row["is_deleted"] as? String)?.toIntOrNull() == 1 || (row["is_deleted"] as? String)?.toBoolean() ?: false,
                                deleted_at = (row["deleted_at"] as? String)?.toLongOrNull()
                            )
                        }
                        categories.forEach { appDatabase.categoryDao().insertCategory(it) }
                    }
                    tabName == "Customers" -> {
                        val customers = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.CustomerEntity(
                                system_row_id = row["system_row_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                sync_status = "synced",
                                created_at = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updated_at = (row["updated_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                pos_terminal_id = row["pos_terminal_id"] as? String ?: "terminal_1",
                                name = row["name"] as? String ?: "",
                                phone = row["phone"] as? String ?: "",
                                whatsapp = row["whatsapp"] as? String,
                                email = row["email"] as? String,
                                address = row["address"] as? String,
                                is_deleted = (row["is_deleted"] as? String)?.toIntOrNull() == 1 || (row["is_deleted"] as? String)?.toBoolean() ?: false,
                                deleted_at = (row["deleted_at"] as? String)?.toLongOrNull()
                            )
                        }
                        customers.forEach { appDatabase.customerDao().insert(it) }
                    }
                    tabName == "Expenses" -> {
                        val expenses = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.ExpenseEntity(
                                system_row_id = row["system_row_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                sync_status = "synced",
                                created_at = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updated_at = (row["updated_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                pos_terminal_id = row["pos_terminal_id"] as? String ?: "terminal_1",
                                category = row["category"] as? String ?: "",
                                amount = (row["amount"] as? String)?.toDoubleOrNull() ?: 0.0,
                                description = row["description"] as? String ?: "",
                                timestamp = (row["timestamp"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                logged_by_user_id = row["logged_by_user_id"] as? String ?: "",
                                is_deleted = (row["is_deleted"] as? String)?.toIntOrNull() == 1 || (row["is_deleted"] as? String)?.toBoolean() ?: false,
                                deleted_at = (row["deleted_at"] as? String)?.toLongOrNull()
                            )
                        }
                        expenses.forEach { appDatabase.expenseDao().insert(it) }
                    }
                    tabName == "Khata_Events" -> {
                        val events = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.KhataEventEntity(
                                system_row_id = row["system_row_id"] as? String ?: row["event_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                sync_status = "synced",
                                created_at = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updated_at = (row["updated_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                pos_terminal_id = row["pos_terminal_id"] as? String ?: "terminal_1",
                                customer_id = row["customer_id"] as? String ?: "",
                                event_type = row["event_type"] as? String ?: row["type"] as? String ?: "",
                                amount = (row["amount"] as? String)?.toDoubleOrNull() ?: 0.0,
                                note = row["note"] as? String,
                                reference_sale_id = row["reference_sale_id"] as? String,
                                is_deleted = (row["is_deleted"] as? String)?.toIntOrNull() == 1 || (row["is_deleted"] as? String)?.toBoolean() ?: false,
                                deleted_at = (row["deleted_at"] as? String)?.toLongOrNull()
                            )
                        }
                        events.forEach { appDatabase.khataEventDao().insert(it) }
                    }
                    tabName == "Product_Units" -> {
                        val units = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.ProductUnitEntity(
                                unitId = row["unitId"] as? String ?: java.util.UUID.randomUUID().toString(),
                                unitName = row["unitName"] as? String ?: "",
                                abbreviation = row["abbreviation"] as? String ?: "",
                                isDeleted = (row["isDeleted"] as? String)?.toIntOrNull() == 1 || (row["isDeleted"] as? String)?.toBoolean() ?: false,
                                syncStatus = "synced",
                                createdAt = (row["createdAt"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updatedAt = (row["updatedAt"] as? String)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        }
                        appDatabase.productUnitDao().insertAll(units)
                    }
                    tabName == "Vendors" -> {
                        val vendors = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.VendorEntity(
                                vendorId = row["vendor_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                name = row["name"] as? String ?: "",
                                phone = row["phone"] as? String ?: "",
                                whatsapp = row["whatsapp"] as? String ?: "",
                                email = row["email"] as? String ?: "",
                                address = row["address"] as? String ?: "",
                                city = row["city"] as? String ?: "",
                                creditLimit = (row["credit_limit"] as? String)?.toDoubleOrNull() ?: (row["creditLimit"] as? String)?.toDoubleOrNull() ?: 0.0,
                                isActive = (row["is_active"] as? String)?.toIntOrNull() == 1 || (row["isActive"] as? String)?.toIntOrNull() == 1 || (row["isActive"] as? String)?.toBoolean() ?: true,
                                isDeleted = (row["is_deleted"] as? String)?.toIntOrNull() == 1 || (row["isDeleted"] as? String)?.toIntOrNull() == 1 || (row["isDeleted"] as? String)?.toBoolean() ?: false,
                                syncStatus = "synced",
                                createdAt = (row["created_at"] as? String)?.toLongOrNull() ?: (row["createdAt"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updatedAt = (row["updated_at"] as? String)?.toLongOrNull() ?: (row["updatedAt"] as? String)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        }
                        vendors.forEach { appDatabase.vendorDao().insertVendor(it) }
                    }
                    tabName == "Product_Batches" -> {
                        val batches = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.ProductBatchEntity(
                                batchId = row["batch_id"] as? String ?: row["batchId"] as? String ?: java.util.UUID.randomUUID().toString(),
                                productId = row["product_id"] as? String ?: row["productId"] as? String ?: "",
                                barcodeId = row["barcode_id"] as? String ?: row["barcodeId"] as? String ?: "",
                                batchNumber = row["batch_number"] as? String ?: row["batchNumber"] as? String ?: "",
                                manufacturingDate = row["manufacturing_date"] as? String ?: row["manufacturingDate"] as? String ?: "",
                                expiryDate = row["expiry_date"] as? String ?: row["expiryDate"] as? String ?: "",
                                stockQty = (row["stock_qty"] as? String)?.toDoubleOrNull() ?: (row["stockQty"] as? String)?.toDoubleOrNull() ?: 0.0,
                                costPrice = (row["cost_price"] as? String)?.toDoubleOrNull() ?: (row["costPrice"] as? String)?.toDoubleOrNull() ?: 0.0,
                                sellingPrice = (row["selling_price"] as? String)?.toDoubleOrNull() ?: (row["sellingPrice"] as? String)?.toDoubleOrNull() ?: 0.0,
                                isActive = (row["is_active"] as? String)?.toIntOrNull() == 1 || (row["isActive"] as? String)?.toIntOrNull() == 1 || (row["isActive"] as? String)?.toBoolean() ?: true,
                                isDeleted = (row["is_deleted"] as? String)?.toIntOrNull() == 1 || (row["isDeleted"] as? String)?.toIntOrNull() == 1 || (row["isDeleted"] as? String)?.toBoolean() ?: false,
                                deletedAt = (row["deleted_at"] as? String)?.toLongOrNull() ?: (row["deletedAt"] as? String)?.toLongOrNull(),
                                syncStatus = "synced",
                                posTerminalId = row["pos_terminal_id"] as? String ?: row["posTerminalId"] as? String ?: "terminal_1",
                                createdAt = (row["created_at"] as? String)?.toLongOrNull() ?: (row["createdAt"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updatedAt = (row["updated_at"] as? String)?.toLongOrNull() ?: (row["updatedAt"] as? String)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        }
                        batches.forEach { appDatabase.productBatchDao().insertBatch(it) }
                    }
                    tabName.startsWith("Sales_") -> {
                        val sales = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.SaleEntity(
                                system_row_id = row["system_row_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                sync_status = "synced",
                                created_at = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updated_at = (row["updated_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                pos_terminal_id = row["pos_terminal_id"] as? String ?: row["pos_id"] as? String ?: "terminal_1",
                                sync_uuid = row["sync_uuid"] as? String ?: row["invoice_id"] as? String ?: "",
                                cashier_id = row["cashier_id"] as? String ?: "",
                                timestamp = (row["timestamp"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                items_json = row["items_json"] as? String ?: "[]",
                                subtotal = (row["subtotal"] as? String)?.toDoubleOrNull() ?: 0.0,
                                tax = (row["tax"] as? String)?.toDoubleOrNull() ?: 0.0,
                                discount = (row["discount"] as? String)?.toDoubleOrNull() ?: 0.0,
                                total = (row["total"] as? String)?.toDoubleOrNull() ?: 0.0,
                                payment_method = row["payment_method"] as? String ?: "CASH",
                                cash_amount = (row["cash_amount"] as? String)?.toDoubleOrNull() ?: 0.0,
                                card_amount = (row["card_amount"] as? String)?.toDoubleOrNull() ?: 0.0,
                                wallet_amount = (row["wallet_amount"] as? String)?.toDoubleOrNull() ?: 0.0,
                                udhaar_amount = (row["udhaar_amount"] as? String)?.toDoubleOrNull() ?: 0.0,
                                customer_id = row["customer_id"] as? String,
                                payment_split_json = row["payment_split_json"] as? String,
                                reference_id = row["reference_id"] as? String,
                                is_deleted = (row["is_deleted"] as? String)?.toIntOrNull() == 1 || (row["is_deleted"] as? String)?.toBoolean() ?: false,
                                deleted_at = (row["deleted_at"] as? String)?.toLongOrNull()
                            )
                        }
                        // FIX (2026-08-06) echo-clobber: never overwrite a locally
                        // pending sale (made offline on this device) with the stale
                        // remote copy — that would clear its pending flag and lose it.
                        val saleDao = appDatabase.saleDao()
                        sales.forEach { sale ->
                            val local = saleDao.getSaleById(sale.system_row_id)
                            if (local == null || local.sync_status != "pending") {
                                saleDao.insertSale(sale)
                            }
                        }
                    }
                    tabName == "BarcodeGeneralConfigs" || tabName == "BarcodeFieldConfigs" -> {
                        // Barcode config migrated to SharedPreferences — skip delta sync
                    }
                    tabName == "Users_Permissions" -> {
                        val users = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.UserEntity(
                                system_row_id = row["system_row_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                sync_status = "synced",
                                created_at = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updated_at = (row["updated_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                pos_terminal_id = row["pos_terminal_id"] as? String ?: "terminal_1",
                                name = row["name"] as? String ?: "",
                                email = row["email"] as? String ?: "",
                                role = row["role"] as? String ?: "CASHIER",
                                password_hash = row["password_hash"] as? String ?: "",
                                permissions_json = row["permissions_json"] as? String,
                                is_deleted = (row["is_deleted"] as? String)?.toIntOrNull() == 1 || (row["is_deleted"] as? String)?.toBoolean() ?: false,
                                deleted_at = (row["deleted_at"] as? String)?.toLongOrNull()
                            )
                        }
                        users.forEach { appDatabase.userDao().insertUser(it) }
                    }
                    tabName == "Purchase_Orders" -> {
                        val pos = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.PurchaseOrderEntity(
                                poId = row["po_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                poNumber = row["po_number"] as? String ?: "",
                                vendorId = row["vendor_id"] as? String ?: "",
                                vendorName = row["vendor_name"] as? String ?: "",
                                status = row["status"] as? String ?: "DRAFT",
                                notes = row["notes"] as? String ?: "",
                                totalAmount = (row["total_amount"] as? String)?.toDoubleOrNull() ?: 0.0,
                                currency = row["currency"] as? String ?: "PKR",
                                expectedDeliveryDate = row["expected_delivery_date"] as? String ?: "",
                                createdBy = row["created_by"] as? String ?: "",
                                syncStatus = "synced",
                                isDeleted = (row["is_deleted"] as? String)?.toIntOrNull() == 1 || (row["isDeleted"] as? String)?.toBoolean() ?: false,
                                deletedAt = (row["deleted_at"] as? String)?.toLongOrNull(),
                                posTerminalId = row["pos_terminal_id"] as? String ?: "terminal_1",
                                createdAt = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updatedAt = (row["updated_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        }
                        pos.forEach { appDatabase.purchaseOrderDao().insertPO(it) }
                    }
                    tabName == "PO_Items" -> {
                        val poItems = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.PurchaseOrderItemEntity(
                                poItemId = row["po_item_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                poId = row["po_id"] as? String ?: "",
                                productId = row["product_id"] as? String ?: "",
                                productName = row["product_name"] as? String ?: "",
                                sku = row["sku"] as? String ?: "",
                                barcodeId = row["barcode_id"] as? String ?: "",
                                orderedQty = (row["ordered_qty"] as? String)?.toDoubleOrNull() ?: 0.0,
                                receivedQty = (row["received_qty"] as? String)?.toDoubleOrNull() ?: 0.0,
                                unitCostPrice = (row["unit_cost_price"] as? String)?.toDoubleOrNull() ?: 0.0,
                                totalCost = (row["total_cost"] as? String)?.toDoubleOrNull() ?: 0.0,
                                unit = row["unit"] as? String ?: "PC",
                                syncStatus = "synced",
                                createdAt = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updatedAt = (row["updated_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        }
                        appDatabase.purchaseOrderDao().insertPOItems(poItems)
                    }
                    tabName == "GRN_Headers" -> {
                        val grns = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.GrnHeaderEntity(
                                grnId = row["grn_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                grnNumber = row["grn_number"] as? String ?: "",
                                poId = row["po_id"] as? String ?: "",
                                poNumber = row["po_number"] as? String ?: "",
                                vendorId = row["vendor_id"] as? String ?: "",
                                vendorName = row["vendor_name"] as? String ?: "",
                                vendorPhone = row["vendor_phone"] as? String ?: "",
                                status = row["status"] as? String ?: "DRAFT",
                                notes = row["notes"] as? String ?: "",
                                receivedBy = row["received_by"] as? String ?: "",
                                receivedByName = row["received_by_name"] as? String ?: "",
                                totalItems = (row["total_items"] as? String)?.toIntOrNull() ?: 0,
                                totalReceivedQty = (row["total_received_qty"] as? String)?.toDoubleOrNull() ?: 0.0,
                                totalAmount = (row["total_amount"] as? String)?.toDoubleOrNull() ?: 0.0,
                                syncStatus = "synced",
                                isDeleted = (row["is_deleted"] as? String)?.toIntOrNull() == 1 || (row["isDeleted"] as? String)?.toBoolean() ?: false,
                                deletedAt = (row["deleted_at"] as? String)?.toLongOrNull(),
                                posTerminalId = row["pos_terminal_id"] as? String ?: "terminal_1",
                                attachedFileId = row["attached_file_id"] as? String ?: "",
                                attachedFileUrl = row["attached_file_url"] as? String ?: "",
                                createdAt = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updatedAt = (row["updated_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        }
                        grns.forEach { appDatabase.grnDao().insertGrnHeader(it) }
                    }
                    tabName == "GRN_Items" -> {
                        val grnItems = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.GrnItemEntity(
                                grnItemId = row["grn_item_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                grnId = row["grn_id"] as? String ?: "",
                                poItemId = row["po_item_id"] as? String ?: "",
                                productId = row["product_id"] as? String ?: "",
                                batchId = row["batch_id"] as? String ?: "",
                                productName = row["product_name"] as? String ?: "",
                                barcodeId = row["barcode_id"] as? String ?: "",
                                sku = row["sku"] as? String ?: "",
                                categoryId = row["category_id"] as? String ?: "",
                                brand = row["brand"] as? String ?: "",
                                orderedQty = (row["ordered_qty"] as? String)?.toDoubleOrNull() ?: 0.0,
                                receivedQty = (row["received_qty"] as? String)?.toDoubleOrNull() ?: 0.0,
                                unitCostPrice = (row["unit_cost_price"] as? String)?.toDoubleOrNull() ?: 0.0,
                                sellingPrice = (row["selling_price"] as? String)?.toDoubleOrNull() ?: 0.0,
                                totalCost = (row["total_cost"] as? String)?.toDoubleOrNull() ?: 0.0,
                                unit = row["unit"] as? String ?: "PC",
                                batchNumber = row["batch_number"] as? String ?: "",
                                manufacturingDate = row["manufacturing_date"] as? String ?: "",
                                expiryDate = row["expiry_date"] as? String ?: "",
                                inventoryAction = row["inventory_action"] as? String ?: "PENDING",
                                isNewProduct = (row["is_new_item"] as? String)?.toIntOrNull() == 1 || (row["is_new_item"] as? String)?.toBoolean() ?: false,
                                lowStockThreshold = (row["low_stock_threshold"] as? String)?.toDoubleOrNull() ?: 5.0,
                                syncStatus = "synced",
                                createdAt = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updatedAt = (row["updated_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        }
                        appDatabase.grnDao().insertGrnItems(grnItems)
                    }
                    tabName == "Wastage_Ledger" -> {
                        val wastage = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.WastageEntity(
                                wastageId = row["wastage_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                productId = row["product_id"] as? String ?: "",
                                productName = row["product_name"] as? String ?: "",
                                batchId = row["batch_id"] as? String ?: "",
                                batchNumber = row["batch_number"] as? String ?: "",
                                quantity = (row["quantity"] as? String)?.toDoubleOrNull() ?: 0.0,
                                unit = row["unit"] as? String ?: "PC",
                                costPrice = (row["cost_price"] as? String)?.toDoubleOrNull() ?: 0.0,
                                totalLoss = (row["total_loss"] as? String)?.toDoubleOrNull() ?: 0.0,
                                reason = row["reason"] as? String ?: "OTHER",
                                notes = row["notes"] as? String ?: "",
                                loggedBy = row["logged_by"] as? String ?: "",
                                wastageDate = row["wastage_date"] as? String ?: "",
                                syncStatus = "synced",
                                posTerminalId = row["pos_terminal_id"] as? String ?: "terminal_1",
                                createdAt = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updatedAt = (row["updated_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        }
                        wastage.forEach { appDatabase.wastageDao().insertWastage(it) }
                    }
                    tabName == "Stock_Adjustments" -> {
                        val adjustments = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.StockAdjustmentEntity(
                                adjustmentId = row["adjustment_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                productId = row["product_id"] as? String ?: "",
                                adjustmentType = row["adjustment_type"] as? String ?: "SET",
                                quantityChanged = (row["quantity_changed"] as? String)?.toDoubleOrNull() ?: 0.0,
                                reason = row["reason"] as? String ?: "",
                                adjustedBy = row["adjusted_by"] as? String ?: "",
                                syncStatus = "synced",
                                createdAt = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        }
                        adjustments.forEach { appDatabase.stockAdjustmentDao().insertStockAdjustment(it) }
                    }
                    tabName == "Till_Sessions" -> {
                        val sessions = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.TillSessionEntity(
                                sessionId = row["session_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                cashierId = row["cashier_id"] as? String ?: "",
                                cashierName = row["cashier_name"] as? String ?: "",
                                posTerminalId = row["pos_terminal_id"] as? String ?: "terminal_1",
                                openingCash = (row["opening_cash"] as? String)?.toDoubleOrNull() ?: 0.0,
                                closingCash = (row["closing_cash"] as? String)?.toDoubleOrNull() ?: 0.0,
                                expectedCash = (row["expected_cash"] as? String)?.toDoubleOrNull() ?: 0.0,
                                totalCashSales = (row["total_cash_sales"] as? String)?.toDoubleOrNull() ?: 0.0,
                                totalCardSales = (row["total_card_sales"] as? String)?.toDoubleOrNull() ?: 0.0,
                                totalWalletSales = (row["total_wallet_sales"] as? String)?.toDoubleOrNull() ?: 0.0,
                                totalUdhaarSales = (row["total_udhaar_sales"] as? String)?.toDoubleOrNull() ?: 0.0,
                                totalSalesCount = (row["total_sales_count"] as? String)?.toIntOrNull() ?: 0,
                                totalRefunds = (row["total_refunds"] as? String)?.toDoubleOrNull() ?: 0.0,
                                netCash = (row["net_cash"] as? String)?.toDoubleOrNull() ?: 0.0,
                                status = row["status"] as? String ?: "CLOSED",
                                notes = row["notes"] as? String ?: "",
                                shiftDate = row["shift_date"] as? String ?: "",
                                openedAt = (row["opened_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                closedAt = (row["closed_at"] as? String)?.toLongOrNull(),
                                syncStatus = "synced",
                                createdAt = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updatedAt = (row["updated_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        }
                        sessions.forEach { appDatabase.tillSessionDao().insertSession(it) }
                    }
                }
            } catch (e: Exception) {
                appLogger.logError(TAG, "Error upserting delta rows for tab $tabName: ${e.message}", e)
                anyFailed = true
            }
        }
        return !anyFailed
    }
}
