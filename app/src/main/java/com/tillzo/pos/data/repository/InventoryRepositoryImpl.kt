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
        val activeBatches = batches.filter { it.isActive && !it.isDeleted }
        val total = activeBatches.sumOf { it.stockQty }
        val product = inventoryDao.getItemById(productId)
        if (product != null) {
            // FIX (2026-08-22, DEF-41): hasBatches was ONLY set at product
            // creation (stock>0 heuristic). GRN ADD_BATCH / UPDATE_BATCH,
            // stock adjustments and this recalc never flipped it — so a product
            // created with 0 stock then GRN'd +10 kept hasBatches=false, sales
            // SKIPPED batch FIFO deduction, and the next recalc "restored" the
            // phantom batch sum → silent stock inflation. Now any active batch
            // forces hasBatches=true.
            inventoryDao.updateItem(
                product.copy(
                    totalStock = total,
                    current_stock = total,
                    hasBatches = activeBatches.isNotEmpty() || product.hasBatches,
                    sync_status = "pending"
                )
            )
        }
    }

    override suspend fun getItemById(id: String): InventoryEntity? {
        return inventoryDao.getItemById(id)
    }

    override suspend fun getItemByBarcode(barcode: String): InventoryEntity? {
        // DEF-64 FIX (2026-08-22): GTIN fallback — auto-GTINs ItemGtins mein hain
        return inventoryDao.getItemByBarcode(barcode) ?: inventoryDao.getItemByGtin(barcode)
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
        val timestamp = System.currentTimeMillis()
        inventoryDao.deleteItemById(id, timestamp)
        productBatchDao.softDeleteAllForProduct(id, timestamp)
    }
}
