package com.tillzo.pos.domain.model

import java.util.UUID

/**
 * Domain representations for M4 POS Module.
 * These are mapped from/to the JSON fields in SaleEntity to keep the UI decoupled
 * from serialized strings.
 */

data class CartItem(
    val itemId: String,
    val name: String,
    val quantity: Double,       // Decimal weight support (M4.3)
    val pricePerUnit: Double,   // Admin-Locked Price (M4.5)
    val unit: String = "PC",    // "PC", "KG", "GM", "ML" — drives decimal numpad
    val taxPercent: Double = 0.0,
    val total: Double = quantity * pricePerUnit
)

data class PaymentDetails(
    val cashAmount: Double = 0.0,
    val cardAmount: Double = 0.0,
    val walletAmount: Double = 0.0,
    val udhaarAmount: Double = 0.0
) {
    val totalPaid: Double
        get() = cashAmount + cardAmount + walletAmount + udhaarAmount
}

data class Sale(
    val systemRowId: String = UUID.randomUUID().toString(),
    val invoiceId: String = UUID.randomUUID().toString(), // Used for QR Code
    val cashierId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val items: List<CartItem>,
    val subtotal: Double,
    val tax: Double,
    val discount: Double = 0.0,
    val total: Double,
    val paymentMethod: String,
    val cashAmount: Double = 0.0,
    val cardAmount: Double = 0.0,
    val walletAmount: Double = 0.0,
    val udhaarAmount: Double = 0.0,
    val customerId: String? = null,
    val paymentSplit: PaymentDetails? = null,
    val referenceId: String? = null
)
