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
import com.tillzo.pos.data.local.dao.KhataEventDao
import com.tillzo.pos.data.local.dao.TillSessionDao // DEF-127 (2026-08-25)
import com.tillzo.pos.data.local.entity.KhataEventEntity
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.local.dao.ProductBatchDao
import com.tillzo.pos.data.local.dao.WastageDao
import com.tillzo.pos.data.local.dao.ReturnsDao // GAP-3 (2026-08-23)
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
    private val wastageDao: WastageDao,
    private val khataEventDao: KhataEventDao,
    private val returnsDao: ReturnsDao, // GAP-3 (2026-08-23)
    private val tillSessionDao: TillSessionDao, // DEF-127 (2026-08-25)
    private val appSetupPrefs: AppSetupPrefs
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
                // If not found by system_row_id, try by invoice ID (QR code content),
                // then partial case-insensitive prefix (receipt 8-char ID) — DEF-86
                _foundInvoice.value = pastSale
                    ?: saleRepository.getSaleByInvoiceId(query)
                    ?: saleRepository.getSaleByInvoiceIdPrefix(query.trim())
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
            // DEF-46b FIX (2026-08-23): double-refund guard — same invoice
            // dobara refund karna ab BLOCK hai (pehle user search karke
            // unlimited baar refund kar sakta tha; har refund ek nayi
            // negative-sale row banata tha → revenue double-dipped).
            if (saleRepository.hasRefundForInvoice(originalSale.invoiceId)) {
                _returnStatus.value = "Error: This invoice has already been refunded."
                return@launch
            }
            val returnInvoiceId = UUID.randomUUID().toString()
            val cashierId = appSetupPrefs.userEmail.ifBlank { "cashier" }
            
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

            // DEF-127 FIX (2026-08-25): refund ab till session ko bhi
            // decrement karta hai. Pehle sirf negative SaleEntity insert hota
            // tha — session ke totalCashSales/totalSalesCount/expectedCash
            // kabhi update nahi hote the, isliye Z-Report "Expected Cash"
            // overstated rehta tha aur day-close par jhoota SHORTAGE dikhata
            // tha (drawer mein refund wali cash nahi hoti). Amounts ORIGINAL
            // sale se derive hote hain: CASH refund par drawer ka net jo gaya
            // tha wahi wapas aata hai (sale total — DEF-39 over-tender change
            // pehle hi net ho chuka hai); SPLIT refund par har method ka
            // exact portion. Non-fatal — koi open session nahi to skip.
            try {
                val posId = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }
                val openSession = tillSessionDao.getOpenSession(posId)
                openSession?.let { session ->
                    val refundMethod = originalSale.paymentMethod.ifBlank { "CASH" }
                    val cashOut: Double
                    val cardOut: Double
                    val walletOut: Double
                    val udhaarOut: Double
                    when {
                        refundMethod.equals("SPLIT", ignoreCase = true) -> {
                            cashOut = originalSale.cashAmount
                            cardOut = originalSale.cardAmount
                            walletOut = originalSale.walletAmount
                            udhaarOut = originalSale.udhaarAmount
                        }
                        refundMethod.equals("CARD", ignoreCase = true) -> {
                            cashOut = 0.0; cardOut = originalSale.total; walletOut = 0.0; udhaarOut = 0.0
                        }
                        refundMethod.equals("WALLET", ignoreCase = true) -> {
                            cashOut = 0.0; cardOut = 0.0; walletOut = originalSale.total; udhaarOut = 0.0
                        }
                        refundMethod.equals("UDHAAR", ignoreCase = true) -> {
                            cashOut = 0.0; cardOut = 0.0; walletOut = 0.0; udhaarOut = originalSale.total
                        }
                        else -> { // CASH
                            cashOut = originalSale.total
                            cardOut = 0.0; walletOut = 0.0; udhaarOut = 0.0
                        }
                    }
                    tillSessionDao.deductRefundFromSession(
                        sessionId = session.sessionId,
                        cashOut = cashOut,
                        cardOut = cardOut,
                        walletOut = walletOut,
                        udhaarOut = udhaarOut
                    )
                }
            } catch (e: Exception) {
                android.util.Log.w("ReturnsVM", "Till session refund update non-fatal: ${e.message}")
            }

            // GAP-3 FIX (2026-08-23): Returns sheet tab was vestigial — never
            // populated. Now every processed return writes one ReturnsEntity
            // row per returned item → synced to the Returns tab. Condition
            // mirrors the reason (RESTOCK / DAMAGED); refund method comes from
            // the ORIGINAL sale's payment method.
            try {
                val condition = when {
                    reason.equals("Restock", ignoreCase = true) -> "RESTOCK"
                    else -> "DAMAGED"
                }
                val refundMethod = originalSale.paymentMethod.ifBlank { "CASH" }
                originalSale.items.forEach { saleItem ->
                    returnsDao.insertReturn(
                        com.tillzo.pos.data.local.entity.ReturnsEntity(
                            systemRowId = UUID.randomUUID().toString(),
                            originalInvoiceId = originalSale.invoiceId,
                            itemId = saleItem.itemId,
                            qtyReturned = saleItem.quantity,
                            condition = condition,
                            refundMethod = refundMethod,
                            amount = saleItem.total,
                            posTerminalId = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }
                        )
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ReturnsVM", "Returns ledger insert failed: ${e.message}")
            }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            when {
                reason.equals("Restock", ignoreCase = true) -> {
                    originalSale.items.forEach { saleItem ->
                        val returnedQty = saleItem.quantity
                        val item = inventoryDao.getItemById(saleItem.itemId)
                        if (item != null) {
                            val now = System.currentTimeMillis()
                            val newStock = item.current_stock + returnedQty
                            inventoryDao.updateStockAndSyncStatus(item.system_row_id, newStock, now)
                            
                            if (item.hasBatches) {
                                val batches = productBatchDao.getAllBatchesForProduct(item.system_row_id)
                                val activeBatch = batches.filter { it.isActive && !it.isDeleted }
                                                                .maxByOrNull { it.createdAt }
                                activeBatch?.let { batch ->
                                    productBatchDao.updateBatchStock(
                                        batch.batchId,
                                        batch.stockQty + returnedQty,
                                        now
                                    )
                                }
                                // FIX (2026-08-22, DEF-83): `batches` list upar update se
                                // PEHLE fetch hui thi — stale stockQty se sum nikalta tha, isliye
                                // +1 restock ke baad bhi totalStock purani (kam) value par
                                // overwrite ho jata tha (sale+return ke baad stock 1 unit
                                // hamesha kam rehta tha). Ab batch update ke BAAD re-fetch
                                // karke hi total sum karo.
                                val refreshed = productBatchDao.getAllBatchesForProduct(item.system_row_id)
                                val total = refreshed.filter { it.isActive && !it.isDeleted }.sumOf { it.stockQty }
                                inventoryDao.updateTotalStockAndSyncStatus(item.system_row_id, total, now)
                            }
                        }
                    }
                }

                // FIX (2026-08-22, DEF-01): UI sends "Damaged/Wastage" but the
                // ViewModel checked only "Damaged" — the whole wastage branch
                // was dead code and damaged returns silently restocked (and
                // double-counted stock). Accept both labels.
                reason.equals("Damaged", ignoreCase = true) || reason.equals("Damaged/Wastage", ignoreCase = true) || reason.equals("Wastage", ignoreCase = true) -> {
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
            
            // FIX (2026-08-22, DEF-46): refund was never recorded in the
            // customer's Khata — a customer who paid cash and then got a
            // refund saw NO credit on their account, and the khata balance
            // stayed overstated. Now a JAMA (credit) event is created for
            // customer-linked sales.
            if (originalSale.customerId != null) {
                try {
                    khataEventDao.insert(
                        KhataEventEntity(
                            customer_id = originalSale.customerId,
                            event_type = "JAMA",
                            amount = originalSale.total,
                            note = "Refund of ${originalSale.invoiceId} ($reason)",
                            reference_sale_id = returnInvoiceId,
                            pos_terminal_id = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }
                        )
                    )
                } catch (e: Exception) {
                    android.util.Log.e("ReturnsVM", "Khata refund event failed: ${e.message}")
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
