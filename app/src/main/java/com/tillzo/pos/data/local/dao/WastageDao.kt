package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tillzo.pos.data.local.entity.WastageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WastageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWastage(wastage: WastageEntity)

    // GAP-4 FIX (2026-08-22): soft-deleted entries (syncStatus='deleted') ab list/totals
    // se exclude hote hain — delete UI ke baad entry card turant gayab ho jata hai.
    @Query("SELECT * FROM wastage_log WHERE wastageDate != '' AND syncStatus != 'deleted' ORDER BY createdAt DESC")
    fun getAllWastage(): Flow<List<WastageEntity>>

    @Query("SELECT * FROM wastage_log WHERE wastageDate = :date AND syncStatus != 'deleted' ORDER BY createdAt DESC")
    fun getWastageByDate(date: String): Flow<List<WastageEntity>>

    @Query("SELECT * FROM wastage_log WHERE productId = :productId AND syncStatus != 'deleted' ORDER BY createdAt DESC")
    fun getWastageByProduct(productId: String): Flow<List<WastageEntity>>

    // Total monetary loss today
    @Query("SELECT COALESCE(SUM(totalLoss), 0) FROM wastage_log WHERE wastageDate = :date AND syncStatus != 'deleted'")
    fun getTotalLossToday(date: String): Flow<Double>

    // Total monetary loss this month (date starts with YYYY-MM)
    @Query("SELECT COALESCE(SUM(totalLoss), 0) FROM wastage_log WHERE wastageDate LIKE :monthPrefix || '%' AND syncStatus != 'deleted'")
    fun getTotalLossThisMonth(monthPrefix: String): Flow<Double>

    // For SyncWorker upload
    @Query("SELECT * FROM wastage_log WHERE syncStatus = 'pending'")
    suspend fun getPendingWastage(): List<WastageEntity>

    // DEF-115 (2026-08-23): ALL wastage rows (incl. soft-deleted) for backup export
    @Query("SELECT * FROM wastage_log ORDER BY createdAt ASC")
    suspend fun getAllWastageForBackup(): List<WastageEntity>

    // DEF-90 FIX (2026-08-23): soft-deleted entries — SyncWorker inhe sheet par
    // sync_status='deleted' marker ke roop mein push karta hai (row delete nahi).
    @Query("SELECT * FROM wastage_log WHERE syncStatus = 'deleted'")
    suspend fun getPendingDeletedWastage(): List<WastageEntity>

    @Query("UPDATE wastage_log SET syncStatus = 'synced' WHERE wastageId = :id")
    suspend fun markSynced(id: String)

    @Query("UPDATE wastage_log SET syncStatus = 'deleted' WHERE wastageId = :id")
    suspend fun softDelete(id: String)

    // Filter by reason
    @Query("SELECT * FROM wastage_log WHERE reason = :reason AND syncStatus != 'deleted' ORDER BY createdAt DESC")
    fun getWastageByReason(reason: String): Flow<List<WastageEntity>>
}
