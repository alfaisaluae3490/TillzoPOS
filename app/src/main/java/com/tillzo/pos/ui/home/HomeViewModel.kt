package com.tillzo.pos.ui.home

import androidx.lifecycle.viewModelScope
import com.tillzo.pos.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.work.WorkInfo
import com.tillzo.pos.data.sync.SyncOrchestrator
import javax.inject.Inject

/**
 * HomeScreen state — immutable snapshot of everything shown on the POS home screen.
 */
enum class SyncStatus {
    IDLE,
    RUNNING,
    SUCCESS,
    FAILED
}

data class HomeUiState(
    val displayValue: String = "0",        // Giant numpad display
    val cartTotal: Double = 0.0,           // Running cart total
    val cartItemCount: Int = 0,            // Number of items in cart
    val quickGridItems: List<QuickGridItem> = emptyList(), // Configurable pinned items
    val isLoading: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val syncMessage: String? = null
)

data class QuickGridItem(
    val id: String,
    val name: String,
    val pricePerKg: Double,
    val emoji: String = "🛒"
)

/**
 * HomeViewModel — drives the POS home screen.
 *
 * Architecture Law: No Room/Retrofit imports here.
 * Future: inject UseCases for cart management, quick grid loading.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator
) : BaseViewModel<HomeUiState>(HomeUiState()) {

    init {
        viewModelScope.launch {
            syncOrchestrator.getManualSyncWorkInfo().collect { workInfoList ->
                val workInfo = workInfoList.firstOrNull() ?: return@collect
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        updateState(uiState.value.copy(
                            syncStatus = SyncStatus.RUNNING,
                            syncMessage = "Sync in progress..."
                        ))
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        updateState(uiState.value.copy(
                            syncStatus = SyncStatus.SUCCESS,
                            syncMessage = "Sync Completed Successfully!"
                        ))
                        delay(3000)
                        updateState(uiState.value.copy(syncStatus = SyncStatus.IDLE, syncMessage = null))
                    }
                    WorkInfo.State.FAILED -> {
                        updateState(uiState.value.copy(
                            syncStatus = SyncStatus.FAILED,
                            syncMessage = "Sync Failed. Please check connection."
                        ))
                        delay(3000)
                        updateState(uiState.value.copy(syncStatus = SyncStatus.IDLE, syncMessage = null))
                    }
                    else -> {
                        updateState(uiState.value.copy(
                            syncStatus = SyncStatus.IDLE,
                            syncMessage = null
                        ))
                    }
                }
            }
        }
    }

    // Numpad digit buffer
    private val _inputBuffer = StringBuilder("0")

    // Quick grid — placeholder items for M1. Real data wired in M4/M6.
    private val _quickGridItems = MutableStateFlow(
        listOf(
            QuickGridItem("1", "Chicken", 650.0, "🐔"),
            QuickGridItem("2", "Mutton", 1800.0, "🐑"),
            QuickGridItem("3", "Beef", 900.0, "🥩"),
            QuickGridItem("4", "Fish", 500.0, "🐟"),
            QuickGridItem("5", "Doodh", 180.0, "🥛"),
            QuickGridItem("6", "Anda", 20.0, "🥚"),
            QuickGridItem("7", "Roti", 15.0, "🫓"),
            QuickGridItem("8", "Custom", 0.0, "➕")
        )
    )
    val quickGridItems: StateFlow<List<QuickGridItem>> = _quickGridItems.asStateFlow()

    /** Called when a numpad digit button is pressed. */
    fun onDigitPressed(digit: String) {
        if (_inputBuffer.toString() == "0") {
            _inputBuffer.clear()
        }
        if (_inputBuffer.length < 10) { // prevent overflow
            _inputBuffer.append(digit)
        }
        updateState(uiState.value.copy(displayValue = _inputBuffer.toString()))
    }

    /** Decimal point for weight entry (e.g. 1.5 kg). */
    fun onDecimalPressed() {
        if (!_inputBuffer.contains('.')) {
            if (_inputBuffer.isEmpty()) _inputBuffer.append("0")
            _inputBuffer.append('.')
        }
        updateState(uiState.value.copy(displayValue = _inputBuffer.toString()))
    }

    /** Backspace — remove last character from numpad display. */
    fun onBackspacePressed() {
        if (_inputBuffer.isNotEmpty()) {
            _inputBuffer.deleteCharAt(_inputBuffer.length - 1)
        }
        if (_inputBuffer.isEmpty()) _inputBuffer.append("0")
        updateState(uiState.value.copy(displayValue = _inputBuffer.toString()))
    }

    /** Clear all — reset numpad. */
    fun onClearPressed() {
        _inputBuffer.clear()
        _inputBuffer.append("0")
        updateState(uiState.value.copy(displayValue = "0"))
    }

    /** Quick Grid item tapped — adds item × entered quantity to cart. */
    fun onQuickGridItemTapped(item: QuickGridItem) {
        val quantity = _inputBuffer.toString().toDoubleOrNull() ?: 1.0
        val lineTotal = item.pricePerKg * quantity
        val newTotal = uiState.value.cartTotal + lineTotal
        val newCount = uiState.value.cartItemCount + 1
        onClearPressed()
        updateState(
            uiState.value.copy(
                cartTotal = newTotal,
                cartItemCount = newCount,
                displayValue = "0"
            )
        )
    }

    /** Complete payment — in M4 this will open full checkout flow. */
    fun onPayPressed() {
        viewModelScope.launch {
            // TODO (M4): Navigate to checkout screen with cart data
            // For now just reset cart
            updateState(HomeUiState())
            _inputBuffer.clear()
            _inputBuffer.append("0")
        }
    }

    /** Manually triggers an immediate background data sync. */
    fun forceSync() {
        syncOrchestrator.triggerManualSync()
    }
}
