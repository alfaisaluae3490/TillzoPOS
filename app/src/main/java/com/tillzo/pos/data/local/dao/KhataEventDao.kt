package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tillzo.pos.data.local.entity.KhataEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KhataEventDao : BaseDao<KhataEventEntity> {

    @Query("SELECT * FROM KhataEvents WHERE customer_id = :customerId AND is_deleted = 0 ORDER BY created_at DESC")
    fun getEventsForCustomer(customerId: String): Flow<List<KhataEventEntity>>
    
    @Query("SELECT * FROM KhataEvents WHERE sync_status = 'pending'")
    suspend fun getPendingKhataEvents(): List<KhataEventEntity>

    // Aggregations
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM KhataEvents WHERE customer_id = :customerId AND event_type = 'UDHAAR' AND is_deleted = 0")
    fun getTotalUdhaarFlow(customerId: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM KhataEvents WHERE customer_id = :customerId AND event_type = 'JAMA' AND is_deleted = 0")
    fun getTotalJamaFlow(customerId: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM KhataEvents WHERE customer_id = :customerId AND is_deleted = 0")
    fun getBaqayaBalanceFlow(customerId: String): Flow<Double>
    
    @Query("UPDATE KhataEvents SET is_deleted = 1, deleted_at = :timestamp, sync_status = 'pending' WHERE system_row_id = :id")
    suspend fun softDeleteById(id: String, timestamp: Long)

    @Query("SELECT * FROM KhataEvents WHERE is_deleted = 1 AND sync_status = 'pending'")
    suspend fun getPendingDeletedRows(): List<KhataEventEntity>

    @Query("UPDATE KhataEvents SET sync_status = 'synced' WHERE system_row_id = :id AND is_deleted = 1")
    suspend fun markSyncedAndDeleted(id: String)
}
