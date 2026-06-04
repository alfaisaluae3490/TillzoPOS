package com.tillzo.pos.ui.hardware.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.InventoryDao
import com.tillzo.pos.data.local.entity.InventoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InlineScannerViewModel @Inject constructor(
    private val inventoryDao: InventoryDao
) : ViewModel() {

    // Camera active/sleep state
    private val _isCameraActive = MutableStateFlow(false)
    val isCameraActive: StateFlow<Boolean> = _isCameraActive.asStateFlow()

    // Scan result state
    sealed class ScanEvent {
        data class ProductFound(val product: InventoryEntity) : ScanEvent()
        data class ProductNotFound(val barcode: String) : ScanEvent()
    }
    private val _scanEvent = MutableSharedFlow<ScanEvent>()
    val scanEvent: SharedFlow<ScanEvent> = _scanEvent.asSharedFlow()

    // Debounce + processing guard
    private var lastScanTime = 0L
    private val DEBOUNCE_MS = 1500L
    private var isProcessing = false

    // Sleep timer job
    private var sleepTimerJob: Job? = null
    private val SLEEP_TIMEOUT_MS = 4 * 60 * 1000L // 4 minutes

    fun activateCamera() {
        _isCameraActive.value = true
        resetSleepTimer()
    }

    fun deactivateCamera() {
        sleepTimerJob?.cancel()
        _isCameraActive.value = false
        isProcessing = false
    }

    fun onBarcodeDetected(barcodeValue: String) {
        val now = System.currentTimeMillis()
        if (now - lastScanTime < DEBOUNCE_MS) return
        if (isProcessing) return
        if (!_isCameraActive.value) return

        lastScanTime = now
        isProcessing = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                resetSleepTimer() // reset 4min timer on every scan attempt

                val product = inventoryDao.getItemByBarcode(barcodeValue)

                if (product != null) {
                    _scanEvent.emit(ScanEvent.ProductFound(product))
                } else {
                    _scanEvent.emit(ScanEvent.ProductNotFound(barcodeValue))
                }
            } finally {
                isProcessing = false
            }
        }
    }

    private fun resetSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = viewModelScope.launch {
            delay(SLEEP_TIMEOUT_MS)
            _isCameraActive.value = false // auto sleep after 4 minutes
        }
    }

    override fun onCleared() {
        super.onCleared()
        sleepTimerJob?.cancel()
    }
}
