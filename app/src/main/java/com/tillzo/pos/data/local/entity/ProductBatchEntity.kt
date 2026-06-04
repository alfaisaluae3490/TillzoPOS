package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "product_batches",
    foreignKeys = [ForeignKey(
        entity = InventoryEntity::class,
        parentColumns = ["system_row_id"],
        childColumns = ["productId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("productId")]
)
data class ProductBatchEntity(
    @PrimaryKey val batchId: String = UUID.randomUUID().toString(), // UUID — generated on creation
    val productId: String,                     // FK → InventoryEntity.system_row_id
    val barcodeId: String = "",                // this batch's barcode (can differ per batch)
    val batchNumber: String = "",              // e.g. "BATCH-2025-03"
    val manufacturingDate: String = "",        // YYYY-MM-DD
    val expiryDate: String = "",               // YYYY-MM-DD
    val stockQty: Double = 0.0,                // stock for this batch only
    val costPrice: Double = 0.0,               // cost for this specific batch
    val sellingPrice: Double = 0.0,            // selling price for this batch
    val isActive: Boolean = true,              // false when fully sold/expired
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val syncStatus: String = "pending",
    val posTerminalId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toSyncMap(): Map<String, Any> {
        return mapOf(
            "batch_id" to batchId,
            "product_id" to productId,
            "barcode_id" to barcodeId,
            "batch_number" to batchNumber,
            "manufacturing_date" to manufacturingDate,
            "expiry_date" to expiryDate,
            "stock_qty" to stockQty,
            "cost_price" to costPrice,
            "selling_price" to sellingPrice,
            "is_active" to (if (isActive) 1 else 0),
            "is_deleted" to (if (isDeleted) 1 else 0),
            "deleted_at" to (deletedAt ?: ""),
            "sync_status" to "synced",
            "pos_terminal_id" to posTerminalId,
            "created_at" to createdAt,
            "updated_at" to updatedAt
        )
    }
}
