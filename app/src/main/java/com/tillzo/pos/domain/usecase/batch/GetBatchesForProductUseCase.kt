package com.tillzo.pos.domain.usecase.batch

import com.tillzo.pos.data.local.dao.ProductBatchDao
import com.tillzo.pos.data.local.entity.ProductBatchEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBatchesForProductUseCase @Inject constructor(
    private val batchDao: ProductBatchDao
) {
    operator fun invoke(productId: String): Flow<List<ProductBatchEntity>> {
        return batchDao.getBatchesForProduct(productId)
    }
}
