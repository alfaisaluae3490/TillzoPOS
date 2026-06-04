package com.tillzo.pos.domain.sync.usecase

import com.tillzo.pos.domain.sync.DataSyncInterface
import com.tillzo.pos.domain.sync.SyncPayload
import com.tillzo.pos.domain.sync.SyncResult
import javax.inject.Inject

/**
 * SyncUploadUseCase — M2.1 domain layer
 *
 * Thin orchestrator: validates payload then delegates to DataSyncInterface.uploadBatch.
 * Architecture Law: UseCase contains business rules, not HTTP logic.
 *
 * Chain: SyncWorker → SyncUploadUseCase → DataSyncInterface → RestApiSyncImpl
 *        → SheetsRepository → SheetsRemoteDataSource → SheetsApiClient
 */
class SyncUploadUseCase @Inject constructor(
    private val syncInterface: DataSyncInterface
) {
    /**
     * Upload a batch of pending rows to the cloud.
     *
     * Business rules:
     *   - Empty payload → return Success(0), no network call
     *   - Non-empty → delegate to DataSyncInterface for actual HTTP POST
     */
    suspend operator fun invoke(payload: SyncPayload): SyncResult {
        if (payload.rows.isEmpty()) return SyncResult.Success(0)
        return syncInterface.uploadBatch(payload)
    }
}
