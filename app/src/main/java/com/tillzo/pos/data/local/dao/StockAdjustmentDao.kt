package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tillzo.pos.data.local.entity.StockAdjustmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockAdjustmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockAdjustment(adjustment: StockAdjustmentEntity)

    @Query("SELECT * FROM StockAdjustments WHERE productId = :productId ORDER BY createdAt DESC")
    fun getAdjustmentsForProduct(productId: String): Flow<List<StockAdjustmentEntity>>

    @Query("SELECT * FROM StockAdjustments WHERE syncStatus = 'pending'")
    suspend fun getPendingAdjustments(): List<StockAdjustmentEntity>

    @Query("UPDATE StockAdjustments SET syncStatus = 'synced' WHERE adjustmentId IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}
