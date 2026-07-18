package com.tillzo.pos.utils

import com.tillzo.pos.data.local.prefs.BarcodeFieldConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class BarcodeGeneratorUtilTest {

    @Test
    fun testGenerateDynamicSerialNumber() {
        val serial1 = BarcodeGeneratorUtil.generateDynamicSerialNumber("SKU123", 0)
        val serial2 = BarcodeGeneratorUtil.generateDynamicSerialNumber("SKU123", 1)
        
        // Assert sequence numbers are unique and end with correctly padded indices
        assert(serial1.endsWith("SKU123001")) { "Serial1 should end with SKU123001: $serial1" }
        assert(serial2.endsWith("SKU123002")) { "Serial2 should end with SKU123002: $serial2" }
        assertEquals(15, serial1.length)
    }

    @Test
    fun testBuildDynamicGs1String_DefaultConfig() {
        val fields = listOf(
            BarcodeFieldConfig(fieldId = "GTIN", fieldName = "GTIN", aiCode = "01", isEnabled = true, sequenceOrder = 0, useFnc1Separator = false),
            BarcodeFieldConfig(fieldId = "EXPIRY", fieldName = "Expiry Date", aiCode = "17", isEnabled = true, sequenceOrder = 1, useFnc1Separator = false),
            BarcodeFieldConfig(fieldId = "BATCH", fieldName = "Batch/Lot Number", aiCode = "10", isEnabled = true, sequenceOrder = 2, useFnc1Separator = true),
            BarcodeFieldConfig(fieldId = "SN", fieldName = "Serial Number", aiCode = "21", isEnabled = true, sequenceOrder = 3, useFnc1Separator = false)
        )

        val result = BarcodeGeneratorUtil.buildDynamicGs1String(
            fields = fields,
            gtin = "12345678901234",
            expiryYYMMDD = "260623",
            batch = "BATCH123",
            serial = "SN12345",
            sku = "SKU1",
            usePrefix = true,
            customPrefix = "]d2",
            prefixPosition = 0,
            useSuffix = false
        )

        // Expected format: ]d2 + 01 + 12345678901234 + 17 + 260623 + 10 + BATCH123 + ~1 + 21 + SN12345
        val expected = "]d201123456789012341726062310BATCH123~121SN12345"
        assertEquals(expected, result)
    }

    @Test
    fun testBuildDynamicGs1String_ReorderedAndDisabled() {
        val fields = listOf(
            BarcodeFieldConfig(fieldId = "BATCH", fieldName = "Batch/Lot Number", aiCode = "10", isEnabled = true, sequenceOrder = 0, useFnc1Separator = true),
            BarcodeFieldConfig(fieldId = "GTIN", fieldName = "GTIN", aiCode = "01", isEnabled = true, sequenceOrder = 1, useFnc1Separator = false),
            BarcodeFieldConfig(fieldId = "EXPIRY", fieldName = "Expiry Date", aiCode = "17", isEnabled = false, sequenceOrder = 2, useFnc1Separator = false),
            BarcodeFieldConfig(fieldId = "SN", fieldName = "Serial Number", aiCode = "21", isEnabled = true, sequenceOrder = 3, useFnc1Separator = false)
        )

        val result = BarcodeGeneratorUtil.buildDynamicGs1String(
            fields = fields,
            gtin = "12345678901234",
            expiryYYMMDD = "260623",
            batch = "BATCH123",
            serial = "SN12345",
            sku = "SKU1",
            usePrefix = true,
            customPrefix = "]d2",
            prefixPosition = 0,
            useSuffix = false
        )

        // Expiry (17) is disabled. Batch (10) comes first.
        // Expected format: ]d2 + 10 + BATCH123 + ~1 + 01 + 12345678901234 + 21 + SN12345
        val expected = "]d210BATCH123~1011234567890123421SN12345"
        assertEquals(expected, result)
    }

    @Test
    fun testBuildDynamicGs1String_PrefixSuffixPositions() {
        val fields = listOf(
            BarcodeFieldConfig(fieldId = "GTIN", fieldName = "GTIN", aiCode = "01", isEnabled = true, sequenceOrder = 0, useFnc1Separator = false)
        )

        // Prefix position > 0, Suffix enabled at last position
        val result = BarcodeGeneratorUtil.buildDynamicGs1String(
            fields = fields,
            gtin = "12345678901234",
            expiryYYMMDD = "260623",
            batch = "BATCH123",
            serial = "SN12345",
            sku = "SKU1",
            usePrefix = true,
            customPrefix = "PRE",
            prefixPosition = 2, // Inserts after "01"
            useSuffix = true,
            customSuffix = "SUF",
            suffixPosition = 100 // Out of bounds -> appends at the end
        )

        // Baseline string: 0112345678901234
        // Insert "PRE" at index 2 -> "01" + "PRE" + "12345678901234" = "01PRE12345678901234"
        // Append "SUF" at the end -> "01PRE12345678901234SUF"
        val expected = "01PRE12345678901234SUF"
        assertEquals(expected, result)
    }
}
