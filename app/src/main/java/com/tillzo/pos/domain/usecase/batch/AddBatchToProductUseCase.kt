package com.tillzo.pos.domain.usecase.batch

import com.tillzo.pos.data.local.dao.InventoryDao
import com.tillzo.pos.data.local.dao.ProductBatchDao
import com.tillzo.pos.data.local.entity.ProductBatchEntity
import javax.inject.Inject

class AddBatchToProductUseCase @Inject constructor(
    private val batchDao: ProductBatchDao,
    private val inventoryDao: InventoryDao
) {
    suspend operator fun invoke(batch: ProductBatchEntity) {
        batchDao.insertBatch(batch)
        // Also ensure inventory is flagged as multi-batch
        val product = inventoryDao.getItemByBarcode(batch.productId)
        if (product != null && !product.hasBatches) {
            val updated = product.copy(hasBatches = true)
            inventoryDao.insertItem(updated)
        }
    }
}
