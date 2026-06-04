package com.tillzo.pos.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import javax.inject.Inject

class BarcodeHelper @Inject constructor() {

    fun generateQRCode(barcodeId: String, width: Int = 512, height: Int = 512): Bitmap? {
        if (barcodeId.isBlank()) return null
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(barcodeId, BarcodeFormat.QR_CODE, width, height)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun autoGenerateBarcodeId(lastId: String? = null): String {
        val randomSuffix = (100000..999999).random() // Simplistic approach as defined previously
        return "CUST-$randomSuffix"
    }

    // Note: The UI layer (BarcodeScannerScreen.kt) handles the camera launch, 
    // lifecycle, debounce, and auto-deactivate functionalities as per Compose rules.
}
