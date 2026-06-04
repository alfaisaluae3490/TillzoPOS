package com.tillzo.pos.domain.usecase.po

import com.tillzo.pos.data.local.dao.PurchaseOrderDao
import javax.inject.Inject

class UpdatePOStatusUseCase @Inject constructor(
    private val poDao: PurchaseOrderDao
) {
    suspend operator fun invoke(poId: String, status: String) {
        poDao.updatePOStatus(poId, status, System.currentTimeMillis())
    }
}
