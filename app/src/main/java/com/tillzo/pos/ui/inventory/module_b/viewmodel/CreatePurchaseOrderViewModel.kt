package com.tillzo.pos.ui.inventory.module_b.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.InventoryDao
import com.tillzo.pos.data.local.dao.PurchaseOrderDao
import com.tillzo.pos.data.local.dao.VendorDao
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderItemEntity
import com.tillzo.pos.data.local.entity.VendorEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreatePurchaseOrderViewModel @Inject constructor(
    private val poDao: PurchaseOrderDao,
    private val vendorDao: VendorDao,
    private val inventoryDao: InventoryDao
) : ViewModel() {

    // ── exposed state ────────────────────────────────────────────────────────

    private val _items = MutableStateFlow<List<PurchaseOrderItemEntity>>(emptyList())
    val items: StateFlow<List<PurchaseOrderItemEntity>> = _items.asStateFlow()

    private val _selectedVendor = MutableStateFlow<VendorEntity?>(null)
    val selectedVendor: StateFlow<VendorEntity?> = _selectedVendor.asStateFlow()

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount.asStateFlow()

    // ── vendor ───────────────────────────────────────────────────────────────

    /** Returns a Flow of vendor search results; collect in composable via LaunchedEffect */
    fun searchVendors(query: String): Flow<List<VendorEntity>> =
        if (query.isBlank()) vendorDao.getAllVendors()
        else kotlinx.coroutines.flow.flow {
            emit(vendorDao.searchVendors(query))
        }

    fun setVendor(vendor: VendorEntity) {
        _selectedVendor.value = vendor
    }

    fun saveNewVendorAndSelect(name: String, phone: String, whatsapp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val vendor = VendorEntity(
                vendorId  = UUID.randomUUID().toString(),
                name      = name,
                phone     = phone,
                whatsapp  = whatsapp,
                syncStatus = "pending",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            vendorDao.insertVendor(vendor)
            _selectedVendor.value = vendor
        }
    }

    // ── inventory search ─────────────────────────────────────────────────────

    /** Returns a Flow of inventory search results */
    fun searchInventory(query: String): Flow<List<InventoryEntity>> =
        inventoryDao.searchItems(query)

    // ── item management ──────────────────────────────────────────────────────

    fun addItem(product: InventoryEntity, qty: Double, cost: Double) {
        val newItem = PurchaseOrderItemEntity(
            poItemId      = UUID.randomUUID().toString(),
            poId          = "",               // assigned on save
            productId     = product.system_row_id,
            productName   = product.item_name,
            sku           = product.sku,
            barcodeId     = product.barcode_id,
            orderedQty    = qty,
            receivedQty   = 0.0,
            unitCostPrice = cost,
            totalCost     = qty * cost,
            unit          = product.unit
        )
        _items.value = _items.value + newItem
        recalcTotal()
    }

    fun removeItem(itemId: String) {
        _items.value = _items.value.filter { it.poItemId != itemId }
        recalcTotal()
    }

    fun updateItemQty(itemId: String, qty: Double) {
        _items.value = _items.value.map { item ->
            if (item.poItemId == itemId)
                item.copy(orderedQty = qty, totalCost = qty * item.unitCostPrice)
            else item
        }
        recalcTotal()
    }

    fun updateItemPrice(itemId: String, price: Double) {
        _items.value = _items.value.map { item ->
            if (item.poItemId == itemId)
                item.copy(unitCostPrice = price, totalCost = item.orderedQty * price)
            else item
        }
        recalcTotal()
    }

    private fun recalcTotal() {
        _totalAmount.value = _items.value.sumOf { it.totalCost }
    }

    // ── save PO ──────────────────────────────────────────────────────────────

    fun savePO(notes: String, expectedDate: String, onSuccess: () -> Unit) {
        val vendor = _selectedVendor.value ?: return
        val currentItems = _items.value
        if (currentItems.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val now  = System.currentTimeMillis()
            val poId = UUID.randomUUID().toString()
            val count = poDao.getTotalPOCount() + 1
            val poNumber = "PO-${java.text.SimpleDateFormat("yyyyMM", java.util.Locale.getDefault()).format(java.util.Date())}-${String.format("%04d", count)}"

            val po = PurchaseOrderEntity(
                poId                  = poId,
                poNumber              = poNumber,
                vendorId              = vendor.vendorId,
                vendorName            = vendor.name,
                status                = "DRAFT",
                notes                 = notes,
                totalAmount           = _totalAmount.value,
                currency              = "PKR",
                expectedDeliveryDate  = expectedDate,
                createdBy             = "admin",
                syncStatus            = "pending",
                isDeleted             = false,
                createdAt             = now,
                updatedAt             = now
            )
            val itemsToSave = currentItems.map { it.copy(poId = poId, updatedAt = now) }

            poDao.insertPO(po)
            poDao.insertPOItems(itemsToSave)

            kotlinx.coroutines.withContext(Dispatchers.Main) { onSuccess() }
        }
    }
}
