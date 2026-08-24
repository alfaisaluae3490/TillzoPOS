package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tillzo.pos.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao : BaseDao<ExpenseEntity> {

    @Query("SELECT * FROM Expenses WHERE is_deleted = 0 ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM Expenses WHERE timestamp >= :startTime AND timestamp <= :endTime AND is_deleted = 0 ORDER BY timestamp DESC")
    fun getExpensesBetweenDates(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM Expenses WHERE sync_status = 'pending'")
    suspend fun getPendingExpenses(): List<ExpenseEntity>

    // FIX (2026-08-22, DEF-52): needed to diff old vs new amount on update /
    // restore value on delete (till reconciliation).
    @Query("SELECT * FROM Expenses WHERE system_row_id = :id LIMIT 1")
    suspend fun getExpenseById(id: String): ExpenseEntity?

    @Query("UPDATE Expenses SET is_deleted = 1, deleted_at = :timestamp, sync_status = 'pending' WHERE system_row_id = :id")
    suspend fun softDeleteById(id: String, timestamp: Long)

    @Query("UPDATE Expenses SET category = :category, amount = :amount, description = :description, sync_status = 'pending', updated_at = :now WHERE system_row_id = :id")
    suspend fun updateExpense(id: String, category: String, amount: Double, description: String, now: Long)

    @Query("SELECT * FROM Expenses WHERE is_deleted = 1 AND sync_status = 'pending'")
    suspend fun getPendingDeletedRows(): List<ExpenseEntity>

    @Query("UPDATE Expenses SET sync_status = 'synced' WHERE system_row_id = :id AND is_deleted = 1")
    suspend fun markSyncedAndDeleted(id: String)
}
