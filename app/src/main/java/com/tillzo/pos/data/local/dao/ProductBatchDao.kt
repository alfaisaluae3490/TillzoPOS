package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tillzo.pos.data.local.entity.ProductBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductBatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: ProductBatchEntity)

    @Query("SELECT * FROM product_batches WHERE productId = :productId AND isDeleted = 0 AND isActive = 1 ORDER BY expiryDate ASC")
    fun getBatchesForProduct(productId: String): Flow<List<ProductBatchEntity>>

    @Query("SELECT * FROM product_batches WHERE productId = :productId AND isDeleted = 0 ORDER BY expiryDate ASC")
    suspend fun getAllBatchesForProduct(productId: String): List<ProductBatchEntity>

    @Query("SELECT * FROM product_batches WHERE barcodeId = :barcodeId AND isDeleted = 0 AND isActive = 1 LIMIT 1")
    suspend fun getBatchByBarcode(barcodeId: String): ProductBatchEntity?

    @Query("SELECT * FROM product_batches WHERE batchId = :batchId LIMIT 1")
    suspend fun getBatchById(batchId: String): ProductBatchEntity?

    @Query("SELECT * FROM product_batches WHERE syncStatus = 'pending' AND isDeleted = 0")
    suspend fun getPendingBatches(): List<ProductBatchEntity>

    @Query("SELECT * FROM product_batches WHERE isDeleted = 1 AND syncStatus = 'pending'")
    suspend fun getPendingDeletedBatches(): List<ProductBatchEntity>

    @Query("UPDATE product_batches SET syncStatus = 'synced' WHERE batchId = :batchId")
    suspend fun markSynced(batchId: String)

    @Query("UPDATE product_batches SET isDeleted = 1, deletedAt = :timestamp, syncStatus = 'pending' WHERE batchId = :id")
    suspend fun softDelete(id: String, timestamp: Long)

    @Query("UPDATE product_batches SET stockQty = :qty, updatedAt = :time, syncStatus = 'pending' WHERE batchId = :batchId")
    suspend fun updateBatchStock(batchId: String, qty: Double, time: Long)

    // Near expiry — within 30 days
    @Query("SELECT * FROM product_batches WHERE isDeleted = 0 AND isActive = 1 AND expiryDate <= :thresholdDate AND expiryDate >= :today")
    fun getNearExpiryBatches(thresholdDate: String, today: String): Flow<List<ProductBatchEntity>>

    // Expired
    @Query("SELECT * FROM product_batches WHERE isDeleted = 0 AND expiryDate < :today")
    fun getExpiredBatches(today: String): Flow<List<ProductBatchEntity>>

    // FIFO: oldest active batch for a product (for stock deduction after sale sync)
    @Query("SELECT * FROM product_batches WHERE productId = :productId AND isActive = 1 AND isDeleted = 0 ORDER BY createdAt ASC LIMIT 1")
    suspend fun getOldestActiveBatch(productId: String): ProductBatchEntity?

    // Deactivate a batch (called when its stockQty reaches 0)
    @Query("UPDATE product_batches SET isActive = 0, updatedAt = :time, syncStatus = 'pending' WHERE batchId = :batchId")
    suspend fun deactivateBatch(batchId: String, time: Long)
}
