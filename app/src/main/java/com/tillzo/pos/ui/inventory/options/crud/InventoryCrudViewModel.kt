package com.tillzo.pos.ui.inventory.options.crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.domain.repository.AuthRepository
import com.tillzo.pos.domain.usecase.inventory.AddProductUseCase
import com.tillzo.pos.domain.usecase.inventory.UpdateProductUseCase
import com.tillzo.pos.domain.usecase.inventory.DeleteProductUseCase
import com.tillzo.pos.domain.usecase.inventory.GetProductsUseCase
import com.tillzo.pos.domain.usecase.inventory.ProductFilter
import com.tillzo.pos.data.local.dao.CategoryDao
import com.tillzo.pos.data.local.entity.CategoryEntity
import com.tillzo.pos.data.local.dao.ProductUnitDao
import com.tillzo.pos.data.local.entity.ProductUnitEntity
import com.tillzo.pos.data.local.dao.ProductBatchDao
import com.tillzo.pos.data.local.entity.ProductBatchEntity
import com.tillzo.pos.data.local.dao.InventoryDao
import com.tillzo.pos.data.local.dao.CustomerDao
import com.tillzo.pos.data.local.dao.VendorDao
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.local.prefs.BarcodePrefs
import com.tillzo.pos.data.local.prefs.BarcodeGeneralConfig
import com.tillzo.pos.data.local.prefs.BarcodeFieldConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class InventoryCrudViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val authRepository: AuthRepository,
    private val categoryDao: CategoryDao,
    private val productUnitDao: ProductUnitDao,
    private val productBatchDao: ProductBatchDao,
    private val inventoryDao: InventoryDao,
    private val customerDao: CustomerDao,
    private val vendorDao: VendorDao,
    @ApplicationContext private val context: Context,
    private val appSetupPrefs: AppSetupPrefs
) : ViewModel() {

    val productUnits: StateFlow<List<ProductUnitEntity>> = productUnitDao
        .getAllUnits()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val barcodePrefs: BarcodePrefs = BarcodePrefs(context)
    val barcodeGeneralConfig: StateFlow<BarcodeGeneralConfig> = barcodePrefs.generalConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BarcodeGeneralConfig())

    val barcodeFieldsConfig: StateFlow<List<BarcodeFieldConfig>> = barcodePrefs.fieldsConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveBarcodeGeneralConfig(config: BarcodeGeneralConfig) {
        barcodePrefs.saveGeneralConfig(config)
    }

    fun updateBarcodeField(field: BarcodeFieldConfig) {
        barcodePrefs.updateField(field)
    }

    fun addCustomBarcodeField(fieldName: String, aiCode: String, useFnc1: Boolean, defaultValue: String) {
        val fields = barcodePrefs.getFieldsConfig()
        val maxOrder = fields.maxOfOrNull { it.sequenceOrder } ?: -1
        val newField = BarcodeFieldConfig(
            fieldId = java.util.UUID.randomUUID().toString(),
            fieldName = fieldName,
            aiCode = aiCode,
            isEnabled = true,
            sequenceOrder = maxOrder + 1,
            useFnc1Separator = useFnc1,
            customValue = defaultValue
        )
        barcodePrefs.addField(newField)
    }

    fun deleteBarcodeField(field: BarcodeFieldConfig) {
        if (field.fieldId in listOf("GTIN", "EXPIRY", "BATCH", "SN", "SKU")) return
        barcodePrefs.deleteField(field.fieldId)
    }

    fun moveFieldUp(field: BarcodeFieldConfig) {
        barcodePrefs.moveFieldUp(field.fieldId)
    }

    fun moveFieldDown(field: BarcodeFieldConfig) {
        barcodePrefs.moveFieldDown(field.fieldId)
    }

    // CATEGORY MANAGEMENT
    val allCategories: StateFlow<List<CategoryEntity>> = categoryDao.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                val entity = CategoryEntity(
                    category_name = name,
                    // FIX (2026-08-23, DEF-106): "terminal_1" hardcoded tha —
                    // ab real terminal id (spreadsheet-id based), baaki modules
                    // ke pattern se consistent.
                    pos_terminal_id = appSetupPrefs.spreadsheetId.take(20).ifBlank { "terminal_1" }
                )
                categoryDao.insertCategory(entity)
                // FIX (2026-08-07): verify commit + log (silent catch ne bug chipaya tha)
                val committed = categoryDao.getPendingSyncCategories()
                Log.i("InventoryCrudVM", "Category added: ${entity.system_row_id} name=$name pendingCount=${committed.size}")
                if (committed.none { it.system_row_id == entity.system_row_id }) {
                    Log.w("InventoryCrudVM", "Category NOT committed to DB! name=$name")
                }
            } catch (e: Exception) {
                Log.e("InventoryCrudVM", "Failed to add category: $name", e)
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                categoryDao.deleteCategory(categoryId)
            } catch (e: Exception) {
                Log.e("InventoryCrudVM", "Failed to delete category", e)
            }
        }
    }

    private val _currentFilter = MutableStateFlow(ProductFilter.ALL)
    val currentFilter = _currentFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val allItems: StateFlow<List<InventoryEntity>> = combine(_currentFilter, _searchQuery) { filter, query ->
        Pair(filter, query)
    }.flatMapLatest { (filter, query) ->
        getProductsUseCase(filter, query)
    }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedItem = MutableStateFlow<InventoryEntity?>(null)
    val selectedItem = _selectedItem.asStateFlow()

    fun selectItem(item: InventoryEntity?) {
        _selectedItem.value = item
    }

    fun setFilter(filter: ProductFilter) { _currentFilter.value = filter }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    /**
     * Reusable logic for both Add and Edit
     * Handles M6.4: Smart Barcode Auto-Assign, GS1 GTINs
     */
    fun saveItem(
        itemName: String,
        category: String,
        gtins: List<String>,
        unit: String,
        pricePerUnit: Double,
        currentStock: Double,
        lowStockThreshold: Double,
        sku: String,
        description: String,
        costPrice: Double,
        taxPercent: Double,
        batchNumber: String,
        expiryDate: String,
        manufacturingDate: String,
        expiryAlertDays: Int,
        isDamaged: Boolean,
        damagedQty: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // FIX (2026-08-23, DEF-103): input validation — blank name /
                // negative price/stock/tax pehle silently save ho jate the
                // (sheet par corrupt rows). Ab reject/clamp.
                if (itemName.isBlank()) {
                    Log.w("InventoryCrudVM", "saveItem rejected: blank item name")
                    return@launch
                }
                val safePrice = pricePerUnit.coerceAtLeast(0.0)
                val safeStock = currentStock.coerceAtLeast(0.0)
                val safeLow = lowStockThreshold.coerceAtLeast(0.0)
                val safeCost = costPrice.coerceAtLeast(0.0)
                val safeTax = taxPercent.coerceAtLeast(0.0)
                val safeDamagedQty = damagedQty.coerceAtLeast(0.0)
                val terminalId = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" } // FIX 2026-08-06: real terminal idwith actual fetch mechanism
            
            val existingItem = _selectedItem.value
            // DEF-67 FIX (2026-08-26): max-of(max, count) — legacy rows carry
            // item_number=0 (migration default), so MAX alone returned 0 and every
            // new product got itemNum=1 → same auto EAN-13 (Panadol collision).
            val itemNum = existingItem?.item_number
                ?: (maxOf(inventoryDao.getMaxItemNumber() ?: 0, inventoryDao.getItemCount()) + 1)
            
            // GS1 default GTIN auto-generation if list is empty
            // FIX (2026-08-22, DEF-64): was "0000000%07d" = 14 digits. EAN-13
            // (the universal retail barcode, what ML Kit and store scanners
            // read) supports EXACTLY 13 digits — 14-digit GTINs decode as
            // ITF-14 (not EAN-13), so products with auto-generated barcodes
            // were unscannable at retail POS. Now: 12-digit GTIN-12 base +
            // EAN-13 check digit = 13 digits, guaranteed scannable.
            // FIX (2026-08-22, DEF-66): itemNum % 100 would COLLIDE after 100
            // products (two products, same barcode → scanner adds the wrong
            // item). itemNum itself is unique (max+1) — use all 12 base digits
            // so collisions are impossible in practice.
            val finalGtins = if (gtins.isEmpty()) {
                listOf(generateEan13("000%09d".format(itemNum % 1_000_000_000)))
            } else {
                gtins
            }
            // Use the first GTIN as the legacy barcode_id
            val primaryBarcode = finalGtins.first()

            if (existingItem != null) {
                // Edit
                val updatedItem = existingItem.copy(
                    item_name = itemName,
                    category = category,
                    barcode_id = primaryBarcode,
                    unit = unit,
                    price_per_unit = safePrice,
                    current_stock = safeStock,
                    low_stock_threshold = safeLow,
                    sku = sku,
                    description = description,
                    cost_price = safeCost,
                    tax_percent = safeTax,
                    batch_number = batchNumber,
                    expiry_date = expiryDate,
                    manufacturing_date = manufacturingDate,
                    expiry_alert_days = expiryAlertDays,
                    is_damaged_stock = isDamaged,
                    damaged_qty = safeDamagedQty
                )
                updateProductUseCase(updatedItem)
                
                // Update GTINs
                inventoryDao.deleteGtinsForItem(updatedItem.system_row_id)
                val gtinEntities = finalGtins.map {
                    com.tillzo.pos.data.local.entity.ItemGtinEntity(
                        item_id = updatedItem.system_row_id,
                        gtin = it
                    )
                }
                inventoryDao.insertGtins(gtinEntities)
            } else {
                // Add
                val newItem = InventoryEntity(
                    pos_terminal_id = terminalId,
                    item_name = itemName,
                    item_number = itemNum,
                    category = category,
                    barcode_id = primaryBarcode,
                    unit = unit,
                    price_per_unit = safePrice,
                    current_stock = safeStock,
                    low_stock_threshold = safeLow,
                    sku = sku,
                    description = description,
                    cost_price = safeCost,
                    tax_percent = safeTax,
                    batch_number = batchNumber,
                    expiry_date = expiryDate,
                    manufacturing_date = manufacturingDate,
                    expiry_alert_days = expiryAlertDays,
                    is_damaged_stock = isDamaged,
                    damaged_qty = safeDamagedQty
                )
                addProductUseCase(newItem)
                
                val gtinEntities = finalGtins.map {
                    com.tillzo.pos.data.local.entity.ItemGtinEntity(
                        item_id = newItem.system_row_id,
                        gtin = it
                    )
                }
                inventoryDao.insertGtins(gtinEntities)
            }
            // Reset selection after save
            _selectedItem.value = null
        } catch (e: Exception) {
            Log.e("InventoryCrudVM", "Failed to save item", e)
        }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                deleteProductUseCase(id)
            } catch (e: Exception) {
                Log.e("InventoryCrudVM", "Failed to delete item", e)
            }
        }
    }
    
    // Auto-generate CUST-10000X style alphanumeric ID (M6.4)
    private fun generateCustomBarcode(): String {
        val randomSuffix = (100000..999999).random()
        return "CUST-$randomSuffix"
    }

    fun getBatchesForProduct(productId: String): kotlinx.coroutines.flow.Flow<List<ProductBatchEntity>> =
        productBatchDao.getBatchesForProduct(productId)

    fun getGtinsForItem(itemId: String): kotlinx.coroutines.flow.Flow<List<com.tillzo.pos.data.local.entity.ItemGtinEntity>> =
        inventoryDao.getGtinsForItemFlow(itemId)

    suspend fun getItemById(itemId: String): InventoryEntity? =
        inventoryDao.getItemById(itemId)

    fun showAddBatchDialog(productId: String) {
        // FIX (2026-08-06): was an empty stub — "Add New Batch" button existed in
        // the batch sheet but did nothing. Now opens the add-batch dialog.
        _addBatchProductId.value = productId
    }

    private val _addBatchProductId = MutableStateFlow<String?>(null)
    val addBatchProductId: StateFlow<String?> = _addBatchProductId

    fun dismissAddBatchDialog() {
        _addBatchProductId.value = null
    }

    fun addBatch(
        productId: String,
        batchNumber: String,
        mfgDate: String,
        expiryDate: String,
        stockQty: Double,
        costPrice: Double,
        sellingPrice: Double,
        onDone: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // FIX (2026-08-23, DEF-104): negative batch qty pehle accept hoti
                // thi → total stock ghat jata tha + sheet par negative batch row.
                if (stockQty < 0.0) {
                    android.util.Log.w("InventoryCrud", "addBatch rejected: negative stockQty ($stockQty)")
                    return@launch
                }
                val product = inventoryDao.getItemById(productId) ?: return@launch
                val batch = ProductBatchEntity(
                    productId = productId,
                    barcodeId = product.barcode_id,
                    batchNumber = batchNumber.ifBlank { "BATCH-${System.currentTimeMillis() % 100000}" },
                    manufacturingDate = mfgDate,
                    expiryDate = expiryDate,
                    stockQty = stockQty,
                    costPrice = costPrice,
                    sellingPrice = sellingPrice,
                    isActive = true,
                    isDeleted = false,
                    syncStatus = "pending",
                    posTerminalId = product.pos_terminal_id
                )
                productBatchDao.insertBatch(batch)
                // Recalculate total stock from all active batches + mark pending
                inventoryDao.updateTotalStockAndSyncStatus(
                    productId,
                    product.totalStock + stockQty,
                    System.currentTimeMillis()
                )
                withContext(Dispatchers.Main) {
                    onDone()
                }
            } catch (e: Exception) {
                android.util.Log.e("InventoryCrud", "addBatch failed: ${e.message}", e)
            }
        }
    }

    fun updateBatch(
        batch: ProductBatchEntity,
        batchNumber: String,
        mfgDate: String,
        expiryDate: String,
        stockQty: Double,
        sellingPrice: Double
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            // FIX (2026-08-23, DEF-104): negative batch qty reject.
            if (stockQty < 0.0) {
                android.util.Log.w("InventoryCrud", "updateBatch rejected: negative stockQty ($stockQty)")
                return@launch
            }
            val updated = batch.copy(
                batchNumber = batchNumber,
                manufacturingDate = mfgDate,
                expiryDate = expiryDate,
                stockQty = stockQty,
                sellingPrice = sellingPrice,
                syncStatus = "pending",
                updatedAt = System.currentTimeMillis()
            )
            productBatchDao.insertBatch(updated) // REPLACE strategy
            recalculateTotalStock(batch.productId)
        }
    }

    private suspend fun recalculateTotalStock(productId: String) {
        val batches = productBatchDao.getAllBatchesForProduct(productId)
        val total = batches.filter { it.isActive && !it.isDeleted }.sumOf { it.stockQty }
        inventoryDao.updateTotalStockAndSyncStatus(productId, total, System.currentTimeMillis())
    }

    // FIX (2026-08-22, DEF-64): EAN-13 generator — takes a 12-digit GTIN-12
    // base and appends the standard EAN check digit (weights 3-1-3-1... from
    // the right). Result is always 13 digits → scannable by ML Kit and retail
    // scanners. Also validates if a caller already passed a full 13-digit code.
    companion object {
        fun generateEan13(base12: String): String {
            val clean = base12.filter { it.isDigit() }.take(12).padStart(12, '0')
            var sum = 0
            for (i in clean.indices) {
                val digit = clean[i] - '0'
                sum += if (i % 2 == 0) digit else digit * 3
            }
            val check = (10 - (sum % 10)) % 10
            return clean + check
        }
    }

    // ── OVERNIGHT-AUDIT Phase 2b (2026-08-23): CSV bulk import ──────────────────
    // Parses a UTF-8 CSV (Excel/Sheets export) and inserts every row through the
    // same validation + EAN-13 path as saveItem(). Result surfaces in UI state.

    private val _importResult = MutableStateFlow<String?>(null)
    val importResult = _importResult.asStateFlow()

    fun clearImportResult() { _importResult.value = null }

    fun importInventoryCsv(bytes: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rows = withContext(Dispatchers.IO) {
                    java.io.ByteArrayInputStream(bytes).use {
                        com.tillzo.pos.utils.CsvImporter.parseInventoryRows(it)
                    }
                }
                if (rows.isEmpty()) {
                    _importResult.value = "No data rows found (header only or empty file)."
                    return@launch
                }
                var inserted = 0
                var skipped = 0
                // DEF-67 FIX (2026-08-26): count-guard — legacy item_number=0 rows
                // made MAX() return 0 → CSV import collided barcodes with Panadol.
                var nextNum = maxOf(inventoryDao.getMaxItemNumber() ?: 0, inventoryDao.getItemCount())
                for ((index, r) in rows.withIndex()) {
                    try {
                        if (r.name.isBlank()) { skipped++; continue }
                        nextNum += 1
                        // Barcode: use provided numeric one, else generate unique EAN-13
                        val barcode = r.barcode.takeIf { it.isNotBlank() && it.all { ch -> ch.isDigit() } }
                            ?: generateEan13("000%09d".format(nextNum % 1_000_000_000))
                        val entity = com.tillzo.pos.data.local.entity.InventoryEntity(
                            pos_terminal_id = "TERM_1",
                            item_name = r.name,
                            item_number = nextNum,
                            category = r.category,
                            barcode_id = barcode,
                            unit = r.unit,
                            price_per_unit = r.sellingPrice,
                            current_stock = r.stockQty,
                            low_stock_threshold = 5.0,
                            sku = r.sku,
                            description = "",
                            cost_price = r.costPrice,
                            tax_percent = 0.0,
                            batch_number = "",
                            expiry_date = "",
                            manufacturing_date = "",
                            expiry_alert_days = 30,
                            is_damaged_stock = false,
                            damaged_qty = 0.0
                        )
                        addProductUseCase(entity)
                        inventoryDao.insertGtins(
                            listOf(
                                com.tillzo.pos.data.local.entity.ItemGtinEntity(
                                    item_id = entity.system_row_id,
                                    gtin = barcode
                                )
                            )
                        )
                        inserted++
                    } catch (rowEx: Exception) {
                        skipped++   // bad row: keep going, report at end
                    }
                }
                _importResult.value = "Import complete: $inserted added, $skipped skipped."
            } catch (e: Exception) {
                _importResult.value = "Import failed: ${e.message}"
            }
        }
    }

    /** OVERNIGHT-AUDIT Phase 1/2 — Bulk import: Customers master (module 7 CRM). */
    fun importCustomersCsv(bytes: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rows = withContext(Dispatchers.IO) {
                    java.io.ByteArrayInputStream(bytes).use {
                        com.tillzo.pos.utils.CsvImporter.parseCustomerRows(it)
                    }
                }
                if (rows.isEmpty()) {
                    _importResult.value = "No data rows found (header only or empty file)."
                    return@launch
                }
                var inserted = 0
                var skipped = 0
                for (r in rows) {
                    try {
                        if (r.name.isBlank()) { skipped++; continue }
                        customerDao.insert(
                            com.tillzo.pos.data.local.entity.CustomerEntity(
                                name = r.name,
                                phone = r.phone,
                                whatsapp = r.whatsapp ?: r.phone,
                                email = r.email ?: "",
                                address = r.address ?: "",
                                pos_terminal_id = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }
                            )
                        )
                        inserted++
                    } catch (rowEx: Exception) {
                        skipped++
                    }
                }
                _importResult.value = "Customers import complete: $inserted added, $skipped skipped."
            } catch (e: Exception) {
                _importResult.value = "Import failed: ${e.message}"
            }
        }
    }

    /** OVERNIGHT-AUDIT Phase 1/2 — Bulk import: Vendors master (module 4 PO). */
    fun importVendorsCsv(bytes: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rows = withContext(Dispatchers.IO) {
                    java.io.ByteArrayInputStream(bytes).use {
                        com.tillzo.pos.utils.CsvImporter.parseVendorRows(it)
                    }
                }
                if (rows.isEmpty()) {
                    _importResult.value = "No data rows found (header only or empty file)."
                    return@launch
                }
                var inserted = 0
                var skipped = 0
                for (r in rows) {
                    try {
                        if (r.name.isBlank()) { skipped++; continue }
                        vendorDao.insertVendor(
                            com.tillzo.pos.data.local.entity.VendorEntity(
                                vendorId = java.util.UUID.randomUUID().toString(),
                                name = r.name,
                                phone = r.phone,
                                whatsapp = r.phone,
                                email = r.email,
                                address = r.address,
                                city = r.city,
                                creditLimit = r.creditLimit
                            )
                        )
                        inserted++
                    } catch (rowEx: Exception) {
                        skipped++
                    }
                }
                _importResult.value = "Vendors import complete: $inserted added, $skipped skipped."
            } catch (e: Exception) {
                _importResult.value = "Import failed: ${e.message}"
            }
        }
    }

    /** OVERNIGHT-AUDIT Phase 1/2 — Bulk import: Product batches (module 6). */
    fun importBatchesCsv(bytes: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rows = withContext(Dispatchers.IO) {
                    java.io.ByteArrayInputStream(bytes).use {
                        com.tillzo.pos.utils.CsvImporter.parseBatchRows(it)
                    }
                }
                if (rows.isEmpty()) {
                    _importResult.value = "No data rows found (header only or empty file)."
                    return@launch
                }
                var inserted = 0
                var skipped = 0
                for (r in rows) {
                    try {
                        if (r.productId.isBlank()) { skipped++; continue }
                        productBatchDao.insertBatch(
                            com.tillzo.pos.data.local.entity.ProductBatchEntity(
                                productId = r.productId,
                                barcodeId = r.barcodeId,
                                batchNumber = r.batchNumber,
                                manufacturingDate = r.manufacturingDate,
                                expiryDate = r.expiryDate,
                                stockQty = r.stockQty,
                                costPrice = r.costPrice,
                                sellingPrice = r.sellingPrice,
                                posTerminalId = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }
                            )
                        )
                        inserted++
                    } catch (rowEx: Exception) {
                        skipped++
                    }
                }
                _importResult.value = "Batches import complete: $inserted added, $skipped skipped."
            } catch (e: Exception) {
                _importResult.value = "Import failed: ${e.message}"
            }
        }
    }
}
