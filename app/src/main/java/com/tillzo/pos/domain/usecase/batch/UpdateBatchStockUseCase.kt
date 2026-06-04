package com.tillzo.pos.domain.usecase.batch

import com.tillzo.pos.data.local.dao.ProductBatchDao
import javax.inject.Inject

class UpdateBatchStockUseCase @Inject constructor(
    private val batchDao: ProductBatchDao
) {
    suspend operator fun invoke(batchId: String, qtyChange: Double) {
        batchDao.updateBatchStock(batchId, qtyChange, System.currentTimeMillis())
    }
}
