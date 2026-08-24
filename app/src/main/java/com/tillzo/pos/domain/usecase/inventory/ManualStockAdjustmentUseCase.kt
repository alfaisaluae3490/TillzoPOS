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

        // DEF-65 FIX (2026-08-23): negative adjustment with no existing batch
        // created a NEGATIVE-stock "ADJ-BATCH" row (stockQty = negative change),
        // and a negative change that would push a batch below zero was allowed —
        // inventory could go permanently negative/invisible. Guard both cases.
        if (quantityChange == 0.0) return

        val now = System.currentTimeMillis()
        val existingBatch = productBatchDao.getNewestActiveBatch(systemRowId)

        if (existingBatch != null) {
            val newQty = existingBatch.stockQty + quantityChange
            if (newQty < 0.0) {
                // Clamp at 0 — never let an adjustment drive stock negative.
                productBatchDao.updateBatchStock(existingBatch.batchId, 0.0, now)
            } else {
                productBatchDao.updateBatchStock(existingBatch.batchId, newQty, now)
            }
        } else {
            if (quantityChange < 0.0) {
                // No batch exists and we'd be removing stock that isn't there —
                // reject rather than mint a negative batch.
                return
            }
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
