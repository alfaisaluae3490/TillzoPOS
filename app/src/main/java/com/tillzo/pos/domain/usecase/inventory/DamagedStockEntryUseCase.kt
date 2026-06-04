package com.tillzo.pos.domain.usecase.inventory

import com.tillzo.pos.data.local.entity.StockAdjustmentEntity
import com.tillzo.pos.domain.repository.InventoryRepository
import com.tillzo.pos.domain.repository.StockAdjustmentRepository
import javax.inject.Inject

class DamagedStockEntryUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val stockAdjustmentRepository: StockAdjustmentRepository
) {
    suspend operator fun invoke(
        systemRowId: String,
        damagedQty: Double,
        reason: String,
        adjustedByUserId: String
    ) {
        val product = inventoryRepository.getItemById(systemRowId) ?: return

        val newStock = product.current_stock - damagedQty
        val updatedProduct = product.copy(
            current_stock = newStock,
            is_damaged_stock = true,
            damaged_qty = damagedQty,
            updated_at = System.currentTimeMillis(),
            sync_status = "pending"
        )

        inventoryRepository.updateItem(updatedProduct)

        val adjustment = StockAdjustmentEntity(
            productId = systemRowId,
            adjustmentType = "DAMAGED",
            quantityChanged = -damagedQty,
            reason = reason,
            adjustedBy = adjustedByUserId
        )
        stockAdjustmentRepository.insertStockAdjustment(adjustment)
    }
}
