package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Users_Permissions")
data class UserEntity(
    @PrimaryKey
    override val system_row_id: String,
    override val sync_status: String = SyncStatus.PENDING,
    override val created_at: Long = System.currentTimeMillis(),
    override val updated_at: Long = System.currentTimeMillis(),
    override val pos_terminal_id: String,

    val email: String,
    val name: String,
    val role: String, // e.g., "Admin", "Manager", "Cashier"
    val password_hash: String?, // Nullable for OAuth users, populated for local users
    val permissions_json: String?, // Granular permissions map
    
    override val is_deleted: Boolean = false,
    override val deleted_at: Long? = null
) : BaseEntity() {
    fun toSyncMap(): Map<String, Any> {
        return mapOf(
            "system_row_id" to system_row_id,
            "email" to email,
            "name" to name,
            "role" to role,
            "password_hash" to (password_hash ?: ""),
            "permissions_json" to (permissions_json ?: ""),
            "is_deleted" to (if (is_deleted) 1 else 0),
            "deleted_at" to (deleted_at ?: ""),
            "sync_status" to "synced",
            "pos_terminal_id" to pos_terminal_id,
            "created_at" to created_at,
            "updated_at" to updated_at
        )
    }
}
