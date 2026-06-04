package com.tillzo.pos.domain.usecase.grn

import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.domain.repository.GrnRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGrnsUseCase @Inject constructor(
    private val repo: GrnRepository
) {
    operator fun invoke(): Flow<List<GrnHeaderEntity>> {
        return repo.getAllGrns()
    }

    fun forPO(poId: String): Flow<List<GrnHeaderEntity>> {
        return repo.getGrnsForPO(poId)
    }
}
