package com.tillzo.pos.data.repository

import com.tillzo.pos.data.local.dao.ProductBatchDao
import com.tillzo.pos.data.local.entity.ProductBatchEntity
import com.tillzo.pos.domain.repository.ProductBatchRepository
import javax.inject.Inject

class ProductBatchRepositoryImpl @Inject constructor(
    private val productBatchDao: ProductBatchDao
) : ProductBatchRepository {

    override suspend fun insertBatch(batch: ProductBatchEntity) {
        productBatchDao.insertBatch(batch)
    }

    override suspend fun getBatchByNumber(productId: String, batchNumber: String): ProductBatchEntity? {
        return productBatchDao.getBatchByNumber(productId, batchNumber)
    }

    override suspend fun incrementBatchStock(batchId: String, additionalQty: Double) {
        val existing = productBatchDao.getBatchById(batchId) ?: return
        val newQty = existing.stockQty + additionalQty
        productBatchDao.updateBatchStock(batchId, newQty, System.currentTimeMillis())
    }
}
