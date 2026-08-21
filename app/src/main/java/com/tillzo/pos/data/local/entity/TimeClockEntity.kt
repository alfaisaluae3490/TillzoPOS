package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Employee Time-Tracking (FIX 2026-08-06 — industry-standard punch clock).
 * Records clock-in / clock-out events per employee (user email) per terminal.
 */
@Entity(tableName = "Time_Clock")
data class TimeClockEntity(
    @PrimaryKey
    override val system_row_id: String = UUID.randomUUID().toString(),
    override val sync_status: String = SyncStatus.PENDING,
    override val created_at: Long = System.currentTimeMillis(),
    override val updated_at: Long = System.currentTimeMillis(),
    override val pos_terminal_id: String,

    val employee_email: String,
    val employee_name: String = "",
    val event_type: String, // "IN" or "OUT"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null,

    override val is_deleted: Boolean = false,
    override val deleted_at: Long? = null
) : BaseEntity() {
    fun toSyncMap(): Map<String, Any> = mapOf(
        "system_row_id" to system_row_id,
        "employee_email" to employee_email,
        "employee_name" to employee_name,
        "event_type" to event_type,
        "timestamp" to timestamp,
        "note" to (note ?: ""),
        "pos_terminal_id" to pos_terminal_id,
        "created_at" to created_at,
        "updated_at" to updated_at,
        "sync_status" to "synced"
    )
}
