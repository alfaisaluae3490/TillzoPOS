package com.tillzo.pos.domain.repository

import com.tillzo.pos.domain.model.Sale
import kotlinx.coroutines.flow.Flow

interface SaleRepository {
    fun getAllSales(): Flow<List<Sale>>
    fun getSalesPaged(limit: Int, offset: Int): Flow<List<Sale>>
    fun getSalesInRange(start: Long, end: Long): Flow<List<Sale>>
    fun getSalesInRangePaged(start: Long, end: Long, limit: Int, offset: Int): Flow<List<Sale>>
    suspend fun getSaleById(systemRowId: String): Sale?
    suspend fun getSaleByInvoiceId(invoiceId: String): Sale?
    suspend fun getSaleByInvoiceIdPrefix(prefix: String): Sale?

    /** DEF-46b (2026-08-23): true if any refund row references this invoice. */
    suspend fun hasRefundForInvoice(invoiceId: String): Boolean
    
    /**
     * Completes a checkout transaction. 
     * Applies strict rules: M4.4 Blind Selling (no stock check), saves as Pending state for background sync.
     */
    suspend fun processCheckout(sale: Sale)

    suspend fun getPendingSyncSales(): List<Sale>
    suspend fun getSaleCount(): Int
}
