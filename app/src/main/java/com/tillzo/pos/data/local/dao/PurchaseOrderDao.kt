package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tillzo.pos.data.local.entity.PurchaseOrderEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPO(po: PurchaseOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPOItems(items: List<PurchaseOrderItemEntity>)

    @Query("SELECT * FROM purchase_orders WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllPOs(): Flow<List<PurchaseOrderEntity>>

    @Query("SELECT * FROM purchase_orders WHERE poId = :poId LIMIT 1")
    suspend fun getPOById(poId: String): PurchaseOrderEntity?

    @Query("SELECT * FROM purchase_order_items WHERE poId = :poId")
    suspend fun getPOItems(poId: String): List<PurchaseOrderItemEntity>

    @Query("UPDATE purchase_orders SET status = :status, updatedAt = :time, syncStatus = 'pending' WHERE poId = :poId")
    suspend fun updatePOStatus(poId: String, status: String, time: Long)

    @Query("SELECT * FROM purchase_orders WHERE syncStatus = 'pending' AND isDeleted = 0")
    suspend fun getPendingPOs(): List<PurchaseOrderEntity>

    @Query("SELECT * FROM purchase_orders WHERE status IN ('SENT','PARTIALLY_RECEIVED') AND isDeleted = 0")
    fun getPendingDeliveryPOs(): Flow<List<PurchaseOrderEntity>>

    @Query("UPDATE purchase_orders SET syncStatus = 'synced' WHERE poId = :poId")
    suspend fun markSynced(poId: String)

    @Query("SELECT COUNT(*) FROM purchase_orders")
    suspend fun getTotalPOCount(): Int
}
