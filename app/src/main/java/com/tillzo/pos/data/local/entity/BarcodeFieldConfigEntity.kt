package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "BarcodeFieldConfigs")
data class BarcodeFieldConfigEntity(
    @PrimaryKey
    override val system_row_id: String = UUID.randomUUID().toString(),
    override var sync_status: String = "pending",
    override val created_at: Long = System.currentTimeMillis(),
    override var updated_at: Long = System.currentTimeMillis(),
    override val pos_terminal_id: String = "terminal_1",
    override val is_deleted: Boolean = false,
    override val deleted_at: Long? = null,

    val fieldId: String,       // e.g. "GTIN", "SN", "EXPIRY", "BATCH", "SKU", or custom ID
    val fieldName: String,     // e.g. "GTIN", "Serial Number", etc.
    val aiCode: String,        // e.g. "01", "21", "17", "10", "240"
    val isEnabled: Boolean = true,
    val sequenceOrder: Int = 0,
    val useFnc1Separator: Boolean = false,
    val customValue: String = "" // if the field is custom, user can type a static value for it
) : BaseEntity()
