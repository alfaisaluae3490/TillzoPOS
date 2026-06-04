package com.tillzo.pos.ui.store.options.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.domain.model.Sale
import com.tillzo.pos.domain.repository.SaleRepository
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
    private val tsplPrinter: TsplPrinter
) : ViewModel() {

    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales = _sales.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _printStatus = MutableStateFlow<String?>(null)
    val printStatus = _printStatus.asStateFlow()

    private val _dateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val dateRange = _dateRange.asStateFlow()

    private var currentJob: Job? = null

    init {
        viewModelScope.launch {
            _dateRange.collectLatest { range ->
                loadSales(range, _searchQuery.value)
            }
        }
        viewModelScope.launch {
            _searchQuery.collectLatest { query ->
                loadSales(_dateRange.value, query)
            }
        }
    }

    private fun loadSales(range: Pair<Long, Long>?, query: String) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            val flow = if (range != null) {
                saleRepository.getSalesInRange(range.first, range.second)
            } else {
                saleRepository.getAllSales()
            }
            flow.collect { list ->
                var filtered = list
                if (query.isNotBlank()) {
                    filtered = list.filter { 
                        it.invoiceId.contains(query, ignoreCase = true) || 
                        it.systemRowId.contains(query, ignoreCase = true) 
                    }
                }
                _sales.value = filtered.sortedByDescending { it.timestamp }.take(100)
            }
        }
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
                // Construct basic duplicate receipt text
                val receiptText = buildString {
                    append("      ** DUPLICATE RECEIPT **      \n")
                    append("================================\n")
                    append("Invoice ID: ${sale.invoiceId.take(8)}\n")
                    append("Time: ${java.util.Date(sale.timestamp)}\n")
                    append("--------------------------------\n")
                    append("TOTAL: Rs ${String.format("%.2f", sale.total)}\n")
                    append("PAID VIA: ${sale.paymentMethod}\n")
                    if (sale.total < 0) {
                        append("Status: REFUNDED\n")
                    }
                    append("================================\n")
                }
                
                // Print command reusing the POS bluetooth method
                val printerMac = "00:00:00:00:00:00" // Hardcoded test MAC
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
