package com.tillzo.pos.domain.usecase

import com.google.gson.Gson
import com.tillzo.pos.data.local.dao.SaleDao
import com.tillzo.pos.data.local.entity.SaleEntity
import com.tillzo.pos.data.local.entity.SyncStatus
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.domain.model.Sale
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
            val rows = sheetsRemoteDataSource.readRange("$salesTab!A:R")
            rows.firstOrNull { row -> row.size > 9 && row[9] == invoiceId }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun determineSalesTabForInvoice(invoiceId: String): String {
        return try {
            val metadata = sheetsRemoteDataSource.getSheetMetadata()
            val salesTabs = metadata.keys.filter { it.startsWith("Sales_") || it == "Sales" }
                .sortedDescending()
            salesTabs.firstOrNull() ?: "Sales"
        } catch (e: Exception) {
            "Sales"
        }
    }

    private fun sheetRowToEntity(row: List<String>): SaleEntity {
        fun get(index: Int): String = row.getOrElse(index) { "" }
        return SaleEntity(
            system_row_id = get(15).ifBlank { UUID.randomUUID().toString() },
            sync_status = SyncStatus.SYNCED,
            pos_terminal_id = get(14),
            sync_uuid = get(0).ifBlank { get(9) },
            cashier_id = get(8),
            timestamp = get(2).toLongOrNull() ?: System.currentTimeMillis(),
            items_json = get(3),
            subtotal = get(4).toDoubleOrNull() ?: 0.0,
            tax = get(5).toDoubleOrNull() ?: 0.0,
            discount = get(0).toDoubleOrNull() ?: 0.0,
            total = get(6).toDoubleOrNull() ?: 0.0,
            payment_method = get(7),
            payment_split_json = null,
            reference_id = null,
            created_at = get(16).toLongOrNull() ?: System.currentTimeMillis(),
            updated_at = get(17).toLongOrNull() ?: System.currentTimeMillis()
        )
    }

    private fun SaleEntity.toDomainModel(): Sale {
        return Sale(
            systemRowId = system_row_id,
            invoiceId = sync_uuid,
            cashierId = cashier_id,
            timestamp = timestamp,
            items = emptyList(),
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
}
