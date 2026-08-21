package com.tillzo.pos.ui.store.options.zreport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.SyncLogDao
import com.tillzo.pos.data.local.dao.TillSessionDao
import com.tillzo.pos.data.local.entity.TillSessionEntity
import com.tillzo.pos.domain.repository.SaleRepository
import com.tillzo.pos.domain.repository.StoreRepository
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.utils.printer.EscPosPrinter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.firstOrNull
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
    private val escPosPrinter: EscPosPrinter,
    private val appSetupPrefs: AppSetupPrefs
) : ViewModel() {
    // FIX (2026-08-06): currency from settings (USA-friendly default)
    private val currencySymbol get() = appSetupPrefs.currencySymbol.ifBlank { "$" }

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
            // FIX (2026-08-06): was "last 24h" (demo logic) — included yesterday's
            // same-hour sales. Now proper calendar day (local midnight).
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val startTime = cal.timeInMillis
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
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val startTime = cal.timeInMillis
            val endTime = System.currentTimeMillis()
            
            storeRepository.getExpensesBetweenDates(startTime, endTime).collect { expenses ->
                val expTotal = expenses.sumOf { it.amount }
                _totalExpensesToday.value = expTotal
                _netCashDrawer.value = _totalSalesToday.value - expTotal
            }
        }

        viewModelScope.launch {
            val tillPending = tillSessionDao.getPendingSyncCount()
            _pendingSyncCount.value = tillPending
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

    fun executeDayClose(physicalCashCount: Double) {
        if (_pendingSyncCount.value > 0) {
            _reportStatus.value = "Error: Cannot close day. ${_pendingSyncCount.value} items pending sync."
            return
        }

        viewModelScope.launch {
            val session = activeSession.value
            if (session != null) {
                val variance = physicalCashCount - session.expectedCash
                tillSessionDao.reconcileSession(
                    sessionId = session.sessionId,
                    physicalCashCount = physicalCashCount,
                    closedAt = System.currentTimeMillis()
                )
            }

            _reportStatus.value = "Day Closed Successfully! Printing Z-Report..."
            try {
                val zReportContent = buildString {
                    append("        == Z-REPORT ==        \n")
                    append("================================\n")
                    append("Date: ${java.util.Date()}\n")
                    append("POS ID: ${appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERMINAL_1" }}\n")
                    append("--------------------------------\n")
                    append("Cash Sales:   $currencySymbol ${String.format("%.2f", _totalCashSales.value)}\n")
                    append("Card Sales:   $currencySymbol ${String.format("%.2f", _totalCardSales.value)}\n")
                    append("Wallet Sales: $currencySymbol ${String.format("%.2f", _totalWalletSales.value)}\n")
                    append("Credit Sales: $currencySymbol ${String.format("%.2f", _totalUdhaarSales.value)}\n")
                    append("--------------------------------\n")
                    append("Gross Sales:  $currencySymbol ${String.format("%.2f", _totalSalesToday.value)}\n")
                    append("Expenses:     $currencySymbol ${String.format("%.2f", _totalExpensesToday.value)}\n")
                    append("--------------------------------\n")
                    append("Expected Cash: $currencySymbol ${String.format("%.2f", session?.expectedCash ?: 0.0)}\n")
                    append("Physical Cash: $currencySymbol ${String.format("%.2f", physicalCashCount)}\n")
                    val variance = physicalCashCount - (session?.expectedCash ?: 0.0)
                    val varianceLabel = if (variance >= 0) "OVERAGE" else "SHORTAGE"
                    append("Cash Variance ($varianceLabel): $currencySymbol ${String.format("%.2f", variance)}\n")
                    append("Status: RECONCILED\n")
                    append("================================\n")
                    append("NET IN DRAWER: $currencySymbol ${String.format("%.2f", _netCashDrawer.value)}\n")
                    append("================================\n")
                    append("    DAY CLOSED COMPLETELY    \n")
                }
                
                // FIX (2026-08-06): was TSPL label protocol for text (wrong —
                // labels use TSPL, receipts/reports use ESC/POS). Now ESC/POS.
                escPosPrinter.printViaBluetooth(appSetupPrefs.printerMac, zReportContent)

                // FIX (2026-08-06) M7.5: CSV auto-backup on Day Close — export today's
                // sales to Downloads as CSV (industry-standard data-export feature).
                exportDayCloseCsv()
            } catch (e: Exception) {
                _reportStatus.value = "Closed, but Print Failed: ${e.message}"
            }
        }
    }

    private fun exportDayCloseCsv() {
        try {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0); cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0); cal.set(java.util.Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis
            viewModelScope.launch {
                val sales = saleRepository.getAllSales().firstOrNull() ?: emptyList()
                val todays = sales.filter { it.timestamp >= dayStart }
                val sb = StringBuilder()
                sb.appendLine("invoice_id,timestamp,items_count,subtotal,tax,discount,total,payment_method")
                todays.forEach { s ->
                    sb.appendLine("\"${s.invoiceId}\",${s.timestamp},${s.items.size},${s.subtotal},${s.tax},${s.discount},${s.total},\"${s.paymentMethod}\"")
                }
                val fileName = "TillzoPOS_Sales_${java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(java.util.Date())}.csv"
                val dir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(dir, fileName)
                file.writeText(sb.toString())
                _reportStatus.value = "Day Closed + CSV exported: $fileName"
                android.util.Log.i("ZReportViewModel", "CSV exported: ${file.absolutePath}")
            }
        } catch (e: Exception) {
            android.util.Log.e("ZReportViewModel", "CSV export failed: ${e.message}")
        }
    }

    fun clearStatus() {
        _reportStatus.value = null
    }
}
