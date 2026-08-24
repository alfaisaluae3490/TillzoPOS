package com.tillzo.pos.utils

/**
 * Barcode validation helpers.
 *
 * FIX (2026-08-22, DEF-08): GTIN checksum validation was missing entirely —
 * any string could be saved as a product barcode, and invalid barcodes never
 * scan at retail POS. These helpers validate EAN-13 check digits (the most
 * common retail format) and normalize user input.
 */
object BarcodeUtils {

    /**
     * Validates a 13-digit EAN-13 code by recomputing its check digit
     * (weights 1-3-1-3... from the LEFT, check digit = (10 - sum%10)%10).
     */
    fun isValidEan13(code: String): Boolean {
        val digits = code.filter { it.isDigit() }
        if (digits.length != 13) return false
        var sum = 0
        for (i in 0 until 12) {
            val d = digits[i] - '0'
            sum += if (i % 2 == 0) d else d * 3
        }
        val check = (10 - (sum % 10)) % 10
        return check == digits[12] - '0'
    }

    /** Normalizes any barcode input: keeps digits only. */
    fun normalizeBarcode(raw: String): String = raw.filter { it.isDigit() }
}
