package com.tillzo.pos.utils

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.Base64

data class CountryTaxPreset(
    val code: String,
    val name: String,
    val flag: String,
    val currencySymbol: String,
    val taxLabel: String,
    val taxIdLabel: String,
    val defaultTaxRate: Double,
    val taxInclusive: Boolean,
    val enableZatcaQr: Boolean = false
)

object TaxUtils {

    val PRESETS = listOf(
        CountryTaxPreset(
            code = "AE",
            name = "United Arab Emirates",
            flag = "🇦🇪",
            currencySymbol = "AED",
            taxLabel = "VAT",
            taxIdLabel = "TRN",
            defaultTaxRate = 5.0,
            taxInclusive = true,
            enableZatcaQr = true
        ),
        CountryTaxPreset(
            code = "SA",
            name = "Saudi Arabia",
            flag = "🇸🇦",
            currencySymbol = "SAR",
            taxLabel = "VAT",
            taxIdLabel = "VAT Number",
            defaultTaxRate = 15.0,
            taxInclusive = true,
            enableZatcaQr = true
        ),
        CountryTaxPreset(
            code = "GB",
            name = "United Kingdom",
            flag = "🇬🇧",
            currencySymbol = "£",
            taxLabel = "VAT",
            taxIdLabel = "VAT Reg No",
            defaultTaxRate = 20.0,
            taxInclusive = true
        ),
        CountryTaxPreset(
            code = "DE",
            name = "Germany (EU)",
            flag = "🇩🇪",
            currencySymbol = "€",
            taxLabel = "MwSt",
            taxIdLabel = "USt-IdNr",
            defaultTaxRate = 19.0,
            taxInclusive = true
        ),
        CountryTaxPreset(
            code = "PK",
            name = "Pakistan",
            flag = "🇵🇰",
            currencySymbol = "Rs.",
            taxLabel = "GST",
            taxIdLabel = "NTN / STRN",
            defaultTaxRate = 18.0,
            taxInclusive = true
        ),
        CountryTaxPreset(
            code = "IN",
            name = "India",
            flag = "🇮🇳",
            currencySymbol = "₹",
            taxLabel = "GST",
            taxIdLabel = "GSTIN",
            defaultTaxRate = 18.0,
            taxInclusive = true
        ),
        CountryTaxPreset(
            code = "US",
            name = "United States",
            flag = "🇺🇸",
            currencySymbol = "$",
            taxLabel = "Sales Tax",
            taxIdLabel = "EIN / Tax ID",
            defaultTaxRate = 0.0,
            taxInclusive = false
        ),
        CountryTaxPreset(
            code = "CA",
            name = "Canada",
            flag = "🇨🇦",
            currencySymbol = "C$",
            taxLabel = "GST/HST",
            taxIdLabel = "Business No",
            defaultTaxRate = 5.0,
            taxInclusive = false
        ),
        CountryTaxPreset(
            code = "AU",
            name = "Australia",
            flag = "🇦🇺",
            currencySymbol = "A$",
            taxLabel = "GST",
            taxIdLabel = "ABN",
            defaultTaxRate = 10.0,
            taxInclusive = true
        ),
        CountryTaxPreset(
            code = "CN",
            name = "China",
            flag = "🇨🇳",
            currencySymbol = "¥",
            taxLabel = "VAT",
            taxIdLabel = "Tax Code",
            defaultTaxRate = 13.0,
            taxInclusive = true
        ),
        CountryTaxPreset(
            code = "OTHER",
            name = "Other / Global",
            flag = "🌐",
            currencySymbol = "$",
            taxLabel = "Tax",
            taxIdLabel = "Tax ID",
            defaultTaxRate = 0.0,
            taxInclusive = true
        )
    )

    fun getPreset(code: String): CountryTaxPreset {
        return PRESETS.firstOrNull { it.code.equals(code, ignoreCase = true) }
            ?: PRESETS.last()
    }

    /**
     * Exact Global Tax Calculation Engine.
     *
     * In Tax-Inclusive mode:
     *   netBase = gross / (1.0 + rate / 100.0)
     *   taxAmount = gross - netBase
     *
     * In Tax-Exclusive mode:
     *   netBase = gross
     *   taxAmount = gross * (rate / 100.0)
     *
     * Returns Pair(netBase, taxAmount).
     */
    fun computeLineTax(grossLineTotal: Double, taxPercent: Double, isTaxInclusive: Boolean): Pair<Double, Double> {
        val safeGross = grossLineTotal.coerceAtLeast(0.0)
        val safeRate = taxPercent.coerceAtLeast(0.0)
        if (safeRate <= 0.0 || safeGross <= 0.0) {
            return Pair(safeGross, 0.0)
        }

        return if (isTaxInclusive) {
            val netBase = safeGross / (1.0 + (safeRate / 100.0))
            val tax = safeGross - netBase
            Pair(netBase, tax)
        } else {
            val tax = safeGross * (safeRate / 100.0)
            Pair(safeGross, tax)
        }
    }

    /**
     * Generates ZATCA (Saudi Arabia & UAE) compliant Base64 TLV (Tag-Length-Value) QR code.
     *
     * Tag 1: Seller Name
     * Tag 2: VAT Registration Number / TRN
     * Tag 3: Invoice Timestamp (ISO 8601)
     * Tag 4: Invoice Total (with VAT)
     * Tag 5: VAT Total
     */
    fun generateZatcaTlvBase64(
        sellerName: String,
        vatNumber: String,
        isoTimestamp: String,
        totalWithVat: String,
        vatTotal: String
    ): String {
        try {
            val out = ByteArrayOutputStream()
            writeTlvTag(out, 1, sellerName)
            writeTlvTag(out, 2, vatNumber)
            writeTlvTag(out, 3, isoTimestamp)
            writeTlvTag(out, 4, totalWithVat)
            writeTlvTag(out, 5, vatTotal)
            val bytes = out.toByteArray()
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Base64.getEncoder().encodeToString(bytes)
            } else {
                android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            }
        } catch (_: Exception) {
            return "$sellerName|$vatNumber|$isoTimestamp|$totalWithVat|$vatTotal"
        }
    }

    private fun writeTlvTag(out: ByteArrayOutputStream, tag: Int, value: String) {
        val valBytes = value.toByteArray(StandardCharsets.UTF_8)
        out.write(tag)
        out.write(valBytes.size)
        out.write(valBytes)
    }
}
