package com.tillzo.pos.domain.usecase.po

import com.tillzo.pos.data.local.dao.PurchaseOrderDao
import com.tillzo.pos.data.local.entity.PurchaseOrderEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderItemEntity
import javax.inject.Inject

class CreatePurchaseOrderUseCase @Inject constructor(
    private val poDao: PurchaseOrderDao
) {
    suspend operator fun invoke(po: PurchaseOrderEntity, items: List<PurchaseOrderItemEntity>) {
        poDao.insertPO(po)
        poDao.insertPOItems(items)
    }
}
