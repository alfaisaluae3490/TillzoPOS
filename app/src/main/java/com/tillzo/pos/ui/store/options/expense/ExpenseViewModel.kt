package com.tillzo.pos.ui.store.options.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.TillSessionDao
import com.tillzo.pos.data.local.entity.ExpenseEntity
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val tillSessionDao: TillSessionDao,
    private val appSetupPrefs: AppSetupPrefs
) : ViewModel() {

    private val _expenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val expenses = _expenses.asStateFlow()

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            // Load last 7 days of expenses for display
            val startTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            storeRepository.getExpensesBetweenDates(startTime, System.currentTimeMillis())
                .catch { /* Handle error */ }
                .collect { _expenses.value = it }
        }
    }

    fun addExpense(category: String, amount: Double, description: String) {
        if (amount <= 0 || category.isBlank() || description.isBlank()) return
        
        viewModelScope.launch {
            val expense = ExpenseEntity(
                category = category,
                amount = amount,
                description = description,
                timestamp = System.currentTimeMillis(),
                logged_by_user_id = appSetupPrefs.userEmail.ifBlank { "cashier" },
                pos_terminal_id = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }
            )
            storeRepository.insertExpense(expense)

            val posTerminalId = appSetupPrefs.spreadsheetId.take(20)
            try {
                val session = tillSessionDao.getOpenSession(posTerminalId)
                if (session != null) {
                    tillSessionDao.deductExpenseFromSession(session.sessionId, amount)
                }
            } catch (_: Exception) {
                // Non-fatal: expense already saved, no open session to deduct from
            }
        }
    }

    fun updateExpense(id: String, category: String, amount: Double, description: String) {
        if (amount <= 0 || category.isBlank() || description.isBlank()) return
        viewModelScope.launch {
            storeRepository.updateExpense(id, category, amount, description, System.currentTimeMillis())
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            storeRepository.softDeleteExpense(id, System.currentTimeMillis())
        }
    }
}
