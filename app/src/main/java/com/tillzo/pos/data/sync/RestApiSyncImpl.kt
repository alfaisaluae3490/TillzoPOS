package com.tillzo.pos.data.sync

import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.domain.sync.AppSettings
import com.tillzo.pos.domain.sync.DataSyncInterface
import com.tillzo.pos.domain.sync.DeltaResult
import com.tillzo.pos.domain.sync.SyncPayload
import com.tillzo.pos.domain.sync.SyncResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RestApiSyncImpl — Google Sheets REST API backend.
 * Active backend implementation leveraging the SheetsRepository.
 *
 * Architecture Law: DataSyncInterface implementation delegates to SheetsRepository.
 * Chain: WorkManager → Repository → RestApiSyncImpl → SheetsRepository → SheetsRemoteDataSource
 *
 * No direct HTTP calls here — all network logic in SheetsRemoteDataSource.
 */
@Singleton
class RestApiSyncImpl @Inject constructor(
    private val sheetsRepository: SheetsRepository
) : DataSyncInterface {

    override suspend fun uploadBatch(payload: SyncPayload): SyncResult =
        sheetsRepository.uploadBatch(payload)

    override suspend fun fetchDelta(lastTimestamp: Long): DeltaResult =
        sheetsRepository.fetchDelta(lastTimestamp)
        
    override suspend fun deleteRow(tableName: String, sheetRowIndex: Int): Boolean =
        sheetsRepository.deleteRow(tableName, sheetRowIndex)

    override suspend fun getSettings(): AppSettings =
        sheetsRepository.getSettings()
}
