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
import dagger.hilt.android.lifecycle.HiltViewModel
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import com.tillzo.pos.data.local.dao.BarcodeConfigDao
import com.tillzo.pos.data.local.entity.BarcodeGeneralConfigEntity
import com.tillzo.pos.data.local.entity.BarcodeFieldConfigEntity
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
    private val barcodeConfigDao: BarcodeConfigDao
) : ViewModel() {

    val productUnits: StateFlow<List<ProductUnitEntity>> = productUnitDao
        .getAllUnits()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val barcodeGeneralConfig: StateFlow<BarcodeGeneralConfigEntity?> = barcodeConfigDao.getGeneralConfigFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val barcodeFieldsConfig: StateFlow<List<BarcodeFieldConfigEntity>> = barcodeConfigDao.getActiveFieldsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            initializeBarcodeSettingsIfNeeded()
        }
    }

    private suspend fun initializeBarcodeSettingsIfNeeded() {
        val existingGeneral = barcodeConfigDao.getGeneralConfig()
        if (existingGeneral == null) {
            val sharedPrefs = context.getSharedPreferences("barcode_layout_prefs", android.content.Context.MODE_PRIVATE)
            val hasPrefs = sharedPrefs.contains("company_name") || sharedPrefs.contains("offset_title_x")
            
            val generalConfig = if (hasPrefs) {
                BarcodeGeneralConfigEntity(
                    companyName = sharedPrefs.getString("company_name", "Tillzo POS") ?: "Tillzo POS",
                    companyLogoPath = sharedPrefs.getString("company_logo_path", "") ?: "",
                    usePrefix = sharedPrefs.getBoolean("use_prefix", true),
                    customPrefix = sharedPrefs.getString("custom_prefix", "]d2") ?: "]d2",
                    prefixPosition = sharedPrefs.getString("prefix_position", "0")?.toIntOrNull() ?: 0,
                    customSuffix = sharedPrefs.getString("custom_suffix", "") ?: "",
                    useSuffix = sharedPrefs.getBoolean("use_suffix", false),
                    suffixPosition = sharedPrefs.getString("suffix_position", "0")?.toIntOrNull() ?: 0,
                    useSeparator = sharedPrefs.getBoolean("use_separator", true),
                    titleTextSize = sharedPrefs.getFloat("title_text_size", 6f),
                    isTitleBold = sharedPrefs.getBoolean("is_title_bold", true),
                    barcodeSize = sharedPrefs.getFloat("barcode_size", 48f),
                    currencySymbol = sharedPrefs.getString("currency_symbol", "Rs") ?: "Rs",
                    labelWidth = sharedPrefs.getString("label_width", "144")?.toIntOrNull() ?: 144,
                    labelHeight = sharedPrefs.getString("label_height", "72")?.toIntOrNull() ?: 72,
                    titleX = sharedPrefs.getFloat("offset_title_x", 4f),
                    titleY = sharedPrefs.getFloat("offset_title_y", 16f),
                    priceX = sharedPrefs.getFloat("offset_price_x", 4f),
                    priceY = sharedPrefs.getFloat("offset_price_y", 24f),
                    skuX = sharedPrefs.getFloat("offset_sku_x", 4f),
                    skuY = sharedPrefs.getFloat("offset_sku_y", 32f),
                    gtinX = sharedPrefs.getFloat("offset_gtin_x", 4f),
                    gtinY = sharedPrefs.getFloat("offset_gtin_y", 40f),
                    lotX = sharedPrefs.getFloat("offset_lot_x", 4f),
                    lotY = sharedPrefs.getFloat("offset_lot_y", 48f),
                    expX = sharedPrefs.getFloat("offset_exp_x", 4f),
                    expY = sharedPrefs.getFloat("offset_exp_y", 56f),
                    snX = sharedPrefs.getFloat("offset_sn_x", 4f),
                    snY = sharedPrefs.getFloat("offset_sn_y", 66f),
                    barcodeX = sharedPrefs.getFloat("offset_barcode_x_pos", 92f),
                    barcodeY = sharedPrefs.getFloat("offset_barcode_y", 12f),
                    companyNameSize = sharedPrefs.getFloat("company_name_size", 5f),
                    companyLogoSize = sharedPrefs.getFloat("company_logo_size", 8f),
                    companyNameX = sharedPrefs.getFloat("offset_company_name_x", 16f),
                    companyNameY = sharedPrefs.getFloat("offset_company_name_y", 8f),
                    companyLogoX = sharedPrefs.getFloat("offset_company_logo_x", 4f),
                    companyLogoY = sharedPrefs.getFloat("offset_company_logo_y", 4f),
                    showCompanyName = sharedPrefs.getBoolean("show_company_name", true),
                    showCompanyLogo = sharedPrefs.getBoolean("show_company_logo", true)
                )
            } else {
                BarcodeGeneralConfigEntity()
            }
            barcodeConfigDao.insertGeneralConfig(generalConfig)
        }

        val existingFields = barcodeConfigDao.getAllFields()
        if (existingFields.isEmpty()) {
            val defaultFields = listOf(
                BarcodeFieldConfigEntity(fieldId = "GTIN", fieldName = "GTIN", aiCode = "01", isEnabled = true, sequenceOrder = 0, useFnc1Separator = false),
                BarcodeFieldConfigEntity(fieldId = "EXPIRY", fieldName = "Expiry Date", aiCode = "17", isEnabled = true, sequenceOrder = 1, useFnc1Separator = false),
                BarcodeFieldConfigEntity(fieldId = "BATCH", fieldName = "Batch/Lot Number", aiCode = "10", isEnabled = true, sequenceOrder = 2, useFnc1Separator = true),
                BarcodeFieldConfigEntity(fieldId = "SN", fieldName = "Serial Number", aiCode = "21", isEnabled = true, sequenceOrder = 3, useFnc1Separator = false),
                BarcodeFieldConfigEntity(fieldId = "SKU", fieldName = "SKU Number", aiCode = "240", isEnabled = false, sequenceOrder = 4, useFnc1Separator = false)
            )
            barcodeConfigDao.insertFields(defaultFields)
        }
    }

    fun saveBarcodeGeneralConfig(config: BarcodeGeneralConfigEntity) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val updated = config.copy(
                sync_status = "pending",
                updated_at = System.currentTimeMillis()
            )
            barcodeConfigDao.insertGeneralConfig(updated)
        }
    }

    fun updateBarcodeField(field: BarcodeFieldConfigEntity) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val updated = field.copy(
                sync_status = "pending",
                updated_at = System.currentTimeMillis()
            )
            barcodeConfigDao.updateField(updated)
        }
    }

    fun addCustomBarcodeField(fieldName: String, aiCode: String, useFnc1: Boolean, defaultValue: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val existing = barcodeConfigDao.getAllFields()
            val maxOrder = existing.maxOfOrNull { it.sequenceOrder } ?: -1
            val newField = BarcodeFieldConfigEntity(
                fieldId = java.util.UUID.randomUUID().toString(),
                fieldName = fieldName,
                aiCode = aiCode,
                isEnabled = true,
                sequenceOrder = maxOrder + 1,
                useFnc1Separator = useFnc1,
                customValue = defaultValue,
                sync_status = "pending",
                updated_at = System.currentTimeMillis()
            )
            barcodeConfigDao.insertField(newField)
        }
    }

    fun deleteBarcodeField(field: BarcodeFieldConfigEntity) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            if (field.fieldId in listOf("GTIN", "EXPIRY", "BATCH", "SN", "SKU")) return@launch
            val deleted = field.copy(
                is_deleted = true,
                deleted_at = System.currentTimeMillis(),
                sync_status = "pending",
                updated_at = System.currentTimeMillis()
            )
            barcodeConfigDao.updateField(deleted)
        }
    }

    fun moveFieldUp(field: BarcodeFieldConfigEntity) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val fields = barcodeConfigDao.getActiveFields().toMutableList()
            val index = fields.indexOfFirst { it.fieldId == field.fieldId }
            if (index > 0) {
                val current = fields[index]
                val previous = fields[index - 1]
                
                fields[index] = current.copy(sequenceOrder = previous.sequenceOrder, sync_status = "pending", updated_at = System.currentTimeMillis())
                fields[index - 1] = previous.copy(sequenceOrder = current.sequenceOrder, sync_status = "pending", updated_at = System.currentTimeMillis())
                
                barcodeConfigDao.insertFields(fields)
            }
        }
    }

    fun moveFieldDown(field: BarcodeFieldConfigEntity) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val fields = barcodeConfigDao.getActiveFields().toMutableList()
            val index = fields.indexOfFirst { it.fieldId == field.fieldId }
            if (index >= 0 && index < fields.size - 1) {
                val current = fields[index]
                val next = fields[index + 1]
                
                fields[index] = current.copy(sequenceOrder = next.sequenceOrder, sync_status = "pending", updated_at = System.currentTimeMillis())
                fields[index + 1] = next.copy(sequenceOrder = current.sequenceOrder, sync_status = "pending", updated_at = System.currentTimeMillis())
                
                barcodeConfigDao.insertFields(fields)
            }
        }
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                categoryDao.insertCategory(
                    CategoryEntity(
                        category_name = name,
                        pos_terminal_id = "terminal_1" // Replace later with actual terminal id
                    )
                )
            } catch (e: Exception) {
                Log.e("InventoryCrudVM", "Failed to add category", e)
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
                val terminalId = "terminal_1" // Hardcoded for now, replace with actual fetch mechanism
            
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
        // Intentionally left blank as user prompt specifies only Edit dialog is needed here, and NO logic was given for showAddBatchDialog.
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
        inventoryDao.updateTotalStock(productId, total)
    }
}
