package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "Categories")
data class CategoryEntity(
    @PrimaryKey
    override val system_row_id: String = UUID.randomUUID().toString(),
    override var sync_status: String = "pending",
    override val created_at: Long = System.currentTimeMillis(),
    override var updated_at: Long = System.currentTimeMillis(),
    override val pos_terminal_id: String,

    val category_name: String,
    val parent_category_id: String? = null,
    
    override val is_deleted: Boolean = false,
    override val deleted_at: Long? = null
) : BaseEntity() {
    fun toSyncMap(): Map<String, Any> {
        return mapOf(
            "system_row_id" to system_row_id,
            "category_name" to category_name,
            "parent_category_id" to (parent_category_id ?: ""),
            "is_deleted" to (if (is_deleted) 1 else 0),
            "deleted_at" to (deleted_at ?: ""),
            "sync_status" to "synced",
            "pos_terminal_id" to pos_terminal_id,
            "created_at" to created_at,
            "updated_at" to updated_at
        )
    }
}
