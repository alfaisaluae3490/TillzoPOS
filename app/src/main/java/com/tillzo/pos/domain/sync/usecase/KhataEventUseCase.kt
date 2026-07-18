package com.tillzo.pos.domain.sync.usecase

import android.util.Log
import com.tillzo.pos.data.local.dao.KhataEventDao
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.domain.sync.SyncPayload
import com.tillzo.pos.domain.sync.SyncResult
import javax.inject.Inject

/**
 * M7: KhataEventUseCase
 *
 * Appends new KhataEvents to the Google Sheet.
 * REST API logic mapping (replacing Apps Script `handleKhataEvent`):
 *   POST :append (via SheetsRepository.uploadBatch)
 *
 * CRITICAL RULE (G-3 / Section 6.1): Append-Only Ledger.
 * NEVER issue a PUT or DELETE for Khata_Events.
 */
class KhataEventUseCase @Inject constructor(
    private val khataEventDao: KhataEventDao,
    private val sheetsRepository: SheetsRepository
) {
    companion object {
        private const val TAG = "KhataEventUseCase"
    }

    suspend operator fun invoke(tableName: String, posTerminalId: String): Boolean {
        return try {
            val pendingEvents = khataEventDao.getPendingKhataEvents()

            if (pendingEvents.isEmpty()) {
                Log.d(TAG, "Table $tableName: 0 pending rows — nothing to upload")
                return true
            }

            // Convert to SyncMap representation
            val payloadRows = pendingEvents.map { it.toSyncMap() }

            val payload = SyncPayload(
                tableName = tableName,
                rows = payloadRows,
                posTerminalId = posTerminalId
            )

            val result = sheetsRepository.uploadBatch(payload)
            
            when (result) {
                is SyncResult.Success -> {
                    // M2.1 Rule: HTTP 200 OK -> mark synced locally
                    pendingEvents.forEach { event ->
                        val updatedEvent = event.copy(sync_status = "synced")
                        khataEventDao.update(updatedEvent)
                    }
                    
                    true
                }
                else -> {
                    Log.w(TAG, "Khata Events upload failed, keep pending. Result: $result")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in KhataEventUseCase: ${e.message}", e)
            false
        }
    }
}
