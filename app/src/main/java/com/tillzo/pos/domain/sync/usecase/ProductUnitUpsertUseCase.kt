package com.tillzo.pos.domain.sync.usecase

import android.util.Log
import com.tillzo.pos.data.local.dao.ProductUnitDao
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.domain.sync.SyncPayload
import com.tillzo.pos.domain.sync.SyncResult
import com.tillzo.pos.utils.SheetColumns
import javax.inject.Inject

/**
 * ProductUnitUpsertUseCase
 *
 * Synchronizes Product Units with Google Sheets "Product_Units" tab.
 *
 * Flow:
 *   GET /values/Product_Units!A:G → build unitId to row index map (1-based)
 *   For each pending item: if unitId exists in map → PUT update, else POST append
 *   For pending deletions: DELETE row from Sheet → local hard-delete
 *
 * Schema: Columns A–G matching SheetColumns.PRODUCT_UNITS order:
 *   unitId, unitName, abbreviation, isDeleted, syncStatus, createdAt, updatedAt
 */
class ProductUnitUpsertUseCase @Inject constructor(
    private val productUnitDao: ProductUnitDao,
    private val sheetsRepository: SheetsRepository,
    private val dataSource: SheetsRemoteDataSource
) {
    companion object {
        private const val TAG = "ProductUnitUpsertUseCase"
        private const val TABLE_NAME = "Product_Units"
    }

    suspend operator fun invoke(posTerminalId: String): Boolean {
        return try {
            val pendingItems = productUnitDao.getPendingSyncUnits()
            val pendingDeletions = productUnitDao.getPendingSyncDeleted()

            val nothingToDo = pendingItems.isEmpty() && pendingDeletions.isEmpty()
            if (nothingToDo) {
                Log.d(TAG, "Table $TABLE_NAME: nothing to sync")
                return true
            }

            // 1. Fetch current remote rows to build unitId → row index map
            val remoteRows = dataSource.readRange("$TABLE_NAME!A:G")
            val idToRowMap = mutableMapOf<String, Int>()
            if (remoteRows.isNotEmpty()) {
                val headers = remoteRows[0]
                val idIndex = headers.indexOf("unitId")
                if (idIndex != -1) {
                    for (i in 1 until remoteRows.size) {
                        val row = remoteRows[i]
                        if (idIndex < row.size) {
                            idToRowMap[row[idIndex]] = i + 1 // 1-based row index
                        }
                    }
                }
            }

            var anyFailure = false

            // ── Upsert: new / updated units ─────────────────────────────────────
            if (pendingItems.isNotEmpty()) {
                val itemsToUpdate = mutableListOf<Map<String, Any>>()
                val itemsToAppend = mutableListOf<Map<String, Any>>()

                for (item in pendingItems) {
                    val syncMap = item.toSyncMap()
                    // Columns A–G = 7 columns matching SheetColumns.PRODUCT_UNITS order
                    val values = listOf(
                        syncMap["unitId"] ?: "",
                        syncMap["unitName"] ?: "",
                        syncMap["abbreviation"] ?: "",
                        syncMap["isDeleted"] ?: "",
                        syncMap["syncStatus"] ?: "synced",
                        syncMap["createdAt"] ?: "",
                        syncMap["updatedAt"] ?: ""
                    )

                    if (idToRowMap.containsKey(item.unitId)) {
                        val rowIndex = idToRowMap[item.unitId]
                        itemsToUpdate.add(mapOf(
                            "range" to "$TABLE_NAME!A$rowIndex:G$rowIndex",
                            "majorDimension" to "ROWS",
                            "values" to listOf(values)
                        ))
                    } else {
                        itemsToAppend.add(syncMap)
                    }
                }

                // Batch update existing rows
                if (itemsToUpdate.isNotEmpty()) {
                    if (!dataSource.batchWrite(itemsToUpdate)) anyFailure = true
                }

                // Append new rows
                if (itemsToAppend.isNotEmpty()) {
                    val payload = SyncPayload(
                        tableName = TABLE_NAME,
                        rows = itemsToAppend,
                        posTerminalId = posTerminalId
                    )
                    val result = sheetsRepository.uploadBatch(payload)
                    if (result !is SyncResult.Success) anyFailure = true
                }

                // Mark local items synced only on full success
                if (!anyFailure) {
                    pendingItems.forEach { productUnitDao.markSynced(it.unitId) }
                } else {
                    Log.w(TAG, "Upsert had failures — skipping local status update")
                    return false
                }
            }

            // ── Deletions: soft-deleted units pending removal from Sheet ───────
            if (pendingDeletions.isNotEmpty()) {
                val sheetId = try { getSheetIdForTab(TABLE_NAME) } catch (e: Exception) {
                    Log.w(TAG, "Could not get sheetId for deletions: ${e.message}")
                    -1
                }

                for (row in pendingDeletions) {
                    if (idToRowMap.containsKey(row.unitId)) {
                        if (sheetId >= 0) {
                            val sheetRowIndex = idToRowMap[row.unitId]!!
                            val deleteSuccess = dataSource.deleteRow(
                                sheetId = sheetId,
                                rowIndex = sheetRowIndex
                            )
                            if (deleteSuccess) {
                                productUnitDao.hardDeleteUnit(row.unitId)
                            } else {
                                Log.w(TAG, "Failed to delete unit ${row.unitId} from Sheet")
                                anyFailure = true
                            }
                        }
                    } else {
                        // Never made it to Sheet — safe to hard-delete locally
                        productUnitDao.hardDeleteUnit(row.unitId)
                    }
                }
            }

            !anyFailure

        } catch (e: Exception) {
            Log.e(TAG, "Error in ProductUnitUpsertUseCase: ${e.message}", e)
            false
        }
    }

    private suspend fun getSheetIdForTab(tabName: String): Int {
        val metadata = dataSource.getSheetMetadata()
        return metadata[tabName] ?: throw IllegalArgumentException("Tab $tabName not found in metadata")
    }
}
