package com.tillzo.pos.data.repository

import com.tillzo.pos.data.local.dao.StockAdjustmentDao
import com.tillzo.pos.data.local.entity.StockAdjustmentEntity
import com.tillzo.pos.domain.repository.StockAdjustmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StockAdjustmentRepositoryImpl @Inject constructor(
    private val dao: StockAdjustmentDao
) : StockAdjustmentRepository {
    override suspend fun insertStockAdjustment(adjustment: StockAdjustmentEntity) {
        dao.insertStockAdjustment(adjustment)
    }

    override fun getAdjustmentsForProduct(productId: String): Flow<List<StockAdjustmentEntity>> {
        return dao.getAdjustmentsForProduct(productId)
    }
}
