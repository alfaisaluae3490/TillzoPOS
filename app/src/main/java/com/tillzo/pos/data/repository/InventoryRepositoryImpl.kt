package com.tillzo.pos.data.repository

import com.tillzo.pos.data.local.dao.InventoryDao
import com.tillzo.pos.data.local.dao.ProductBatchDao
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val inventoryDao: InventoryDao,
    private val productBatchDao: ProductBatchDao
) : InventoryRepository {

    override suspend fun insertItem(item: InventoryEntity) {
        inventoryDao.insertItem(item)
    }

    override suspend fun updateItem(item: InventoryEntity) {
        inventoryDao.updateItem(item.copy(
            updated_at = System.currentTimeMillis(),
            sync_status = "pending" // M2 trigger Delta Sync
        ))
    }

    override suspend fun recalculateTotalStock(productId: String) {
        val batches = productBatchDao.getAllBatchesForProduct(productId)
        val total = batches.filter { it.isActive && !it.isDeleted }
            .sumOf { it.stockQty }
        val product = inventoryDao.getItemById(productId)
        if (product != null) {
            inventoryDao.updateItem(product.copy(totalStock = total, current_stock = total, sync_status = "pending"))
        }
    }

    override suspend fun getItemById(id: String): InventoryEntity? {
        return inventoryDao.getItemById(id)
    }

    override suspend fun getItemByBarcode(barcode: String): InventoryEntity? {
        return inventoryDao.getItemByBarcode(barcode)
    }

    override fun getAllItems(): Flow<List<InventoryEntity>> {
        return inventoryDao.getAllItems()
    }

    override fun getLowStockItems(): Flow<List<InventoryEntity>> {
        return inventoryDao.getLowStockItems()
    }

    override suspend fun getPendingItems(): List<InventoryEntity> {
        return inventoryDao.getPendingItems()
    }

    override fun getOutOfStockItems(): Flow<List<InventoryEntity>> {
        return inventoryDao.getOutOfStockItems()
    }

    override fun getNearExpiryItems(thresholdDate: String): Flow<List<InventoryEntity>> {
        return inventoryDao.getNearExpiryItems(thresholdDate)
    }

    override fun getExpiredItems(todayDate: String): Flow<List<InventoryEntity>> {
        return inventoryDao.getExpiredItems(todayDate)
    }

    override fun getDamagedItems(): Flow<List<InventoryEntity>> {
        return inventoryDao.getDamagedItems()
    }

    override fun searchItems(query: String): Flow<List<InventoryEntity>> {
        return inventoryDao.searchItems(query)
    }

    override suspend fun deleteItemById(id: String) {
        inventoryDao.deleteItemById(id, System.currentTimeMillis())
    }
}
