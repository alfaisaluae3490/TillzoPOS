package com.tillzo.pos.domain.usecase.grn

import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.data.local.entity.GrnItemEntity
import com.tillzo.pos.domain.repository.GrnRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGrnDetailUseCase @Inject constructor(
    private val repo: GrnRepository
) {
    suspend fun getHeader(grnId: String): GrnHeaderEntity? {
        return repo.getGrnById(grnId)
    }

    fun getItems(grnId: String): Flow<List<GrnItemEntity>> {
        return repo.getGrnItemsFlow(grnId)
    }
}
