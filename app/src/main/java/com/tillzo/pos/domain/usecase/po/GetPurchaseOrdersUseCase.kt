package com.tillzo.pos.domain.usecase.po

import com.tillzo.pos.data.local.dao.PurchaseOrderDao
import com.tillzo.pos.data.local.entity.PurchaseOrderEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPurchaseOrdersUseCase @Inject constructor(
    private val poDao: PurchaseOrderDao
) {
    operator fun invoke(): Flow<List<PurchaseOrderEntity>> {
        return poDao.getAllPOs()
    }
}
