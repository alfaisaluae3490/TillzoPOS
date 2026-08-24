package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Accounts Payable (AP) Ledger — tracks all bills, payments, and debit/credit notes for vendors.
 * Append-only ledger to maintain auditability.
 */
@Entity(
    tableName = "vendor_payments",
    indices = [
        Index(value = ["vendorId"]),
        Index(value = ["grnId"]),
        Index(value = ["createdAt"])
    ]
)
data class VendorPaymentEntity(
    @PrimaryKey val paymentId: String = UUID.randomUUID().toString(),
    val vendorId: String,
    val vendorName: String,
    val grnId: String = "",                          // Linked GRN (if any)
    val poId: String = "",                           // Linked PO (if any)
    val type: String,                                // BILL | PAYMENT | DEBIT_NOTE | CREDIT_NOTE
    val amount: Double,
    val paymentMethod: String = "CASH",              // CASH | BANK_TRANSFER | CHEQUE | CARD | CREDIT
    val paidBy: String = "",                         // Cashier/Admin who recorded transaction
    val note: String = "",
    val dueDate: String = "",                        // YYYY-MM-DD (for bills)
    val syncStatus: String = "pending",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val posTerminalId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toSyncMap(): Map<String, Any> {
        return mapOf(
            "payment_id" to paymentId,
            "vendor_id" to vendorId,
            "vendor_name" to vendorName,
            "grn_id" to grnId,
            "po_id" to poId,
            "type" to type,
            "amount" to amount,
            "payment_method" to paymentMethod,
            "paid_by" to paidBy,
            "note" to note,
            "due_date" to dueDate,
            "sync_status" to "synced",
            "is_deleted" to (if (isDeleted) 1 else 0),
            "deleted_at" to (deletedAt ?: ""),
            "pos_terminal_id" to posTerminalId,
            "created_at" to createdAt,
            "updated_at" to updatedAt
        )
    }
}
