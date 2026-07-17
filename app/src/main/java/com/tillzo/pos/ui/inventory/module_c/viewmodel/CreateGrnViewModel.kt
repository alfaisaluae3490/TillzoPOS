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
        
        viewModelScope.launch {
            _isLoading.value = true
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
                receivedBy = "admin_user_id",
                receivedByName = "Admin",
                totalItems = currentItems.size,
                totalReceivedQty = totalReceivedQty,
                totalAmount = currentItems.sumOf { it.totalCost },
                posTerminalId = "terminal_1",
                attachedFileId = attachedFileId,
                attachedFileUrl = attachedFileUrl
            )
            
            val itemsToSave = currentItems.map { it.copy(grnId = grnId) }
            
            saveGrnDraftUseCase(header, itemsToSave)
            val result = confirmGrnUseCase(grnId)
            if (result.success) _confirmedGrnId.value = grnId
            _confirmResult.value = result
            _isLoading.value = false
        }
    }

    private suspend fun resolveUploadFolderId(): String? {
        val saved = appSetupPrefs.grnFolderId
        if (saved.isNotBlank()) return saved

        val folders = driveSearchHelper.searchFolders()
        val target = folders.find { it.name == "Tillzo POS Uploads" }
        if (target != null) {
            appSetupPrefs.saveGrnFolder(target.spreadsheetId, target.name)
            return target.spreadsheetId
        }

        val newId = driveSearchHelper.createFolder("Tillzo POS Uploads")
        if (newId != null) {
            appSetupPrefs.saveGrnFolder(newId, "Tillzo POS Uploads")
        }
        return newId
    }

    fun resetConfirmResult() {
        _confirmResult.value = null
        _confirmedGrnId.value = null
    }
}
