package com.tillzo.pos.domain.repository

import com.tillzo.pos.data.local.entity.StockAdjustmentEntity
import kotlinx.coroutines.flow.Flow

interface StockAdjustmentRepository {
    suspend fun insertStockAdjustment(adjustment: StockAdjustmentEntity)
    fun getAdjustmentsForProduct(productId: String): Flow<List<StockAdjustmentEntity>>
}
