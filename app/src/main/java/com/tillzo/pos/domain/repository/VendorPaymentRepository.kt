package com.tillzo.pos.domain.repository

import com.tillzo.pos.data.local.dao.VendorBalanceTuple
import com.tillzo.pos.data.local.entity.VendorPaymentEntity
import kotlinx.coroutines.flow.Flow

interface VendorPaymentRepository {
    fun getPaymentsForVendor(vendorId: String): Flow<List<VendorPaymentEntity>>
    fun getAllPayments(): Flow<List<VendorPaymentEntity>>
    fun getVendorBalanceFlow(vendorId: String): Flow<Double?>
    fun getAllVendorBalances(): Flow<List<VendorBalanceTuple>>
    suspend fun getVendorBalance(vendorId: String): Double
    suspend fun recordBill(
        vendorId: String,
        vendorName: String,
        grnId: String,
        poId: String,
        totalAmount: Double,
        paidAmount: Double,
        dueDate: String,
        paymentMethod: String,
        paidBy: String,
        note: String
    )
    suspend fun recordPayment(
        vendorId: String,
        vendorName: String,
        amount: Double,
        paymentMethod: String,
        paidBy: String,
        note: String,
        grnId: String = ""
    )
    suspend fun recordDebitNote(
        vendorId: String,
        vendorName: String,
        amount: Double,
        reason: String,
        paidBy: String,
        grnId: String = ""
    )
    suspend fun recordCreditNote(
        vendorId: String,
        vendorName: String,
        amount: Double,
        reason: String,
        paidBy: String
    )
}
