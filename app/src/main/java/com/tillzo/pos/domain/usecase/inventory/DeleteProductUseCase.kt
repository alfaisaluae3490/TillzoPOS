package com.tillzo.pos.domain.usecase.inventory

import com.tillzo.pos.domain.repository.InventoryRepository
import com.tillzo.pos.data.sync.SyncOrchestrator
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val syncOrchestrator: SyncOrchestrator
) {
    suspend operator fun invoke(systemRowId: String) {
        inventoryRepository.deleteItemById(systemRowId)
        syncOrchestrator.triggerManualSync()
    }
}
