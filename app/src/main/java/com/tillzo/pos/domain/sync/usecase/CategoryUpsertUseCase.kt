package com.tillzo.pos.domain.sync.usecase

import android.util.Log
import com.tillzo.pos.data.local.dao.CategoryDao
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.domain.sync.SyncPayload
import com.tillzo.pos.domain.sync.SyncResult
import com.tillzo.pos.utils.SheetColumns
import javax.inject.Inject

/**
 * CategoryUpsertUseCase
 *
 * Synchronizes Categories with Google Sheets.
 *
 * Flow:
 *   GET /values/Categories!A:Z → build system_row_id to row index map
 *   For each pending item: if ID exists in map → PUT update, else POST append
 *   For pending deletions: DELETE row from Sheet → local hard-delete
 *
 * Schema: Columns A–I matching SheetColumns.CATEGORIES order:
 *   system_row_id, category_name, parent_category_id, is_deleted, deleted_at,
 *   sync_status, pos_terminal_id, created_at, updated_at
 */
class CategoryUpsertUseCase @Inject constructor(
    private val categoryDao: CategoryDao,
    private val sheetsRepository: SheetsRepository,
    private val dataSource: SheetsRemoteDataSource
) {
    companion object {
        private const val TAG = "CategoryUpsertUseCase"
        private const val TABLE_NAME = "Categories"
    }

    suspend operator fun invoke(posTerminalId: String): Boolean {
        return try {
            val pendingItems = categoryDao.getPendingSyncCategories()
            val pendingDeletions = categoryDao.getPendingSyncDeleted()

            val nothingToDo = pendingItems.isEmpty() && pendingDeletions.isEmpty()
            if (nothingToDo) {
                Log.d(TAG, "Table $TABLE_NAME: nothing to sync")
                return true
            }

            // 1. Fetch current remote categories to build id → row index map
            val remoteRows = dataSource.readRange("$TABLE_NAME!A:ZZ")
            val idToRowMap = mutableMapOf<String, Int>()
            if (remoteRows.isNotEmpty()) {
                val headers = remoteRows[0]
                val idIndex = headers.indexOf("system_row_id")
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

            // ── Upsert: new / updated categories ───────────────────────────────
            if (pendingItems.isNotEmpty()) {
                val itemsToUpdate = mutableListOf<Map<String, Any>>()
                val itemsToAppend = mutableListOf<Map<String, Any>>()

                for (item in pendingItems) {
                    val syncMap = item.toSyncMap()
                    // Columns A–I = 9 columns matching SheetColumns.CATEGORIES order
                    val values = listOf(
                        syncMap["system_row_id"] ?: "",
                        syncMap["category_name"] ?: "",
                        syncMap["parent_category_id"] ?: "",
                        syncMap["is_deleted"] ?: "",
                        syncMap["deleted_at"] ?: "",
                        syncMap["sync_status"] ?: "synced",
                        syncMap["pos_terminal_id"] ?: "",
                        syncMap["created_at"] ?: "",
                        syncMap["updated_at"] ?: ""
                    )

                    if (idToRowMap.containsKey(item.system_row_id)) {
                        val rowIndex = idToRowMap[item.system_row_id]
                        itemsToUpdate.add(mapOf(
                            "range" to "$TABLE_NAME!A$rowIndex:I$rowIndex",
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
                    pendingItems.forEach { categoryDao.markSynced(it.system_row_id) }
                } else {
                    Log.w(TAG, "Upsert had failures — skipping local status update")
                    return false
                }
            }

            // ── Deletions: soft-deleted rows pending removal from Sheet ───────
            if (pendingDeletions.isNotEmpty()) {
                val sheetId = try { getSheetIdForTab(TABLE_NAME) } catch (e: Exception) {
                    Log.w(TAG, "Could not get sheetId for deletions: ${e.message}")
                    -1
                }

                for (row in pendingDeletions) {
                    if (idToRowMap.containsKey(row.system_row_id)) {
                        if (sheetId >= 0) {
                            val sheetRowIndex = idToRowMap[row.system_row_id]!!
                            val deleteSuccess = dataSource.deleteRow(
                                sheetId = sheetId,
                                rowIndex = sheetRowIndex
                            )
                            if (deleteSuccess) {
                                categoryDao.hardDeleteCategory(row.system_row_id)
                            } else {
                                Log.w(TAG, "Failed to delete category ${row.system_row_id} from Sheet")
                                anyFailure = true
                            }
                        }
                    } else {
                        // Never made it to Sheet — safe to hard-delete locally
                        categoryDao.hardDeleteCategory(row.system_row_id)
                    }
                }
            }

            !anyFailure

        } catch (e: Exception) {
            Log.e(TAG, "Error in CategoryUpsertUseCase: ${e.message}", e)
            false
        }
    }

    private suspend fun getSheetIdForTab(tabName: String): Int {
        val metadata = dataSource.getSheetMetadata()
        return metadata[tabName] ?: throw IllegalArgumentException("Tab $tabName not found in metadata")
    }
}
