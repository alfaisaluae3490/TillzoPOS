package com.tillzo.pos.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.tillzo.pos.domain.model.Sale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptGenerator {

    /**
     * M4.9 Generate Receipt QR Code containing Invoice UUID
     */
    fun generateQrCode(invoiceId: String, size: Int = 512): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(invoiceId, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * M4.8 Send Receipt via WhatsApp via Android Intent
     */
    fun sendWhatsAppReceipt(context: Context, phoneNumber: String, sale: Sale) {
        val message = buildReceiptText(sale)
        
        // Clean phone number
        val cleanPhone = phoneNumber.replace("[^0-9+]".toRegex(), "")
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // In real app, show Toast: "WhatsApp not installed"
        }
    }

    private fun buildReceiptText(sale: Sale): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateString = sdf.format(Date(sale.timestamp))
        
        val sb = java.lang.StringBuilder()
        sb.appendLine("🧾 *Tillzo POS Receipt* 🧾")
        sb.appendLine("========================")
        sb.appendLine("Invoice: ${sale.invoiceId.takeLast(8).uppercase()}")
        sb.appendLine("Date: $dateString")
        sb.appendLine("Cashier: ${sale.cashierId}")
        sb.appendLine("========================")
        
        sale.items.forEach { item ->
            sb.appendLine("▪ ${item.name}")
            sb.appendLine("  ${item.quantity} x Rs ${item.pricePerUnit} = Rs ${item.total}")
        }
        
        sb.appendLine("------------------------")
        sb.appendLine("*Total:* Rs ${sale.total}")
        if (sale.paymentMethod == "Split") {
            sb.appendLine("Payment Split:")
            sale.paymentSplit?.let {
                if(it.cashAmount > 0) sb.appendLine("- Cash: Rs ${it.cashAmount}")
                if(it.cardAmount > 0) sb.appendLine("- Card: Rs ${it.cardAmount}")
                if(it.walletAmount > 0) sb.appendLine("- Wallet: Rs ${it.walletAmount}")
                if(it.udhaarAmount > 0) sb.appendLine("- Udhaar: Rs ${it.udhaarAmount}")
            }
        } else {
            sb.appendLine("Payment: ${sale.paymentMethod}")
        }
        sb.appendLine("========================")
        sb.appendLine("Thank you for your visit!")
        
        return sb.toString()
    }
}
