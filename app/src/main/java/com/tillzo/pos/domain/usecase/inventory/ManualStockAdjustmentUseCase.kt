package com.tillzo.pos.domain.usecase.inventory

import com.tillzo.pos.data.local.dao.ProductBatchDao
import com.tillzo.pos.data.local.entity.ProductBatchEntity
import com.tillzo.pos.data.local.entity.StockAdjustmentEntity
import com.tillzo.pos.domain.repository.InventoryRepository
import com.tillzo.pos.domain.repository.StockAdjustmentRepository
import javax.inject.Inject

class ManualStockAdjustmentUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val stockAdjustmentRepository: StockAdjustmentRepository,
    private val productBatchDao: ProductBatchDao
) {
    suspend operator fun invoke(
        systemRowId: String,
        quantityChange: Double,
        reason: String,
        adjustedByUserId: String
    ) {
        val product = inventoryRepository.getItemById(systemRowId) ?: return

        val now = System.currentTimeMillis()
        val existingBatch = productBatchDao.getNewestActiveBatch(systemRowId)

        if (existingBatch != null) {
            val newQty = existingBatch.stockQty + quantityChange
            productBatchDao.updateBatchStock(existingBatch.batchId, newQty, now)
        } else {
            val newBatch = ProductBatchEntity(
                productId = systemRowId,
                batchNumber = "ADJ-BATCH",
                stockQty = quantityChange,
                createdAt = now,
                updatedAt = now
            )
            productBatchDao.insertBatch(newBatch)
        }

        inventoryRepository.recalculateTotalStock(systemRowId)

        val adjustment = StockAdjustmentEntity(
            productId = systemRowId,
            adjustmentType = if (quantityChange > 0) "RECEIVED" else "CORRECTION",
            quantityChanged = quantityChange,
            reason = reason,
            adjustedBy = adjustedByUserId
        )
        stockAdjustmentRepository.insertStockAdjustment(adjustment)
    }
}
