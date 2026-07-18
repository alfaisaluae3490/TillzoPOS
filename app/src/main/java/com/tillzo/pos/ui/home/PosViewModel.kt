package com.tillzo.pos.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.CustomerDao
import com.tillzo.pos.data.local.dao.InventoryDao
import com.tillzo.pos.data.local.dao.SaleDao
import com.tillzo.pos.data.local.dao.TillSessionDao
import com.tillzo.pos.data.local.entity.CustomerEntity
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.data.local.entity.SaleEntity
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.domain.model.CartItem
import com.tillzo.pos.domain.usecase.CompleteSaleUseCase
import com.tillzo.pos.utils.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * M4 POS ViewModel — drives PosScreen (HomeScreen), PaymentDialog, and triggers CompleteSaleUseCase.
 *
 * Architecture Law: No Room or Retrofit calls directly here — only via UseCases and DAOs via inject.
 * Note: InventoryDao and CustomerDao are injected here per the existing pattern in SalesUploadUseCase
 * which also uses DAOs directly.
 */
sealed class SaleResult {
    data class Success(val sale: SaleEntity) : SaleResult()
    data class Error(val message: String) : SaleResult()
}

enum class PaymentMethod { CASH, CARD, WALLET, UDHAAR }

data class PaymentBreakdown(
    val cashAmount: Double = 0.0,
    val cardAmount: Double = 0.0,
    val walletAmount: Double = 0.0,
    val udhaarAmount: Double = 0.0
) {
    val total get() = cashAmount + cardAmount + walletAmount + udhaarAmount
    val methodString: String
        get() {
            val used = buildList {
                if (cashAmount > 0) add("CASH")
                if (cardAmount > 0) add("CARD")
                if (walletAmount > 0) add("WALLET")
                if (udhaarAmount > 0) add("UDHAAR")
            }
            return when {
                used.size > 1 -> "SPLIT"
                used.size == 1 -> used.first()
                else -> "CASH"
            }
        }
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class PosViewModel @Inject constructor(
    private val completeSaleUseCase: CompleteSaleUseCase,
    private val inventoryDao: InventoryDao,
    private val customerDao: CustomerDao,
    private val saleDao: SaleDao,
    private val tillSessionDao: TillSessionDao,
    private val appSetupPrefs: AppSetupPrefs,
    private val appLogger: AppLogger
) : ViewModel() {

    companion object {
        private const val TAG = "PosViewModel"
    }

    // ── Cart State ────────────────────────────────────────────────────────────

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val cartSubtotal: StateFlow<Double> = _cartItems
        .map { items -> items.sumOf { it.total } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val cartTax: StateFlow<Double> = _cartItems
        .map { items -> items.sumOf { item -> item.total * item.taxPercent / 100.0 } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    private val _cartDiscount = MutableStateFlow(0.0)
    val cartDiscount: StateFlow<Double> = _cartDiscount.asStateFlow()

    val cartTotal: StateFlow<Double> = _cartItems
        .map { items ->
            val sub = items.sumOf { it.total }
            val tax = items.sumOf { item -> item.total * item.taxPercent / 100.0 }
            sub + tax - _cartDiscount.value
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    // ── Search State ──────────────────────────────────────────────────────────

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<InventoryEntity>> = _searchQuery
        .debounce(200)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else inventoryDao.searchItems(query)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ── Currency Symbol ───────────────────────────────────────────────────────
    val currencySymbol: String = appSetupPrefs.currencySymbol

    // Quick-Access Grid: admin-pinned items, ordered by pinnedOrder
    val quickGridItems: StateFlow<List<InventoryEntity>> = inventoryDao.getPinnedItems()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Payment State ─────────────────────────────────────────────────────────

    private val _paymentBreakdown = MutableStateFlow(PaymentBreakdown())
    val paymentBreakdown: StateFlow<PaymentBreakdown> = _paymentBreakdown.asStateFlow()

    val remainingAmount: StateFlow<Double> = _cartItems.flatMapLatest { items ->
        val sub = items.sumOf { it.total }
        val tax = items.sumOf { item -> item.total * item.taxPercent / 100.0 }
        val total = sub + tax - _cartDiscount.value
        _paymentBreakdown.map { pb -> (total - pb.total).coerceAtLeast(0.0) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    // ── Customer State ────────────────────────────────────────────────────────

    private val _customerQuery = MutableStateFlow("")
    val customerQuery: StateFlow<String> = _customerQuery.asStateFlow()

    val customerSearchResults: StateFlow<List<CustomerEntity>> = _customerQuery
        .debounce(200)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else customerDao.searchCustomers(query)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer.asStateFlow()

    // ── Sync State ────────────────────────────────────────────────────────────

    private val _hasPendingSync = MutableStateFlow(false)
    val hasPendingSync: StateFlow<Boolean> = _hasPendingSync.asStateFlow()

    init {
        viewModelScope.launch {
            val pendingSales = saleDao.getPendingSyncSales()
            val pendingSessions = tillSessionDao.getPendingSessions()
            _hasPendingSync.value = pendingSales.isNotEmpty() || pendingSessions.isNotEmpty()
        }
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                saleDao.getAllSales().map { sales -> sales.any { it.sync_status == "pending" } },
                tillSessionDao.getAllSessions().map { sessions -> sessions.any { it.syncStatus == "pending" } }
            ) { salesPending, sessionsPending ->
                salesPending || sessionsPending
            }.collect { pending ->
                _hasPendingSync.value = pending
            }
        }
    }

    // ── Stock Warning State ───────────────────────────────────────────────────

    data class StockWarning(
        val itemName: String,
        val available: Double,
        val requested: Double
    )

    private val _stockWarning = MutableStateFlow<StockWarning?>(null)
    val stockWarning: StateFlow<StockWarning?> = _stockWarning.asStateFlow()

    // ── Sale Completion State ─────────────────────────────────────────────────

    private val _saleResult = MutableStateFlow<SaleResult?>(null)
    val saleResult: StateFlow<SaleResult?> = _saleResult.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // ── Cart Functions ────────────────────────────────────────────────────────

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(product: InventoryEntity, qty: Double = 1.0) {
        val currentCart = _cartItems.value.toMutableList()
        val existingIndex = currentCart.indexOfFirst { it.itemId == product.system_row_id }
        val currentQtyInCart = if (existingIndex >= 0) currentCart[existingIndex].quantity else 0.0
        val totalRequested = currentQtyInCart + qty

        // Check stock availability
        if (totalRequested > product.current_stock) {
            _stockWarning.value = StockWarning(
                itemName = product.item_name,
                available = product.current_stock,
                requested = totalRequested
            )
            appLogger.logWarn("UI_CLICK", "Stock warning for ${product.item_name}: requested $totalRequested, available ${product.current_stock}")
            return
        }
        _stockWarning.value = null

        if (existingIndex >= 0) {
            val existing = currentCart[existingIndex]
            val newQty = existing.quantity + qty
            currentCart[existingIndex] = existing.copy(
                quantity = newQty,
                total = newQty * existing.pricePerUnit
            )
        } else {
            currentCart.add(
                CartItem(
                    itemId = product.system_row_id,
                    name = product.item_name,
                    quantity = qty,
                    pricePerUnit = product.price_per_unit,
                    unit = product.unit,
                    taxPercent = product.tax_percent,
                    total = qty * product.price_per_unit
                )
            )
        }
        _cartItems.value = currentCart
        _searchQuery.value = ""
        appLogger.logInfo("UI_CLICK", "Added to cart: ${product.item_name} x $qty")
    }

    // ── Quick Grid Pinning ────────────────────────────────────────────────────

    fun togglePinItem(itemId: String, shouldPin: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (shouldPin) {
                val pinnedItems = inventoryDao.getPinnedItemsOnce()
                val nextOrder = pinnedItems.size + 1
                inventoryDao.updatePinStatus(itemId, pinned = true, order = nextOrder)
            } else {
                inventoryDao.updatePinStatus(itemId, pinned = false, order = 0)
            }
        }
    }

    fun updateCartItemQty(itemId: String, newQty: Double) {
        if (newQty <= 0.0) {
            removeFromCart(itemId)
            return
        }
        // Check stock availability from local inventory
        viewModelScope.launch {
            val product = inventoryDao.getItemById(itemId)
            if (product != null && newQty > product.current_stock) {
                _stockWarning.value = StockWarning(
                    itemName = product.item_name,
                    available = product.current_stock,
                    requested = newQty
                )
                return@launch
            }
            _stockWarning.value = null
            _cartItems.value = _cartItems.value.map { item ->
                if (item.itemId == itemId) {
                    item.copy(quantity = newQty, total = newQty * item.pricePerUnit)
                } else item
            }
        }
    }

    fun removeFromCart(itemId: String) {
        _cartItems.value = _cartItems.value.filter { it.itemId != itemId }
        appLogger.logInfo("UI_CLICK", "Removed from cart: itemId=$itemId")
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _cartDiscount.value = 0.0
        _paymentBreakdown.value = PaymentBreakdown()
        _selectedCustomer.value = null
        _saleResult.value = null
        _stockWarning.value = null
        appLogger.logInfo("UI_CLICK", "Cart cleared")
    }

    fun setDiscount(amount: Double) {
        _cartDiscount.value = amount
        appLogger.logInfo("UI_CLICK", "Discount applied: Rs $amount")
    }

    // ── Payment Functions ─────────────────────────────────────────────────────

    fun onPaymentAmountChanged(method: PaymentMethod, amount: Double) {
        val pb = _paymentBreakdown.value
        _paymentBreakdown.value = when (method) {
            PaymentMethod.CASH    -> pb.copy(cashAmount = amount)
            PaymentMethod.CARD    -> pb.copy(cardAmount = amount)
            PaymentMethod.WALLET  -> pb.copy(walletAmount = amount)
            PaymentMethod.UDHAAR  -> pb.copy(udhaarAmount = amount)
        }
    }

    fun onCustomerQueryChanged(query: String) {
        _customerQuery.value = query
    }

    fun selectCustomer(customer: CustomerEntity) {
        _selectedCustomer.value = customer
    }

    fun clearSelectedCustomer() {
        _selectedCustomer.value = null
    }

    fun createAndSelectNewCustomer(name: String, phone: String, whatsapp: String) {
        viewModelScope.launch {
            try {
                val customer = completeSaleUseCase.createNewCustomer(
                    name = name,
                    phone = phone,
                    whatsapp = whatsapp.ifBlank { null }
                )
                _selectedCustomer.value = customer
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create customer: ${e.message}", e)
            }
        }
    }

    // ── Sale Completion ───────────────────────────────────────────────────────

    fun completeSale() {
        if (_isProcessing.value) return
        val items = _cartItems.value
        if (items.isEmpty()) return

        appLogger.logInfo("UI_CLICK", "Cash checkout initiated with ${items.size} items")

        viewModelScope.launch {
            _isProcessing.value = true
            _saleResult.value = null
            try {
                // Verify stock sufficiency for all cart items
                for (cartItem in items) {
                    val product = inventoryDao.getItemById(cartItem.itemId)
                    if (product != null && cartItem.quantity > product.current_stock) {
                        _stockWarning.value = StockWarning(
                            itemName = cartItem.name,
                            available = product.current_stock,
                            requested = cartItem.quantity
                        )
                        _isProcessing.value = false
                        _saleResult.value = SaleResult.Error(
                            "Insufficient stock for ${cartItem.name}: " +
                            "available ${product.current_stock} ${product.unit}, " +
                            "requested ${cartItem.quantity}"
                        )
                        appLogger.logError("UI_CLICK", "Sale failed: insufficient stock for ${cartItem.name}")
                        return@launch
                    }
                }
                _stockWarning.value = null

                val sub = items.sumOf { it.total }
                val tax = items.sumOf { item -> item.total * item.taxPercent / 100.0 }
                val disc = _cartDiscount.value
                val total = sub + tax - disc
                val pb = _paymentBreakdown.value

                val sale = completeSaleUseCase(
                    cartItems = items,
                    subtotal = sub,
                    tax = tax,
                    discount = disc,
                    total = total,
                    paymentMethod = pb.methodString,
                    cashAmount = pb.cashAmount,
                    cardAmount = pb.cardAmount,
                    walletAmount = pb.walletAmount,
                    udhaarAmount = pb.udhaarAmount,
                    selectedCustomerId = _selectedCustomer.value?.system_row_id,
                    cashierId = appSetupPrefs.userEmail.ifBlank { "cashier" }
                )
                _saleResult.value = SaleResult.Success(sale)
                appLogger.logInfo("UI_CLICK", "Sale completed: ${sale.sync_uuid}, total=$total, method=${pb.methodString}")
            } catch (e: Exception) {
                Log.e(TAG, "Sale failed: ${e.message}", e)
                appLogger.logError("UI_CLICK", "Sale failed: ${e.message}", e)
                _saleResult.value = SaleResult.Error(e.message ?: "Sale failed")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun resetAfterSale() {
        clearCart()
        _saleResult.value = null
        appLogger.logInfo("UI_CLICK", "New sale started")
    }

    fun logClick(tag: String, message: String) {
        appLogger.logInfo(tag, message)
    }
}
