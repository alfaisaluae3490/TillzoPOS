package com.tillzo.pos.ui.store.options.returns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.domain.model.Sale
import com.tillzo.pos.domain.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import com.tillzo.pos.data.local.dao.InventoryDao
import com.tillzo.pos.data.local.dao.ProductBatchDao
import com.tillzo.pos.data.local.dao.WastageDao
import com.tillzo.pos.data.local.entity.WastageEntity
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class ReturnsViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val inventoryDao: InventoryDao,
    private val productBatchDao: ProductBatchDao,
    private val wastageDao: WastageDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _foundInvoice = MutableStateFlow<Sale?>(null)
    val foundInvoice = _foundInvoice.asStateFlow()

    private val _returnStatus = MutableStateFlow<String?>(null)
    val returnStatus = _returnStatus.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isNotBlank()) {
                val pastSale = saleRepository.getSaleById(query)
                // If not found by system_row_id, try by invoice ID (QR code content)
                _foundInvoice.value = pastSale ?: saleRepository.getSaleByInvoiceId(query)
            } else {
                _foundInvoice.value = null
            }
        }
    }

    /**
     * M7.3 Process a full return/refund.
     * Generates a NEW negative Sale row that gets appended to the Sheet to log the cancellation.
     * The original sale record remains untouched for historical accuracy.
     *
     * reason = "Restock"  → stock incremented in Room  
     * reason = "Damaged"  → WastageEntity created, stock NOT incremented  
     */
    fun processFullReturn(reason: String) {
        val originalSale = _foundInvoice.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val returnInvoiceId = UUID.randomUUID().toString()
            val cashierId = "user_1" // TODO: Fetch from actual AuthRepository user session
            
            // Negate the amounts to represent a deduction in revenue
            val returnSale = Sale(
                systemRowId = returnInvoiceId,
                invoiceId = UUID.randomUUID().toString(),
                cashierId = cashierId,
                timestamp = System.currentTimeMillis(),
                items = originalSale.items.map { it.copy(quantity = -it.quantity, total = -it.total) },
                subtotal = -originalSale.subtotal,
                tax = -originalSale.tax,
                total = -originalSale.total,
                paymentMethod = originalSale.paymentMethod,
                paymentSplit = null,
                referenceId = "REFUND_OF_${originalSale.invoiceId}_$reason"
            )
            
            saleRepository.processCheckout(returnSale)
            
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            when {
                reason.equals("Restock", ignoreCase = true) -> {
                    originalSale.items.forEach { saleItem ->
                        val returnedQty = saleItem.quantity
                        val item = inventoryDao.getItemById(saleItem.itemId)
                        if (item != null) {
                            val newStock = item.current_stock + returnedQty
                            inventoryDao.updateStock(item.system_row_id, newStock)
                            
                            if (item.hasBatches) {
                                val batches = productBatchDao.getAllBatchesForProduct(item.system_row_id)
                                val activeBatch = batches.filter { it.isActive && !it.isDeleted }
                                                         .maxByOrNull { it.createdAt }
                                activeBatch?.let { batch ->
                                    productBatchDao.updateBatchStock(
                                        batch.batchId,
                                        batch.stockQty + returnedQty,
                                        System.currentTimeMillis()
                                    )
                                }
                            }
                        }
                    }
                }

                reason.equals("Damaged", ignoreCase = true) -> {
                    // DAMAGED: log to wastage, do NOT add back to stock
                    originalSale.items.forEach { saleItem ->
                        val item = inventoryDao.getItemById(saleItem.itemId)
                        val wastage = WastageEntity(
                            productId   = saleItem.itemId,
                            productName = saleItem.name,
                            quantity    = saleItem.quantity,
                            unit        = item?.unit ?: "",
                            costPrice   = item?.cost_price ?: 0.0,
                            totalLoss   = saleItem.quantity * (item?.cost_price ?: 0.0),
                            reason      = "DAMAGED",
                            notes       = "Sales return — damaged (Refund of ${originalSale.invoiceId})",
                            loggedBy    = cashierId,
                            wastageDate = today,
                            posTerminalId = item?.pos_terminal_id ?: "terminal_1"
                        )
                        wastageDao.insertWastage(wastage)
                    }
                }
            }
            
            _returnStatus.value = "Refund Processed Successfully."
            _foundInvoice.value = null
            _searchQuery.value = ""
        }
    }
    
    fun clearStatus() {
        _returnStatus.value = null
    }
}
