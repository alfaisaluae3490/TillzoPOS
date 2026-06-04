package com.tillzo.pos.ui.inventory.module_c.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.PurchaseOrderDao
import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.data.local.entity.GrnItemEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderEntity
import com.tillzo.pos.domain.repository.ConfirmGrnResult
import com.tillzo.pos.domain.usecase.grn.ConfirmGrnUseCase
import com.tillzo.pos.domain.usecase.grn.SaveGrnDraftUseCase
import com.tillzo.pos.domain.repository.GrnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateGrnViewModel @Inject constructor(
    private val poDao: PurchaseOrderDao,
    private val grnRepository: GrnRepository,
    private val saveGrnDraftUseCase: SaveGrnDraftUseCase,
    private val confirmGrnUseCase: ConfirmGrnUseCase
) : ViewModel() {

    private val _selectedPO = MutableStateFlow<PurchaseOrderEntity?>(null)
    val selectedPO: StateFlow<PurchaseOrderEntity?> = _selectedPO.asStateFlow()

    private val _items = MutableStateFlow<List<GrnItemEntity>>(emptyList())
    val items: StateFlow<List<GrnItemEntity>> = _items.asStateFlow()

    private val _confirmResult = MutableStateFlow<ConfirmGrnResult?>(null)
    val confirmResult: StateFlow<ConfirmGrnResult?> = _confirmResult.asStateFlow()

    // Exposed so CreateGrnScreen can pass the real grnId to the success route
    private val _confirmedGrnId = MutableStateFlow<String?>(null)
    val confirmedGrnId: StateFlow<String?> = _confirmedGrnId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadPO(poId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val po = poDao.getPOById(poId)
            _selectedPO.value = po
            
            if (po != null) {
                val poItemsList = poDao.getPOItems(po.poId)
                _items.value = poItemsList.map { poItem ->
                    val remainingQty = (poItem.orderedQty - poItem.receivedQty).coerceAtLeast(0.0)
                    GrnItemEntity(
                        grnItemId = UUID.randomUUID().toString(),
                        grnId = "", // assigned on save
                        poItemId = poItem.poItemId,
                        productId = poItem.productId,
                        productName = poItem.productName,
                        barcodeId = poItem.barcodeId,
                        sku = poItem.sku,
                        orderedQty = poItem.orderedQty,
                        receivedQty = remainingQty,
                        unitCostPrice = poItem.unitCostPrice,
                        sellingPrice = 0.0, // Should be fetched from product if possible, defaults to 0
                        totalCost = remainingQty * poItem.unitCostPrice,
                        unit = poItem.unit,
                        batchNumber = "",
                        manufacturingDate = "",
                        expiryDate = "",
                        inventoryAction = "PENDING", // User should select this in UI or logic decides
                        isNewProduct = false // Assume false unless known
                    )
                }
            }
            _isLoading.value = false
        }
    }

    fun updateItemReceivedQty(itemId: String, qty: Double) {
        _items.value = _items.value.map {
            if (it.grnItemId == itemId) {
                it.copy(receivedQty = qty, totalCost = qty * it.unitCostPrice)
            } else it
        }
    }
    
    fun updateItemBatchInfo(itemId: String, batch: String, mfg: String, exp: String) {
        _items.value = _items.value.map {
            if (it.grnItemId == itemId) {
                it.copy(batchNumber = batch, manufacturingDate = mfg, expiryDate = exp)
            } else it
        }
    }

    fun updateItemInventoryAction(itemId: String, action: String, isNew: Boolean) {
        _items.value = _items.value.map {
            if (it.grnItemId == itemId) {
                it.copy(inventoryAction = action, isNewProduct = isNew)
            } else it
        }
    }

    fun updateItemCategoryAndBrand(itemId: String, categoryId: String, brand: String) {
        _items.value = _items.value.map {
            if (it.grnItemId == itemId) {
                it.copy(categoryId = categoryId, brand = brand)
            } else it
        }
    }

    fun updateItemSellingPrice(itemId: String, price: Double) {
        _items.value = _items.value.map {
            if (it.grnItemId == itemId) {
                it.copy(sellingPrice = price)
            } else it
        }
    }

    fun saveAndConfirmGRN(notes: String) {
        val po = _selectedPO.value ?: return
        if (_isLoading.value) return
        
        viewModelScope.launch {
            _isLoading.value = true
            val currentItems = _items.value
            val grnNumber = grnRepository.generateGrnNumber()
            val grnId = UUID.randomUUID().toString()
            
            val totalReceivedQty = currentItems.sumOf { it.receivedQty }
            val header = GrnHeaderEntity(
                grnId = grnId,
                grnNumber = grnNumber,
                poId = po.poId,
                poNumber = po.poNumber,
                vendorId = po.vendorId,
                vendorName = po.vendorName,
                vendorPhone = "", // Add if PO had it
                status = "DRAFT",
                notes = notes,
                receivedBy = "admin_user_id", // Should come from Auth authRepo.getCurrentUser()
                receivedByName = "Admin",
                totalItems = currentItems.size,
                totalReceivedQty = totalReceivedQty,
                totalAmount = currentItems.sumOf { it.totalCost },
                posTerminalId = "terminal_1"
            )
            
            val itemsToSave = currentItems.map { it.copy(grnId = grnId) }
            
            saveGrnDraftUseCase(header, itemsToSave)
            val result = confirmGrnUseCase(grnId)
            if (result.success) _confirmedGrnId.value = grnId  // expose real grnId
            _confirmResult.value = result
            _isLoading.value = false
        }
    }

    fun resetConfirmResult() {
        _confirmResult.value = null
        _confirmedGrnId.value = null
    }
}
