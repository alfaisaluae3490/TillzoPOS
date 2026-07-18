package com.tillzo.pos.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tillzo.pos.data.local.dao.SaleDao
import com.tillzo.pos.data.local.entity.SaleEntity
import com.tillzo.pos.domain.model.CartItem
import com.tillzo.pos.domain.model.PaymentDetails
import com.tillzo.pos.domain.model.Sale
import com.tillzo.pos.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SaleRepositoryImpl @Inject constructor(
    private val saleDao: SaleDao,
    private val gson: Gson
) : SaleRepository {

    override fun getAllSales(): Flow<List<Sale>> {
        return saleDao.getAllSales().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getSalesPaged(limit: Int, offset: Int): Flow<List<Sale>> {
        return saleDao.getSalesPaged(limit, offset).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getSalesInRange(start: Long, end: Long): Flow<List<Sale>> {
        return saleDao.getSalesInRange(start, end).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getSalesInRangePaged(start: Long, end: Long, limit: Int, offset: Int): Flow<List<Sale>> {
        return saleDao.getSalesInRangePaged(start, end, limit, offset).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun getSaleById(systemRowId: String): Sale? {
        return saleDao.getSaleById(systemRowId)?.toDomainModel()
    }

    override suspend fun getSaleByInvoiceId(invoiceId: String): Sale? {
        return saleDao.getSaleByInvoiceId(invoiceId)?.toDomainModel()
    }

    override suspend fun processCheckout(sale: Sale) {
        // Enforces M4.4 Blind Selling saving locally with pending sync
        val entity = sale.toEntity()
        saleDao.insertSale(entity)
    }

    override suspend fun getPendingSyncSales(): List<Sale> {
        return saleDao.getPendingSyncSales().map { it.toDomainModel() }
    }

    override suspend fun getSaleCount(): Int {
        return saleDao.getSaleCount()
    }

    // --- Mappers ---
    
    private fun SaleEntity.toDomainModel(): Sale {
        val cartType = object : TypeToken<List<CartItem>>() {}.type
        val cartItems: List<CartItem> = gson.fromJson(items_json, cartType) ?: emptyList()

        val paymentDetails: PaymentDetails? = payment_split_json?.let {
            gson.fromJson(it, PaymentDetails::class.java)
        }

        return Sale(
            systemRowId = system_row_id,
            invoiceId = sync_uuid,
            cashierId = cashier_id,
            timestamp = timestamp,
            items = cartItems,
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
            paymentSplit = paymentDetails,
            referenceId = reference_id
        )
    }

    private fun Sale.toEntity(): SaleEntity {
        // Note: For a real app, pos_terminal_id would come from DataStore/Environment
        val terminalId = "TERM_1" 
        
        return SaleEntity(
            system_row_id = systemRowId,
            sync_status = com.tillzo.pos.data.local.entity.SyncStatus.PENDING,
            pos_terminal_id = terminalId,
            sync_uuid = invoiceId,
            cashier_id = cashierId,
            timestamp = timestamp,
            items_json = gson.toJson(items),
            subtotal = subtotal,
            tax = tax,
            discount = discount,
            total = total,
            payment_method = paymentMethod,
            cash_amount = cashAmount,
            card_amount = cardAmount,
            wallet_amount = walletAmount,
            udhaar_amount = udhaarAmount,
            customer_id = customerId,
            payment_split_json = paymentSplit?.let { gson.toJson(it) },
            reference_id = referenceId
        )
    }
}
