package com.tillzo.pos.domain.sync.usecase

import android.util.Log
import com.tillzo.pos.data.local.dao.VendorDao
import com.tillzo.pos.data.local.entity.VendorEntity
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.utils.SheetColumns
import javax.inject.Inject

/**
 * VendorUpsertUseCase — handles upsert reconciling of vendors to the Google Sheet.
 *
 * Flow:
 *   1. Read all remote rows from Vendors tab (column A = vendor_id)
 *   2. Build vendor_id → row index map
 *   3. For each pending vendor:
 *      - If ID exists in map → PUT update using batchWrite at the correct row range
 *      - If ID is new → POST append via uploadBatch
 *   4. Mark synced records locally
 *
 * Schema: Columns match SheetColumns.VENDORS order (see Constants.kt)
 */
class VendorUpsertUseCase @Inject constructor(
    private val vendorDao: VendorDao,
    private val sheetsRepository: SheetsRepository,
    private val dataSource: SheetsRemoteDataSource
) {
    companion object {
        private const val TAG = "VendorUpsertUseCase"
        private const val TABLE_NAME = "Vendors"
    }

    suspend fun invoke(): Boolean {
        return try {
            val pendingVendors = vendorDao.getPendingVendors()
            val pendingDeletions = vendorDao.getPendingSyncDeleted()

            if (pendingVendors.isEmpty() && pendingDeletions.isEmpty()) {
                Log.d(TAG, "No pending vendors to sync")
                return true
            }

            // 1. Fetch remote vendor rows and build vendor_id → row index map
            val remoteRows = dataSource.readRange("$TABLE_NAME!A:ZZ")
            val idToRowMap = mutableMapOf<String, Int>()
            if (remoteRows.isNotEmpty()) {
                val headers = remoteRows[0]
                val idIndex = headers.indexOf("vendor_id")
                if (idIndex != -1) {
                    for (i in 1 until remoteRows.size) {
                        val row = remoteRows[i]
                        if (idIndex < row.size) {
                            idToRowMap[row[idIndex]] = i + 1 // 1-based row index
                        }
                    }
                }
            }

            Log.d(TAG, "Remote vendors count: ${idToRowMap.size}, pending: ${pendingVendors.size}, pendingDeletions: ${pendingDeletions.size}")

            val itemsToUpdate = mutableListOf<Map<String, Any>>()
            val itemsToAppend = mutableListOf<VendorEntity>()

            for (vendor in pendingVendors) {
                val values = vendor.toSheetRowValues()
                if (idToRowMap.containsKey(vendor.vendorId)) {
                    val rowIndex = idToRowMap[vendor.vendorId]!!
                    itemsToUpdate.add(mapOf(
                        "range" to "$TABLE_NAME!A$rowIndex:M$rowIndex",
                        "majorDimension" to "ROWS",
                        "values" to listOf(values)
                    ))
                } else {
                    itemsToAppend.add(vendor)
                }
            }

            var anyFailure = false

            // 3a. Batch update existing rows
            if (itemsToUpdate.isNotEmpty()) {
                if (!dataSource.batchWrite(itemsToUpdate)) {
                    Log.w(TAG, "Failed to batch update existing vendors")
                    anyFailure = true
                } else {
                    Log.d(TAG, "Updated ${itemsToUpdate.size} existing vendors")
                }
            }

            // 3b. Append new rows
            if (itemsToAppend.isNotEmpty()) {
                val newRows = itemsToAppend.map { it.toSheetRowValues() }
                val result = sheetsRepository.uploadBatch(TABLE_NAME, newRows)
                if (result is com.tillzo.pos.domain.sync.SyncResult.Success) {
                    vendorDao.markMultipleSynced(itemsToAppend.map { it.vendorId })
                    Log.d(TAG, "Appended ${itemsToAppend.size} new vendors")
                } else {
                    Log.w(TAG, "Failed to append new vendors: $result")
                    anyFailure = true
                }
            }

            // 3c. Delete synced deletions from remote and local
            if (pendingDeletions.isNotEmpty()) {
                for (vendor in pendingDeletions) {
                    try {
                        val rowIndex = idToRowMap[vendor.vendorId]
                        if (rowIndex != null) {
                            if (sheetsRepository.deleteRow(TABLE_NAME, rowIndex)) {
                                Log.d(TAG, "Deleted remote row for vendor: ${vendor.vendorId}")
                            } else {
                                Log.w(TAG, "Failed to delete remote row for vendor: ${vendor.vendorId}")
                            }
                        } else {
                            Log.d(TAG, "Vendor ${vendor.vendorId} not found remotely, deleting locally only")
                        }
                        vendorDao.hardDeleteVendor(vendor.vendorId)
                        Log.d(TAG, "Hard deleted vendor locally: ${vendor.vendorId}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to delete vendor ${vendor.vendorId}: ${e.message}", e)
                        anyFailure = true
                    }
                }
            }

            // Mark all updated vendors as synced
            val updatedIds = itemsToUpdate.mapNotNull { pendingVendors.find { v -> idToRowMap.containsKey(v.vendorId) }?.vendorId }
            if (updatedIds.isNotEmpty()) {
                vendorDao.markMultipleSynced(updatedIds)
            }

            Log.d(TAG, "Vendor sync complete: ${pendingVendors.size} upserted, ${pendingDeletions.size} deleted")
            !anyFailure
        } catch (e: Exception) {
            Log.e(TAG, "Vendor sync failed: ${e.message}", e)
            false
        }
    }

    /**
     * Maps VendorEntity to a flat list matching SheetColumns.VENDORS order.
     */
    private fun VendorEntity.toSheetRowValues(): List<Any> = listOf(
        vendorId, name, phone, whatsapp, email, address,
        city, creditLimit,
        if (isActive) 1 else 0,
        if (isDeleted) 1 else 0, "synced", createdAt, updatedAt
    )
}
