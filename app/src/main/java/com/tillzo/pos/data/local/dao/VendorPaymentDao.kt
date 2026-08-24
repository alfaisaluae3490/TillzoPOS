package com.tillzo.pos.data.local.dao

import androidx.room.*
import com.tillzo.pos.data.local.entity.VendorPaymentEntity
import kotlinx.coroutines.flow.Flow

data class VendorBalanceTuple(
    val vendorId: String,
    val totalBills: Double,
    val totalPayments: Double,
    val totalDebitNotes: Double,
    val totalCreditNotes: Double
) {
    val netBalance: Double
        get() = (totalBills - totalPayments - totalDebitNotes + totalCreditNotes).coerceAtLeast(0.0)
}

@Dao
interface VendorPaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: VendorPaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<VendorPaymentEntity>)

    @Query("SELECT * FROM vendor_payments WHERE vendorId = :vendorId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getPaymentsForVendor(vendorId: String): Flow<List<VendorPaymentEntity>>

    @Query("SELECT * FROM vendor_payments WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllPayments(): Flow<List<VendorPaymentEntity>>

    @Query("SELECT * FROM vendor_payments WHERE grnId = :grnId AND isDeleted = 0 ORDER BY createdAt ASC")
    suspend fun getPaymentsForGrn(grnId: String): List<VendorPaymentEntity>

    @Query("SELECT * FROM vendor_payments WHERE syncStatus = 'pending'")
    suspend fun getUnsyncedPayments(): List<VendorPaymentEntity>

    @Query("UPDATE vendor_payments SET syncStatus = :status, updatedAt = :updatedAt WHERE paymentId = :paymentId")
    suspend fun updateSyncStatus(paymentId: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN type = 'BILL' THEN amount ELSE 0 END), 0) -
            COALESCE(SUM(CASE WHEN type = 'PAYMENT' THEN amount ELSE 0 END), 0) -
            COALESCE(SUM(CASE WHEN type = 'DEBIT_NOTE' THEN amount ELSE 0 END), 0) +
            COALESCE(SUM(CASE WHEN type = 'CREDIT_NOTE' THEN amount ELSE 0 END), 0)
        FROM vendor_payments 
        WHERE vendorId = :vendorId AND isDeleted = 0
    """)
    fun getVendorBalanceFlow(vendorId: String): Flow<Double?>

    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN type = 'BILL' THEN amount ELSE 0 END), 0) -
            COALESCE(SUM(CASE WHEN type = 'PAYMENT' THEN amount ELSE 0 END), 0) -
            COALESCE(SUM(CASE WHEN type = 'DEBIT_NOTE' THEN amount ELSE 0 END), 0) +
            COALESCE(SUM(CASE WHEN type = 'CREDIT_NOTE' THEN amount ELSE 0 END), 0)
        FROM vendor_payments 
        WHERE vendorId = :vendorId AND isDeleted = 0
    """)
    suspend fun getVendorBalance(vendorId: String): Double?

    @Query("""
        SELECT 
            vendorId,
            COALESCE(SUM(CASE WHEN type = 'BILL' THEN amount ELSE 0 END), 0) as totalBills,
            COALESCE(SUM(CASE WHEN type = 'PAYMENT' THEN amount ELSE 0 END), 0) as totalPayments,
            COALESCE(SUM(CASE WHEN type = 'DEBIT_NOTE' THEN amount ELSE 0 END), 0) as totalDebitNotes,
            COALESCE(SUM(CASE WHEN type = 'CREDIT_NOTE' THEN amount ELSE 0 END), 0) as totalCreditNotes
        FROM vendor_payments 
        WHERE isDeleted = 0
        GROUP BY vendorId
    """)
    fun getAllVendorBalances(): Flow<List<VendorBalanceTuple>>
}
