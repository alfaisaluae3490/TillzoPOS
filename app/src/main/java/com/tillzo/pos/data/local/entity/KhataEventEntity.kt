package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * M7.1 Khata Module — Append-Only Ledger for Udhaar / Jama.
 * DO NOT UPDATE existing records.
 */
@Entity(
    tableName = "KhataEvents",
    indices = [
        Index(value = ["customer_id"])
    ]
)
data class KhataEventEntity(
    @PrimaryKey
    override val system_row_id: String = UUID.randomUUID().toString(),
    override val sync_status: String = SyncStatus.PENDING,
    override val created_at: Long = System.currentTimeMillis(),
    override val updated_at: Long = System.currentTimeMillis(),
    override val pos_terminal_id: String,

    val customer_id: String, // Maps to CustomerEntity.system_row_id
    val event_type: String,  // "UDHAAR" (negative) or "JAMA" (positive)
    val amount: Double,
    val note: String? = null,
    val reference_sale_id: String? = null, // Optional linking to a specific Sale
    
    override val is_deleted: Boolean = false,
    override val deleted_at: Long? = null
) : BaseEntity() {

    fun toSyncMap(): Map<String, Any> {
        return mapOf(
            "event_id" to system_row_id,
            "system_row_id" to system_row_id,
            "customer_id" to customer_id,
            "pos_id" to pos_terminal_id,
            "event_type" to event_type,
            "type" to event_type,
            "amount" to amount,
            "note" to (note ?: ""),
            "reference_sale_id" to (reference_sale_id ?: ""),
            "sync_uuid" to system_row_id, // Note: no distinct sync_uuid generated, reusing systemRowId
            "is_deleted" to (if (is_deleted) 1 else 0),
            "deleted_at" to (deleted_at ?: ""),
            "last_updated" to updated_at,
            "sync_status" to "synced",
            "created_at" to created_at,
            "updated_at" to updated_at,
            "pos_terminal_id" to pos_terminal_id
        )
    }
}
