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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
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
    private val inventoryDao: InventoryDao
) : ViewModel() {

    val productUnits: StateFlow<List<ProductUnitEntity>> = productUnitDao
        .getAllUnits()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
            categoryDao.insertCategory(
                CategoryEntity(
                    category_name = name,
                    pos_terminal_id = "terminal_1" // Replace later with actual terminal id
                )
            )
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            categoryDao.deleteCategory(categoryId)
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
     * Handles M6.4: Smart Barcode Auto-Assign (Generates CUST-XXXXX if blank)
     */
    fun saveItem(
        itemName: String,
        category: String,
        barcodeInput: String,
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
        viewModelScope.launch {
            val terminalId = "terminal_1" // Hardcoded for now, replace with actual fetch mechanism
            
            // M6.4 Smart Barcode Assignment
            val finalBarcode = if (barcodeInput.isBlank()) {
                generateCustomBarcode()
            } else {
                barcodeInput
            }

            val existingItem = _selectedItem.value
            if (existingItem != null) {
                // Edit
                val updatedItem = existingItem.copy(
                    item_name = itemName,
                    category = category,
                    barcode_id = finalBarcode,
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
            } else {
                // Add
                val newItem = InventoryEntity(
                    pos_terminal_id = terminalId,
                    item_name = itemName,
                    category = category,
                    barcode_id = finalBarcode,
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
            }
            // Reset selection after save
            _selectedItem.value = null
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            deleteProductUseCase(id)
        }
    }
    
    // Auto-generate CUST-10000X style alphanumeric ID (M6.4)
    private fun generateCustomBarcode(): String {
        val randomSuffix = (100000..999999).random()
        return "CUST-$randomSuffix"
    }

    fun getBatchesForProduct(productId: String): kotlinx.coroutines.flow.Flow<List<ProductBatchEntity>> =
        productBatchDao.getBatchesForProduct(productId)

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
