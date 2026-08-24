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

    @Query("SELECT * FROM purchase_order_items WHERE syncStatus = 'pending'")
    suspend fun getPendingPOItems(): List<PurchaseOrderItemEntity>

    // DEF-115 (2026-08-23): ALL PO items for backup export
    @Query("SELECT * FROM purchase_order_items ORDER BY createdAt ASC")
    suspend fun getAllPOItemsForBackup(): List<PurchaseOrderItemEntity>

    // FIX (2026-08-22, DEF-44): receivedQty was never incremented, so
    // allFullyReceived/anyReceived stayed false and every PO remained "SENT"
    // forever (RECEIVED/PARTIALLY_RECEIVED unreachable — root cause of DEF-10).
    @Query("UPDATE purchase_order_items SET receivedQty = receivedQty + :qty, syncStatus = 'pending', updatedAt = :now WHERE poItemId = :poItemId")
    suspend fun incrementReceivedQty(poItemId: String, qty: Double, now: Long = System.currentTimeMillis())

    @Query("UPDATE purchase_orders SET status = :status, updatedAt = :time, syncStatus = 'pending' WHERE poId = :poId")
    suspend fun updatePOStatus(poId: String, status: String, time: Long)

    @Query("SELECT * FROM purchase_orders WHERE syncStatus = 'pending' AND isDeleted = 0")
    suspend fun getPendingPOs(): List<PurchaseOrderEntity>

    @Query("SELECT * FROM purchase_orders WHERE status IN ('SENT','PARTIALLY_RECEIVED') AND isDeleted = 0")
    fun getPendingDeliveryPOs(): Flow<List<PurchaseOrderEntity>>

    @Query("UPDATE purchase_orders SET syncStatus = 'synced' WHERE poId = :poId")
    suspend fun markSynced(poId: String)

    // FIX (2026-08-23, DEF-61): MAX-based sequence instead of COUNT(*)+1.
    // COUNT(*) includes soft-deleted rows → next number could collide after a
    // delete; read-then-insert also raced on double-save. MAX of the numeric
    // suffix +1 gives the true next sequence.
    @Query("SELECT COALESCE(MAX(CAST(SUBSTR(poNumber, -4) AS INTEGER)), 0) + 1 FROM purchase_orders")
    suspend fun getNextPoSequence(): Int
}
