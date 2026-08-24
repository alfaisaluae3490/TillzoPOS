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

    // DEF-86 FIX: partial / case-insensitive invoice lookup — receipt par 8-char
    // invoice ID dikhta hai; exact-match lookup se manual entry fail ho jati thi.
    @Query("SELECT * FROM Sales WHERE lower(sync_uuid) LIKE lower(:prefix) || '%' AND is_deleted = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getSaleByInvoiceIdPrefix(prefix: String): SaleEntity?

    // DEF-46b (2026-08-23): double-refund guard — count existing refund rows
    // referencing an invoice (REFUND_OF_<invoiceId>_<reason>).
    @Query("SELECT COUNT(*) FROM Sales WHERE reference_id LIKE :refPrefix || '%' AND is_deleted = 0")
    suspend fun countRefundsByInvoice(refPrefix: String): Int

    // FIX (2026-08-22, DEF-32): purge corrupt local sales imported from
    // scattered legacy sheet rows (blank invoice id → empty history entries,
    // total 0.0). Called after a pull; safe because a real sale ALWAYS has an
    // invoice id — a row without one is data garbage, not a valid sale.
    @Query("DELETE FROM Sales WHERE sync_uuid = '' OR sync_uuid IS NULL")
    suspend fun deleteCorruptSales()

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
