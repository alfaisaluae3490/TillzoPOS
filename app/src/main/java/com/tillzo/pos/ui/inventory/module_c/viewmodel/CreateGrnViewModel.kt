package com.tillzo.pos.ui.inventory.module_c.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.ProductBatchDao
import com.tillzo.pos.data.local.dao.PurchaseOrderDao
import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.data.local.entity.GrnItemEntity
import com.tillzo.pos.data.local.entity.ProductBatchEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderEntity
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.remote.DriveSearchHelper
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.domain.repository.ConfirmGrnResult
import com.tillzo.pos.domain.usecase.grn.ConfirmGrnUseCase
import com.tillzo.pos.domain.usecase.grn.SaveGrnDraftUseCase
import com.tillzo.pos.domain.repository.GrnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateGrnViewModel @Inject constructor(
    private val poDao: PurchaseOrderDao,
    private val productBatchDao: ProductBatchDao,
    private val grnRepository: GrnRepository,
    private val vendorPaymentRepository: com.tillzo.pos.domain.repository.VendorPaymentRepository,
    private val saveGrnDraftUseCase: SaveGrnDraftUseCase,
    private val confirmGrnUseCase: ConfirmGrnUseCase,
    private val sheetsRemoteDataSource: SheetsRemoteDataSource,
    private val appSetupPrefs: AppSetupPrefs,
    private val driveSearchHelper: DriveSearchHelper,
    @ApplicationContext private val appContext: Context
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

    private val _attachedFileUri = MutableStateFlow<Uri?>(null)
    val attachedFileUri: StateFlow<Uri?> = _attachedFileUri.asStateFlow()

    private val _attachedFileName = MutableStateFlow("")
    val attachedFileName: StateFlow<String> = _attachedFileName.asStateFlow()

    // ── Payment Terms & Reminder State ───────────────────────────────────────
    private val _paymentOption = MutableStateFlow("FULL_PAID") // FULL_PAID | PARTIAL | CREDIT
    val paymentOption: StateFlow<String> = _paymentOption.asStateFlow()

    private val _paidAmountInput = MutableStateFlow("")
    val paidAmountInput: StateFlow<String> = _paidAmountInput.asStateFlow()

    private val _paymentMethod = MutableStateFlow("CASH") // CASH | BANK_TRANSFER | CHEQUE | CARD
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _paymentDueDate = MutableStateFlow("")
    val paymentDueDate: StateFlow<String> = _paymentDueDate.asStateFlow()

    private val _reminderEnabled = MutableStateFlow(true)
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    private val _reminderIntervalDays = MutableStateFlow(1)
    val reminderIntervalDays: StateFlow<Int> = _reminderIntervalDays.asStateFlow()

    fun setPaymentOption(option: String) { _paymentOption.value = option }
    fun setPaidAmountInput(amount: String) { _paidAmountInput.value = amount }
    fun setPaymentMethod(method: String) { _paymentMethod.value = method }
    fun setPaymentDueDate(date: String) { _paymentDueDate.value = date }
    fun setReminderEnabled(enabled: Boolean) { _reminderEnabled.value = enabled }
    fun setReminderIntervalDays(days: Int) { _reminderIntervalDays.value = days }

    fun setAttachedFile(uri: Uri?, fileName: String) {
        _attachedFileUri.value = uri
        _attachedFileName.value = fileName
    }

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

    fun getBatchesForProduct(productId: String): kotlinx.coroutines.flow.Flow<List<ProductBatchEntity>> {
        return productBatchDao.getBatchesForProduct(productId)
    }

    fun updateItemBatchSelection(itemId: String, batchId: String, batchNumber: String, mfgDate: String, expiryDate: String) {
        _items.value = _items.value.map {
            if (it.grnItemId == itemId) {
                it.copy(
                    batchId = batchId,
                    batchNumber = batchNumber,
                    manufacturingDate = mfgDate,
                    expiryDate = expiryDate
                )
            } else it
        }
    }

    fun saveAndConfirmGRN(notes: String) {
        val po = _selectedPO.value ?: return
        if (_isLoading.value) return
        // OVERNIGHT-AUDIT FIX (2026-08-24, D7-2): PO already fully received → block new GRN.
        // Pehle har 'Receive Goods' navigation naya GRN bana deta tha, duplicate batches
        // ban jaate the jab user button ko baar-baar dabata tha.
        if (po.status.equals("RECEIVED", ignoreCase = true)) return
        
        viewModelScope.launch {
            _isLoading.value = true
            // FIX (2026-08-23, DEF-101): pehle koi try/catch nahi tha aur
            // _isLoading sirf normal path par false hota tha — koi exception
            // (DB/network) aane par app crash + UI hamesha loading stuck.
            // Ab catch + finally reset.
            try {
            val currentItems = _items.value
            val grnNumber = grnRepository.generateGrnNumber()
            val grnId = UUID.randomUUID().toString()

            var attachedFileId = ""
            var attachedFileUrl = ""

            val uri = _attachedFileUri.value
            if (uri != null) {
                try {
                    val fileName = _attachedFileName.value.ifEmpty { "grn_attachment_${grnNumber}" }
                    val mimeType = withContext(Dispatchers.IO) {
                        appContext.contentResolver.getType(uri) ?: "application/octet-stream"
                    }
                    val fileBytes = withContext(Dispatchers.IO) {
                        appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
                    }
                    if (fileBytes.isNotEmpty()) {
                        val parentId = resolveUploadFolderId()
                        val result = sheetsRemoteDataSource.uploadDocument(
                            filename = fileName,
                            mimeType = mimeType,
                            fileBytes = fileBytes,
                            parentFolderId = parentId
                        )
                        if (result != null) {
                            attachedFileId = result.first
                            attachedFileUrl = result.second
                        }
                    }
                } catch (_: Exception) { }
            }
            
            val totalReceivedQty = currentItems.sumOf { it.receivedQty }
            val totalAmount = currentItems.sumOf { it.totalCost }
            val paidAmount = when (_paymentOption.value) {
                "FULL_PAID" -> totalAmount
                "PARTIAL" -> _paidAmountInput.value.toDoubleOrNull()?.coerceIn(0.0, totalAmount) ?: 0.0
                else -> 0.0
            }
            val dueBalance = (totalAmount - paidAmount).coerceAtLeast(0.0)
            val paymentStatus = when {
                dueBalance <= 0.0 -> "PAID"
                paidAmount > 0.0 -> "PARTIALLY_PAID"
                else -> "UNPAID"
            }

            val header = GrnHeaderEntity(
                grnId = grnId,
                grnNumber = grnNumber,
                poId = po.poId,
                poNumber = po.poNumber,
                vendorId = po.vendorId,
                vendorName = po.vendorName,
                vendorPhone = "",
                status = "DRAFT",
                notes = notes,
                receivedBy = appSetupPrefs.userEmail.ifBlank { "admin_user_id" },
                receivedByName = appSetupPrefs.userDisplayName.ifBlank { "Admin" },
                totalItems = currentItems.size,
                totalReceivedQty = totalReceivedQty,
                totalAmount = totalAmount,
                paymentStatus = paymentStatus,
                paidAmount = paidAmount,
                dueBalance = dueBalance,
                paymentMethod = _paymentMethod.value,
                paymentDueDate = if (dueBalance > 0.0) _paymentDueDate.value else "",
                reminderEnabled = _reminderEnabled.value && dueBalance > 0.0,
                reminderIntervalDays = _reminderIntervalDays.value,
                // FIX (2026-08-23, DEF-105): posTerminalId hardcoded "terminal_1"
                // tha (baaki modules real terminal id use karte hain). Ab
                // spreadsheet-id based — sheet par terminal identity consistent.
                posTerminalId = appSetupPrefs.spreadsheetId.take(20).ifBlank { "terminal_1" },
                attachedFileId = attachedFileId,
                attachedFileUrl = attachedFileUrl
            )
            
            // FIX (2026-08-26): blank batch number → auto-generate on save,
            // taaki manual GRN entry mein bhi har item ka batch trackable ho
            // (ConfirmGrnUseCase mein bhi fallback hai — double safety).
            val itemsToSave = currentItems.map {
                val autoBatch = if (it.batchNumber.isBlank()) {
                    val seed = listOf(it.sku, it.barcodeId, it.productId)
                        .firstOrNull { s -> s.isNotBlank() } ?: "NEW"
                    "B-${grnNumber.takeLast(4)}-${seed.take(4).uppercase()}"
                } else it.batchNumber
                it.copy(grnId = grnId, batchNumber = autoBatch)
            }
            
            saveGrnDraftUseCase(header, itemsToSave)
            val result = confirmGrnUseCase(grnId)
            if (result.success) {
                _confirmedGrnId.value = grnId
                // Record in Vendor AP Ledger
                vendorPaymentRepository.recordBill(
                    vendorId = po.vendorId,
                    vendorName = po.vendorName,
                    grnId = grnId,
                    poId = po.poId,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount,
                    dueDate = if (dueBalance > 0.0) _paymentDueDate.value else "",
                    paymentMethod = _paymentMethod.value,
                    paidBy = appSetupPrefs.userEmail.ifBlank { "Admin" },
                    note = "GRN $grnNumber (${po.poNumber})"
                )
            }
            _confirmResult.value = result
            } catch (e: Exception) {
                // FIX (2026-08-23, DEF-101): GRN confirm failure ab crash nahi —
                // log + result surface (UI error dikha sakta hai).
                android.util.Log.e("CreateGrnVM", "GRN confirm failed", e)
                _confirmResult.value = ConfirmGrnResult(
                    success = false,
                    newProductsCreated = 0,
                    batchesAdded = 0,
                    batchesUpdated = 0,
                    errorMessage = "GRN confirm failed: ${e.message ?: "unknown error"}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun resolveUploadFolderId(): String? {
        val saved = appSetupPrefs.grnFolderId.ifBlank { appSetupPrefs.businessFolderId }
        if (saved.isNotBlank()) return saved

        val sheetId = appSetupPrefs.spreadsheetId
        val bizName = appSetupPrefs.businessName.ifBlank { "TillzoPOS Business" }
        val target = driveSearchHelper.findBusinessFolderForSheet(sheetId, bizName)
        if (target != null) {
            appSetupPrefs.saveGrnFolder(target.spreadsheetId, target.name)
            appSetupPrefs.saveBusinessFolder(target.spreadsheetId)
            return target.spreadsheetId
        }

        val newId = driveSearchHelper.createFolder("$bizName Folder", sheetId, bizName)
        if (newId != null) {
            appSetupPrefs.saveGrnFolder(newId, "$bizName Folder")
            appSetupPrefs.saveBusinessFolder(newId)
        }
        return newId
    }

    fun resetConfirmResult() {
        _confirmResult.value = null
        _confirmedGrnId.value = null
    }
}
