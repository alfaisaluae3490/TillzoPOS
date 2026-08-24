package com.tillzo.pos.data.repository

import com.tillzo.pos.data.local.dao.GrnDao
import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.data.local.entity.GrnItemEntity
import com.tillzo.pos.domain.repository.ConfirmGrnResult
import com.tillzo.pos.domain.repository.GrnRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GrnRepositoryImpl @Inject constructor(
    private val grnDao: GrnDao
) : GrnRepository {

    override fun getAllGrns() = grnDao.getAllGrns()
    override fun getGrnsForPO(poId: String) = grnDao.getGrnsForPO(poId)
    override suspend fun getGrnById(grnId: String) = grnDao.getGrnById(grnId)
    override suspend fun getGrnItems(grnId: String) = grnDao.getGrnItems(grnId)
    override fun getGrnItemsFlow(grnId: String) = grnDao.getGrnItemsFlow(grnId)

    override suspend fun saveGrnDraft(
        header: GrnHeaderEntity,
        items: List<GrnItemEntity>
    ) {
        grnDao.insertGrnHeader(header)
        grnDao.insertGrnItems(items)
    }

    override suspend fun confirmGrn(grnId: String): ConfirmGrnResult {
        // Delegated to ConfirmGrnUseCase — this just updates status
        grnDao.updateGrnStatus(grnId, "CONFIRMED", System.currentTimeMillis())
        return ConfirmGrnResult(true, 0, 0, 0)
    }

    override suspend fun generateGrnNumber(): String {
        // FIX (2026-08-23, DEF-61): MAX-based sequence — COUNT(*)+1 raced and
        // reused numbers after soft-deletes. getNextGrnSequence is atomic.
        val seq = grnDao.getNextGrnSequence()
        val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        return "GRN-$year-${seq.toString().padStart(4, '0')}"
    }

    override suspend fun updateGrnStatus(grnId: String, status: String) =
        grnDao.updateGrnStatus(grnId, status, System.currentTimeMillis())

    override suspend fun updateGrnItemBatchId(grnItemId: String, batchId: String) =
        grnDao.updateGrnItemBatchId(grnItemId, batchId)

    override suspend fun getPendingGrns() = grnDao.getPendingGrns()

    override suspend fun markGrnSynced(grnId: String) = grnDao.markGrnSynced(grnId, System.currentTimeMillis())
}
