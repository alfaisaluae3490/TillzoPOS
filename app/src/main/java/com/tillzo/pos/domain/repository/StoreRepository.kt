package com.tillzo.pos.domain.repository

import com.tillzo.pos.data.local.entity.CustomerEntity
import com.tillzo.pos.data.local.entity.ExpenseEntity
import com.tillzo.pos.data.local.entity.KhataEventEntity
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    // Customers
    fun getAllCustomers(): Flow<List<CustomerEntity>>
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>
    suspend fun getCustomerById(customerId: String): CustomerEntity?
    suspend fun insertCustomer(customer: CustomerEntity)
    suspend fun updateCustomer(customer: CustomerEntity)
    suspend fun softDeleteCustomer(customerId: String, timestamp: Long)

    // Khata Ledger
    fun getKhataEventsForCustomer(customerId: String): Flow<List<KhataEventEntity>>
    fun getCustomerUdhaar(customerId: String): Flow<Double>
    fun getCustomerJama(customerId: String): Flow<Double>
    fun getCustomerBaqaya(customerId: String): Flow<Double>
    suspend fun insertKhataEvent(event: KhataEventEntity)

    // Expenses
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    fun getExpensesBetweenDates(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>>
    suspend fun insertExpense(expense: ExpenseEntity)
    suspend fun updateExpense(id: String, category: String, amount: Double, description: String, now: Long)
    suspend fun softDeleteExpense(id: String, timestamp: Long)
}
