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
import com.tillzo.pos.data.local.entity.CategoryEntity
import com.tillzo.pos.data.local.entity.ProductBatchEntity
import com.tillzo.pos.data.local.entity.StockAdjustmentEntity
import com.tillzo.pos.data.local.entity.WastageEntity
import com.tillzo.pos.data.local.entity.VendorEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderItemEntity
import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.data.local.entity.GrnItemEntity
import com.tillzo.pos.data.local.entity.ProductUnitEntity
import com.tillzo.pos.data.local.entity.ItemGtinEntity
import com.tillzo.pos.data.local.entity.ReturnsEntity
import com.tillzo.pos.data.local.entity.UserEntity
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
            // PLAY POLICY (2026-08-24, T5): Legacy (API < 29) direct write now
            // targets app-scoped external dir — no WRITE_EXTERNAL_STORAGE needed.
            // Public Documents write on old APIs required the deprecated
            // permission; scoped dir is always writable and policy-compliant.
            val dir = java.io.File(
                context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)
                    ?: android.os.Environment.getExternalStoragePublicDirectory(
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

        // ── DEF-115 (2026-08-23): backup completeness — 14 missing tables ──
        // Pehle sirf Inventory/Sales/Customers/Khata/Expenses/TillSessions export
        // hote the; uninstall ke baad manual restore mein baaki saara data (batches,
        // adjustments, wastage, time clock, vendors, PO/GRN, units, GTINs, returns,
        // users) kho jata tha. Ab har syncable table ka full CSV ZIP mein hai.

        onProgress("Exporting Categories...")
        val categories: List<CategoryEntity> = appDatabase.categoryDao().getAllCategories().first()
        csvFiles["Categories.csv"] = toCsv(categories, listOf(
            "system_row_id", "category_name", "parent_category_id", "is_deleted",
            "deleted_at", "sync_status", "pos_terminal_id", "created_at", "updated_at"
        )) { e -> e.system_row_id to listOf(
            e.system_row_id, e.category_name, e.parent_category_id ?: "", e.is_deleted.toString(),
            e.deleted_at?.toString() ?: "", e.sync_status, e.pos_terminal_id,
            e.created_at.toString(), e.updated_at.toString()
        ) }

        onProgress("Exporting Product Batches...")
        val batches: List<ProductBatchEntity> = appDatabase.productBatchDao().getAllBatchesForBackup()
        csvFiles["ProductBatches.csv"] = toCsv(batches, listOf(
            "batchId", "productId", "barcodeId", "batchNumber", "manufacturingDate",
            "expiryDate", "stockQty", "costPrice", "sellingPrice", "isActive",
            "isDeleted", "deletedAt", "syncStatus", "posTerminalId", "createdAt", "updatedAt"
        )) { e -> e.batchId to listOf(
            e.batchId, e.productId, e.barcodeId, e.batchNumber, e.manufacturingDate,
            e.expiryDate, e.stockQty.toString(), e.costPrice.toString(), e.sellingPrice.toString(),
            e.isActive.toString(), e.isDeleted.toString(), e.deletedAt?.toString() ?: "",
            e.syncStatus, e.posTerminalId, e.createdAt.toString(), e.updatedAt.toString()
        ) }

        onProgress("Exporting Stock Adjustments...")
        val adjustments: List<StockAdjustmentEntity> = appDatabase.stockAdjustmentDao().getAllAdjustmentsForBackup()
        csvFiles["StockAdjustments.csv"] = toCsv(adjustments, listOf(
            "adjustmentId", "productId", "adjustmentType", "quantityChanged", "reason",
            "adjustedBy", "syncStatus", "createdAt", "updatedAt", "posTerminalId"
        )) { e -> e.adjustmentId to listOf(
            e.adjustmentId, e.productId, e.adjustmentType, e.quantityChanged.toString(),
            e.reason, e.adjustedBy, e.syncStatus, e.createdAt.toString(),
            e.updatedAt.toString(), e.posTerminalId
        ) }

        onProgress("Exporting Wastage...")
        val wastage: List<WastageEntity> = appDatabase.wastageDao().getAllWastageForBackup()
        csvFiles["Wastage.csv"] = toCsv(wastage, listOf(
            "wastageId", "productId", "productName", "batchId", "batchNumber", "quantity",
            "unit", "costPrice", "totalLoss", "reason", "notes", "loggedBy",
            "wastageDate", "syncStatus", "posTerminalId", "createdAt", "updatedAt"
        )) { e -> e.wastageId to listOf(
            e.wastageId, e.productId, e.productName, e.batchId, e.batchNumber,
            e.quantity.toString(), e.unit, e.costPrice.toString(), e.totalLoss.toString(),
            e.reason, e.notes, e.loggedBy, e.wastageDate, e.syncStatus,
            e.posTerminalId, e.createdAt.toString(), e.updatedAt.toString()
        ) }

        onProgress("Exporting Vendors...")
        val vendors: List<VendorEntity> = appDatabase.vendorDao().getAllVendorsAsList()
        csvFiles["Vendors.csv"] = toCsv(vendors, listOf(
            "vendorId", "name", "phone", "whatsapp", "email", "address", "city",
            "creditLimit", "isActive", "isDeleted", "syncStatus", "createdAt", "updatedAt"
        )) { e -> e.vendorId to listOf(
            e.vendorId, e.name, e.phone, e.whatsapp, e.email, e.address, e.city,
            e.creditLimit.toString(), e.isActive.toString(), e.isDeleted.toString(),
            e.syncStatus, e.createdAt.toString(), e.updatedAt.toString()
        ) }

        onProgress("Exporting Purchase Orders...")
        val pos: List<PurchaseOrderEntity> = appDatabase.purchaseOrderDao().getAllPOs().first()
        csvFiles["PurchaseOrders.csv"] = toCsv(pos, listOf(
            "poId", "poNumber", "vendorId", "vendorName", "status", "notes", "totalAmount",
            "currency", "expectedDeliveryDate", "createdBy", "syncStatus", "isDeleted",
            "deletedAt", "posTerminalId", "createdAt", "updatedAt"
        )) { e -> e.poId to listOf(
            e.poId, e.poNumber, e.vendorId, e.vendorName, e.status, e.notes,
            e.totalAmount.toString(), e.currency, e.expectedDeliveryDate, e.createdBy,
            e.syncStatus, e.isDeleted.toString(), e.deletedAt?.toString() ?: "",
            e.posTerminalId, e.createdAt.toString(), e.updatedAt.toString()
        ) }

        onProgress("Exporting PO Items...")
        val poItems: List<PurchaseOrderItemEntity> = appDatabase.purchaseOrderDao().getAllPOItemsForBackup()
        csvFiles["PurchaseOrderItems.csv"] = toCsv(poItems, listOf(
            "poItemId", "poId", "productId", "productName", "sku", "barcodeId",
            "orderedQty", "receivedQty", "unitCostPrice", "totalCost", "unit",
            "syncStatus", "createdAt", "updatedAt"
        )) { e -> e.poItemId to listOf(
            e.poItemId, e.poId, e.productId, e.productName, e.sku, e.barcodeId,
            e.orderedQty.toString(), e.receivedQty.toString(), e.unitCostPrice.toString(),
            e.totalCost.toString(), e.unit, e.syncStatus, e.createdAt.toString(),
            e.updatedAt.toString()
        ) }

        onProgress("Exporting GRNs...")
        val grns: List<GrnHeaderEntity> = appDatabase.grnDao().getAllGrns().first()
        csvFiles["GrnHeaders.csv"] = toCsv(grns, listOf(
            "grnId", "grnNumber", "poId", "poNumber", "vendorId", "vendorName",
            "vendorPhone", "status", "notes", "receivedBy", "receivedByName",
            "totalItems", "totalReceivedQty", "totalAmount", "syncStatus", "isDeleted",
            "deletedAt", "posTerminalId", "attachedFileId", "attachedFileUrl",
            "createdAt", "updatedAt"
        )) { e -> e.grnId to listOf(
            e.grnId, e.grnNumber, e.poId, e.poNumber, e.vendorId, e.vendorName,
            e.vendorPhone, e.status, e.notes, e.receivedBy, e.receivedByName,
            e.totalItems.toString(), e.totalReceivedQty.toString(), e.totalAmount.toString(),
            e.syncStatus, e.isDeleted.toString(), e.deletedAt?.toString() ?: "",
            e.posTerminalId, e.attachedFileId, e.attachedFileUrl,
            e.createdAt.toString(), e.updatedAt.toString()
        ) }

        onProgress("Exporting GRN Items...")
        val grnItems: List<GrnItemEntity> = appDatabase.grnDao().getAllGrnItemsForBackup()
        csvFiles["GrnItems.csv"] = toCsv(grnItems, listOf(
            "grnItemId", "grnId", "poItemId", "productId", "batchId", "productName",
            "barcodeId", "sku", "categoryId", "brand", "orderedQty", "receivedQty",
            "unitCostPrice", "sellingPrice", "totalCost", "unit", "batchNumber",
            "manufacturingDate", "expiryDate", "inventoryAction", "isNewProduct",
            "lowStockThreshold", "syncStatus", "createdAt", "updatedAt"
        )) { e -> e.grnItemId to listOf(
            e.grnItemId, e.grnId, e.poItemId, e.productId, e.batchId, e.productName,
            e.barcodeId, e.sku, e.categoryId, e.brand, e.orderedQty.toString(),
            e.receivedQty.toString(), e.unitCostPrice.toString(), e.sellingPrice.toString(),
            e.totalCost.toString(), e.unit, e.batchNumber, e.manufacturingDate,
            e.expiryDate, e.inventoryAction, e.isNewProduct.toString(),
            e.lowStockThreshold.toString(), e.syncStatus, e.createdAt.toString(),
            e.updatedAt.toString()
        ) }

        onProgress("Exporting Product Units...")
        val units: List<ProductUnitEntity> = appDatabase.productUnitDao().getAllUnits().first()
        csvFiles["ProductUnits.csv"] = toCsv(units, listOf(
            "unitId", "unitName", "abbreviation", "isDeleted", "syncStatus",
            "createdAt", "updatedAt"
        )) { e -> e.unitId to listOf(
            e.unitId, e.unitName, e.abbreviation, e.isDeleted.toString(),
            e.syncStatus, e.createdAt.toString(), e.updatedAt.toString()
        ) }

        onProgress("Exporting Item GTINs...")
        val gtins: List<ItemGtinEntity> = appDatabase.inventoryDao().getAllGtins()
        csvFiles["ItemGtins.csv"] = toCsv(gtins, listOf(
            "gtin_id", "item_id", "gtin"
        )) { e -> e.gtin_id to listOf(e.gtin_id, e.item_id, e.gtin) }

        onProgress("Exporting Returns...")
        val returns: List<ReturnsEntity> = appDatabase.returnsDao().getAllReturns().first()
        csvFiles["Returns.csv"] = toCsv(returns, listOf(
            "returnId", "systemRowId", "originalInvoiceId", "itemId", "qtyReturned",
            "condition", "refundMethod", "amount", "lastUpdated", "syncStatus",
            "posTerminalId", "createdAt"
        )) { e -> e.returnId to listOf(
            e.returnId, e.systemRowId, e.originalInvoiceId, e.itemId,
            e.qtyReturned.toString(), e.condition, e.refundMethod, e.amount.toString(),
            e.lastUpdated.toString(), e.syncStatus, e.posTerminalId,
            e.createdAt.toString()
        ) }

        onProgress("Exporting Users...")
        // SECURITY (DEF-115): password_hash kabhi backup mein nahi jata —
        // sheet sync ke same rule (UserEntity.toSyncMap blank placeholder).
        val users: List<UserEntity> = appDatabase.userDao().getAllUsers()
        csvFiles["Users.csv"] = toCsv(users, listOf(
            "system_row_id", "email", "name", "role", "permissions_json",
            "is_deleted", "deleted_at", "sync_status", "pos_terminal_id",
            "created_at", "updated_at"
        )) { e -> e.system_row_id to listOf(
            e.system_row_id, e.email, e.name, e.role, e.permissions_json ?: "",
            e.is_deleted.toString(), e.deleted_at?.toString() ?: "", e.sync_status,
            e.pos_terminal_id, e.created_at.toString(), e.updated_at.toString()
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
