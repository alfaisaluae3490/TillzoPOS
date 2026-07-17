package com.tillzo.pos.domain.sync.usecase

import android.util.Log
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.utils.SheetColumns
import javax.inject.Inject

/**
 * SchemaGuardUseCase
 *
 * Checks that all required tabs exist and that column headers are up-to-date.
 * Self-heals by creating missing tabs and rewriting outdated headers.
 */
class SchemaGuardUseCase @Inject constructor(
    private val dataSource: SheetsRemoteDataSource
) {
    companion object {
        private const val TAG = "SchemaGuardUseCase"

        // Map of tab name → expected first-row headers
        private val TAB_HEADERS = mapOf(
            "Inventory" to SheetColumns.INVENTORY,
            "Customers" to SheetColumns.CUSTOMERS,
            "Khata_Events" to SheetColumns.KHATA_EVENTS,
            "Expenses" to SheetColumns.EXPENSES,
            "Categories" to SheetColumns.CATEGORIES,
            "Users_Permissions" to SheetColumns.USERS,
            "Purchase_Orders" to SheetColumns.PURCHASE_ORDERS,
            "PO_Items" to SheetColumns.PO_ITEMS,
            "GRN_Headers" to SheetColumns.GRN_HEADERS,
            "GRN_Items" to SheetColumns.GRN_ITEMS,
            "Vendors" to SheetColumns.VENDORS,
            "Product_Batches" to SheetColumns.PRODUCT_BATCHES,
            "Product_Units" to SheetColumns.PRODUCT_UNITS,
            "BarcodeGeneralConfigs" to SheetColumns.BARCODE_GENERAL_CONFIGS,
            "BarcodeFieldConfigs" to SheetColumns.BARCODE_FIELD_CONFIGS
        )
    }

    suspend operator fun invoke(): Boolean {
        return try {
            val metadataMap = dataSource.getSheetMetadata()
            if (metadataMap.isEmpty()) {
                Log.e(TAG, "Failed to fetch spreadsheet metadata")
                return false
            }

            val requiredTabs = listOf(
                "Inventory", "Customers", "Khata_Events",
                "Expenses", "Returns", "Wastage_Ledger", "Users_Permissions",
                "Settings", "Sync_Log", "Dashboard", "SYS_DB_DO_NOT_TOUCH",
                "Purchase_Orders", "PO_Items", "GRN_Headers", "GRN_Items", "Vendors", "Product_Batches", "Product_Units",
                "BarcodeGeneralConfigs", "BarcodeFieldConfigs"
            )

            var schemaValid = true
            for (tab in requiredTabs) {
                if (!metadataMap.containsKey(tab)) {
                    Log.w(TAG, "Missing required tab: $tab")
                    schemaValid = false
                    val created = dataSource.addSheet(tab)
                    if (created) {
                        Log.i(TAG, "Self-healed by creating missing tab: $tab")
                        schemaValid = true
                    }
                }
            }

            // ── Repair outdated column headers ───────────────────────────────
            for ((tab, expectedHeaders) in TAB_HEADERS) {
                try {
                    val rows = dataSource.readRange("$tab!1:1")
                    val currentHeaders = if (rows.isNotEmpty()) rows[0] else emptyList()

                    // Rewrite headers if they are outdated (wrong count or wrong names)
                    val needsRepair = currentHeaders.size != expectedHeaders.size ||
                        currentHeaders.zip(expectedHeaders).any { (cur, exp) -> cur != exp }

                    if (needsRepair) {
                        Log.i(TAG, "Repairing headers for tab: $tab (had ${currentHeaders.size}, expected ${expectedHeaders.size})")
                        val headerRange = mapOf(
                            "range" to "$tab!A1",
                            "majorDimension" to "ROWS",
                            "values" to listOf(expectedHeaders)
                        )
                        dataSource.batchWrite(listOf(headerRange))
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to check/repair headers for $tab: ${e.message}")
                }
            }

            schemaValid
        } catch (e: Exception) {
            Log.e(TAG, "Error in SchemaGuardUseCase: ${e.message}", e)
            false
        }
    }
}
