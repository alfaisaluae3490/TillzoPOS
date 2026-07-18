package com.tillzo.pos.data.repository

import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.domain.sync.AppSettings
import com.tillzo.pos.domain.sync.DeltaResult
import com.tillzo.pos.domain.sync.SyncPayload
import com.tillzo.pos.domain.sync.SyncResult
import com.tillzo.pos.utils.AppLogger
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SheetsRepository — Repository layer for all Google Sheets operations.
 *
 * Architecture Law: This is the ONLY bridge between domain/use-case layer
 * and the data/remote layer. UseCase calls Repository, Repository calls DataSource.
 *
 * Chain: UseCase → SheetsRepository → SheetsRemoteDataSource → HTTP
 */
@Singleton
class SheetsRepository @Inject constructor(
    private val dataSource: SheetsRemoteDataSource,
    private val appSetupPrefs: AppSetupPrefs,
    private val appLogger: AppLogger
) {
    private val spreadsheetId: String get() = appSetupPrefs.spreadsheetId

    // ── Sheet Setup (called once on first sign-in) ───────────────────────────

    data class SheetSetupResult(
        val success: Boolean,
        val spreadsheetId: String = "",
        val error: String = ""
    )

    suspend fun createWorkspace(shopName: String): SheetSetupResult {
        // Already provisioned? Skip.
        if (spreadsheetId.isNotEmpty()) {
            return SheetSetupResult(success = true, spreadsheetId = spreadsheetId)
        }

        val curTab = currentSalesTab()

        // Blueprint M2.1 tabs — including SYS_DB_DO_NOT_TOUCH (hidden system tab)
        val sheetDefs = listOf(
            curTab, "Inventory", "Customers", "Khata_Events",
            "Expenses", "Categories", "Returns", "Wastage_Ledger", "Users_Permissions",
            "Purchase_Orders", "PO_Items",
            "GRN_Headers", "GRN_Items",
            "Vendors", "Product_Batches",
            "Product_Units",
            "Till_Sessions",
            "BarcodeGeneralConfigs", "BarcodeFieldConfigs",
            "Settings", "Sync_Log", "Dashboard", "SYS_DB_DO_NOT_TOUCH"
        ).mapIndexed { idx, title ->
            mapOf("properties" to mapOf("title" to title, "index" to idx))
        }

        val result = dataSource.createSpreadsheet(
            title     = "$shopName — TillzoPOS",
            sheetDefs = sheetDefs
        )

        if (!result.success) return SheetSetupResult(false, error = result.error)

        // Tag the newly created sheet so we can find it later (Drive API appProperties)
        dataSource.tagSheetAsPosSheet(result.spreadsheetId, shopName)

        // Save SpreadsheetId FIRST — DataSource reads it from prefs for batchWrite
        appSetupPrefs.saveProvisioningResult(
            spreadsheetId = result.spreadsheetId
        )

        // Write column headers for each tab
        val headersOk = dataSource.batchWrite(buildHeaders(curTab))
        if (!headersOk) return SheetSetupResult(false, error = "Failed to write headers")

        // Seed default Settings values so last_updated_timestamp is available immediately
        val initialSettings = listOf(
            listOf("last_updated_timestamp", "0"),
            listOf("min_app_version", "1"),
            listOf("shop_name", shopName)
        )
        dataSource.appendRows("Settings!A:B", initialSettings)

        return SheetSetupResult(success = true, spreadsheetId = result.spreadsheetId)
    }

    // ── Sync Operations ──────────────────────────────────────────────────────

    suspend fun uploadBatch(payload: SyncPayload): SyncResult {
        val range = tabForTable(payload.tableName)
        appLogger.logInfo("SYNC_PROCESS", "Uploading batch: ${payload.tableName} (${payload.rows.size} rows)")
        
        val columnsList = when (payload.tableName) {
            "Sales" -> com.tillzo.pos.utils.SheetColumns.SALES
            "Inventory" -> com.tillzo.pos.utils.SheetColumns.INVENTORY
            "Customers" -> com.tillzo.pos.utils.SheetColumns.CUSTOMERS
            "KhataEvents", "Khata_Events" -> com.tillzo.pos.utils.SheetColumns.KHATA_EVENTS
            "Expenses" -> com.tillzo.pos.utils.SheetColumns.EXPENSES
            "Categories" -> com.tillzo.pos.utils.SheetColumns.CATEGORIES
            "Users", "Users_Permissions" -> com.tillzo.pos.utils.SheetColumns.USERS
            "Product_Units" -> com.tillzo.pos.utils.SheetColumns.PRODUCT_UNITS
            "Till_Sessions" -> com.tillzo.pos.utils.SheetColumns.TILL_SESSIONS
            else -> null
        }

        val rows = if (columnsList != null) {
            payload.rows.map { rowMap ->
                columnsList.map { colName -> rowMap[colName] ?: "" }
            }
        } else {
            payload.rows.map { it.values.toList() }
        }

        val result = dataSource.appendRows(range, rows)
        return if (result.success) {
            appLogger.logInfo("SYNC_PROCESS", "Upload success: ${payload.tableName} (${rows.size} rows)")
            SyncResult.Success(rows.size)
        } else {
            appLogger.logError("SYNC_PROCESS", "Upload failed: ${payload.tableName}, http_code=${result.httpCode}, error=${result.errorMessage}")
            SyncResult.ServerError(result.httpCode, result.errorMessage)
        }
    }

    suspend fun uploadBatch(tableName: String, rows: List<List<Any>>): SyncResult {
        val range = tabForTable(tableName)
        appLogger.logInfo("SYNC_PROCESS", "Uploading batch: $tableName (${rows.size} rows)")
        val result = dataSource.appendRows(range, rows)
        return if (result.success) {
            appLogger.logInfo("SYNC_PROCESS", "Upload success: $tableName (${rows.size} rows)")
            SyncResult.Success(rows.size)
        } else {
            appLogger.logError("SYNC_PROCESS", "Upload failed: $tableName, http_code=${result.httpCode}, error=${result.errorMessage}")
            SyncResult.ServerError(result.httpCode, result.errorMessage)
        }
    }

    suspend fun fetchDelta(lastTimestamp: Long): DeltaResult {
        val allRows = mutableListOf<Map<String, Any>>()
        val tabs    = listOf(currentSalesTab(), "Inventory", "Customers",
                             "Khata_Events", "Expenses", "Returns", "Users_Permissions",
                             "Categories", "Product_Units", "Till_Sessions",
                             "Vendors", "Product_Batches",
                             "BarcodeGeneralConfigs", "BarcodeFieldConfigs")

        for (tab in tabs) {
            val raw = dataSource.readRange("$tab!A:ZZ")
            if (raw.size < 2) continue

            val headers = raw[0]
            val tsIndex = headers.indexOfFirst {
                it == "updated_at" || it == "updatedAt" || it == "last_updated" ||
                it == "timestamp" || it == "created_at" || it == "createdAt"
            }

            for (i in 1 until raw.size) {
                val row    = raw[i]
                val rowTs  = if (tsIndex >= 0 && tsIndex < row.size)
                    row[tsIndex].toLongOrNull() ?: 0L else 0L
                if (lastTimestamp == 0L || rowTs > lastTimestamp) {
                    val obj = mutableMapOf<String, Any>("_sheet" to tab)
                    headers.forEachIndexed { idx, h -> if (idx < row.size) obj[h] = row[idx] }
                    allRows.add(obj)
                }
            }
        }

        return DeltaResult(rows = allRows)
    }

    suspend fun getSettings(): AppSettings {
        val rows = dataSource.readRange("Settings!A:B")
        val map  = mutableMapOf<String, String>()
        for (i in 1 until rows.size) {
            val row = rows[i]
            if (row.size >= 2) map[row[0]] = row[1]
        }
        return AppSettings(
            lastUpdatedTimestamp = map["last_updated_timestamp"]?.toLongOrNull() ?: 0L,
            minAppVersion        = map["min_app_version"]?.toIntOrNull() ?: 1,
            backupSheetUrl       = map["backup_sheet_url"] ?: "",
            shopName             = map["shop_name"] ?: "",
            shopPhone            = map["shop_phone"] ?: ""
        )
    }

    /**
     * Updates the remote last_updated_timestamp in the Settings tab.
     * Called by SyncWorker after a successful full sync cycle.
     */
    suspend fun updateLastUpdatedTimestamp(timestamp: Long): Boolean {
        val rows = dataSource.readRange("Settings!A:B")
        var rowIndex = -1
        for (i in 0 until rows.size) {
            if (rows[i].getOrNull(0) == "last_updated_timestamp") {
                rowIndex = i + 1
                break
            }
        }
        val values = listOf(listOf("last_updated_timestamp", timestamp.toString()))
        return if (rowIndex != -1) {
            dataSource.batchWrite(listOf(mapOf(
                "range" to "Settings!A$rowIndex:B$rowIndex",
                "majorDimension" to "ROWS",
                "values" to values
            )))
        } else {
            dataSource.appendRows("Settings!A:B", values).success
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun currentSalesTab(): String {
        val months = arrayOf("Jan","Feb","Mar","Apr","May","Jun",
                             "Jul","Aug","Sep","Oct","Nov","Dec")
        val cal = Calendar.getInstance()
        return "Sales_${months[cal.get(Calendar.MONTH)]}_${cal.get(Calendar.YEAR)}"
    }

    private fun tabForTable(tableName: String) = when (tableName) {
        "Sales" -> currentSalesTab()
        else    -> tableName
    }

    private fun buildHeaders(salesTab: String): List<Map<String, Any>> = listOf(
        salesTab to com.tillzo.pos.utils.SheetColumns.SALES,
        "Inventory" to com.tillzo.pos.utils.SheetColumns.INVENTORY,
        "Customers" to com.tillzo.pos.utils.SheetColumns.CUSTOMERS,
        "Khata_Events" to com.tillzo.pos.utils.SheetColumns.KHATA_EVENTS,
        "Expenses" to com.tillzo.pos.utils.SheetColumns.EXPENSES,
        "Categories" to com.tillzo.pos.utils.SheetColumns.CATEGORIES,
        "Returns" to listOf("return_id","system_row_id","original_invoice_id","item_id",
                            "qty_returned","condition","refund_method","amount",
                            "last_updated","sync_status","created_at","updated_at","pos_terminal_id"),
        "Wastage_Ledger" to listOf("wastage_id","system_row_id","item_id","qty","value",
                                   "reason","date","last_updated","sync_status",
                                   "created_at","updated_at","pos_terminal_id"),
        "Users_Permissions" to com.tillzo.pos.utils.SheetColumns.USERS,
        "Purchase_Orders" to com.tillzo.pos.utils.SheetColumns.PURCHASE_ORDERS,
        "PO_Items" to com.tillzo.pos.utils.SheetColumns.PO_ITEMS,
        "GRN_Headers" to com.tillzo.pos.utils.SheetColumns.GRN_HEADERS,
        "GRN_Items" to com.tillzo.pos.utils.SheetColumns.GRN_ITEMS,
        "Vendors" to com.tillzo.pos.utils.SheetColumns.VENDORS,
        "Product_Batches" to com.tillzo.pos.utils.SheetColumns.PRODUCT_BATCHES,
        "Product_Units" to com.tillzo.pos.utils.SheetColumns.PRODUCT_UNITS,
        "Till_Sessions" to com.tillzo.pos.utils.SheetColumns.TILL_SESSIONS,
        "Settings" to listOf("setting_key","setting_value"),
        "Sync_Log" to listOf("sync_uuid","pos_id","status","timestamp","error_msg"),
        "SYS_DB_DO_NOT_TOUCH" to listOf("schema_version","last_verified","integrity_check")
    ).map { (tab, cols) ->
        mapOf(
            "range"          to "$tab!A1",
            "majorDimension" to "ROWS",
            "values"         to listOf(cols)
        )
    }

    // ── M2 Sheet Management (used by SyncWorker M2.1/2.3, ShardingWorker M2.2) ────────

    /**
     * Creates a new Sales tab (M2.2 monthly sharding).
     * Idempotent: if tab already exists, Google Sheets API returns 400 — we catch and return true.
     */
    suspend fun createTab(tabName: String): Boolean =
        dataSource.addSheet(tabName)

    /**
     * Renames a tab — used to archive previous month's Sales tab (M2.2).
     * Resolves title→numericSheetId via metadata call first.
     */
    suspend fun renameTab(oldName: String, newName: String): Boolean {
        val metadata = dataSource.getSheetMetadata()
        val sheetId  = metadata[oldName] ?: run {
            android.util.Log.w("SheetsRepository", "renameTab: '$oldName' not found in metadata")
            return false
        }
        return dataSource.renameSheet(sheetId, newName)
    }

    /**
     * Row count for a tab — ShardingWorker uses this to check the 18k row limit (M2.2).
     */
    suspend fun getRowCount(tabName: String): Int =
        dataSource.getRowCount(tabName)

    /**
     * UUID dedupe check (M2.1) — reads existing system_row_ids from the Sheet range
     * before uploading, to prevent double-writes on retry.
     *
     * @param tableName  logical table name (e.g., "Sales", "Inventory")
     * @return Set of system_row_id strings already present in the Sheet
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun getExistingUuids(tableName: String, columnLetter: String = "A"): Set<String> {
        val tab     = tabForTable(tableName)
        val col     = columnLetter.ifEmpty { "A" }
        val rows    = dataSource.readRange("$tab!$col:$col")
        if (rows.size < 2) return emptySet()
        // Skip header row
        return rows.drop(1).mapNotNull { it.firstOrNull() }.toHashSet()
    }

    /**
     * M8 — Soft Delete: Physically removes a row from a Google Sheet tab by its 1-indexed row number.
     * Helper to support deletion of synced soft-deleted rows.
     */
    suspend fun deleteRow(tableName: String, sheetRowIndex: Int): Boolean {
        val tab = tabForTable(tableName)
        val metadata = dataSource.getSheetMetadata()
        val sheetId = metadata[tab] ?: run {
            android.util.Log.w("SheetsRepository", "deleteRow: '$tab' not found in metadata")
            return false
        }
        return dataSource.deleteRow(sheetId, sheetRowIndex)
    }

    /**
     * M2.3 — Verifies SYS_DB_DO_NOT_TOUCH tab exists and hides it.
     * Called by SyncWorker after each upload cycle.
     */
    suspend fun ensureSysDbTabHidden() {
        val metadata = dataSource.getSheetMetadata()
        val sysDbId  = metadata["SYS_DB_DO_NOT_TOUCH"]

        if (sysDbId == null) {
            // Tab missing — create it (schema recovery)
            android.util.Log.i("SheetsRepository", "SYS_DB tab missing — creating it")
            dataSource.addSheet("SYS_DB_DO_NOT_TOUCH")
            // Re-fetch metadata to get the new sheetId
            val newMeta  = dataSource.getSheetMetadata()
            val newSysId = newMeta["SYS_DB_DO_NOT_TOUCH"] ?: return
            dataSource.setSheetHidden(newSysId, hidden = true)
        } else {
            // Tab exists — ensure it's hidden
            dataSource.setSheetHidden(sysDbId, hidden = true)
        }
    }

    // ── Drive Search & Verification ──────────────────────────────────────────

    suspend fun searchExistingPosSheets(): List<SheetsRemoteDataSource.ExistingSheetInfo> =
        dataSource.searchExistingPosSheets()

    suspend fun tagSheetAsPosSheet(sheetId: String, shopName: String): Boolean =
        dataSource.tagSheetAsPosSheet(sheetId, shopName)

    suspend fun verifySheetAccess(sheetId: String): Boolean =
        dataSource.verifySheetAccess(sheetId)
}

