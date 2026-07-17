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

        if (localTimestamp > 0L && remoteTimestamp == 0L) {
            Log.d(TAG, "Remote timestamp=0 — Settings tab not yet populated. Skipping.")
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
                                province = row["province"] as? String ?: "",
                                country = row["country"] as? String ?: "",
                                billingAddress = row["billing_address"] as? String ?: row["billingAddress"] as? String ?: "",
                                ownerName = row["owner_name"] as? String ?: row["ownerName"] as? String ?: "",
                                bankAccountTitle = row["bank_account_title"] as? String ?: row["bankAccountTitle"] as? String ?: "",
                                bankName = row["bank_name"] as? String ?: row["bankName"] as? String ?: "",
                                bankAccountNumber = row["bank_account_number"] as? String ?: row["bankAccountNumber"] as? String ?: "",
                                bankIban = row["bank_iban"] as? String ?: row["bankIban"] as? String ?: "",
                                bankSwiftCode = row["bank_swift_code"] as? String ?: row["bankSwiftCode"] as? String ?: "",
                                bankBranch = row["bank_branch"] as? String ?: row["bankBranch"] as? String ?: "",
                                paymentTerms = row["payment_terms"] as? String ?: row["paymentTerms"] as? String ?: "",
                                preferredCurrency = row["preferred_currency"] as? String ?: row["preferredCurrency"] as? String ?: "",
                                creditLimit = (row["credit_limit"] as? String)?.toDoubleOrNull() ?: (row["creditLimit"] as? String)?.toDoubleOrNull() ?: 0.0,
                                registrationNumber = row["registration_number"] as? String ?: row["registrationNumber"] as? String ?: "",
                                ntnNumber = row["ntn_number"] as? String ?: row["ntnNumber"] as? String ?: "",
                                cnicNumber = row["cnic_number"] as? String ?: row["cnicNumber"] as? String ?: "",
                                trnNumber = row["trn_number"] as? String ?: row["trnNumber"] as? String ?: "",
                                tradeLicenseNumber = row["trade_license_number"] as? String ?: row["tradeLicenseNumber"] as? String ?: "",
                                tradeLicenseExpiryDate = row["trade_license_expiry_date"] as? String ?: row["tradeLicenseExpiryDate"] as? String ?: "",
                                primaryManagerName = row["primary_manager_name"] as? String ?: row["primaryManagerName"] as? String ?: "",
                                primaryManagerPhone = row["primary_manager_phone"] as? String ?: row["primaryManagerPhone"] as? String ?: "",
                                primaryManagerEmail = row["primary_manager_email"] as? String ?: row["primaryManagerEmail"] as? String ?: "",
                                techSupportName = row["tech_support_name"] as? String ?: row["techSupportName"] as? String ?: "",
                                techSupportPhone = row["tech_support_phone"] as? String ?: row["techSupportPhone"] as? String ?: "",
                                techSupportEmail = row["tech_support_email"] as? String ?: row["techSupportEmail"] as? String ?: "",
                                billingContactName = row["billing_contact_name"] as? String ?: row["billingContactName"] as? String ?: "",
                                billingContactPhone = row["billing_contact_phone"] as? String ?: row["billingContactPhone"] as? String ?: "",
                                billingContactEmail = row["billing_contact_email"] as? String ?: row["billingContactEmail"] as? String ?: "",
                                escalationL1Name = row["escalation_l1_name"] as? String ?: row["escalationL1Name"] as? String ?: "",
                                escalationL1Phone = row["escalation_l1_phone"] as? String ?: row["escalationL1Phone"] as? String ?: "",
                                escalationL1Email = row["escalation_l1_email"] as? String ?: row["escalationL1Email"] as? String ?: "",
                                escalationL2Name = row["escalation_l2_name"] as? String ?: row["escalationL2Name"] as? String ?: "",
                                escalationL2Phone = row["escalation_l2_phone"] as? String ?: row["escalationL2Phone"] as? String ?: "",
                                escalationL2Email = row["escalation_l2_email"] as? String ?: row["escalationL2Email"] as? String ?: "",
                                escalationL3Name = row["escalation_l3_name"] as? String ?: row["escalationL3Name"] as? String ?: "",
                                escalationL3Phone = row["escalation_l3_phone"] as? String ?: row["escalationL3Phone"] as? String ?: "",
                                escalationL3Email = row["escalation_l3_email"] as? String ?: row["escalationL3Email"] as? String ?: "",
                                contractStartDate = row["contract_start_date"] as? String ?: row["contractStartDate"] as? String ?: "",
                                contractExpiryDate = row["contract_expiry_date"] as? String ?: row["contractExpiryDate"] as? String ?: "",
                                slaResponseTimes = row["sla_response_times"] as? String ?: row["slaResponseTimes"] as? String ?: "",
                                warrantyTerms = row["warranty_terms"] as? String ?: row["warrantyTerms"] as? String ?: "",
                                complianceCertificates = row["compliance_certificates"] as? String ?: row["complianceCertificates"] as? String ?: "",
                                contractFileId = row["contract_file_id"] as? String ?: row["contractFileId"] as? String ?: "",
                                contractFileUrl = row["contract_file_url"] as? String ?: row["contractFileUrl"] as? String ?: "",
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
                    tabName == "BarcodeGeneralConfigs" -> {
                        val configs = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.BarcodeGeneralConfigEntity(
                                system_row_id = row["system_row_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                sync_status = "synced",
                                created_at = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updated_at = (row["updated_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                pos_terminal_id = row["pos_terminal_id"] as? String ?: "terminal_1",
                                is_deleted = (row["is_deleted"] as? String)?.toIntOrNull() == 1 || (row["is_deleted"] as? String)?.toBoolean() ?: false,
                                deleted_at = (row["deleted_at"] as? String)?.toLongOrNull(),
                                labelWidth = (row["labelWidth"] as? String)?.toIntOrNull() ?: 144,
                                labelHeight = (row["labelHeight"] as? String)?.toIntOrNull() ?: 72,
                                titleTextSize = (row["titleTextSize"] as? String)?.toFloatOrNull() ?: 6f,
                                isTitleBold = (row["isTitleBold"] as? String)?.toIntOrNull() == 1 || (row["isTitleBold"] as? String)?.toBoolean() ?: true,
                                barcodeSize = (row["barcodeSize"] as? String)?.toFloatOrNull() ?: 48f,
                                currencySymbol = row["currencySymbol"] as? String ?: "PKR",
                                companyName = row["companyName"] as? String ?: "Tillzo POS",
                                companyLogoPath = row["companyLogoPath"] as? String ?: "",
                                showCompanyName = (row["showCompanyName"] as? String)?.toIntOrNull() == 1 || (row["showCompanyName"] as? String)?.toBoolean() ?: true,
                                showCompanyLogo = (row["showCompanyLogo"] as? String)?.toIntOrNull() == 1 || (row["showCompanyLogo"] as? String)?.toBoolean() ?: true,
                                titleX = (row["titleX"] as? String)?.toFloatOrNull() ?: 4f,
                                titleY = (row["titleY"] as? String)?.toFloatOrNull() ?: 16f,
                                priceX = (row["priceX"] as? String)?.toFloatOrNull() ?: 4f,
                                priceY = (row["priceY"] as? String)?.toFloatOrNull() ?: 24f,
                                skuX = (row["skuX"] as? String)?.toFloatOrNull() ?: 4f,
                                skuY = (row["skuY"] as? String)?.toFloatOrNull() ?: 32f,
                                gtinX = (row["gtinX"] as? String)?.toFloatOrNull() ?: 4f,
                                gtinY = (row["gtinY"] as? String)?.toFloatOrNull() ?: 40f,
                                lotX = (row["lotX"] as? String)?.toFloatOrNull() ?: 4f,
                                lotY = (row["lotY"] as? String)?.toFloatOrNull() ?: 48f,
                                expX = (row["expX"] as? String)?.toFloatOrNull() ?: 4f,
                                expY = (row["expY"] as? String)?.toFloatOrNull() ?: 56f,
                                snX = (row["snX"] as? String)?.toFloatOrNull() ?: 4f,
                                snY = (row["snY"] as? String)?.toFloatOrNull() ?: 66f,
                                barcodeX = (row["barcodeX"] as? String)?.toFloatOrNull() ?: 92f,
                                barcodeY = (row["barcodeY"] as? String)?.toFloatOrNull() ?: 12f,
                                companyNameSize = (row["companyNameSize"] as? String)?.toFloatOrNull() ?: 5f,
                                companyLogoSize = (row["companyLogoSize"] as? String)?.toFloatOrNull() ?: 8f,
                                companyNameX = (row["companyNameX"] as? String)?.toFloatOrNull() ?: 16f,
                                companyNameY = (row["companyNameY"] as? String)?.toFloatOrNull() ?: 8f,
                                companyLogoX = (row["companyLogoX"] as? String)?.toFloatOrNull() ?: 4f,
                                companyLogoY = (row["companyLogoY"] as? String)?.toFloatOrNull() ?: 4f,
                                usePrefix = (row["usePrefix"] as? String)?.toIntOrNull() == 1 || (row["usePrefix"] as? String)?.toBoolean() ?: true,
                                customPrefix = row["customPrefix"] as? String ?: "]d2",
                                prefixPosition = (row["prefixPosition"] as? String)?.toIntOrNull() ?: 0,
                                useSuffix = (row["useSuffix"] as? String)?.toIntOrNull() == 1 || (row["useSuffix"] as? String)?.toBoolean() ?: false,
                                customSuffix = row["customSuffix"] as? String ?: "",
                                suffixPosition = (row["suffixPosition"] as? String)?.toIntOrNull() ?: 0,
                                useSeparator = (row["useSeparator"] as? String)?.toIntOrNull() == 1 || (row["useSeparator"] as? String)?.toBoolean() ?: true
                            )
                        }
                        configs.firstOrNull()?.let { appDatabase.barcodeConfigDao().insertGeneralConfig(it) }
                    }
                    tabName == "BarcodeFieldConfigs" -> {
                        val fields = tabRows.map { row ->
                            com.tillzo.pos.data.local.entity.BarcodeFieldConfigEntity(
                                system_row_id = row["system_row_id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                sync_status = "synced",
                                created_at = (row["created_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                updated_at = (row["updated_at"] as? String)?.toLongOrNull() ?: System.currentTimeMillis(),
                                pos_terminal_id = row["pos_terminal_id"] as? String ?: "terminal_1",
                                is_deleted = (row["is_deleted"] as? String)?.toIntOrNull() == 1 || (row["is_deleted"] as? String)?.toBoolean() ?: false,
                                deleted_at = (row["deleted_at"] as? String)?.toLongOrNull(),
                                fieldId = row["fieldId"] as? String ?: "",
                                fieldName = row["fieldName"] as? String ?: "",
                                aiCode = row["aiCode"] as? String ?: "",
                                isEnabled = (row["isEnabled"] as? String)?.toIntOrNull() == 1 || (row["isEnabled"] as? String)?.toBoolean() ?: true,
                                sequenceOrder = (row["sequenceOrder"] as? String)?.toIntOrNull() ?: 0,
                                useFnc1Separator = (row["useFnc1Separator"] as? String)?.toIntOrNull() == 1 || (row["useFnc1Separator"] as? String)?.toBoolean() ?: false,
                                customValue = row["customValue"] as? String ?: ""
                            )
                        }
                        appDatabase.barcodeConfigDao().insertFields(fields)
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
