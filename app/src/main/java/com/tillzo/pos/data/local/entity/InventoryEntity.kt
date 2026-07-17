package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "Inventory")
data class InventoryEntity(
    @PrimaryKey
    override val system_row_id: String = UUID.randomUUID().toString(),
    override var sync_status: String = "pending",
    override val created_at: Long = System.currentTimeMillis(),
    override var updated_at: Long = System.currentTimeMillis(),
    override val pos_terminal_id: String,

    val item_name: String,
    val item_number: Int = 0,
    val category: String,
    val barcode_id: String,
    val unit: String, // "KG", "ML", "PC", "GM"
    val price_per_unit: Double,
    val current_stock: Double,
    val low_stock_threshold: Double,
    
    // M6 Advanced SaaS additions
    val sku: String = "",
    val brand: String = "",
    val description: String = "",
    val cost_price: Double = 0.0,
    val tax_percent: Double = 0.0,
    val batch_number: String = "",
    val expiry_date: String = "", // YYYY-MM-DD
    val manufacturing_date: String = "", // YYYY-MM-DD
    val expiry_alert_days: Int = 30,     // M7 custom expiry alert threshold
    val is_damaged_stock: Boolean = false,
    val damaged_qty: Double = 0.0,
    
    val totalStock: Double = 0.0,
    val hasBatches: Boolean = false,

    // Quick-grid pinning (M-Till)
    val isPinned: Boolean = false,
    val pinnedOrder: Int = 0,

    override val is_deleted: Boolean = false,
    override val deleted_at: Long? = null
) : BaseEntity() {

    fun toSyncMap(): Map<String, Any> {
        return mapOf(
            "system_row_id" to system_row_id,
            "item_number" to item_number,
            "barcode_id" to barcode_id,
            "name" to item_name,
            "category" to category,
            "unit" to unit,
            "selling_price" to price_per_unit,
            "price" to price_per_unit,
            "stock_qty" to current_stock,
            "low_threshold" to low_stock_threshold,
            "sku" to sku,
            "brand" to brand,
            "description" to description,
            "cost_price" to cost_price,
            "tax_percent" to tax_percent,
            "batch_number" to batch_number,
            "expiry_date" to expiry_date,
            "manufacturing_date" to manufacturing_date,
            "expiry_alert_days" to expiry_alert_days,
            "is_damaged" to (if (is_damaged_stock) 1 else 0),
            "damaged_qty" to damaged_qty,
            "total_stock" to totalStock,
            "has_batches" to (if (hasBatches) 1 else 0),
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
