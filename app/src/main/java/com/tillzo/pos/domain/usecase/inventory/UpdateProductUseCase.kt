package com.tillzo.pos.domain.usecase.inventory

import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.domain.repository.InventoryRepository
import com.tillzo.pos.data.sync.SyncOrchestrator
import javax.inject.Inject

class UpdateProductUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val syncOrchestrator: SyncOrchestrator
) {
    suspend operator fun invoke(product: InventoryEntity) {
        inventoryRepository.updateItem(
            product.copy(
                updated_at = System.currentTimeMillis(),
                sync_status = "pending"
            )
        )
        syncOrchestrator.triggerManualSync()
    }
}
