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
                    pos_terminal_id = "terminal_1" // Replace later with actual terminal id
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
                val terminalId = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" } // FIX 2026-08-06: real terminal idwith actual fetch mechanism
            
            val existingItem = _selectedItem.value
            val itemNum = existingItem?.item_number ?: ((inventoryDao.getMaxItemNumber() ?: 0) + 1)
            
            // GS1 default GTIN auto-generation if list is empty
            val finalGtins = if (gtins.isEmpty()) {
                listOf("0000000%07d".format(itemNum))
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
                    price_per_unit = pricePerUnit,
                    current_stock = currentStock,
                    low_stock_threshold = lowStockThreshold,
                    sku = sku,
                    description = description,
                    cost_price = costPrice,
                    tax_percent = taxPercent,
                    batch_number = batchNumber,
                    expiry_date = expiryDate,
                    manufacturing_date = manufacturingDate,
                    expiry_alert_days = expiryAlertDays,
                    is_damaged_stock = isDamaged,
                    damaged_qty = damagedQty
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
                    price_per_unit = pricePerUnit,
                    current_stock = currentStock,
                    low_stock_threshold = lowStockThreshold,
                    sku = sku,
                    description = description,
                    cost_price = costPrice,
                    tax_percent = taxPercent,
                    batch_number = batchNumber,
                    expiry_date = expiryDate,
                    manufacturing_date = manufacturingDate,
                    expiry_alert_days = expiryAlertDays,
                    is_damaged_stock = isDamaged,
                    damaged_qty = damagedQty
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
}
