package com.tillzo.pos.data.local.entity

/**
 * Abstract base for ALL Room entities in this app.
 *
 * Architecture Law (Blueprint M1.2):
 * Every table MUST include these 5 columns — no exceptions.
 *
 * system_row_id  → UUID string, immutable PK. Never changed after creation.
 * sync_status    → TEXT: "pending" | "synced" | "failed". Default = "pending".
 * created_at     → Unix timestamp millis (Long). Set once on creation.
 * updated_at     → Unix timestamp millis (Long). Updated on every mutation.
 * pos_terminal_id→ Device/terminal identifier string. Set from device settings.
 */
abstract class BaseEntity {
    abstract val system_row_id: String
    abstract val sync_status: String
    abstract val created_at: Long
    abstract val updated_at: Long
    abstract val pos_terminal_id: String
    
    // M8 Soft Delete fields
    abstract val is_deleted: Boolean
    abstract val deleted_at: Long?
}

/**
 * Sync status constants — use these everywhere, never hardcode strings.
 */
object SyncStatus {
    const val PENDING = "pending"
    const val SYNCED  = "synced"
    const val FAILED  = "failed"
}
