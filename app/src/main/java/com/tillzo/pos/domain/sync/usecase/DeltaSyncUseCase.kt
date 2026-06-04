package com.tillzo.pos.domain.sync.usecase

import com.tillzo.pos.domain.sync.DataSyncInterface
import com.tillzo.pos.domain.sync.DeltaResult
import javax.inject.Inject

/**
 * DeltaSyncUseCase — M2.6, M2.8
 *
 * Fetches delta rows from cloud for all terminals since [lastTimestamp].
 * Called by DeltaSyncManager every 60 seconds.
 *
 * Business rules:
 *   - lastTimestamp = 0 → full snapshot (used by DisasterWorker M2.9)
 *   - lastTimestamp > 0 → incremental delta for that window
 *   - Results include ALL terminals' data — no filtering (M2.8 multi-POS replica)
 */
class DeltaSyncUseCase @Inject constructor(
    private val syncInterface: DataSyncInterface
) {
    suspend operator fun invoke(lastTimestamp: Long): DeltaResult =
        syncInterface.fetchDelta(lastTimestamp)
}
