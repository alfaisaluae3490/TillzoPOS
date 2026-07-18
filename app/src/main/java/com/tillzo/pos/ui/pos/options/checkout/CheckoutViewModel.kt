package com.tillzo.pos.ui.pos.options.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.domain.model.CartItem
import com.tillzo.pos.domain.model.PaymentDetails
import com.tillzo.pos.domain.model.Sale
import com.tillzo.pos.domain.repository.SaleRepository
import com.tillzo.pos.utils.AppLogger
import com.tillzo.pos.utils.ReceiptGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Handles logic for M4.6 (Multi-Tender) and final Sale Commit.
 */
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    @ApplicationContext private val context: Context,
    private val appLogger: AppLogger
) : ViewModel() {

    private val _totalDue = MutableStateFlow(0.0)
    val totalDue: StateFlow<Double> = _totalDue

    private val _cashInput = MutableStateFlow("")
    val cashInput: StateFlow<String> = _cashInput

    private val _cardInput = MutableStateFlow("")
    val cardInput: StateFlow<String> = _cardInput

    private val _walletInput = MutableStateFlow("")
    val walletInput: StateFlow<String> = _walletInput

    private val _udhaarInput = MutableStateFlow("")
    val udhaarInput: StateFlow<String> = _udhaarInput

    // Passed from CasioScreen router param or shared state. Dummy load here.
    fun loadCartData(total: Double) {
        _totalDue.value = total
        // Auto-fill cash default
        _cashInput.value = total.toString()
    }

    fun updateTender(type: String, amount: String) {
        when (type) {
            "CASH" -> _cashInput.value = amount
            "CARD" -> _cardInput.value = amount
            "WALLET" -> _walletInput.value = amount
            "UDHAAR" -> _udhaarInput.value = amount
        }
    }

    fun completeCheckout(
        printEnabled: Boolean, 
        whatsappNumber: String,
        onSuccess: () -> Unit
    ) {
        val cash = _cashInput.value.toDoubleOrNull() ?: 0.0
        val card = _cardInput.value.toDoubleOrNull() ?: 0.0
        val wallet = _walletInput.value.toDoubleOrNull() ?: 0.0
        val udhaar = _udhaarInput.value.toDoubleOrNull() ?: 0.0

        val totalPaid = cash + card + wallet + udhaar
        if (totalPaid < _totalDue.value) {
            appLogger.logWarn("UI_CLICK", "Checkout failed: total paid ($totalPaid) < due (${_totalDue.value})")
            return // Basic validation
        }

        val paymentMethod = if (cash > 0 && card == 0.0 && wallet == 0.0 && udhaar == 0.0) "Cash" else "Split"

        val sale = Sale(
            cashierId = "User_123", // Later pulled from AuthSession
            items = emptyList(), // In reality, passed via SavedStateHandle or SharedViewModel
            subtotal = _totalDue.value,
            tax = 0.0,
            total = _totalDue.value,
            paymentMethod = paymentMethod,
            paymentSplit = PaymentDetails(cash, card, wallet, udhaar)
        )

        appLogger.logInfo("UI_CLICK", "Checkout complete: method=$paymentMethod, total=${_totalDue.value}, print=$printEnabled, whatsapp=${whatsappNumber.isNotBlank()}")

        viewModelScope.launch {
            saleRepository.processCheckout(sale)
            
            // M4.7 Hardware Print Toggle (Mock triggering ESC/POS command here)
            if (printEnabled) {
                appLogger.logInfo("UI_CLICK", "Print receipt triggered for ${sale.invoiceId}")
            }
            
            // M4.8 WhatsApp Receipt Intent
            if (whatsappNumber.isNotBlank()) {
                appLogger.logInfo("UI_CLICK", "WhatsApp receipt sent to $whatsappNumber")
                ReceiptGenerator.sendWhatsAppReceipt(context, whatsappNumber, sale)
            }
            
            onSuccess()
        }
    }
}
