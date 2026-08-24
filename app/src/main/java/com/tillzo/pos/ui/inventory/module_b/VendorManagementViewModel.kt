package com.tillzo.pos.ui.inventory.module_b

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.VendorDao
import com.tillzo.pos.data.local.entity.VendorEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VendorManagementViewModel @Inject constructor(
    private val vendorDao: VendorDao,
    private val vendorPaymentRepository: com.tillzo.pos.domain.repository.VendorPaymentRepository
) : ViewModel() {

    companion object {
        private const val TAG = "VendorVM"
    }

    val vendors: StateFlow<List<VendorEntity>> = vendorDao.getAllVendors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vendorBalances: StateFlow<Map<String, Double>> = vendorPaymentRepository.getAllVendorBalances()
        .map { list -> list.associate { it.vendorId to it.netBalance } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _searchResults = MutableStateFlow<List<VendorEntity>>(emptyList())
    val searchResults: StateFlow<List<VendorEntity>> = _searchResults.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private val _errorChannel = Channel<String>(Channel.BUFFERED)
    val errorChannel: Flow<String> = _errorChannel.receiveAsFlow()

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    fun getPaymentsForVendor(vendorId: String): Flow<List<com.tillzo.pos.data.local.entity.VendorPaymentEntity>> {
        return vendorPaymentRepository.getPaymentsForVendor(vendorId)
    }

    fun recordVendorPayment(
        vendorId: String,
        vendorName: String,
        amount: Double,
        paymentMethod: String,
        note: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (amount <= 0.0) {
                    _errorChannel.send("Payment amount must be greater than 0")
                    return@launch
                }
                vendorPaymentRepository.recordPayment(
                    vendorId = vendorId,
                    vendorName = vendorName,
                    amount = amount,
                    paymentMethod = paymentMethod,
                    paidBy = "Admin",
                    note = note
                )
            } catch (e: Exception) {
                Log.e(TAG, "Record payment error: ${e.message}", e)
                _errorChannel.send(e.localizedMessage ?: "Failed to record payment")
            }
        }
    }

    fun recordDebitNote(
        vendorId: String,
        vendorName: String,
        amount: Double,
        reason: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (amount <= 0.0) {
                    _errorChannel.send("Debit note amount must be greater than 0")
                    return@launch
                }
                vendorPaymentRepository.recordDebitNote(
                    vendorId = vendorId,
                    vendorName = vendorName,
                    amount = amount,
                    reason = reason,
                    paidBy = "Admin"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Debit note error: ${e.message}", e)
                _errorChannel.send(e.localizedMessage ?: "Failed to record debit note")
            }
        }
    }

    fun recordCreditNote(
        vendorId: String,
        vendorName: String,
        amount: Double,
        reason: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (amount <= 0.0) {
                    _errorChannel.send("Credit note amount must be greater than 0")
                    return@launch
                }
                vendorPaymentRepository.recordCreditNote(
                    vendorId = vendorId,
                    vendorName = vendorName,
                    amount = amount,
                    reason = reason,
                    paidBy = "Admin"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Credit note error: ${e.message}", e)
                _errorChannel.send(e.localizedMessage ?: "Failed to record credit note")
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _searchResults.value = if (query.isBlank()) emptyList()
                else vendorDao.searchVendors(query)
            } catch (e: Exception) {
                _errorChannel.send(e.localizedMessage ?: "Failed to search vendors")
            }
        }
    }

    fun save(
        existing: VendorEntity?,
        name: String, phone: String, whatsapp: String, email: String, address: String,
        city: String, creditLimit: Double, isActive: Boolean = true
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _saveState.value = SaveState.Saving
            // FIX (2026-08-23, DEF-108): blank name / negative credit limit pehle
            // save ho jate the — sheet par naam-less vendor + negative limit.
            // Ab reject/clamp.
            if (name.isBlank()) {
                _saveState.value = SaveState.Error("Vendor name is required")
                return@launch
            }
            val safeCreditLimit = creditLimit.coerceAtLeast(0.0)
            try {
                val now = System.currentTimeMillis()
                val targetVendorId = existing?.vendorId ?: UUID.randomUUID().toString()

                if (existing == null) {
                    vendorDao.insertVendor(
                        VendorEntity(
                            vendorId = targetVendorId,
                            isActive = isActive,
                            name = name.trim(), phone = phone.trim(),
                            whatsapp = whatsapp.trim(), email = email.trim(),
                            address = address.trim(), city = city.trim(),
                            creditLimit = safeCreditLimit,
                            syncStatus = "pending",
                            createdAt = now, updatedAt = now
                        )
                    )
                } else {
                    vendorDao.updateVendor(
                        existing.copy(
                            isActive = isActive,
                            name = name.trim(), phone = phone.trim(),
                            whatsapp = whatsapp.trim(), email = email.trim(),
                            address = address.trim(), city = city.trim(),
                            creditLimit = safeCreditLimit,
                            syncStatus = "pending",
                            updatedAt = now
                        )
                    )
                }
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                Log.e(TAG, "Save error: ${e.message}", e)
                _saveState.value = SaveState.Error(e.localizedMessage ?: "Failed to save vendor")
            }
        }
    }

    fun deleteVendor(vendorId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                vendorDao.softDeleteVendor(vendorId, System.currentTimeMillis())
            } catch (e: Exception) {
                Log.e(TAG, "Delete error: ${e.message}", e)
                _errorChannel.send(e.localizedMessage ?: "Failed to delete vendor")
            }
        }
    }
}

sealed class SaveState {
    data object Idle : SaveState()
    data object Saving : SaveState()
    data object Success : SaveState()
    data class Error(val message: String) : SaveState()
}
