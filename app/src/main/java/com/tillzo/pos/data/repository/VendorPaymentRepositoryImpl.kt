package com.tillzo.pos.data.repository

import com.tillzo.pos.data.local.dao.GrnDao
import com.tillzo.pos.data.local.dao.VendorBalanceTuple
import com.tillzo.pos.data.local.dao.VendorPaymentDao
import com.tillzo.pos.data.local.entity.VendorPaymentEntity
import com.tillzo.pos.domain.repository.VendorPaymentRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorPaymentRepositoryImpl @Inject constructor(
    private val vendorPaymentDao: VendorPaymentDao,
    private val grnDao: GrnDao
) : VendorPaymentRepository {

    override fun getPaymentsForVendor(vendorId: String): Flow<List<VendorPaymentEntity>> {
        return vendorPaymentDao.getPaymentsForVendor(vendorId)
    }

    override fun getAllPayments(): Flow<List<VendorPaymentEntity>> {
        return vendorPaymentDao.getAllPayments()
    }

    override fun getVendorBalanceFlow(vendorId: String): Flow<Double?> {
        return vendorPaymentDao.getVendorBalanceFlow(vendorId)
    }

    override fun getAllVendorBalances(): Flow<List<VendorBalanceTuple>> {
        return vendorPaymentDao.getAllVendorBalances()
    }

    override suspend fun getVendorBalance(vendorId: String): Double {
        return vendorPaymentDao.getVendorBalance(vendorId) ?: 0.0
    }

    override suspend fun recordBill(
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
    ) {
        val now = System.currentTimeMillis()
        val billRecord = VendorPaymentEntity(
            paymentId = UUID.randomUUID().toString(),
            vendorId = vendorId,
            vendorName = vendorName,
            grnId = grnId,
            poId = poId,
            type = "BILL",
            amount = totalAmount,
            paymentMethod = paymentMethod,
            paidBy = paidBy,
            note = note.ifBlank { "GRN Purchase Bill" },
            dueDate = dueDate,
            syncStatus = "pending",
            createdAt = now,
            updatedAt = now
        )
        vendorPaymentDao.insertPayment(billRecord)

        if (paidAmount > 0.0) {
            val paymentRecord = VendorPaymentEntity(
                paymentId = UUID.randomUUID().toString(),
                vendorId = vendorId,
                vendorName = vendorName,
                grnId = grnId,
                poId = poId,
                type = "PAYMENT",
                amount = paidAmount,
                paymentMethod = paymentMethod,
                paidBy = paidBy,
                note = "Initial payment on receiving",
                dueDate = "",
                syncStatus = "pending",
                createdAt = now + 1,
                updatedAt = now + 1
            )
            vendorPaymentDao.insertPayment(paymentRecord)
        }

        if (grnId.isNotBlank()) {
            val due = (totalAmount - paidAmount).coerceAtLeast(0.0)
            val status = when {
                due <= 0.0 -> "PAID"
                paidAmount > 0.0 -> "PARTIALLY_PAID"
                else -> "UNPAID"
            }
            grnDao.updateGrnPayment(grnId = grnId, paid = paidAmount, due = due, status = status, time = now)
        }
    }

    override suspend fun recordPayment(
        vendorId: String,
        vendorName: String,
        amount: Double,
        paymentMethod: String,
        paidBy: String,
        note: String,
        grnId: String
    ) {
        val now = System.currentTimeMillis()
        val paymentRecord = VendorPaymentEntity(
            paymentId = UUID.randomUUID().toString(),
            vendorId = vendorId,
            vendorName = vendorName,
            grnId = grnId,
            poId = "",
            type = "PAYMENT",
            amount = amount,
            paymentMethod = paymentMethod,
            paidBy = paidBy,
            note = note.ifBlank { "Vendor payment" },
            dueDate = "",
            syncStatus = "pending",
            createdAt = now,
            updatedAt = now
        )
        vendorPaymentDao.insertPayment(paymentRecord)

        // If a specific GRN is targeted, update it
        if (grnId.isNotBlank()) {
            val grn = grnDao.getGrnById(grnId)
            if (grn != null) {
                val newPaid = grn.paidAmount + amount
                val newDue = (grn.totalAmount - newPaid).coerceAtLeast(0.0)
                val newStatus = if (newDue <= 0.0) "PAID" else "PARTIALLY_PAID"
                grnDao.updateGrnPayment(grnId, newPaid, newDue, newStatus, now)
            }
        }
    }

    override suspend fun recordDebitNote(
        vendorId: String,
        vendorName: String,
        amount: Double,
        reason: String,
        paidBy: String,
        grnId: String
    ) {
        val now = System.currentTimeMillis()
        val debitNote = VendorPaymentEntity(
            paymentId = UUID.randomUUID().toString(),
            vendorId = vendorId,
            vendorName = vendorName,
            grnId = grnId,
            poId = "",
            type = "DEBIT_NOTE",
            amount = amount,
            paymentMethod = "DEBIT_NOTE",
            paidBy = paidBy,
            note = reason.ifBlank { "Debit Note / Purchase Return Adjustment" },
            dueDate = "",
            syncStatus = "pending",
            createdAt = now,
            updatedAt = now
        )
        vendorPaymentDao.insertPayment(debitNote)
    }

    override suspend fun recordCreditNote(
        vendorId: String,
        vendorName: String,
        amount: Double,
        reason: String,
        paidBy: String
    ) {
        val now = System.currentTimeMillis()
        val creditNote = VendorPaymentEntity(
            paymentId = UUID.randomUUID().toString(),
            vendorId = vendorId,
            vendorName = vendorName,
            grnId = "",
            poId = "",
            type = "CREDIT_NOTE",
            amount = amount,
            paymentMethod = "CREDIT_NOTE",
            paidBy = paidBy,
            note = reason.ifBlank { "Vendor Credit Adjustment" },
            dueDate = "",
            syncStatus = "pending",
            createdAt = now,
            updatedAt = now
        )
        vendorPaymentDao.insertPayment(creditNote)
    }
}
