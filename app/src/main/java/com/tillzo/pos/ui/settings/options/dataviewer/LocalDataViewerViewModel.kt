package com.tillzo.pos.ui.settings.options.dataviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.AppDatabase
import com.tillzo.pos.data.local.entity.CustomerEntity
import com.tillzo.pos.data.local.entity.ExpenseEntity
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.data.local.entity.KhataEventEntity
import com.tillzo.pos.data.local.entity.SaleEntity
import com.tillzo.pos.data.local.entity.TillSessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Local Data Viewer (FIX 2026-08-06 — Faisal's requirement: "phone stored data
 * user dekh bhi sakay"). Shows everything stored in the on-device Room DB.
 */
@HiltViewModel
class LocalDataViewerViewModel @Inject constructor(
    appDatabase: AppDatabase
) : ViewModel() {

    val inventory: StateFlow<List<InventoryEntity>> = appDatabase.inventoryDao().getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<SaleEntity>> = appDatabase.saleDao().getAllSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = appDatabase.customerDao().getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = appDatabase.expenseDao().getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val khataEvents: StateFlow<List<KhataEventEntity>> = kotlinx.coroutines.flow.flow {
        emit(appDatabase.khataEventDao().getAllKhataEvents())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tillSessions: StateFlow<List<TillSessionEntity>> = appDatabase.tillSessionDao().getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<Map<String, Int>> = combine(
        listOf(inventory, sales, customers, expenses, khataEvents, tillSessions)
    ) { values ->
        mapOf(
            "Inventory Items" to (values[0] as List<*>).size,
            "Sales" to (values[1] as List<*>).size,
            "Customers" to (values[2] as List<*>).size,
            "Expenses" to (values[3] as List<*>).size,
            "Khata Events" to (values[4] as List<*>).size,
            "Till Sessions" to (values[5] as List<*>).size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}
