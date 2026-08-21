package com.tillzo.pos.utils

import android.content.Context
import android.net.Uri
import com.tillzo.pos.data.local.AppDatabase
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.data.local.entity.SaleEntity
import com.tillzo.pos.data.local.entity.CustomerEntity
import com.tillzo.pos.data.local.entity.KhataEventEntity
import com.tillzo.pos.data.local.entity.ExpenseEntity
import com.tillzo.pos.data.local.entity.TillSessionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase
) {

    suspend fun exportToZip(uri: Uri, onProgress: (String) -> Unit = {}): Result<Unit> = runCatching {
        onProgress("Exporting backup...")

        val csvFiles = buildFullSnapshot(onProgress)

        onProgress("Writing ZIP file...")
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            writeZip(outputStream, csvFiles)
        } ?: throw Exception("Could not open output URI")

        onProgress("Backup complete!")
    }

    /**
     * FIX (2026-08-06): Faisal's requirement — one-tap backup copy to PUBLIC
     * Documents/TillzoPOS (survives uninstall/reinstall). Returns file path.
     * Uses MediaStore (API 29+) so it works on modern Android without
     * MANAGE_EXTERNAL_STORAGE; falls back to direct File on older APIs.
     */
    suspend fun exportToPublicDocuments(): String {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
            .format(java.util.Date())
        val fileName = "tillzo_backup_$timestamp.zip"
        val csvFiles = buildFullSnapshot {}

        val path = if (android.os.Build.VERSION.SDK_INT >= 29) {
            // MediaStore — scoped storage safe write
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                put(
                    android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_DOCUMENTS + "/TillzoPOS"
                )
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(
                android.provider.MediaStore.Files.getContentUri("external"), values
            ) ?: throw Exception("Could not create MediaStore entry")
            resolver.openOutputStream(uri)?.use { writeZip(it, csvFiles) }
                ?: throw Exception("Could not open output stream")

            // Also write the rotating "latest" copy
            val latestValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "latest_tillzo_backup.zip")
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                put(
                    android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_DOCUMENTS + "/TillzoPOS"
                )
            }
            val latestUri = resolver.insert(
                android.provider.MediaStore.Files.getContentUri("external"), latestValues
            )
            latestUri?.let { resolver.openOutputStream(it)?.use { os -> writeZip(os, csvFiles) } }

            "Documents/TillzoPOS/$fileName"
        } else {
            // Legacy direct file write (API < 29)
            val dir = java.io.File(
                android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOCUMENTS
                ),
                "TillzoPOS"
            )
            if (!dir.exists()) dir.mkdirs()
            val target = java.io.File(dir, fileName)
            java.io.FileOutputStream(target).use { writeZip(it, csvFiles) }
            val latest = java.io.File(dir, "latest_tillzo_backup.zip")
            java.io.FileOutputStream(latest).use { writeZip(it, csvFiles) }
            target.absolutePath
        }
        return path
    }

    private suspend fun buildFullSnapshot(onProgress: (String) -> Unit): MutableMap<String, String> {
        val csvFiles = mutableMapOf<String, String>()

        onProgress("Exporting Inventory...")
        val inventory: List<InventoryEntity> = appDatabase.inventoryDao().getAllItems().first()
        csvFiles["Inventory.csv"] = toCsv(inventory, listOf(
            "system_row_id", "item_name", "category", "barcode_id", "unit",
            "price_per_unit", "current_stock", "low_stock_threshold", "sku",
            "cost_price", "tax_percent"
        )) { e -> e.system_row_id to listOf(
            e.system_row_id, e.item_name, e.category, e.barcode_id, e.unit,
            e.price_per_unit.toString(), e.current_stock.toString(), e.low_stock_threshold.toString(),
            e.sku, e.cost_price.toString(), e.tax_percent.toString()
        ) }

        onProgress("Exporting Sales...")
        // FIX (2026-08-06): backup ALL sales (synced + pending) — pending-only lost synced data
        val sales: List<SaleEntity> = appDatabase.saleDao().getAllSales().first()
        csvFiles["Sales.csv"] = toCsv(sales, listOf(
            "system_row_id", "sync_uuid", "cashier_id", "timestamp", "subtotal", "tax",
            "discount", "total", "payment_method", "cash_amount", "card_amount",
            "wallet_amount", "udhaar_amount", "customer_id"
        )) { e -> e.system_row_id to listOf(
            e.system_row_id, e.sync_uuid, e.cashier_id, e.timestamp.toString(),
            e.subtotal.toString(), e.tax.toString(), e.discount.toString(),
            e.total.toString(), e.payment_method, e.cash_amount.toString(),
            e.card_amount.toString(), e.wallet_amount.toString(),
            e.udhaar_amount.toString(), e.customer_id ?: ""
        ) }

        onProgress("Exporting Customers...")
        // FIX (2026-08-06): ALL customers
        val customers: List<CustomerEntity> = appDatabase.customerDao().getAllCustomers().first()
        csvFiles["Customers.csv"] = toCsv(customers, listOf(
            "system_row_id", "name", "phone", "whatsapp", "email", "address"
        )) { e -> e.system_row_id to listOf(
            e.system_row_id, e.name, e.phone, e.whatsapp ?: "", e.email ?: "", e.address ?: ""
        ) }

        onProgress("Exporting Khata Events...")
        // FIX (2026-08-06): ALL khata events
        val khataEvents: List<KhataEventEntity> = appDatabase.khataEventDao().getAllKhataEvents()
        csvFiles["KhataEvents.csv"] = toCsv(khataEvents, listOf(
            "system_row_id", "customer_id", "event_type", "amount", "note", "reference_sale_id"
        )) { e -> e.system_row_id to listOf(
            e.system_row_id, e.customer_id, e.event_type, e.amount.toString(),
            e.note ?: "", e.reference_sale_id ?: ""
        ) }

        onProgress("Exporting Expenses...")
        // FIX (2026-08-06): ALL expenses
        val expenses: List<ExpenseEntity> = appDatabase.expenseDao().getAllExpenses().first()
        csvFiles["Expenses.csv"] = toCsv(expenses, listOf(
            "system_row_id", "category", "amount", "description", "timestamp", "logged_by_user_id"
        )) { e -> e.system_row_id to listOf(
            e.system_row_id, e.category, e.amount.toString(), e.description,
            e.timestamp.toString(), e.logged_by_user_id
        ) }

        onProgress("Exporting Till Sessions...")
        val sessions: List<TillSessionEntity> = appDatabase.tillSessionDao().getAllSessions().first()
        csvFiles["TillSessions.csv"] = toCsv(sessions, listOf(
            "sessionId", "cashierName", "openingCash", "closingCash", "expectedCash",
            "totalCashSales", "totalCardSales", "totalWalletSales", "totalUdhaarSales",
            "totalSalesCount", "totalRefunds", "netCash", "status", "shiftDate",
            "openedAt", "closedAt"
        )) { e -> e.sessionId to listOf(
            e.sessionId, e.cashierName, e.openingCash.toString(), e.closingCash.toString(),
            e.expectedCash.toString(), e.totalCashSales.toString(), e.totalCardSales.toString(),
            e.totalWalletSales.toString(), e.totalUdhaarSales.toString(),
            e.totalSalesCount.toString(), e.totalRefunds.toString(), e.netCash.toString(),
            e.status, e.shiftDate, e.openedAt.toString(), e.closedAt?.toString() ?: ""
        ) }

        return csvFiles
    }

    private fun writeZip(outputStream: OutputStream, files: Map<String, String>) {
        ZipOutputStream(outputStream).use { zos ->
            for ((fileName, content) in files) {
                zos.putNextEntry(ZipEntry(fileName))
                zos.write(content.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        }
    }

    private fun <T> toCsv(
        items: List<T>,
        headers: List<String>,
        mapper: (T) -> Pair<String, List<String>>
    ): String {
        val sb = StringBuilder()
        sb.appendLine(headers.joinToString(",") { escapeCsv(it) })
        for (item in items) {
            val (_, values) = mapper(item)
            sb.appendLine(values.joinToString(",") { escapeCsv(it) })
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
    }
}
