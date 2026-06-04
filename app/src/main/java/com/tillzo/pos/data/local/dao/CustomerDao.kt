package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tillzo.pos.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao : BaseDao<CustomerEntity> {

    @Query("SELECT * FROM Customers WHERE is_deleted = 0 ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM Customers WHERE system_row_id = :customerId AND is_deleted = 0")
    fun getCustomerFlow(customerId: String): Flow<CustomerEntity?>

    @Query("SELECT * FROM Customers WHERE system_row_id = :customerId AND is_deleted = 0")
    suspend fun getCustomerById(customerId: String): CustomerEntity?

    @Query("SELECT * FROM Customers WHERE sync_status = 'pending'")
    suspend fun getPendingCustomers(): List<CustomerEntity>

    @Query("SELECT * FROM Customers WHERE (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') AND is_deleted = 0")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("UPDATE Customers SET is_deleted = 1, deleted_at = :timestamp, sync_status = 'pending' WHERE system_row_id = :id")
    suspend fun softDeleteById(id: String, timestamp: Long)

    @Query("SELECT * FROM Customers WHERE is_deleted = 1 AND sync_status = 'pending'")
    suspend fun getPendingDeletedRows(): List<CustomerEntity>

    @Query("UPDATE Customers SET sync_status = 'synced' WHERE system_row_id = :id AND is_deleted = 1")
    suspend fun markSyncedAndDeleted(id: String)
}
