package com.tillzo.pos.ui.store.options.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.entity.ExpenseEntity
import com.tillzo.pos.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val storeRepository: StoreRepository
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
                logged_by_user_id = "user_1", // TODO: Auth repo
                pos_terminal_id = "terminal_1"
            )
            storeRepository.insertExpense(expense)
        }
    }
}
