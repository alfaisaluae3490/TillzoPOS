package com.tillzo.pos.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.tillzo.pos.data.local.entity.BarcodeFieldConfigEntity

object BarcodeGeneratorUtil {

    private const val FNC1 = "~1" // Represents FNC1/Group Separator in ZXing for Code 128/DataMatrix
    private const val PREFIX = "]d2"

    /**
     * Generates a dynamic serial number using the format: [HHMMSS][SKU_part][Index_part]
     */
    fun generateDynamicSerialNumber(sku: String = "0000", index: Int = 0): String {
        val sdfTime = SimpleDateFormat("HHmmss", Locale.getDefault())
        val time = sdfTime.format(Date())
        val cleanSku = sku.replace("-", "").take(6).ifBlank { "00" }
        val paddedIndex = String.format(Locale.getDefault(), "%03d", index + 1)
        return "$time$cleanSku$paddedIndex"
    }

    /**
     * Generates the GS1 format string.
     * Rules:
     * - (01) GTIN: 14 chars
     * - (17) Expiry: 6 chars (YYMMDD)
     * - (10) Batch/Lot: Variable length (max 20) -> must be followed by FNC1
     * - (21) Serial: Variable length (max 20)
     */
    fun buildGs1String(
        gtin: String,
        expiryYYMMDD: String,
        batch: String,
        serial: String,
        usePrefix: Boolean = true,
        customPrefix: String = PREFIX,
        prefixPosition: Int = 0,
        useSeparator: Boolean = true,
        customSuffix: String = "",
        useSuffix: Boolean = false,
        suffixPosition: Int = 0
    ): String {
        val builder = java.lang.StringBuilder()
        if (usePrefix && prefixPosition <= 0) {
            builder.append(customPrefix)
        }
        
        var adjustedGtin = gtin.padStart(14, '0')
        if (usePrefix && prefixPosition > 0 && prefixPosition <= adjustedGtin.length) {
            adjustedGtin = adjustedGtin.substring(0, prefixPosition) + customPrefix + adjustedGtin.substring(prefixPosition)
        }
        
        if (useSuffix) {
            if (suffixPosition > 0 && suffixPosition < adjustedGtin.length) {
                adjustedGtin = adjustedGtin.substring(0, suffixPosition) + customSuffix + adjustedGtin.substring(suffixPosition)
            } else {
                adjustedGtin = adjustedGtin + customSuffix
            }
        }
        
        // AI 01
        builder.append("01").append(adjustedGtin)
        
        // AI 17
        if (expiryYYMMDD.length == 6) {
            builder.append("17").append(expiryYYMMDD)
        } else {
            builder.append("17").append("000000") // Fallback
        }
        
        // AI 10 (Variable length, requires FNC1 separator after it, unless it's the last AI)
        builder.append("10").append(batch)
        if (useSeparator) {
            builder.append(FNC1)
        }
        
        // AI 21
        builder.append("21").append(serial)
        
        return builder.toString()
    }

    /**
     * Generates a dynamic GS1 string based on the user's custom fields order and enable status.
     */
    fun buildDynamicGs1String(
        fields: List<BarcodeFieldConfigEntity>,
        gtin: String,
        expiryYYMMDD: String,
        batch: String,
        serial: String,
        sku: String,
        usePrefix: Boolean = true,
        customPrefix: String = PREFIX,
        prefixPosition: Int = 0,
        useSuffix: Boolean = false,
        customSuffix: String = "",
        suffixPosition: Int = 0
    ): String {
        if (fields.isEmpty()) {
            return buildGs1String(
                gtin = gtin,
                expiryYYMMDD = expiryYYMMDD,
                batch = batch,
                serial = serial,
                usePrefix = usePrefix,
                customPrefix = customPrefix,
                prefixPosition = prefixPosition,
                useSeparator = true,
                customSuffix = customSuffix,
                useSuffix = useSuffix,
                suffixPosition = suffixPosition
            )
        }

        val builder = java.lang.StringBuilder()
        val enabledFields = fields.filter { it.isEnabled }.sortedBy { it.sequenceOrder }

        for (field in enabledFields) {
            builder.append(field.aiCode)
            val rawValue = when (field.fieldId) {
                "GTIN" -> gtin.padStart(14, '0')
                "EXPIRY" -> if (expiryYYMMDD.length == 6) expiryYYMMDD else "000000"
                "BATCH" -> batch
                "SN" -> serial
                "SKU" -> sku
                else -> field.customValue
            }
            builder.append(rawValue)
            if (field.useFnc1Separator) {
                builder.append(FNC1)
            }
        }

        var finalString = builder.toString()

        if (usePrefix) {
            if (prefixPosition <= 0) {
                finalString = customPrefix + finalString
            } else if (prefixPosition < finalString.length) {
                finalString = finalString.substring(0, prefixPosition) + customPrefix + finalString.substring(prefixPosition)
            } else {
                finalString = finalString + customPrefix
            }
        }

        if (useSuffix) {
            if (suffixPosition <= 0) {
                finalString = customSuffix + finalString
            } else if (suffixPosition < finalString.length) {
                finalString = finalString.substring(0, suffixPosition) + customSuffix + finalString.substring(suffixPosition)
            } else {
                finalString = finalString + customSuffix
            }
        }

        return finalString
    }

    /**
     * Renders a string as a DataMatrix Bitmap, forcing a square symbol shape.
     */
    fun generateDataMatrix(content: String, width: Int, height: Int): Bitmap? {
        val writer = MultiFormatWriter()
        return try {
            val hints = mapOf(
                com.google.zxing.EncodeHintType.DATA_MATRIX_SHAPE to com.google.zxing.datamatrix.encoder.SymbolShapeHint.FORCE_SQUARE
            )
            // Encode at 0x0 to get the minimal raw symbol (no padding)
            val bitMatrix = writer.encode(content, BarcodeFormat.DATA_MATRIX, 0, 0, hints)
            val w = bitMatrix.width
            val h = bitMatrix.height
            val pixels = IntArray(w * h)
            for (y in 0 until h) {
                for (x in 0 until w) {
                    pixels[y * w + x] = if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                }
            }
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }
}
