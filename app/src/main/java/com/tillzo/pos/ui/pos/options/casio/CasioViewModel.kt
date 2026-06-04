package com.tillzo.pos.ui.pos.options.casio

import androidx.lifecycle.ViewModel
import com.tillzo.pos.domain.model.CartItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Handles logic for M4.1 (Casio Numpad), M4.2 (Quick Grid), 
 * M4.3 (Decimal Weights), and M4.4 (Blind Selling - Cart State).
 */
@HiltViewModel
class CasioViewModel @Inject constructor() : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    private val _numpadInput = MutableStateFlow("")
    val numpadInput: StateFlow<String> = _numpadInput

    val cartTotal: StateFlow<Double> = MutableStateFlow(0.0) // Derived state simplified for example

    fun appendNumpad(digit: String) {
        if (digit == "." && _numpadInput.value.contains(".")) return
        _numpadInput.update { it + digit }
    }

    fun clearNumpad() {
        _numpadInput.value = ""
    }

    /**
     * M4.4 Blind Selling: Add item without stock check
     * M4.3 Decimal Weight: parses numpadInput as Double quantity
     */
    fun addItemToCart(item: QuickGridItem) {
        val qtyInput = _numpadInput.value
        val qty = if (qtyInput.isNotBlank()) qtyInput.toDoubleOrNull() ?: 1.0 else 1.0

        val cartItem = CartItem(
            itemId = item.id,
            name = item.name,
            quantity = qty,
            pricePerUnit = item.price // M4.5 Admin-Locked Price
        )

        _cartItems.update { current ->
            val existing = current.find { it.itemId == item.id }
            if (existing != null) {
                // Update quantity of existing
                current.map {
                    if (it.itemId == item.id) it.copy(quantity = it.quantity + qty, total = (it.quantity + qty) * it.pricePerUnit)
                    else it
                }
            } else {
                current + cartItem
            }
        }
        
        clearNumpad() // Reset input after adding 
    }

    /**
     * M5.1 Universal HID Scanner / M5.2 ML Kit lookup
     */
    fun onBarcodeScanned(barcode: String) {
        // Dummy lookup database for hardware simulation:
        val item = when (barcode) {
            "123456789" -> QuickGridItem("BAR_1", "Scanned Coke 1L", 100.0)
            "987654321" -> QuickGridItem("BAR_2", "Scanned Lays 50g", 50.0)
            else -> QuickGridItem("UNK_${barcode.hashCode()}", "Unknown Item", 0.0)
        }
        addItemToCart(item)
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // Dummy data for testing M4.2 Quick Access Grid
    fun getQuickGridItems() = listOf(
        QuickGridItem("ITEM1", "Loose Apples", 150.0),
        QuickGridItem("ITEM2", "Milk 1L", 250.0),
        QuickGridItem("ITEM3", "Bread", 120.0),
        QuickGridItem("ITEM4", "Eggs (Dozen)", 400.0)
    )
}

data class QuickGridItem(val id: String, val name: String, val price: Double)
