package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * M4 POS Module — The main Sale record.
 * 
 * items_json: Serialized List of CartItems
 * payment_split_json: Serialized Map or List for Multi-Tender (Cash, Card, Wallet, Udhaar).
 * sync_uuid: Used as the QR Code invoice identifier. It maps to Google Sheets "sync_uuid" / "invoice_id".
 */
@Entity(
    tableName = "Sales",
    indices = [
        Index(value = ["customer_id"]),
        Index(value = ["timestamp"])
    ]
)
data class SaleEntity(
    @PrimaryKey
    override val system_row_id: String,
    override val sync_status: String = SyncStatus.PENDING,
    override val created_at: Long = System.currentTimeMillis(),
    override val updated_at: Long = System.currentTimeMillis(),
    override val pos_terminal_id: String,

    val sync_uuid: String,         // Invoice ID / QR Code UUID
    val cashier_id: String,        // User UUID who processed the sale
    val timestamp: Long,           // Time of sale occurrence
    
    val items_json: String,        // Serialized cart
    
    val subtotal: Double,
    val tax: Double,
    val discount: Double = 0.0,    // Admin-applied discount
    val total: Double,
    
    val payment_method: String,    // "CASH" | "CARD" | "WALLET" | "UDHAAR" | "SPLIT"
    val cash_amount: Double = 0.0,
    val card_amount: Double = 0.0,
    val wallet_amount: Double = 0.0,
    val udhaar_amount: Double = 0.0,
    val customer_id: String? = null, // Set when UDHAAR payment used
    val payment_split_json: String?, // Full JSON breakdown
    val reference_id: String?,      // e.g. Wallet transaction ID
    
    override val is_deleted: Boolean = false,
    override val deleted_at: Long? = null
) : BaseEntity() {
    
    fun toSyncMap(): Map<String, Any> {
        return mapOf(
            "invoice_id" to sync_uuid,
            "pos_id" to pos_terminal_id,
            "timestamp" to timestamp,
            "items_json" to items_json,
            "subtotal" to subtotal,
            "tax" to tax,
            "discount" to discount,
            "total" to total,
            "payment_method" to payment_method,
            "payment_split_json" to (payment_split_json ?: ""),
            "cash_amount" to cash_amount,
            "card_amount" to card_amount,
            "wallet_amount" to wallet_amount,
            "udhaar_amount" to udhaar_amount,
            "customer_id" to (customer_id ?: ""),
            "reference_id" to (reference_id ?: ""),
            "cashier_id" to cashier_id,
            "sync_uuid" to sync_uuid,
            "is_deleted" to (if (is_deleted) 1 else 0),
            "deleted_at" to (deleted_at ?: ""),
            "last_updated" to updated_at,
            "sync_status" to "synced",
            "pos_terminal_id" to pos_terminal_id,
            "system_row_id" to system_row_id,
            "created_at" to created_at,
            "updated_at" to updated_at
        )
    }
}
