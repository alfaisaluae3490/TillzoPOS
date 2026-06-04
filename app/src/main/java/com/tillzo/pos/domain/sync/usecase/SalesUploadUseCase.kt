package com.tillzo.pos.domain.sync.usecase

import android.util.Log
import com.tillzo.pos.data.local.dao.SaleDao
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.domain.sync.SyncPayload
import com.tillzo.pos.domain.sync.SyncResult
import javax.inject.Inject

/**
 * M3/M4: SalesUploadUseCase
 *
 * Appends new Sales to the Google Sheet.
 * Includes a UUID deduplication check by reading the Sheet's A column first.
 *
 * REST API logic mapping (replacing Apps Script `handleSalesUpload`):
 *   GET /values/Sales_[MMM_YYYY]!A:A (checked via SheetsRepository.getExistingUuids)
 *   POST :append (via SheetsRepository.uploadBatch)
 */
class SalesUploadUseCase @Inject constructor(
    private val saleDao: SaleDao,
    private val sheetsRepository: SheetsRepository
) {
    companion object {
        private const val TAG = "SalesUploadUseCase"
    }

    suspend operator fun invoke(tableName: String, posTerminalId: String): Boolean {
        return try {
            val pendingSales = saleDao.getPendingSyncSales()

            if (pendingSales.isEmpty()) {
                Log.d(TAG, "Table $tableName: 0 pending rows — nothing to upload")
                return true
            }

            // M2.1 UUID dedupe check — prevents double-writes on retry
            val existingIds = sheetsRepository.getExistingUuids(tableName)
            val newSales = pendingSales.filter { sale ->
                sale.system_row_id !in existingIds
            }

            if (newSales.isEmpty()) {
                Log.d(TAG, "Table $tableName: all rows already uploaded — deduped")
                // Still mark them synced locally so they aren't processed forever
                pendingSales.forEach { sale ->
                    val updatedSale = sale.copy(sync_status = "synced")
                    saleDao.updateSale(updatedSale)
                }
                return true
            }

            // Convert to SyncMap representation
            val payloadRows = newSales.map { it.toSyncMap() }

            val payload = SyncPayload(
                tableName = tableName,
                rows = payloadRows,
                posTerminalId = posTerminalId
            )

            val result = sheetsRepository.uploadBatch(payload)
            
            when (result) {
                is SyncResult.Success -> {
                    // M2.1 Rule: HTTP 200 OK -> mark synced locally
                    newSales.forEach { sale ->
                        val updatedSale = sale.copy(sync_status = "synced")
                        saleDao.updateSale(updatedSale)
                    }
                    
                    // Process Pending Deletions
                    processDeletions(tableName)
                    
                    true
                }
                else -> {
                    Log.w(TAG, "Sales upload failed, keep pending. Result: $result")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in SalesUploadUseCase: ${e.message}", e)
            false
        }
    }

    private suspend fun processDeletions(tableName: String) {
        val deletedRows = saleDao.getPendingDeletedRows()
        if (deletedRows.isEmpty()) return

        // Wait to optimize imports by manually doing metadata and idToRowMapping since sales is append only
        // Normally sales are never deleted.
    }
}
