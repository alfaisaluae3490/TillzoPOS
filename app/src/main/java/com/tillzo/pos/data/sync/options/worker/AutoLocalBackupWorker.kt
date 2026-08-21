package com.tillzo.pos.data.sync.options.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tillzo.pos.data.local.AppDatabase
import com.tillzo.pos.data.local.entity.CustomerEntity
import com.tillzo.pos.data.local.entity.ExpenseEntity
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.data.local.entity.KhataEventEntity
import com.tillzo.pos.data.local.entity.SaleEntity
import com.tillzo.pos.data.local.entity.TillSessionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/**
 * Auto Local Backup Worker (FIX 2026-08-06 — Faisal's requirement).
 *
 * Every night at 00:15 (after the cloud backup), writes a FULL snapshot of the
 * local Room database (all rows, synced + pending) to the PUBLIC Documents
 * folder as TillzoPOS_Backup_<date>.zip.
 *
 * Why public Documents: app-private storage is wiped on uninstall — public
 * Documents survives reinstall, so the user's data is NEVER lost even if they
 * uninstall + reinstall the app (they can re-import / re-sync from the zip).
 *
 * Files written:
 *   - tillzo_backup_<yyyyMMdd_HHmmss>.zip  (CSV snapshot of every table)
 *   - latest_tillzo_backup.zip              (rotating "latest" copy)
 */
class AutoLocalBackupWorker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val params: WorkerParameters,
    private val appDatabase: AppDatabase
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "AutoLocalBackupWorker"
        const val WORK_NAME = "auto_local_backup"
    }

    override suspend fun doWork(): Result {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "tillzo_backup_$timestamp.zip"
            val csvFiles = buildSnapshot()

            if (android.os.Build.VERSION.SDK_INT >= 29) {
                // MediaStore (scoped storage) — survives reinstall
                val resolver = context.contentResolver
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                    put(
                        android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                        android.os.Environment.DIRECTORY_DOCUMENTS + "/TillzoPOS"
                    )
                }
                val uri = resolver.insert(
                    android.provider.MediaStore.Files.getContentUri("external"), values
                ) ?: return Result.failure()
                resolver.openOutputStream(uri)?.use { writeZip(it, csvFiles) }
                    ?: return Result.failure()

                // Rotating latest copy
                val latestValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "latest_tillzo_backup.zip")
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                    put(
                        android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                        android.os.Environment.DIRECTORY_DOCUMENTS + "/TillzoPOS"
                    )
                }
                resolver.insert(
                    android.provider.MediaStore.Files.getContentUri("external"), latestValues
                )?.let { resolver.openOutputStream(it)?.use { os -> writeZip(os, csvFiles) } }
            } else {
                // Legacy direct write
                val dir = getBackupDir() ?: return Result.failure()
                val target = File(dir, fileName)
                writeZip(FileOutputStream(target), csvFiles)
                writeZip(FileOutputStream(File(dir, "latest_tillzo_backup.zip")), csvFiles)
                dir.listFiles { f -> f.name.startsWith("tillzo_backup_") && f.name.endsWith(".zip") }
                    ?.sortedByDescending { it.lastModified() }
                    ?.drop(14)
                    ?.forEach { it.delete() }
            }

            Log.i(TAG, "Auto backup complete: $fileName (${csvFiles.size} tables)")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Auto backup failed: ${e.message}", e)
            Result.retry()
        }
    }

    private fun getBackupDir(): File? {
        return try {
            val dir = File(
                android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOCUMENTS
                ),
                "TillzoPOS"
            )
            if (!dir.exists()) dir.mkdirs()
            dir
        } catch (e: Exception) {
            Log.e(TAG, "Cannot access Documents: ${e.message}")
            // Fallback: app-external dir (survives most, not uninstall)
            context.getExternalFilesDir(null)?.let { File(it, "backups") }?.also { it.mkdirs() }
        }
    }

    private suspend fun buildSnapshot(): MutableMap<String, String> {
        val csvFiles = mutableMapOf<String, String>()

        // Inventory
        val inventory: List<InventoryEntity> = appDatabase.inventoryDao().getAllItems().first()
        csvFiles["Inventory.csv"] = toCsv(inventory, listOf(
            "system_row_id", "item_name", "category", "barcode_id", "unit",
            "price_per_unit", "current_stock", "low_stock_threshold", "sku",
            "cost_price", "tax_percent", "is_deleted"
        )) { e -> e.system_row_id to listOf(
            e.system_row_id, e.item_name, e.category, e.barcode_id, e.unit,
            e.price_per_unit.toString(), e.current_stock.toString(), e.low_stock_threshold.toString(),
            e.sku, e.cost_price.toString(), e.tax_percent.toString(),
            if (e.is_deleted) "1" else "0"
        ) }

        // Sales (ALL)
        val sales: List<SaleEntity> = appDatabase.saleDao().getAllSales().first()
        csvFiles["Sales.csv"] = toCsv(sales, listOf(
            "system_row_id", "sync_uuid", "cashier_id", "timestamp", "subtotal", "tax",
            "discount", "total", "payment_method", "cash_amount", "card_amount",
            "wallet_amount", "udhaar_amount", "customer_id", "items_json", "sync_status"
        )) { e -> e.system_row_id to listOf(
            e.system_row_id, e.sync_uuid, e.cashier_id, e.timestamp.toString(),
            e.subtotal.toString(), e.tax.toString(), e.discount.toString(),
            e.total.toString(), e.payment_method, e.cash_amount.toString(),
            e.card_amount.toString(), e.wallet_amount.toString(),
            e.udhaar_amount.toString(), e.customer_id ?: "", e.items_json, e.sync_status
        ) }

        // Customers (ALL)
        val customers: List<CustomerEntity> = appDatabase.customerDao().getAllCustomers().first()
        csvFiles["Customers.csv"] = toCsv(customers, listOf(
            "system_row_id", "name", "phone", "whatsapp", "email", "address",
            "loyalty_points", "lifetime_spend", "is_deleted"
        )) { e -> e.system_row_id to listOf(
            e.system_row_id, e.name, e.phone, e.whatsapp ?: "", e.email ?: "", e.address ?: "",
            e.loyalty_points.toString(), e.lifetime_spend.toString(),
            if (e.is_deleted) "1" else "0"
        ) }

        // Khata events (ALL)
        val khataEvents: List<KhataEventEntity> = appDatabase.khataEventDao().getAllKhataEvents()
        csvFiles["KhataEvents.csv"] = toCsv(khataEvents, listOf(
            "system_row_id", "customer_id", "event_type", "amount", "note", "reference_sale_id"
        )) { e -> e.system_row_id to listOf(
            e.system_row_id, e.customer_id, e.event_type, e.amount.toString(),
            e.note ?: "", e.reference_sale_id ?: ""
        ) }

        // Expenses (ALL)
        val expenses: List<ExpenseEntity> = appDatabase.expenseDao().getAllExpenses().first()
        csvFiles["Expenses.csv"] = toCsv(expenses, listOf(
            "system_row_id", "category", "amount", "description", "timestamp", "logged_by_user_id"
        )) { e -> e.system_row_id to listOf(
            e.system_row_id, e.category, e.amount.toString(), e.description,
            e.timestamp.toString(), e.logged_by_user_id
        ) }

        // Till sessions (ALL)
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

    private fun writeZip(outputStream: java.io.OutputStream, files: Map<String, String>) {
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
