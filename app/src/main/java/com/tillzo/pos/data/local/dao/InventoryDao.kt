package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tillzo.pos.data.local.entity.InventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryEntity)

    @Update
    suspend fun updateItem(item: InventoryEntity)

    @Query("SELECT * FROM Inventory WHERE system_row_id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getItemById(id: String): InventoryEntity?
    
    @Query("SELECT * FROM Inventory WHERE barcode_id = :barcode AND is_deleted = 0 LIMIT 1")
    suspend fun getItemByBarcode(barcode: String): InventoryEntity?

    @Query("SELECT * FROM Inventory WHERE is_deleted = 0 ORDER BY item_name ASC")
    fun getAllItems(): Flow<List<InventoryEntity>>

    // Quick-grid pinning
    @Query("SELECT * FROM Inventory WHERE isPinned = 1 AND is_deleted = 0 ORDER BY pinnedOrder ASC")
    fun getPinnedItems(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM Inventory WHERE isPinned = 1 AND is_deleted = 0 ORDER BY pinnedOrder ASC")
    suspend fun getPinnedItemsOnce(): List<InventoryEntity>

    @Query("UPDATE Inventory SET isPinned = :pinned, pinnedOrder = :order, sync_status = 'pending' WHERE system_row_id = :id")
    suspend fun updatePinStatus(id: String, pinned: Boolean, order: Int)

    @Query("SELECT * FROM Inventory WHERE sync_status = 'pending'")
    suspend fun getPendingItems(): List<InventoryEntity>

    // M6.6 Low Stock Alert Query
    @Query("SELECT * FROM Inventory WHERE current_stock <= low_stock_threshold AND is_deleted = 0")
    fun getLowStockItems(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM Inventory WHERE current_stock <= low_stock_threshold AND is_deleted = 0")
    suspend fun getLowStockItemsAsList(): List<InventoryEntity>

    // Advanced Reporting / Status Queries
    @Query("SELECT * FROM Inventory WHERE current_stock <= 0 AND is_deleted = 0")
    fun getOutOfStockItems(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM Inventory WHERE expiry_date != '' AND expiry_date <= :thresholdDate AND is_deleted = 0")
    fun getNearExpiryItems(thresholdDate: String): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM Inventory WHERE expiry_date != '' AND expiry_date < :todayDate AND is_deleted = 0")
    fun getExpiredItems(todayDate: String): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM Inventory WHERE is_damaged_stock = 1 AND is_deleted = 0")
    fun getDamagedItems(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM Inventory WHERE (item_name LIKE '%' || :query || '%' OR barcode_id LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%') AND is_deleted = 0 ORDER BY item_name ASC")
    fun searchItems(query: String): Flow<List<InventoryEntity>>

    @Query("UPDATE Inventory SET is_deleted = 1, deleted_at = :timestamp, sync_status = 'pending' WHERE system_row_id = :id")
    suspend fun softDeleteById(id: String, timestamp: Long)

    @Query("SELECT * FROM Inventory WHERE is_deleted = 1 AND sync_status = 'pending'")
    suspend fun getPendingDeletedRows(): List<InventoryEntity>

    @Query("UPDATE Inventory SET sync_status = 'synced' WHERE system_row_id = :id AND is_deleted = 1")
    suspend fun markSyncedAndDeleted(id: String)
    
    // Legacy soft-delete fallback
    @Query("UPDATE Inventory SET is_deleted = 1, deleted_at = :timestamp, sync_status = 'pending' WHERE system_row_id = :id")
    suspend fun deleteItemById(id: String, timestamp: Long)

    @Query("UPDATE Inventory SET current_stock = :newStock, sync_status = 'pending', updated_at = :now WHERE system_row_id = :id")
    suspend fun updateStockAndSyncStatus(id: String, newStock: Double, now: Long = System.currentTimeMillis())

    @Query("UPDATE Inventory SET totalStock = :total, sync_status = 'pending', updated_at = :time WHERE system_row_id = :id")
    suspend fun updateTotalStockAndSyncStatus(id: String, total: Double, time: Long = System.currentTimeMillis())
    // Also needed by ExpiryCheckWorker (suspend, list-form)
    @Query("SELECT * FROM Inventory WHERE is_deleted = 0 AND current_stock > 0 AND expiry_date != '' AND expiry_date < :today")
    suspend fun getExpiredItemsList(today: String): List<InventoryEntity>

    @Query("SELECT * FROM Inventory WHERE is_deleted = 0 AND expiry_date != '' AND expiry_date >= :today AND expiry_date <= :thresholdDate")
    suspend fun getNearExpiryItemsList(today: String, thresholdDate: String): List<InventoryEntity>

    @Query("SELECT MAX(item_number) FROM Inventory")
    suspend fun getMaxItemNumber(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGtins(gtins: List<com.tillzo.pos.data.local.entity.ItemGtinEntity>)

    @Query("SELECT * FROM ItemGtins WHERE item_id = :itemId")
    suspend fun getGtinsForItem(itemId: String): List<com.tillzo.pos.data.local.entity.ItemGtinEntity>

    @Query("SELECT * FROM ItemGtins WHERE item_id = :itemId")
    fun getGtinsForItemFlow(itemId: String): Flow<List<com.tillzo.pos.data.local.entity.ItemGtinEntity>>

    @Query("DELETE FROM ItemGtins WHERE item_id = :itemId")
    suspend fun deleteGtinsForItem(itemId: String)
}
