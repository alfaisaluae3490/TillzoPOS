package com.tillzo.pos.domain.usecase

import com.google.gson.Gson
import com.tillzo.pos.data.local.dao.SaleDao
import com.tillzo.pos.data.local.entity.SaleEntity
import com.tillzo.pos.data.local.entity.SyncStatus
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.domain.model.Sale
import com.tillzo.pos.domain.model.CartItem
import com.tillzo.pos.utils.SheetColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class ReprintReceiptUseCase @Inject constructor(
    private val saleDao: SaleDao,
    private val sheetsRemoteDataSource: SheetsRemoteDataSource,
    private val gson: Gson
) {

    suspend operator fun invoke(invoiceId: String): Sale? = withContext(Dispatchers.IO) {
        val local = saleDao.getSaleByInvoiceId(invoiceId)
        if (local != null) {
            return@withContext local.toDomainModel()
        }

        val sheetRow = findSaleInSheets(invoiceId)
        if (sheetRow != null) {
            val entity = sheetRowToEntity(sheetRow)
            saleDao.insertSale(entity)
            return@withContext entity.toDomainModel()
        }

        null
    }

    private suspend fun findSaleInSheets(invoiceId: String): List<String>? {
        val salesTab = determineSalesTabForInvoice(invoiceId)
        return try {
            // FIX (2026-08-06): read full A:Y and match invoice_id (col 0),
            // was A:R + row[9] (cash_amount) — never matched, reprint failed.
            val rows = sheetsRemoteDataSource.readRange("$salesTab!A:Y")
            rows.firstOrNull { row -> row.size > 0 && row[0] == invoiceId }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun determineSalesTabForInvoice(invoiceId: String): String {
        return try {
            val metadata = sheetsRemoteDataSource.getSheetMetadata()
            val salesTabs = metadata.keys.filter { it.startsWith("Sales_") || it == "Sales" }
                .sortedDescending()
            // FIX (2026-08-22, DEF-65): the OLD code always returned the MOST
            // RECENT Sales tab — reprinting an invoice from last month (or any
            // older month) looked in the current tab, missed, and failed. Now:
            // scan each Sales_* tab (newest first) for the invoice id; the
            // current-month tab is only the fallback, not the only answer.
            for (tab in salesTabs) {
                try {
                    val rows = sheetsRemoteDataSource.readRange("$tab!A:A")
                    if (rows.any { row -> row.isNotEmpty() && row[0] == invoiceId }) {
                        return tab
                    }
                } catch (_: Exception) {
                    // skip unreadable tab, try next
                }
            }
            salesTabs.firstOrNull() ?: "Sales"
        } catch (e: Exception) {
            "Sales"
        }
    }

    private fun sheetRowToEntity(row: List<String>): SaleEntity {
        // FIX (2026-08-06): indices now match SheetColumns.SALES:
        // 0 invoice_id, 2 timestamp, 3 items_json, 4 subtotal, 5 tax, 6 discount,
        // 7 total, 8 payment_method, 13 customer_id, 14 payment_split_json,
        // 15 reference_id, 16 cashier_id, 17 sync_uuid, 21 pos_terminal_id,
        // 22 system_row_id, 23 created_at, 24 updated_at
        fun get(index: Int): String = row.getOrElse(index) { "" }
        return SaleEntity(
            system_row_id = get(22).ifBlank { UUID.randomUUID().toString() },
            sync_status = SyncStatus.SYNCED,
            pos_terminal_id = get(21),
            sync_uuid = get(0).ifBlank { get(17) },
            cashier_id = get(16),
            timestamp = get(2).toLongOrNull() ?: System.currentTimeMillis(),
            items_json = get(3),
            subtotal = get(4).toDoubleOrNull() ?: 0.0,
            tax = get(5).toDoubleOrNull() ?: 0.0,
            discount = get(6).toDoubleOrNull() ?: 0.0,
            total = get(7).toDoubleOrNull() ?: 0.0,
            payment_method = get(8),
            payment_split_json = get(14).ifBlank { null },
            customer_id = get(13).ifBlank { null },
            reference_id = get(15).ifBlank { null },
            created_at = get(23).toLongOrNull() ?: System.currentTimeMillis(),
            updated_at = get(24).toLongOrNull() ?: System.currentTimeMillis()
        )
    }

    private fun SaleEntity.toDomainModel(): Sale {
        // FIX (2026-08-23, DEF-116): items pehle hamesha emptyList() the —
        // History duplicate-receipt print par sirf header chhapta tha, koi
        // item line nahi. Ab items_json (cart snapshot) parse karke Sale.items
        // mein bharo; corrupt/old JSON par empty fallback (print header-only).
        val parsedItems: List<CartItem> = try {
            gson.fromJson(items_json, cartItemListType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        return Sale(
            systemRowId = system_row_id,
            invoiceId = sync_uuid,
            cashierId = cashier_id,
            timestamp = timestamp,
            items = parsedItems,
            subtotal = subtotal,
            tax = tax,
            discount = discount,
            total = total,
            paymentMethod = payment_method,
            cashAmount = cash_amount,
            cardAmount = card_amount,
            walletAmount = wallet_amount,
            udhaarAmount = udhaar_amount,
            customerId = customer_id,
            referenceId = reference_id
        )
    }

    companion object {
        private val cartItemListType = object : com.google.gson.reflect.TypeToken<List<CartItem>>() {}.type
    }
}
