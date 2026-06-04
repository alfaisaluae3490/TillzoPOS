package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_orders")
data class PurchaseOrderEntity(
    @PrimaryKey val poId: String,              // UUID
    val poNumber: String,                      // auto-generated: "PO-2026-0001"
    val vendorId: String,                      // FK to vendors table
    val vendorName: String,                    // denormalized for offline display
    val status: String,                        // DRAFT | SENT | PARTIALLY_RECEIVED | RECEIVED | CANCELLED
    val notes: String = "",
    val totalAmount: Double,
    val currency: String = "PKR",
    val expectedDeliveryDate: String = "",     // YYYY-MM-DD
    val createdBy: String,                     // cashier/admin ID
    val syncStatus: String = "pending",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val posTerminalId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toSyncMap(): Map<String, Any> {
        return mapOf(
            "po_id" to poId,
            "po_number" to poNumber,
            "vendor_id" to vendorId,
            "vendor_name" to vendorName,
            "status" to status,
            "notes" to notes,
            "total_amount" to totalAmount,
            "currency" to currency,
            "expected_delivery_date" to expectedDeliveryDate,
            "created_by" to createdBy,
            "sync_status" to "synced",
            "pos_terminal_id" to posTerminalId,
            "created_at" to createdAt,
            "updated_at" to updatedAt
        )
    }
}

@Entity(
    tableName = "purchase_order_items",
    foreignKeys = [ForeignKey(
        entity = PurchaseOrderEntity::class,
        parentColumns = ["poId"],
        childColumns = ["poId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [androidx.room.Index(value = ["poId"])]
)
data class PurchaseOrderItemEntity(
    @PrimaryKey val poItemId: String,          // UUID
    val poId: String,                          // FK → PurchaseOrderEntity
    val productId: String,                     // FK → InventoryEntity (nullable — item may not exist yet)
    val productName: String,                   // written at PO creation time
    val sku: String = "",
    val barcodeId: String = "",
    val orderedQty: Double,
    val receivedQty: Double = 0.0,             // updated when GRN created
    val unitCostPrice: Double,
    val totalCost: Double,
    val unit: String,                          // KG/PC/BOX etc.
    val syncStatus: String = "pending",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toSyncMap(): Map<String, Any> {
        return mapOf(
            "po_item_id" to poItemId,
            "po_id" to poId,
            "product_id" to productId,
            "product_name" to productName,
            "sku" to sku,
            "barcode_id" to barcodeId,
            "ordered_qty" to orderedQty,
            "received_qty" to receivedQty,
            "unit_cost_price" to unitCostPrice,
            "total_cost" to totalCost,
            "unit" to unit,
            "sync_status" to "synced",
            "created_at" to createdAt,
            "updated_at" to updatedAt
        )
    }
}

@Entity(tableName = "vendors")
data class VendorEntity(
    @PrimaryKey val vendorId: String,          // UUID
    val name: String,
    val phone: String,
    val whatsapp: String = "",
    val email: String = "",
    val address: String = "",
    val isDeleted: Boolean = false,
    val syncStatus: String = "pending",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toSyncMap(): Map<String, Any> {
        return mapOf(
            "vendor_id" to vendorId,
            "name" to name,
            "phone" to phone,
            "whatsapp" to whatsapp,
            "email" to email,
            "address" to address,
            "is_deleted" to (if (isDeleted) 1 else 0),
            "sync_status" to "synced",
            "created_at" to createdAt,
            "updated_at" to updatedAt
        )
    }
}
