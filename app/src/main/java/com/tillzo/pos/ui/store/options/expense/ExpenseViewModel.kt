package com.tillzo.pos.ui.store.options.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.ExpenseDao
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
    private val expenseDao: ExpenseDao,
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
            val old = expenseDao.getExpenseById(id)
            storeRepository.updateExpense(id, category, amount, description, System.currentTimeMillis())
            // FIX (2026-08-22, DEF-52): update/delete never re-adjusted the
            // till — editing 100 → 200 left the drawer expecting 100 less
            // than it should, and deleting an expense never returned the
            // money. Now the delta is applied to the open session.
            if (old != null && old.amount != amount) {
                val delta = old.amount - amount // +ve = expense decreased
                if (delta != 0.0) {
                    val posTerminalId = appSetupPrefs.spreadsheetId.take(20)
                    try {
                        val session = tillSessionDao.getOpenSession(posTerminalId)
                        if (session != null) {
                            if (delta > 0) {
                                // expense reduced → drawer should have MORE
                                tillSessionDao.addPayIn(session.sessionId, delta)
                            } else {
                                tillSessionDao.deductExpenseFromSession(session.sessionId, -delta)
                            }
                        }
                    } catch (_: Exception) {
                        // Non-fatal: no open session
                    }
                }
            }
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            val old = expenseDao.getExpenseById(id)
            storeRepository.softDeleteExpense(id, System.currentTimeMillis())
            // FIX (2026-08-22, DEF-52): deleting an expense returns its value
            // to the drawer (the money was never actually spent).
            if (old != null) {
                val posTerminalId = appSetupPrefs.spreadsheetId.take(20)
                try {
                    val session = tillSessionDao.getOpenSession(posTerminalId)
                    if (session != null) {
                        tillSessionDao.addPayIn(session.sessionId, old.amount)
                    }
                } catch (_: Exception) {
                    // Non-fatal: no open session
                }
            }
        }
    }
}
