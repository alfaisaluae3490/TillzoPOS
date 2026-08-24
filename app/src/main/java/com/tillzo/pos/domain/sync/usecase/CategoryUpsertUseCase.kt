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
            // OVERNIGHT-AUDIT D2-1 FIX (2026-08-23): track read success. On API failure
            // (e.g. HTTP 429) the map is empty and treating deletions as "not on sheet"
            // hard-deletes the local marker forever → orphaned sheet rows. Now we
            // distinguish "read failed" from "genuinely empty" and defer deletions.
            val remoteRead = dataSource.readRangeResult("$TABLE_NAME!A:ZZ")
            val remoteRows = remoteRead.rows
            val readFailed = !remoteRead.success
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
            // OVERNIGHT-AUDIT D2-1: if remote read failed, DEFER deletions (don't
            // hard-delete markers) so the next successful sync removes sheet rows.
            if (pendingDeletions.isNotEmpty()) {
                if (readFailed) {
                    Log.w(TAG, "Remote read failed — deferring ${pendingDeletions.size} category deletion(s) to next sync")
                    return false
                }
                val (onSheet, notOnSheet) = pendingDeletions.partition { idToRowMap.containsKey(it.system_row_id) }

                for (row in notOnSheet) {
                    categoryDao.hardDeleteCategory(row.system_row_id)
                    Log.d(TAG, "Hard deleted local-only category: ${row.system_row_id}")
                }

                for (row in onSheet) {
                    val rowIndex = idToRowMap[row.system_row_id]
                    if (rowIndex == null) {
                        categoryDao.hardDeleteCategory(row.system_row_id)
                        Log.d(TAG, "Category ${row.system_row_id} missing row index — hard deleting locally")
                        continue
                    }
                    // FIX (2026-08-05, TillzoTest Bug #2): use the repository-level
                    // deleteRow which resolves sheetId from metadata internally and
                    // reports failure properly. The old getSheetIdForTab() could throw
                    // (tab missing from metadata) → sheetId=-1 → delete silently skipped
                    // while the local row was left in a permanent pending state.
                    val deleteSuccess = sheetsRepository.deleteRow(TABLE_NAME, rowIndex)
                    if (deleteSuccess) {
                        categoryDao.hardDeleteCategory(row.system_row_id)
                        Log.d(TAG, "Deleted remote row for category: ${row.system_row_id}")
                    } else {
                        Log.w(TAG, "Failed to delete category ${row.system_row_id} from Sheet")
                        anyFailure = true
                    }
                }
            }

            !anyFailure

        } catch (e: Exception) {
            Log.e(TAG, "Error in CategoryUpsertUseCase: ${e.message}", e)
            false
        }
    }
}
