package com.tillzo.pos.data.sync.options.delta

import android.util.Log
import com.tillzo.pos.data.local.AppDatabase
import com.tillzo.pos.data.local.SyncLogEntity
import com.tillzo.pos.domain.sync.DataSyncInterface
import com.tillzo.pos.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    private val syncInterface: DataSyncInterface,
    private val appDatabase: AppDatabase
) {
    companion object {
        private const val TAG = "DeltaSyncManager"
        /** Special "table name" used in SyncLogDao to track the delta cursor position. */
        private const val DELTA_CURSOR_KEY = "delta_cursor"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null

    /**
     * Starts the 60-second delta sync poll loop.
     * Safe to call multiple times — existing loop is cancelled before starting new one.
     */
    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            Log.i(TAG, "Delta sync polling started (every ${Constants.DELTA_SYNC_INTERVAL_MS / 1000}s)")

            // Register delta cursor in SyncLog if not already tracked
            appDatabase.syncLogDao().ensureTableRegistered(DELTA_CURSOR_KEY)

            while (isActive) {
                try {
                    pollOnce()
                } catch (e: Exception) {
                    Log.e(TAG, "Delta sync poll error: ${e.message}", e)
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
                Log.e(TAG, "Immediate delta sync poll error: ${e.message}", e)
            }
        }
    }

    /**
     * Stops the polling loop. Called on sign-out or app destruction.
     */
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        Log.i(TAG, "Delta sync polling stopped")
    }

    /**
     * Performs a single delta sync cycle.
     */
    private suspend fun pollOnce() {
        // Step 1: Compare with local cursor first
        val localTimestamp = appDatabase.syncLogDao()
            .getLastSyncedAt(DELTA_CURSOR_KEY) ?: 0L

        // Step 2: Fetch remote Settings for last_updated_timestamp
        val settings = syncInterface.getSettings()
        val remoteTimestamp = settings.lastUpdatedTimestamp

        if (remoteTimestamp == 0L) {
            if (localTimestamp > 0L) {
                Log.d(TAG, "Remote timestamp=0 — Settings tab not yet populated. Skipping.")
                return
            }
            // First run with zero remote — set cursor to 0 so subsequent polls detect changes
            Log.d(TAG, "First run with remote=0 — saving cursor and allowing subsequent checks")
            appDatabase.syncLogDao().upsertSyncLog(
                SyncLogEntity(
                    table_name     = DELTA_CURSOR_KEY,
                    lastSyncedAt   = 0L,
                    lastSyncStatus = "synced"
                )
            )
            return
        }

        if (localTimestamp > 0L && remoteTimestamp <= localTimestamp) {
            Log.d(TAG, "No remote updates (remote=$remoteTimestamp, local=$localTimestamp)")
            return
        }

        Log.i(TAG, "Remote updates detected (remote=$remoteTimestamp > local=$localTimestamp)")

        // Step 3: Fetch delta rows (ALL terminals — M2.8 Multi-POS replica sync)
        val delta = syncInterface.fetchDelta(lastTimestamp = localTimestamp)

        if (delta.rows.isEmpty()) {
            Log.d(TAG, "Delta fetch returned 0 rows")
        } else {
            Log.i(TAG, "Fetched ${delta.rows.size} delta rows from all terminals")

            // Step 4: UPSERT delta rows into Room via system_row_id
            // M3+: Each module registers its own UPSERT handler here
            // e.g., saleDao.upsertBySysId(rows.filter { it["table"] == "sales" }.map { ... })
            upsertDeltaRows(delta.rows)
        }

        // Step 5: Update local cursor to remote timestamp
        appDatabase.syncLogDao().upsertSyncLog(
            SyncLogEntity(
                table_name     = DELTA_CURSOR_KEY,
                lastSyncedAt   = remoteTimestamp,
                lastSyncStatus = "synced"
            )
        )
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
    private suspend fun upsertDeltaRows(rows: List<Map<String, Any>>) {
        val grouped = rows.groupBy { it["_sheet"] as? String }
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
                        items.forEach { appDatabase.inventoryDao().insertItem(it) }
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
                        sales.forEach { appDatabase.saleDao().insertSale(it) }
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
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error upserting delta rows for tab $tabName: ${e.message}", e)
            }
        }
    }
}
