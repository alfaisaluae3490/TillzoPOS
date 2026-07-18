package com.tillzo.pos.ui.store.options.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.domain.model.Sale
import com.tillzo.pos.domain.repository.SaleRepository
import com.tillzo.pos.domain.usecase.ReprintReceiptUseCase
import com.tillzo.pos.utils.printer.TsplPrinter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val tsplPrinter: TsplPrinter,
    private val appSetupPrefs: AppSetupPrefs,
    private val reprintReceiptUseCase: ReprintReceiptUseCase
) : ViewModel() {

    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales = _sales.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _printStatus = MutableStateFlow<String?>(null)
    val printStatus = _printStatus.asStateFlow()

    private val _dateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val dateRange = _dateRange.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore = _hasMore.asStateFlow()

    private var currentJob: Job? = null
    private var currentPage = 0
    private val pageSize = 30

    init {
        viewModelScope.launch {
            _dateRange.collectLatest { range ->
                currentPage = 0
                _hasMore.value = true
                _sales.value = emptyList()
                loadSales(range, _searchQuery.value, reset = true)
            }
        }
        viewModelScope.launch {
            _searchQuery.collectLatest { query ->
                currentPage = 0
                _hasMore.value = true
                _sales.value = emptyList()
                loadSales(_dateRange.value, query, reset = true)
            }
        }
    }

    private fun loadSales(range: Pair<Long, Long>?, query: String, reset: Boolean = false) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            val offset = if (reset) 0 else currentPage * pageSize
            val flow = if (range != null) {
                saleRepository.getSalesInRangePaged(range.first, range.second, pageSize, offset)
            } else {
                saleRepository.getSalesPaged(pageSize, offset)
            }
            flow.collect { list ->
                var filtered = list
                if (query.isNotBlank()) {
                    filtered = list.filter { 
                        it.invoiceId.contains(query, ignoreCase = true) || 
                        it.systemRowId.contains(query, ignoreCase = true) 
                    }
                }
                if (reset) {
                    _sales.value = filtered
                    currentPage = 1
                } else {
                    _sales.value = _sales.value + filtered
                    currentPage++
                }
                _hasMore.value = filtered.size >= pageSize
                _isLoadingMore.value = false
            }
        }
    }

    fun loadMore() {
        if (_isLoadingMore.value || !_hasMore.value) return
        _isLoadingMore.value = true
        loadSales(_dateRange.value, _searchQuery.value)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setDateRange(range: Pair<Long, Long>?) {
        _dateRange.value = range
    }

    fun printDuplicateReceipt(sale: Sale) {
        viewModelScope.launch {
            _printStatus.value = "Printing Duplicate Receipt..."
            try {
                val saleForPrint = reprintReceiptUseCase(sale.invoiceId) ?: sale

                val receiptText = buildString {
                    append("      ** DUPLICATE RECEIPT **      \n")
                    append("================================\n")
                    append("Invoice ID: ${saleForPrint.invoiceId.take(8)}\n")
                    append("Time: ${java.util.Date(saleForPrint.timestamp)}\n")
                    append("--------------------------------\n")
                    append("TOTAL: Rs ${String.format("%.2f", saleForPrint.total)}\n")
                    append("PAID VIA: ${saleForPrint.paymentMethod}\n")
                    if (saleForPrint.total < 0) {
                        append("Status: REFUNDED\n")
                    }
                    append("================================\n")
                }
                
                val printerMac = appSetupPrefs.printerMac
                val success = tsplPrinter.printBarcodeLabel(printerMac, "Tillzo POS", receiptText)
                
                if (success) {
                    _printStatus.value = "Duplicate Printed Successfully."
                } else {
                    _printStatus.value = "Print Failed. Check Printer connection."
                }
            } catch (e: Exception) {
                _printStatus.value = "Error: ${e.message}"
            }
        }
    }
    
    fun clearPrintStatus() {
        _printStatus.value = null
    }
}
