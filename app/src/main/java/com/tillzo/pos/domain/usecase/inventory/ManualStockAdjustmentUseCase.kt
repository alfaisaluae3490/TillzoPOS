package com.tillzo.pos.domain.usecase.inventory

import com.tillzo.pos.data.local.entity.StockAdjustmentEntity
import com.tillzo.pos.domain.repository.InventoryRepository
import com.tillzo.pos.domain.repository.StockAdjustmentRepository
import javax.inject.Inject

class ManualStockAdjustmentUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val stockAdjustmentRepository: StockAdjustmentRepository
) {
    suspend operator fun invoke(
        systemRowId: String,
        quantityChange: Double,
        reason: String,
        adjustedByUserId: String
    ) {
        val product = inventoryRepository.getItemById(systemRowId) ?: return

        val newStock = product.current_stock + quantityChange
        val updatedProduct = product.copy(
            current_stock = newStock,
            updated_at = System.currentTimeMillis(),
            sync_status = "pending"
        )

        inventoryRepository.updateItem(updatedProduct)

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
