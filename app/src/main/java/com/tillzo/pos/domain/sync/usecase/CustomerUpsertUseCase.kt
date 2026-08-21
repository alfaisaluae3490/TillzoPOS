package com.tillzo.pos.domain.sync.usecase

import android.util.Log
import com.tillzo.pos.data.local.dao.CustomerDao
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.domain.sync.SyncPayload
import com.tillzo.pos.domain.sync.SyncResult
import javax.inject.Inject

/**
 * CustomerUpsertUseCase
 *
 * Synchronizes CRM Customer profiles with Google Sheets.
 */
class CustomerUpsertUseCase @Inject constructor(
    private val customerDao: CustomerDao,
    private val sheetsRepository: SheetsRepository,
    private val dataSource: SheetsRemoteDataSource
) {
    companion object {
        private const val TAG = "CustomerUpsertUseCase"
        private const val TABLE_NAME = "Customers"
    }

    suspend operator fun invoke(posTerminalId: String): Boolean {
        return try {
            val allPending = customerDao.getPendingCustomers()
            val pendingItems = allPending.filter { !it.is_deleted }
            val pendingDeletions = customerDao.getPendingDeletedRows()

            val nothingToDo = pendingItems.isEmpty() && pendingDeletions.isEmpty()
            if (nothingToDo) {
                Log.d(TAG, "Table $TABLE_NAME: nothing to sync")
                return true
            }

            // 1. Fetch current remote customers to build id → row index map
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

            // ── Upsert: new / updated customers ─────────────────────────────────
            if (pendingItems.isNotEmpty()) {
                val itemsToUpdate = mutableListOf<Map<String, Any>>()
                val itemsToAppend = mutableListOf<Map<String, Any>>()

                for (item in pendingItems) {
                    val syncMap = item.toSyncMap()
                    val values = listOf(
                        syncMap["system_row_id"] ?: "",
                        syncMap["name"] ?: "",
                        syncMap["phone"] ?: "",
                        syncMap["whatsapp"] ?: "",
                        syncMap["email"] ?: "",
                        syncMap["address"] ?: "",
                        syncMap["loyalty_points"] ?: 0.0,
                        syncMap["lifetime_spend"] ?: 0.0,
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
                            "range" to "$TABLE_NAME!A$rowIndex:N$rowIndex",
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
                        customerDao.update(updated)
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
                    customerDao.markSyncedAndDeleted(row.system_row_id)
                }

                for (row in onSheet) {
                    val rowIndex = idToRowMap[row.system_row_id]
                    if (rowIndex == null) {
                        customerDao.markSyncedAndDeleted(row.system_row_id)
                        continue
                    }
                    // FIX (2026-08-06): repository-level deleteRow resolves sheetId
                    // internally — old getSheetIdForTab() threw when tab missing from
                    // metadata → sheetId=-1 → remote delete silently skipped and the
                    // local row stayed pending forever (same bug Category had).
                    val deleteSuccess = sheetsRepository.deleteRow(TABLE_NAME, rowIndex)
                    if (deleteSuccess) {
                        customerDao.markSyncedAndDeleted(row.system_row_id)
                    } else {
                        Log.w(TAG, "Failed to delete customer ${row.system_row_id} from Sheet")
                        anyFailure = true
                    }
                }
            }

            !anyFailure

        } catch (e: Exception) {
            Log.e(TAG, "Error in CustomerUpsertUseCase: ${e.message}", e)
            false
        }
    }
}
