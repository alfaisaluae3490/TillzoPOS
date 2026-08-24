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
            "Wastage_Ledger" to SheetColumns.WASTAGE_LEDGER,
            "Stock_Adjustments" to SheetColumns.STOCK_ADJUSTMENTS,
            "Returns" to SheetColumns.RETURNS,
            "Purchase_Orders" to SheetColumns.PURCHASE_ORDERS,
            "PO_Items" to SheetColumns.PO_ITEMS,
            "GRN_Headers" to SheetColumns.GRN_HEADERS,
            "GRN_Items" to SheetColumns.GRN_ITEMS,
            "Vendors" to SheetColumns.VENDORS,
            "Product_Batches" to SheetColumns.PRODUCT_BATCHES,
            "Product_Units" to SheetColumns.PRODUCT_UNITS,
            "Till_Sessions" to SheetColumns.TILL_SESSIONS,
            // DEF-92 FIX (2026-08-23): ItemGtins tab — purani sheets par missing
            // ho to SchemaGuard self-heal (create + header) karta hai.
            "ItemGtins" to SheetColumns.ITEM_GTINS,
            "Vendor_Payments" to SheetColumns.VENDOR_PAYMENTS
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
                "Till_Sessions",
                "Categories", "Stock_Adjustments", "BarcodeGeneralConfigs", "BarcodeFieldConfigs",
                // DEF-92 FIX (2026-08-23): ItemGtins required tab (GTIN sync)
                "ItemGtins",
                "Vendor_Payments"
            )

            // FIX (2026-08-22, DEF-32): Sales_[MMM_YYYY] tabs are DYNAMIC (month
            // shards) — they were never in TAB_HEADERS, so SchemaGuard skipped
            // their header check entirely. When a Sales header row got deleted
            // (observed 2026-08-22), no repair ever fired: row 1 held sale data,
            // pulls imported corrupt rows (empty invoice_id, total 0), and sales
            // went "missing" from history. Resolve the current Sales tab and
            // treat it like any other guarded tab.
            // FIX (2026-08-22, DEF-80): the previous code called
            // readRange("Sales_1:1") — a HARDCODED legacy tab name that does not
            // exist on this spreadsheet (the actual tab is Sales_Aug_2026). The
            // Sheets API returned HTTP 400 "Unable to parse range: Sales_1:1" on
            // EVERY schema check. Metadata is already fetched above — no extra
            // read needed.
            val resolvedSalesTab = metadataMap.keys
                .filter { it.startsWith("Sales_") }
                .sortedByDescending { it }
                .firstOrNull()

            val headersToCheck = TAB_HEADERS.toMutableMap()
            if (resolvedSalesTab != null) {
                headersToCheck[resolvedSalesTab] = SheetColumns.SALES
            }

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
            val headerRanges = headersToCheck.keys.map { "$it!1:1" }
            val batchHeaders = dataSource.batchReadRanges(headerRanges)

            for ((tab, expectedHeaders) in headersToCheck) {
                try {
                    val rows = batchHeaders[tab] ?: batchHeaders["$tab!1:1"] ?: emptyList()
                    val currentHeaders = if (rows.isNotEmpty()) rows[0] else emptyList()

                    // FIX (2026-08-22, DEF-33 follow-up): if the read FAILED
                    // (HTTP 429/5xx returns an empty list), currentHeaders is []
                    // and "needsRepair" becomes true for a healthy tab — which
                    // would insert a phantom header row and corrupt the sheet.
                    // Skip repair when the read itself is suspect (0 rows).
                    if (rows.isEmpty()) {
                        Log.w(TAG, "Header read for $tab returned no rows — skipping repair (possible API error)")
                        continue
                    }

                    // Rewrite headers if they are outdated (wrong count or wrong names)
                    val needsRepair = currentHeaders.size != expectedHeaders.size ||
                        currentHeaders.zip(expectedHeaders).any { (cur, exp) -> cur != exp }

                    // FIX (2026-08-22, DEF-32): if row 1 doesn't look like a header
                    // row at all (e.g. it holds sale data because the header row was
                    // deleted), INSERT a fresh row at top FIRST, then write headers
                    // into it. Writing headers directly over row 1 would DESTROY
                    // that data row (observed: Sales_Aug_2026 row 1 held sale 1's
                    // full row after the header was deleted — 11 sales became 6
                    // visible + 4 corrupt local imports).
                    if (needsRepair) {
                        val firstCell = currentHeaders.firstOrNull() as? String ?: ""
                        val headerLooksLikeData = firstCell.length > 30 ||
                            (firstCell.contains("-") && !firstCell.contains("_"))
                        if (headerLooksLikeData && currentHeaders.size >= 3) {
                            val sheetId = dataSource.getSheetMetadata()[tab]
                            if (sheetId != null) {
                                val inserted = dataSource.insertRowsTop(sheetId, 1)
                                Log.i(TAG, "DEF-32: inserting header row for tab: $tab (inserted=$inserted)")
                            } else {
                                Log.w(TAG, "DEF-32: could not resolve sheetId for $tab")
                            }
                        }
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
