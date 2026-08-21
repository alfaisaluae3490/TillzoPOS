package com.tillzo.pos.ui.store.options.verifyqr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.SaleDao
import com.tillzo.pos.data.local.entity.SaleEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receipt QR verification (FIX 2026-08-06 — industry-standard anti-fraud workflow).
 * Scans a receipt's QR (which encodes the invoiceId) and looks up the sale locally.
 */
@HiltViewModel
class VerifyQrViewModel @Inject constructor(
    private val saleDao: SaleDao
) : ViewModel() {

    sealed class VerifyState {
        object Idle : VerifyState()
        object Scanning : VerifyState()
        data class Verified(val sale: SaleEntity) : VerifyState()
        data class NotFound(val qrValue: String) : VerifyState()
        data class Error(val message: String) : VerifyState()
    }

    private val _state = MutableStateFlow<VerifyState>(VerifyState.Idle)
    val state: StateFlow<VerifyState> = _state.asStateFlow()

    private val _rawQr = MutableStateFlow("")
    val rawQr: StateFlow<String> = _rawQr.asStateFlow()

    fun reset() { _state.value = VerifyState.Idle; _rawQr.value = "" }

    fun onQrDetected(qrValue: String) {
        if (qrValue.isBlank()) return
        _rawQr.value = qrValue
        _state.value = VerifyState.Scanning
        val invoiceId = qrValue.trim()
        viewModelScope.launch {
            val sale = saleDao.getSaleByInvoiceId(invoiceId)
                ?: saleDao.getSaleById(invoiceId)
            if (sale != null) {
                _state.value = VerifyState.Verified(sale)
            } else {
                _state.value = VerifyState.NotFound(qrValue)
            }
        }
    }
}
