package com.tillzo.pos.data.repository

import com.tillzo.pos.data.local.dao.CustomerDao
import com.tillzo.pos.data.local.dao.ExpenseDao
import com.tillzo.pos.data.local.dao.KhataEventDao
import com.tillzo.pos.data.local.entity.CustomerEntity
import com.tillzo.pos.data.local.entity.ExpenseEntity
import com.tillzo.pos.data.local.entity.KhataEventEntity
import com.tillzo.pos.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StoreRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao,
    private val khataEventDao: KhataEventDao,
    private val expenseDao: ExpenseDao
) : StoreRepository {

    // Customers
    override fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    
    override fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)
    
    override suspend fun getCustomerById(customerId: String): CustomerEntity? = customerDao.getCustomerById(customerId)
    
    override suspend fun insertCustomer(customer: CustomerEntity) { customerDao.insert(customer) }
    
    override suspend fun updateCustomer(customer: CustomerEntity) { customerDao.update(customer) }
    override suspend fun softDeleteCustomer(customerId: String, timestamp: Long) { customerDao.softDeleteById(customerId, timestamp) }

    // Khata Ledger
    override fun getKhataEventsForCustomer(customerId: String): Flow<List<KhataEventEntity>> = khataEventDao.getEventsForCustomer(customerId)
    
    override fun getCustomerUdhaar(customerId: String): Flow<Double> = khataEventDao.getTotalUdhaarFlow(customerId)
    
    override fun getCustomerJama(customerId: String): Flow<Double> = khataEventDao.getTotalJamaFlow(customerId)
    
    override fun getCustomerBaqaya(customerId: String): Flow<Double> = khataEventDao.getBaqayaBalanceFlow(customerId)
    
    override suspend fun insertKhataEvent(event: KhataEventEntity) { khataEventDao.insert(event) }

    // Expenses
    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    
    override fun getExpensesBetweenDates(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>> = expenseDao.getExpensesBetweenDates(startTime, endTime)
    
    override suspend fun insertExpense(expense: ExpenseEntity) { expenseDao.insert(expense) }
    override suspend fun updateExpense(id: String, category: String, amount: Double, description: String, now: Long) { expenseDao.updateExpense(id, category, amount, description, now) }
    override suspend fun softDeleteExpense(id: String, timestamp: Long) { expenseDao.softDeleteById(id, timestamp) }
}
