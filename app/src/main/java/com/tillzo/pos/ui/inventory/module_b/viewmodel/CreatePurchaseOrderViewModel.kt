package com.tillzo.pos.ui.inventory.module_b.viewmodel

import android.util.Log
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
    private val inventoryDao: InventoryDao,
    private val appSetupPrefs: com.tillzo.pos.data.local.prefs.AppSetupPrefs
) : ViewModel() {

    // ── exposed state ────────────────────────────────────────────────────────

    private val _items = MutableStateFlow<List<PurchaseOrderItemEntity>>(emptyList())
    val items: StateFlow<List<PurchaseOrderItemEntity>> = _items.asStateFlow()

    private val _selectedVendor = MutableStateFlow<VendorEntity?>(null)
    val selectedVendor: StateFlow<VendorEntity?> = _selectedVendor.asStateFlow()

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount.asStateFlow()

    // FIX (2026-08-23, DEF-61): double-save guard — rapid taps could launch two
    // concurrent saves, both reading the same sequence (race). GRN VM already had
    // _isLoading; PO VM had none.
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // ── vendor ───────────────────────────────────────────────────────────────

    /** Returns a Flow of vendor search results; collect in composable via LaunchedEffect */
    fun searchVendors(query: String): Flow<List<VendorEntity>> =
        if (query.isBlank()) vendorDao.getActiveVendors()
        else kotlinx.coroutines.flow.flow {
            emit(vendorDao.searchActiveVendors(query))
        }

    fun setVendor(vendor: VendorEntity) {
        _selectedVendor.value = vendor
    }

    fun saveNewVendorAndSelect(name: String, phone: String, whatsapp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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
            } catch (e: Exception) {
                Log.e("CreatePOVM", "Failed to save new vendor", e)
            }
        }
    }

    // ── inventory search ─────────────────────────────────────────────────────

    /** Returns a Flow of inventory search results */
    fun searchInventory(query: String): Flow<List<InventoryEntity>> =
        inventoryDao.searchItems(query)

    // ── item management ──────────────────────────────────────────────────────

    fun addItem(product: InventoryEntity, qty: Double, cost: Double) {
        // FIX (2026-08-23, DEF-112): negative qty/price pehle cart mein add ho
        // jate the → negative PO total. Ab reject.
        if (qty <= 0.0 || cost < 0.0) return
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
        // FIX (2026-08-23, DEF-112): negative qty reject.
        if (qty < 0.0) return
        _items.value = _items.value.map { item ->
            if (item.poItemId == itemId)
                item.copy(orderedQty = qty, totalCost = qty * item.unitCostPrice)
            else item
        }
        recalcTotal()
    }

    fun updateItemPrice(itemId: String, price: Double) {
        // FIX (2026-08-23, DEF-112): negative price reject.
        if (price < 0.0) return
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

    fun savePO(notes: String, expectedDate: String, markAsSent: Boolean = false, onSuccess: () -> Unit) {
        val vendor = _selectedVendor.value ?: return
        val currentItems = _items.value
        if (currentItems.isEmpty()) return
        // FIX (2026-08-23, DEF-61): double-save guard — second tap while saving
        // is dropped instead of racing the same sequence number.
        if (_isSaving.value) return
        _isSaving.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val now  = System.currentTimeMillis()
                val poId = UUID.randomUUID().toString()
                // FIX (2026-08-23, DEF-61): MAX-based atomic sequence instead of
                // COUNT(*)+1 — no reuse after soft-deletes, no read-then-insert race.
                val seq = poDao.getNextPoSequence()
                val poNumber = "PO-${java.text.SimpleDateFormat("yyyyMM", java.util.Locale.getDefault()).format(java.util.Date())}-${String.format("%04d", seq)}"

            val po = PurchaseOrderEntity(
                poId                  = poId,
                poNumber              = poNumber,
                vendorId              = vendor.vendorId,
                vendorName            = vendor.name,
                status                = if (markAsSent) "SENT" else "DRAFT",
                notes                 = notes,
                totalAmount           = _totalAmount.value,
                // FIX (2026-08-22, DEF-02): currency was hardcoded '$' —
                // stores using AED/other symbols got wrong POs on the sheet.
                // Use the user's configured currency symbol.
                currency              = appSetupPrefs.currencySymbol.ifBlank { "$" },
                expectedDeliveryDate  = expectedDate,
                // FIX (2026-08-23, DEF-93): createdBy was hardcoded "admin" —
                // sheet Purchase_Orders.created_by hamesha "admin" dikhata tha,
                // signed-in user kabhi record nahi hota tha (DEF-05 GRN pattern).
                createdBy             = appSetupPrefs.userEmail.ifBlank { "admin" },
                syncStatus            = "pending",
                isDeleted             = false,
                createdAt             = now,
                updatedAt             = now
            )
            val itemsToSave = currentItems.map { it.copy(poId = poId, updatedAt = now) }

            poDao.insertPO(po)
            poDao.insertPOItems(itemsToSave)

            kotlinx.coroutines.withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                // FIX (2026-08-23, DEF-111): pehle koi catch nahi tha — DB failure
                // par uncaught exception app crash karta tha aur user ko kuch
                // nahi dikhta tha. Ab log + silent (finally _isSaving reset).
                Log.e("CreatePOVM", "Failed to save PO", e)
            } finally {
                _isSaving.value = false
            }
        }
    }
}
