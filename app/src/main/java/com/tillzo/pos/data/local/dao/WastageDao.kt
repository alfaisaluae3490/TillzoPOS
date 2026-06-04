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

    @Query("SELECT * FROM wastage_log WHERE wastageDate != '' ORDER BY createdAt DESC")
    fun getAllWastage(): Flow<List<WastageEntity>>

    @Query("SELECT * FROM wastage_log WHERE wastageDate = :date ORDER BY createdAt DESC")
    fun getWastageByDate(date: String): Flow<List<WastageEntity>>

    @Query("SELECT * FROM wastage_log WHERE productId = :productId ORDER BY createdAt DESC")
    fun getWastageByProduct(productId: String): Flow<List<WastageEntity>>

    // Total monetary loss today
    @Query("SELECT COALESCE(SUM(totalLoss), 0) FROM wastage_log WHERE wastageDate = :date")
    fun getTotalLossToday(date: String): Flow<Double>

    // Total monetary loss this month (date starts with YYYY-MM)
    @Query("SELECT COALESCE(SUM(totalLoss), 0) FROM wastage_log WHERE wastageDate LIKE :monthPrefix || '%'")
    fun getTotalLossThisMonth(monthPrefix: String): Flow<Double>

    // For SyncWorker upload
    @Query("SELECT * FROM wastage_log WHERE syncStatus = 'pending'")
    suspend fun getPendingWastage(): List<WastageEntity>

    @Query("UPDATE wastage_log SET syncStatus = 'synced' WHERE wastageId = :id")
    suspend fun markSynced(id: String)

    @Query("UPDATE wastage_log SET syncStatus = 'deleted' WHERE wastageId = :id")
    suspend fun softDelete(id: String)

    // Filter by reason
    @Query("SELECT * FROM wastage_log WHERE reason = :reason ORDER BY createdAt DESC")
    fun getWastageByReason(reason: String): Flow<List<WastageEntity>>
}
