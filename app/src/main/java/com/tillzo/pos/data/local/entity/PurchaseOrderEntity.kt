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

    // ── Geographic ────────────────────────────────────────────────────────────
    val city: String = "",
    val province: String = "",
    val country: String = "",
    val billingAddress: String = "",
    val ownerName: String = "",

    // ── Financial / Tax ───────────────────────────────────────────────────────
    val bankAccountTitle: String = "",
    val bankName: String = "",
    val bankAccountNumber: String = "",
    val bankIban: String = "",
    val bankSwiftCode: String = "",
    val bankBranch: String = "",
    val paymentTerms: String = "",
    val preferredCurrency: String = "",
    val creditLimit: Double = 0.0,
    val registrationNumber: String = "",
    val ntnNumber: String = "",
    val cnicNumber: String = "",
    val trnNumber: String = "",
    val tradeLicenseNumber: String = "",
    val tradeLicenseExpiryDate: String = "",

    // ── Contacts — Primary Manager ────────────────────────────────────────────
    val primaryManagerName: String = "",
    val primaryManagerPhone: String = "",
    val primaryManagerEmail: String = "",

    // ── Contacts — Tech Support ───────────────────────────────────────────────
    val techSupportName: String = "",
    val techSupportPhone: String = "",
    val techSupportEmail: String = "",

    // ── Contacts — Billing ────────────────────────────────────────────────────
    val billingContactName: String = "",
    val billingContactPhone: String = "",
    val billingContactEmail: String = "",

    // ── Contacts — Escalation L1 ──────────────────────────────────────────────
    val escalationL1Name: String = "",
    val escalationL1Phone: String = "",
    val escalationL1Email: String = "",

    // ── Contacts — Escalation L2 ──────────────────────────────────────────────
    val escalationL2Name: String = "",
    val escalationL2Phone: String = "",
    val escalationL2Email: String = "",

    // ── Contacts — Escalation L3 ──────────────────────────────────────────────
    val escalationL3Name: String = "",
    val escalationL3Phone: String = "",
    val escalationL3Email: String = "",

    // ── SLA & Files ───────────────────────────────────────────────────────────
    val contractStartDate: String = "",
    val contractExpiryDate: String = "",
    val slaResponseTimes: String = "",
    val warrantyTerms: String = "",
    val complianceCertificates: String = "",

    // ── Google Drive Attachment Metadata ──────────────────────────────────────
    val contractFileId: String = "",
    val contractFileUrl: String = "",

    // ── Status fields ─────────────────────────────────────────────────────────
    val isActive: Boolean = true,
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
            "city" to city,
            "province" to province,
            "country" to country,
            "billing_address" to billingAddress,
            "owner_name" to ownerName,
            "bank_account_title" to bankAccountTitle,
            "bank_name" to bankName,
            "bank_account_number" to bankAccountNumber,
            "bank_iban" to bankIban,
            "bank_swift_code" to bankSwiftCode,
            "bank_branch" to bankBranch,
            "payment_terms" to paymentTerms,
            "preferred_currency" to preferredCurrency,
            "credit_limit" to creditLimit,
            "registration_number" to registrationNumber,
            "ntn_number" to ntnNumber,
            "cnic_number" to cnicNumber,
            "trn_number" to trnNumber,
            "trade_license_number" to tradeLicenseNumber,
            "trade_license_expiry_date" to tradeLicenseExpiryDate,
            "primary_manager_name" to primaryManagerName,
            "primary_manager_phone" to primaryManagerPhone,
            "primary_manager_email" to primaryManagerEmail,
            "tech_support_name" to techSupportName,
            "tech_support_phone" to techSupportPhone,
            "tech_support_email" to techSupportEmail,
            "billing_contact_name" to billingContactName,
            "billing_contact_phone" to billingContactPhone,
            "billing_contact_email" to billingContactEmail,
            "escalation_l1_name" to escalationL1Name,
            "escalation_l1_phone" to escalationL1Phone,
            "escalation_l1_email" to escalationL1Email,
            "escalation_l2_name" to escalationL2Name,
            "escalation_l2_phone" to escalationL2Phone,
            "escalation_l2_email" to escalationL2Email,
            "escalation_l3_name" to escalationL3Name,
            "escalation_l3_phone" to escalationL3Phone,
            "escalation_l3_email" to escalationL3Email,
            "contract_start_date" to contractStartDate,
            "contract_expiry_date" to contractExpiryDate,
            "sla_response_times" to slaResponseTimes,
            "warranty_terms" to warrantyTerms,
            "compliance_certificates" to complianceCertificates,
            "contract_file_id" to contractFileId,
            "contract_file_url" to contractFileUrl,
            "is_active" to (if (isActive) 1 else 0),
            "is_deleted" to (if (isDeleted) 1 else 0),
            "sync_status" to "synced",
            "created_at" to createdAt,
            "updated_at" to updatedAt
        )
    }
}
