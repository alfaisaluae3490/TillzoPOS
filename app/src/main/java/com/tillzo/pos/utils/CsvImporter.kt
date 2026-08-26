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

    /** OVERNIGHT-AUDIT Phase 1/2 — Customer master import (module 7 CRM). */
    fun parseCustomerRows(input: InputStream): List<CsvCustomerRow> {
        val rows = readCsv(input)
        val first = rows.firstOrNull() ?: return emptyList()
        if (first.values["name"].isNullOrBlank() && first.values["customer"].isNullOrBlank()) {
            return emptyList()
        }
        return rows.mapNotNull { r ->
            val name = r.values["name"] ?: r.values["customer"] ?: r.values["customer_name"] ?: ""
            if (name.isBlank()) return@mapNotNull null
            CsvCustomerRow(
                name = name.trim(),
                phone = (r.values["phone"] ?: r.values["mobile"] ?: r.values["contact"] ?: "").trim(),
                whatsapp = r.values["whatsapp"]?.trim()?.takeIf { it.isNotEmpty() },
                email = r.values["email"]?.trim()?.takeIf { it.isNotEmpty() },
                address = r.values["address"]?.trim()?.takeIf { it.isNotEmpty() },
                openingBalance = (r.values["opening_balance"]
                    ?: r.values["balance"] ?: r.values["openingbal"] ?: "").toDoubleOrNull() ?: 0.0,
                creditLimit = (r.values["credit_limit"]
                    ?: r.values["creditlimit"] ?: "").toDoubleOrNull() ?: 0.0
            )
        }
    }

    /** OVERNIGHT-AUDIT Phase 1/2 — Batch master import (module 6 batch engine). */
    fun parseBatchRows(input: InputStream): List<CsvBatchRow> {
        val rows = readCsv(input)
        val first = rows.firstOrNull() ?: return emptyList()
        if (first.values["product_id"].isNullOrBlank() && first.values["sku"].isNullOrBlank()) {
            return emptyList()
        }
        return rows.mapNotNull { r ->
            val product = r.values["product_id"] ?: r.values["productid"] ?: r.values["sku"] ?: r.values["product"] ?: ""
            if (product.isBlank()) return@mapNotNull null
            CsvBatchRow(
                productId = product.trim(),
                barcodeId = (r.values["barcode"] ?: r.values["barcode_id"] ?: r.values["barcodeid"] ?: "").trim(),
                batchNumber = (r.values["batch"] ?: r.values["batch_no"]
                    ?: r.values["batch_number"] ?: r.values["batchno"] ?: "").trim(),
                manufacturingDate = (r.values["manufacturing_date"]
                    ?: r.values["mfg_date"] ?: r.values["mfgdate"] ?: r.values["mfg"] ?: "").trim(),
                expiryDate = (r.values["expiry_date"] ?: r.values["exp_date"]
                    ?: r.values["expirydate"] ?: r.values["exp"] ?: "").trim(),
                stockQty = (r.values["stock_qty"] ?: r.values["quantity"]
                    ?: r.values["qty"] ?: r.values["stock"] ?: "").toDoubleOrNull() ?: 0.0,
                costPrice = (r.values["cost_price"] ?: r.values["cost"]
                    ?: r.values["costprice"] ?: "").toDoubleOrNull() ?: 0.0,
                sellingPrice = (r.values["selling_price"] ?: r.values["sale_price"]
                    ?: r.values["price"] ?: r.values["sellingprice"] ?: "").toDoubleOrNull() ?: 0.0
            )
        }
    }

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

/** OVERNIGHT-AUDIT Phase 1/2 — Customer master import row (module 7 CRM). */
data class CsvCustomerRow(
    val name: String,
    val phone: String,
    val whatsapp: String?,
    val email: String?,
    val address: String?,
    val openingBalance: Double,
    val creditLimit: Double,
)

/** OVERNIGHT-AUDIT Phase 1/2 — Batch master import row (module 6 batch engine). */
data class CsvBatchRow(
    val productId: String,
    val barcodeId: String,
    val batchNumber: String,
    val manufacturingDate: String,
    val expiryDate: String,
    val stockQty: Double,
    val costPrice: Double,
    val sellingPrice: Double,
)
