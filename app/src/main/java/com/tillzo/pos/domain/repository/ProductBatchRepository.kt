package com.tillzo.pos.domain.repository

import com.tillzo.pos.data.local.entity.ProductBatchEntity

interface ProductBatchRepository {
    suspend fun insertBatch(batch: ProductBatchEntity)
    suspend fun incrementBatchStock(batchId: String, additionalQty: Double)
    suspend fun getBatchByNumber(productId: String, batchNumber: String): ProductBatchEntity?
}
