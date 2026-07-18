package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tillzo.pos.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM Sales WHERE is_deleted = 0 ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM Sales WHERE is_deleted = 0 ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getSalesPaged(limit: Int, offset: Int): Flow<List<SaleEntity>>

    @Query("SELECT * FROM Sales WHERE timestamp BETWEEN :start AND :end AND is_deleted = 0 ORDER BY timestamp DESC")
    fun getSalesInRange(start: Long, end: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM Sales WHERE timestamp BETWEEN :start AND :end AND is_deleted = 0 ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getSalesInRangePaged(start: Long, end: Long, limit: Int, offset: Int): Flow<List<SaleEntity>>

    @Query("SELECT * FROM Sales WHERE system_row_id = :systemRowId AND is_deleted = 0")
    suspend fun getSaleById(systemRowId: String): SaleEntity?

    @Query("SELECT * FROM Sales WHERE sync_uuid = :invoiceId AND is_deleted = 0")
    suspend fun getSaleByInvoiceId(invoiceId: String): SaleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Query("SELECT * FROM Sales WHERE sync_status = 'pending'")
    suspend fun getPendingSyncSales(): List<SaleEntity>

    // For M2 Shard Logic — fetching total rows
    @Query("SELECT COUNT(*) FROM Sales WHERE is_deleted = 0")
    suspend fun getSaleCount(): Int

    @Query("UPDATE Sales SET is_deleted = 1, deleted_at = :timestamp, sync_status = 'pending' WHERE system_row_id = :id")
    suspend fun softDeleteById(id: String, timestamp: Long)

    @Query("SELECT * FROM Sales WHERE is_deleted = 1 AND sync_status = 'pending'")
    suspend fun getPendingDeletedRows(): List<SaleEntity>

    @Query("UPDATE Sales SET sync_status = 'synced' WHERE system_row_id = :id AND is_deleted = 1")
    suspend fun markSyncedAndDeleted(id: String)

    @Query("UPDATE Sales SET sync_status = 'synced' WHERE sync_uuid = :invoiceUuid")
    suspend fun markSyncedByInvoiceId(invoiceUuid: String)
}
