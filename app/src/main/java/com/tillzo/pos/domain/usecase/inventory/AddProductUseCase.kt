package com.tillzo.pos.domain.usecase.inventory

import com.tillzo.pos.data.local.dao.ProductBatchDao
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.data.local.entity.ProductBatchEntity
import com.tillzo.pos.domain.repository.InventoryRepository
import com.tillzo.pos.data.sync.SyncOrchestrator
import java.util.UUID
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val productBatchDao: ProductBatchDao,
    private val syncOrchestrator: SyncOrchestrator
) {
    suspend operator fun invoke(product: InventoryEntity) {
        inventoryRepository.insertItem(product.copy(sync_status = "pending", hasBatches = product.current_stock > 0, totalStock = product.current_stock))
        if (product.current_stock > 0) {
            val batch = ProductBatchEntity(
                batchId = UUID.randomUUID().toString(),
                productId = product.system_row_id,
                barcodeId = product.barcode_id,
                batchNumber = product.batch_number.ifBlank { "BATCH-INITIAL" },
                manufacturingDate = product.manufacturing_date,
                expiryDate = product.expiry_date,
                stockQty = product.current_stock,
                costPrice = product.cost_price,
                sellingPrice = product.price_per_unit,
                isActive = true,
                isDeleted = false,
                syncStatus = "pending",
                posTerminalId = product.pos_terminal_id
            )
            productBatchDao.insertBatch(batch)
        }
        syncOrchestrator.triggerManualSync()
    }
}
