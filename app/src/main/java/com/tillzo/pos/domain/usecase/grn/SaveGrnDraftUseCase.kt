package com.tillzo.pos.domain.usecase.grn

import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.data.local.entity.GrnItemEntity
import com.tillzo.pos.domain.repository.GrnRepository
import javax.inject.Inject

class SaveGrnDraftUseCase @Inject constructor(
    private val repo: GrnRepository
) {
    suspend operator fun invoke(header: GrnHeaderEntity, items: List<GrnItemEntity>) {
        repo.saveGrnDraft(header, items)
    }
}
