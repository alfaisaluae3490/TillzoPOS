package com.tillzo.pos.ui.store.options.zreport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.SyncLogDao
import com.tillzo.pos.data.local.dao.TillSessionDao
import com.tillzo.pos.data.local.entity.TillSessionEntity
import com.tillzo.pos.domain.repository.SaleRepository
import com.tillzo.pos.domain.repository.StoreRepository
import com.tillzo.pos.utils.printer.TsplPrinter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ZReportViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val storeRepository: StoreRepository,
    private val syncLogDao: SyncLogDao,
    private val tillSessionDao: TillSessionDao,
    private val tsplPrinter: TsplPrinter
) : ViewModel() {

    private val _totalSalesToday = MutableStateFlow(0.0)
    val totalSalesToday = _totalSalesToday.asStateFlow()

    private val _totalExpensesToday = MutableStateFlow(0.0)
    val totalExpensesToday = _totalExpensesToday.asStateFlow()

    private val _netCashDrawer = MutableStateFlow(0.0)
    val netCashDrawer = _netCashDrawer.asStateFlow()

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount = _pendingSyncCount.asStateFlow()

    private val _reportStatus = MutableStateFlow<String?>(null)
    val reportStatus = _reportStatus.asStateFlow()

    // Payment breakdown from active till session
    private val _totalCashSales = MutableStateFlow(0.0)
    val totalCashSales = _totalCashSales.asStateFlow()

    private val _totalCardSales = MutableStateFlow(0.0)
    val totalCardSales = _totalCardSales.asStateFlow()

    private val _totalWalletSales = MutableStateFlow(0.0)
    val totalWalletSales = _totalWalletSales.asStateFlow()

    private val _totalUdhaarSales = MutableStateFlow(0.0)
    val totalUdhaarSales = _totalUdhaarSales.asStateFlow()

    // Till / Shift data
    private val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

    val activeSession = tillSessionDao.getOpenSessionFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val allSessionsToday = tillSessionDao.getSessionsForDate(todayDate)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadDailyMetrics()
    }

    private fun loadDailyMetrics() {
        viewModelScope.launch {
            // Assume "today" logic for demo purposes (last 24h)
            val startTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            val endTime = System.currentTimeMillis()

            var salesTotal = 0.0
            saleRepository.getAllSales().collect { sales ->
                val todaysSales = sales.filter { it.timestamp in startTime..endTime }
                salesTotal = todaysSales.sumOf { it.total }
                _totalSalesToday.value = salesTotal
                
                // Real-time combine (gross simplistic)
                _netCashDrawer.value = salesTotal - _totalExpensesToday.value
            }
        }
        
        viewModelScope.launch {
            val startTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            val endTime = System.currentTimeMillis()
            
            storeRepository.getExpensesBetweenDates(startTime, endTime).collect { expenses ->
                val expTotal = expenses.sumOf { it.amount }
                _totalExpensesToday.value = expTotal
                _netCashDrawer.value = _totalSalesToday.value - expTotal
            }
        }

        viewModelScope.launch {
            // Simulate reading sync logic (M2 artifact)
            _pendingSyncCount.value = 0 // In real impl: syncLogDao.getPendingRowCountsSum() 
        }

        // Populate payment breakdown from active till session
        viewModelScope.launch {
            activeSession.collect { session ->
                if (session != null) {
                    _totalCashSales.value = session.totalCashSales
                    _totalCardSales.value = session.totalCardSales
                    _totalWalletSales.value = session.totalWalletSales
                    _totalUdhaarSales.value = session.totalUdhaarSales
                }
            }
        }
    }

    fun executeDayClose() {
        if (_pendingSyncCount.value > 0) {
            _reportStatus.value = "Error: Cannot close day. ${_pendingSyncCount.value} items pending sync."
            return
        }

        viewModelScope.launch {
            // Close the active till session in the database
            val session = activeSession.value
            if (session != null) {
                tillSessionDao.closeSession(
                    sessionId = session.sessionId,
                    closingCash = session.expectedCash,
                    netCash = 0.0,
                    closedAt = System.currentTimeMillis()
                )
            }

            _reportStatus.value = "Day Closed Successfully! Printing Z-Report..."
            try {
                val zReportContent = buildString {
                    append("        == Z-REPORT ==        \n")
                    append("================================\n")
                    append("Date: ${java.util.Date()}\n")
                    append("POS ID: TERMINAL_1\n")
                    append("--------------------------------\n")
                    append("Cash Sales:   Rs ${String.format("%.2f", _totalCashSales.value)}\n")
                    append("Card Sales:   Rs ${String.format("%.2f", _totalCardSales.value)}\n")
                    append("Wallet Sales: Rs ${String.format("%.2f", _totalWalletSales.value)}\n")
                    append("Udhaar Sales: Rs ${String.format("%.2f", _totalUdhaarSales.value)}\n")
                    append("--------------------------------\n")
                    append("Gross Sales:  Rs ${String.format("%.2f", _totalSalesToday.value)}\n")
                    append("Expenses:     Rs ${String.format("%.2f", _totalExpensesToday.value)}\n")
                    append("--------------------------------\n")
                    append("NET IN DRAWER: Rs ${String.format("%.2f", _netCashDrawer.value)}\n")
                    append("================================\n")
                    append("    DAY CLOSED COMPLETELY    \n")
                }
                
                tsplPrinter.printBarcodeLabel("00:00:00:00:00:00", "Z-Report", zReportContent)
            } catch (e: Exception) {
                _reportStatus.value = "Closed, but Print Failed: ${e.message}"
            }
        }
    }

    fun clearStatus() {
        _reportStatus.value = null
    }
}
