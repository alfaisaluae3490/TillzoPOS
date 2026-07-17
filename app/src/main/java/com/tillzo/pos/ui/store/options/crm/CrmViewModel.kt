package com.tillzo.pos.ui.store.options.crm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.entity.CustomerEntity
import com.tillzo.pos.data.local.entity.KhataEventEntity
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class CrmViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val appSetupPrefs: AppSetupPrefs
) : ViewModel() {

    private val _customers = MutableStateFlow<List<CustomerEntity>>(emptyList())
    val customers: StateFlow<List<CustomerEntity>> = _customers.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Khata Aggregations for selected customer
    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer = _selectedCustomer.asStateFlow()
    
    private val _totalUdhaar = MutableStateFlow(0.0)
    val totalUdhaar = _totalUdhaar.asStateFlow()

    private val _totalJama = MutableStateFlow(0.0)
    val totalJama = _totalJama.asStateFlow()

    private val _baqaya = MutableStateFlow(0.0)
    val baqaya = _baqaya.asStateFlow()

    init {
        loadCustomers()
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            storeRepository.getAllCustomers().catch { }.collect {
                _customers.value = it
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                loadCustomers()
            } else {
                storeRepository.searchCustomers(query).catch { }.collect {
                    _customers.value = it
                }
            }
        }
    }

    fun selectCustomer(customer: CustomerEntity) {
        _selectedCustomer.value = customer
        viewModelScope.launch {
            storeRepository.getCustomerUdhaar(customer.system_row_id).collect { _totalUdhaar.value = it }
        }
        viewModelScope.launch {
            storeRepository.getCustomerJama(customer.system_row_id).collect { _totalJama.value = it }
        }
        viewModelScope.launch {
            storeRepository.getCustomerBaqaya(customer.system_row_id).collect { _baqaya.value = it }
        }
    }

    fun getEventsForCustomer(customerId: String): Flow<List<KhataEventEntity>> =
        storeRepository.getKhataEventsForCustomer(customerId)

    fun saveCustomer(
        existing: CustomerEntity?,
        name: String, phone: String,
        whatsapp: String, email: String, address: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val termId = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }
            val customer = existing?.copy(
                name = name, phone = phone,
                whatsapp = whatsapp, email = email, address = address,
                sync_status = "pending",
                updated_at = System.currentTimeMillis()
            ) ?: CustomerEntity(
                system_row_id = java.util.UUID.randomUUID().toString(),
                name = name, phone = phone,
                whatsapp = whatsapp, email = email, address = address,
                sync_status = "pending",
                created_at = System.currentTimeMillis(),
                updated_at = System.currentTimeMillis(),
                pos_terminal_id = termId
            )
            storeRepository.insertCustomer(customer)
        }
    }

    fun addKhataEvent(amount: Double, type: String, note: String) {
        val custId = _selectedCustomer.value?.system_row_id ?: return
        if (amount <= 0) return
        viewModelScope.launch {
            val termId = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }
            val actualAmount = amount
            
            val event = KhataEventEntity(
                customer_id = custId,
                event_type = type,
                amount = actualAmount,
                note = note,
                pos_terminal_id = termId
            )
            storeRepository.insertKhataEvent(event)
        }
    }
}
