package com.tillzo.pos.domain.usecase.grn

import com.tillzo.pos.data.local.dao.GrnDao
import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.data.local.entity.GrnItemEntity
import javax.inject.Inject

class CreateGrnUseCase @Inject constructor(
    private val grnDao: GrnDao
) {
    suspend operator fun invoke(header: GrnHeaderEntity, items: List<GrnItemEntity>) {
        grnDao.insertGrnHeader(header)
        grnDao.insertGrnItems(items)
    }
}
