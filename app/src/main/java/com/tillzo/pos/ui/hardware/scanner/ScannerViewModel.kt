package com.tillzo.pos.ui.hardware.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.InventoryDao
import com.tillzo.pos.data.local.entity.InventoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ScannerState {
    object Idle : ScannerState()
    object Scanning : ScannerState()
    object Processing : ScannerState()
    data class Success(val product: InventoryEntity) : ScannerState()
    object NotFound : ScannerState()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val inventoryDao: InventoryDao
) : ViewModel() {

    // Scanner state
    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val scannerState: StateFlow<ScannerState> = _scannerState.asStateFlow()

    // Scan result
    private val _scannedProduct = MutableStateFlow<InventoryEntity?>(null)
    val scannedProduct: StateFlow<InventoryEntity?> = _scannedProduct.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Debounce control
    private var lastScanTime = 0L
    private val DEBOUNCE_MS = 1500L
    private var isProcessing = false

    // 30s idle timer
    private var idleJob: Job? = null

    fun onBarcodeDetected(barcodeValue: String) {
        val now = System.currentTimeMillis()
        if (now - lastScanTime < DEBOUNCE_MS) return
        if (isProcessing) return
        lastScanTime = now
        isProcessing = true

        viewModelScope.launch(Dispatchers.IO) {
            _scannerState.value = ScannerState.Processing
            resetIdleTimer()

            // Lookup product from Room DB (DEF-64: barcode_id + ItemGtins dono)
            val product = inventoryDao.getItemByBarcode(barcodeValue) ?: inventoryDao.getItemByGtin(barcodeValue)

            if (product != null) {
                _scannedProduct.value = product
                _scannerState.value = ScannerState.Success(product)
            } else {
                _errorMessage.value = "Product not found: $barcodeValue"
                _scannerState.value = ScannerState.NotFound
                delay(2000)
                _errorMessage.value = null
                _scannerState.value = ScannerState.Scanning
            }
            isProcessing = false
        }
    }

    fun startScanning() {
        _scannerState.value = ScannerState.Scanning
        resetIdleTimer()
    }

    fun stopScanning() {
        idleJob?.cancel()
        _scannerState.value = ScannerState.Idle
    }

    private fun resetIdleTimer() {
        idleJob?.cancel()
        idleJob = viewModelScope.launch {
            delay(30_000L) // 30 seconds idle timeout
            _scannerState.value = ScannerState.Idle // auto deactivate
        }
    }

    fun clearResult() {
        _scannedProduct.value = null
        _errorMessage.value = null
        // Reset state back to scanning after clear
        if (_scannerState.value is ScannerState.Success) {
            _scannerState.value = ScannerState.Scanning
        }
    }
}
