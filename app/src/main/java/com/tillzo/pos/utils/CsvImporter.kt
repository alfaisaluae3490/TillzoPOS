package com.tillzo.pos.utils

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * OVERNIGHT-AUDIT Phase 2b — manual bulk upload via Excel/CSV.
 *
 * FIX (2026-08-23): bulk data could only be entered item-by-item in the UI or
 * synced down from the Sheet. Now users can import a UTF-8 CSV exported from
 * Excel/Sheets directly into Inventory and Vendors.
 *
 * Expected headers (first row, case-insensitive, order-independent):
 *   inventory.csv : name, sku, barcode, category, cost_price, selling_price, stock_qty, unit
 *   vendors.csv   : name, phone, email, address, city, credit_limit
 */
object CsvImporter {

    /** Thrown when the file's header row is missing required columns. */
    class HeaderException(missing: List<String>, expected: String) :
        Exception("Missing columns: $missing. Required: $expected")

    data class Row(val values: Map<String, String>)

    /**
     * RFC-4180-ish CSV reader: handles quoted fields with embedded commas/newlines
     * and escaped quotes (""). Excel/Google-Sheets exports parse cleanly.
     */
    fun readCsv(input: InputStream): List<Row> {
        val rows = mutableListOf<List<String>>()
        BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
            var field = StringBuilder()
            var record = mutableListOf<String>()
            var inQuotes = false
            var sawAnyChar = false

            fun endField() {
                record.add(field.toString())
                field = StringBuilder()
            }

            fun endRecord() {
                if (record.isNotEmpty() || sawAnyChar) {
                    rows.add(record)
                    record = mutableListOf()
                    sawAnyChar = false
                }
            }

            while (true) {
                val i = reader.read()
                if (i < 0) break
                sawAnyChar = true
                val c = i.toChar()
                when {
                    inQuotes && c == '"' -> {
                        // Peek: "" inside quotes is an escaped quote
                        reader.mark(1)
                        val next = reader.read()
                        if (next == '"'.code) field.append('"') else {
                            if (next >= 0) reader.reset() else return@use
                            inQuotes = false
                        }
                    }
                    c == '"' -> inQuotes = true
                    !inQuotes && c == ',' -> endField()
                    !inQuotes && (c == '\n' || c == '\r') -> {
                        if (c == '\r') {
                            reader.mark(1)
                            if (reader.read() != '\n'.code) reader.reset()
                        }
                        endField()
                        endRecord()
                    }
                    else -> field.append(c)
                }
            }
            if (field.isNotEmpty() || record.isNotEmpty()) {
                endField()
                endRecord()
            }
        }
        if (rows.isEmpty()) return emptyList()

        val headers = rows.first().map { it.trim().lowercase().replace(' ', '_') }
        return rows.drop(1)
            .filter { cells -> cells.any { it.isNotBlank() } } // skip blank lines
            .map { cells ->
                val map = mutableMapOf<String, String>()
                headers.forEachIndexed { idx, h ->
                    map[h] = cells.getOrNull(idx)?.trim().orEmpty()
                }
                Row(map)
            }
    }

    private fun requireColumns(row: Row, needed: List<String>, context: String) {
        val missing = needed.filter { row.values[it] == null }
        if (missing.isNotEmpty()) throw HeaderException(missing, "$context headers")
    }

    // ---- Inventory -------------------------------------------------------------

    fun parseInventoryRows(input: InputStream): List<CsvInventoryRow> {
        val rows = readCsv(input)
        val first = rows.firstOrNull() ?: return emptyList()
        requireColumns(
            first,
            listOf("name", "sku", "cost_price", "selling_price"),
            "inventory",
        )
        return rows.mapNotNull { r ->
            val name = r.values["name"]?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            CsvInventoryRow(
                name = name,
                sku = r.values["sku"].orEmpty(),
                barcode = r.values["barcode"].orEmpty(),
                category = r.values["category"].orEmpty(),
                costPrice = r.values["cost_price"]?.toDoubleOrNull() ?: 0.0,
                sellingPrice = r.values["selling_price"]?.toDoubleOrNull() ?: 0.0,
                stockQty = r.values["stock_qty"]?.toDoubleOrNull() ?: 0.0,
                unit = r.values["unit"].orEmpty().ifBlank { "pcs" },
            )
        }
    }

    // ---- Vendors ---------------------------------------------------------------

    fun parseVendorRows(input: InputStream): List<CsvVendorRow> {
        val rows = readCsv(input)
        val first = rows.firstOrNull() ?: return emptyList()
        requireColumns(first, listOf("name"), "vendors")
        return rows.mapNotNull { r ->
            val name = r.values["name"]?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            CsvVendorRow(
                name = name,
                phone = r.values["phone"].orEmpty(),
                email = r.values["email"].orEmpty(),
                address = r.values["address"].orEmpty(),
                city = r.values["city"].orEmpty(),
                creditLimit = r.values["credit_limit"]?.toDoubleOrNull() ?: 0.0,
            )
        }
    }
}

data class CsvInventoryRow(
    val name: String,
    val sku: String,
    val barcode: String,
    val category: String,
    val costPrice: Double,
    val sellingPrice: Double,
    val stockQty: Double,
    val unit: String,
)

data class CsvVendorRow(
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val city: String,
    val creditLimit: Double,
)
