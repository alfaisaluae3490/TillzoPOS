package com.tillzo.pos.domain.sync.usecase

import android.util.Log
import com.tillzo.pos.data.local.dao.ExpenseDao
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.domain.sync.SyncPayload
import com.tillzo.pos.domain.sync.SyncResult
import javax.inject.Inject

/**
 * ExpenseUpsertUseCase
 *
 * Synchronizes Expense entries with Google Sheets.
 */
class ExpenseUpsertUseCase @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val sheetsRepository: SheetsRepository,
    private val dataSource: SheetsRemoteDataSource
) {
    companion object {
        private const val TAG = "ExpenseUpsertUseCase"
        private const val TABLE_NAME = "Expenses"
    }

    suspend operator fun invoke(posTerminalId: String): Boolean {
        return try {
            val allPending = expenseDao.getPendingExpenses()
            val pendingItems = allPending.filter { !it.is_deleted }
            val pendingDeletions = expenseDao.getPendingDeletedRows()

            val nothingToDo = pendingItems.isEmpty() && pendingDeletions.isEmpty()
            if (nothingToDo) {
                Log.d(TAG, "Table $TABLE_NAME: nothing to sync")
                return true
            }

            // 1. Fetch current remote expenses to build id → row index map
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

            // ── Upsert: new / updated expenses ──────────────────────────────────
            if (pendingItems.isNotEmpty()) {
                val itemsToUpdate = mutableListOf<Map<String, Any>>()
                val itemsToAppend = mutableListOf<Map<String, Any>>()

                for (item in pendingItems) {
                    val syncMap = item.toSyncMap()
                    val values = listOf(
                        syncMap["system_row_id"] ?: "",
                        syncMap["category"] ?: "",
                        syncMap["amount"] ?: "",
                        syncMap["description"] ?: "",
                        syncMap["timestamp"] ?: "",
                        syncMap["logged_by_user_id"] ?: "",
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
                            "range" to "$TABLE_NAME!A$rowIndex:L$rowIndex",
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
                    pendingItems.forEach {
                        val updated = it.copy(sync_status = "synced")
                        expenseDao.update(updated)
                    }
                } else {
                    Log.w(TAG, "Upsert had failures — skipping local status update")
                    return false
                }
            }

            // ── Deletions: soft-deleted rows pending removal from Sheet ───────
            if (pendingDeletions.isNotEmpty()) {
                val (onSheet, notOnSheet) = pendingDeletions.partition { idToRowMap.containsKey(it.system_row_id) }

                for (row in notOnSheet) {
                    expenseDao.markSyncedAndDeleted(row.system_row_id)
                }

                for (row in onSheet) {
                    val rowIndex = idToRowMap[row.system_row_id]
                    if (rowIndex == null) {
                        expenseDao.markSyncedAndDeleted(row.system_row_id)
                        continue
                    }
                    // FIX (2026-08-06): repository-level deleteRow resolves sheetId internally
                    val deleteSuccess = sheetsRepository.deleteRow(TABLE_NAME, rowIndex)
                    if (deleteSuccess) {
                        expenseDao.markSyncedAndDeleted(row.system_row_id)
                    } else {
                        Log.w(TAG, "Failed to delete expense ${row.system_row_id} from Sheet")
                        anyFailure = true
                    }
                }
            }

            !anyFailure

        } catch (e: Exception) {
            Log.e(TAG, "Error in ExpenseUpsertUseCase: ${e.message}", e)
            false
        }
    }
}
