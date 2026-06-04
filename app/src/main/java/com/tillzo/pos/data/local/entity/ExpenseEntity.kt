package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * M7.6 Store Module — Daily Expense Tracking.
 */
@Entity(tableName = "Expenses")
data class ExpenseEntity(
    @PrimaryKey
    override val system_row_id: String = UUID.randomUUID().toString(),
    override val sync_status: String = SyncStatus.PENDING,
    override val created_at: Long = System.currentTimeMillis(),
    override val updated_at: Long = System.currentTimeMillis(),
    override val pos_terminal_id: String,

    val category: String, // e.g., "Rent", "Electricity", "Wages", "Misc"
    val amount: Double,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val logged_by_user_id: String, // User UUID who entered the expense
    
    override val is_deleted: Boolean = false,
    override val deleted_at: Long? = null
) : BaseEntity() {
    fun toSyncMap(): Map<String, Any> {
        return mapOf(
            "system_row_id" to system_row_id,
            "category" to category,
            "amount" to amount,
            "description" to description,
            "timestamp" to timestamp,
            "logged_by_user_id" to logged_by_user_id,
            "is_deleted" to (if (is_deleted) 1 else 0),
            "deleted_at" to (deleted_at ?: ""),
            "sync_status" to "synced",
            "pos_terminal_id" to pos_terminal_id,
            "created_at" to created_at,
            "updated_at" to updated_at
        )
    }
}
