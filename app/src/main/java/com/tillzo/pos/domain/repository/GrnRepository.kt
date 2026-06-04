package com.tillzo.pos.domain.repository

import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.data.local.entity.GrnItemEntity
import kotlinx.coroutines.flow.Flow

interface GrnRepository {
    fun getAllGrns(): Flow<List<GrnHeaderEntity>>
    fun getGrnsForPO(poId: String): Flow<List<GrnHeaderEntity>>
    suspend fun getGrnById(grnId: String): GrnHeaderEntity?
    suspend fun getGrnItems(grnId: String): List<GrnItemEntity>
    fun getGrnItemsFlow(grnId: String): Flow<List<GrnItemEntity>>
    suspend fun saveGrnDraft(header: GrnHeaderEntity, items: List<GrnItemEntity>)
    suspend fun confirmGrn(grnId: String): ConfirmGrnResult
    suspend fun generateGrnNumber(): String
    suspend fun updateGrnStatus(grnId: String, status: String)
    suspend fun updateGrnItemBatchId(grnItemId: String, batchId: String)
    suspend fun getPendingGrns(): List<GrnHeaderEntity>
    suspend fun markGrnSynced(grnId: String)
}

data class ConfirmGrnResult(
    val success: Boolean,
    val newProductsCreated: Int,
    val batchesAdded: Int,
    val batchesUpdated: Int,
    val errorMessage: String? = null
)
