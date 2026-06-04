package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * M7.1 CRM Module — Customer Profile properties.
 */
@Entity(tableName = "Customers")
data class CustomerEntity(
    @PrimaryKey
    override val system_row_id: String = UUID.randomUUID().toString(),
    override val sync_status: String = SyncStatus.PENDING,
    override val created_at: Long = System.currentTimeMillis(),
    override val updated_at: Long = System.currentTimeMillis(),
    override val pos_terminal_id: String,

    val name: String,
    val phone: String,
    val whatsapp: String? = null, // WhatsApp number (may differ from phone)
    val email: String? = null,
    val address: String? = null,
    
    override val is_deleted: Boolean = false,
    override val deleted_at: Long? = null
) : BaseEntity() {
    fun toSyncMap(): Map<String, Any> {
        return mapOf(
            "system_row_id" to system_row_id,
            "name" to name,
            "phone" to phone,
            "whatsapp" to (whatsapp ?: ""),
            "email" to (email ?: ""),
            "address" to (address ?: ""),
            "is_deleted" to (if (is_deleted) 1 else 0),
            "deleted_at" to (deleted_at ?: ""),
            "sync_status" to "synced",
            "pos_terminal_id" to pos_terminal_id,
            "created_at" to created_at,
            "updated_at" to updated_at
        )
    }
}
