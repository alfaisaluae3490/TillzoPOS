package com.tillzo.pos.domain.repository

import com.tillzo.pos.data.local.entity.InventoryEntity
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    suspend fun insertItem(item: InventoryEntity)
    suspend fun updateItem(item: InventoryEntity)
    suspend fun recalculateTotalStock(productId: String)
    suspend fun getItemById(id: String): InventoryEntity?
    suspend fun getItemByBarcode(barcode: String): InventoryEntity?
    fun getAllItems(): Flow<List<InventoryEntity>>
    suspend fun getPendingItems(): List<InventoryEntity>
    fun getLowStockItems(): Flow<List<InventoryEntity>>
    fun getOutOfStockItems(): Flow<List<InventoryEntity>>
    fun getNearExpiryItems(thresholdDate: String): Flow<List<InventoryEntity>>
    fun getExpiredItems(todayDate: String): Flow<List<InventoryEntity>>
    fun getDamagedItems(): Flow<List<InventoryEntity>>
    fun searchItems(query: String): Flow<List<InventoryEntity>>
    suspend fun deleteItemById(id: String)
}
