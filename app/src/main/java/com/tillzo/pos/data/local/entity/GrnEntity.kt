package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grn_headers")
data class GrnHeaderEntity(
    @PrimaryKey val grnId: String,             // UUID
    val grnNumber: String,                     // "GRN-2026-0001"
    val poId: String,                          // linked PO (can be empty for unplanned GRN)
    val poNumber: String = "",
    val vendorId: String,
    val vendorName: String,
    val vendorPhone: String = "",
    val status: String,                        // DRAFT | CONFIRMED
    val notes: String = "",
    val receivedBy: String,                    // user ID
    val receivedByName: String = "",
    val totalItems: Int = 0,
    val totalReceivedQty: Double = 0.0,
    val totalAmount: Double,
    val paymentStatus: String = "UNPAID",      // PAID | PARTIALLY_PAID | UNPAID
    val paidAmount: Double = 0.0,
    val dueBalance: Double = 0.0,
    val paymentMethod: String = "CREDIT",      // CASH | BANK_TRANSFER | CHEQUE | CARD | CREDIT
    val paymentDueDate: String = "",           // YYYY-MM-DD
    val reminderEnabled: Boolean = false,
    val reminderIntervalDays: Int = 1,
    val syncStatus: String = "pending",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val posTerminalId: String = "",
    val attachedFileId: String = "",
    val attachedFileUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toSyncMap(): Map<String, Any> {
        return mapOf(
            "grn_id" to grnId,
            "grn_number" to grnNumber,
            "po_id" to poId,
            "po_number" to poNumber,
            "vendor_id" to vendorId,
            "vendor_name" to vendorName,
            "vendor_phone" to vendorPhone,
            "status" to status,
            "notes" to notes,
            "received_by" to receivedBy,
            "received_by_name" to receivedByName,
            "total_items" to totalItems,
            "total_received_qty" to totalReceivedQty,
            "total_amount" to totalAmount,
            "payment_status" to paymentStatus,
            "paid_amount" to paidAmount,
            "due_balance" to dueBalance,
            "payment_method" to paymentMethod,
            "payment_due_date" to paymentDueDate,
            "reminder_enabled" to (if (reminderEnabled) 1 else 0),
            "reminder_interval_days" to reminderIntervalDays,
            "sync_status" to "synced",
            "is_deleted" to (if (isDeleted) 1 else 0),
            "deleted_at" to (deletedAt ?: ""),
            "pos_terminal_id" to posTerminalId,
            "attached_file_id" to attachedFileId,
            "attached_file_url" to attachedFileUrl,
            "created_at" to createdAt,
            "updated_at" to updatedAt
        )
    }
}

@Entity(
    tableName = "grn_items",
    foreignKeys = [androidx.room.ForeignKey(
        entity = GrnHeaderEntity::class,
        parentColumns = ["grnId"],
        childColumns = ["grnId"],
        onDelete = androidx.room.ForeignKey.CASCADE
    )],
    indices = [
        androidx.room.Index("grnId"),
        androidx.room.Index("productId")
    ]
)
data class GrnItemEntity(
    @PrimaryKey val grnItemId: String,         // UUID
    val grnId: String,                         // FK → GrnHeaderEntity
    val poItemId: String = "",                 // FK → PurchaseOrderItemEntity (empty if unplanned)
    val productId: String = "",                // FK → InventoryEntity (empty if new product)
    val batchId: String = "",
    val productName: String,
    val barcodeId: String = "",
    val sku: String = "",
    val categoryId: String = "",
    val brand: String = "",
    val orderedQty: Double = 0.0,
    val receivedQty: Double,
    val unitCostPrice: Double,
    val sellingPrice: Double = 0.0,
    val totalCost: Double,
    val unit: String,
    // Batch info for this received lot
    val batchNumber: String = "",
    val manufacturingDate: String = "",
    val expiryDate: String = "",
    // Resolution
    val inventoryAction: String = "PENDING",   // PENDING | NEW_ITEM | ADD_BATCH | UPDATE_BATCH
    val isNewProduct: Boolean = false,             // true if item didn't exist in inventory
    val lowStockThreshold: Double = 5.0,
    val syncStatus: String = "pending",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toSyncMap(): Map<String, Any> {
        return mapOf(
            "grn_item_id" to grnItemId,
            "grn_id" to grnId,
            "po_item_id" to poItemId,
            "product_id" to productId,
            "batch_id" to batchId,
            "product_name" to productName,
            "barcode_id" to barcodeId,
            "sku" to sku,
            "category_id" to categoryId,
            "brand" to brand,
            "ordered_qty" to orderedQty,
            "received_qty" to receivedQty,
            "unit_cost_price" to unitCostPrice,
            "selling_price" to sellingPrice,
            "total_cost" to totalCost,
            "unit" to unit,
            "batch_number" to batchNumber,
            "manufacturing_date" to manufacturingDate,
            "expiry_date" to expiryDate,
            "inventory_action" to inventoryAction,
            "is_new_product" to (if (isNewProduct) 1 else 0),
            "low_stock_threshold" to lowStockThreshold,
            "sync_status" to "synced",
            "created_at" to createdAt,
            "updated_at" to updatedAt
        )
    }
}
