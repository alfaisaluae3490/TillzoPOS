package com.tillzo.pos.domain.sync.usecase

import android.util.Log
import com.tillzo.pos.data.local.dao.InventoryDao
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.domain.sync.SyncPayload
import javax.inject.Inject

/**
 * M6: InventoryUpsertUseCase
 *
 * REST API logic mapping (replacing Apps Script `handleInventoryUpsert`):
 *   GET /values/Inventory!A:Z
 *   If system_row_id matches: PUT /values/Inventory!A[row]:Y[row]
 *   If no match: POST /values/Inventory!A:Z:append
 *
 * CRITICAL: always processes pending deletions even when no new/updated items exist.
 */
class InventoryUpsertUseCase @Inject constructor(
    private val inventoryDao: InventoryDao,
    private val sheetsRepository: SheetsRepository,
    private val dataSource: SheetsRemoteDataSource
) {
    companion object {
        private const val TAG = "InventoryUpsertUseCase"
    }

    suspend operator fun invoke(posTerminalId: String): Boolean {
        val tableName = "Inventory"
        return try {
            // Only non-deleted items are upserted — deleted items are handled below
            val pendingItems = inventoryDao.getPendingItems().filter { !it.is_deleted }
            val pendingDeletions = inventoryDao.getPendingDeletedRows()

            val nothingToDo = pendingItems.isEmpty() && pendingDeletions.isEmpty()
            if (nothingToDo) {
                Log.d(TAG, "Table $tableName: nothing to sync")
                return true
            }

            // 1. Fetch current remote inventory to build id→row index map
            val remoteRows = dataSource.readRange("$tableName!A:ZZ")
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

            // ── Upsert: new / updated items ──────────────────────────────────
            if (pendingItems.isNotEmpty()) {
                val itemsToUpdate = mutableListOf<Map<String, Any>>()
                val itemsToAppend = mutableListOf<Map<String, Any>>()
                val updatedLocalItems = mutableListOf<com.tillzo.pos.data.local.entity.InventoryEntity>()

                for (item in pendingItems) {
                    val syncMap = item.toSyncMap()
                    // Columns A–Y = 25 columns matching SheetColumns.INVENTORY order
                    val values = listOf(
                        syncMap["system_row_id"] ?: "",
                        syncMap["barcode_id"] ?: "",
                        syncMap["name"] ?: "",
                        syncMap["sku"] ?: "",
                        syncMap["category"] ?: "",
                        syncMap["brand"] ?: "",
                        syncMap["description"] ?: "",
                        syncMap["cost_price"] ?: "",
                        syncMap["selling_price"] ?: syncMap["price"] ?: "",
                        syncMap["tax_percent"] ?: "",
                        syncMap["unit"] ?: "",
                        syncMap["stock_qty"] ?: "",
                        syncMap["low_threshold"] ?: "",
                        syncMap["batch_number"] ?: "",
                        syncMap["expiry_date"] ?: "",
                        syncMap["manufacturing_date"] ?: "",
                        syncMap["expiry_alert_days"] ?: "",
                        syncMap["is_damaged"] ?: "",
                        syncMap["damaged_qty"] ?: "",
                        syncMap["is_deleted"] ?: "",
                        syncMap["deleted_at"] ?: "",
                        "synced",
                        syncMap["pos_terminal_id"] ?: "",
                        syncMap["created_at"] ?: "",
                        syncMap["updated_at"] ?: ""
                    )

                    if (idToRowMap.containsKey(item.system_row_id)) {
                        val rowIndex = idToRowMap[item.system_row_id]
                        itemsToUpdate.add(mapOf(
                            "range" to "$tableName!A$rowIndex:Y$rowIndex",
                            "majorDimension" to "ROWS",
                            "values" to listOf(values)
                        ))
                    } else {
                        itemsToAppend.add(syncMap)
                    }
                    updatedLocalItems.add(item.copy(sync_status = "synced"))
                }

                // Batch update existing rows
                if (itemsToUpdate.isNotEmpty()) {
                    if (!dataSource.batchWrite(itemsToUpdate)) anyFailure = true
                }

                // Append new rows
                if (itemsToAppend.isNotEmpty()) {
                    val payload = SyncPayload(
                        tableName = tableName,
                        rows = itemsToAppend,
                        posTerminalId = posTerminalId
                    )
                    val result = sheetsRepository.uploadBatch(payload)
                    if (result !is com.tillzo.pos.domain.sync.SyncResult.Success) anyFailure = true
                }

                // Mark local items synced only on full success
                if (!anyFailure) {
                    updatedLocalItems.forEach { inventoryDao.updateItem(it) }
                } else {
                    Log.w(TAG, "Upsert had failures — skipping local status update")
                    return false
                }
            }

            // ── Deletions: soft-deleted rows pending removal from Sheet ───────
            if (pendingDeletions.isNotEmpty()) {
                val (onSheet, notOnSheet) = pendingDeletions.partition { idToRowMap.containsKey(it.system_row_id) }

                for (row in notOnSheet) {
                    inventoryDao.markSyncedAndDeleted(row.system_row_id)
                }

                for (row in onSheet) {
                    val rowIndex = idToRowMap[row.system_row_id]
                    if (rowIndex == null) {
                        inventoryDao.markSyncedAndDeleted(row.system_row_id)
                        continue
                    }
                    // FIX (2026-08-06): repository-level deleteRow resolves sheetId internally
                    val deleteSuccess = sheetsRepository.deleteRow(tableName, rowIndex)
                    if (deleteSuccess) {
                        inventoryDao.markSyncedAndDeleted(row.system_row_id)
                    } else {
                        Log.w(TAG, "Failed to delete row ${row.system_row_id} from Sheet")
                        anyFailure = true
                    }
                }
            }

            !anyFailure

        } catch (e: Exception) {
            Log.e(TAG, "Error in InventoryUpsertUseCase: ${e.message}", e)
            false
        }
    }
}
