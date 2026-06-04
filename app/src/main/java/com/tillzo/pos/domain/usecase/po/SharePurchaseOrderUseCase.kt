package com.tillzo.pos.domain.usecase.po

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.tillzo.pos.data.local.entity.PurchaseOrderEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderItemEntity
import com.tillzo.pos.data.local.entity.VendorEntity
import javax.inject.Inject

class SharePurchaseOrderUseCase @Inject constructor() {
    operator fun invoke(
        context: Context,
        po: PurchaseOrderEntity,
        items: List<PurchaseOrderItemEntity>,
        vendor: VendorEntity?,
        shopName: String,
        shopPhone: String
    ) {
        val sb = StringBuilder()
        sb.append("PURCHASE ORDER\n")
        sb.append("$shopName\n\n")
        sb.append("PO Number: ${po.poNumber}\n")
        sb.append("Date: ${po.createdAt}\n") // Format this properly in real usage
        sb.append("Expected Delivery: ${po.expectedDeliveryDate}\n\n")
        sb.append("VENDOR: ${po.vendorName}\n")
        if (vendor != null) sb.append("Phone: ${vendor.phone}\n")
        sb.append("\nITEM              QTY    PRICE   TOTAL\n")
        sb.append("-".repeat(40) + "\n")
        
        items.forEach { item ->
            sb.append("${item.productName.take(16).padEnd(16)} ${item.orderedQty} ${item.unit}  ${item.unitCostPrice}  ${item.totalCost}\n")
        }
        sb.append("-".repeat(40) + "\n")
        sb.append("TOTAL AMOUNT:          ${po.currency} ${po.totalAmount}\n")
        if (po.notes.isNotBlank()) {
            sb.append("Notes: ${po.notes}\n")
        }
        sb.append("\nPlease confirm receipt of this order.\n")
        sb.append("$shopName | $shopPhone")

        val message = sb.toString()
        val whatsappNumber = vendor?.whatsapp?.takeIf { it.isNotBlank() } ?: vendor?.phone

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            if (!whatsappNumber.isNullOrBlank()) {
                putExtra("jid", "$whatsappNumber@s.whatsapp.net")
                setPackage("com.whatsapp")
            }
        }
        
        try {
            context.startActivity(sendIntent)
        } catch (e: Exception) {
            // Fallback to standard share sheet if WA is not installed
            val fallbackIntent = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }, "Share PO")
            context.startActivity(fallbackIntent)
        }
    }
}
