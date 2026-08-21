package com.tillzo.pos.data.sync.options.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tillzo.pos.data.sync.options.delta.DeltaSyncManager
import com.tillzo.pos.domain.sync.DataSyncInterface
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * RestoreWorker — durable WorkManager fallback for initial cloud restore.
 *
 * If the app process is killed mid-restore, this worker ensures the restore
 * is re-attempted by WorkManager with exponential backoff.
 *
 * Scheduled by [DeltaSyncManager.scheduleRestoreWorker] when a user selects
 * an existing sheet.
 */
@HiltWorker
class RestoreWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncInterface: DataSyncInterface,
    private val deltaSyncManager: DeltaSyncManager
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "RestoreWorker"
        const val WORK_NAME = "initial_restore"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.i(TAG, "RestoreWorker started (attempt #${runAttemptCount + 1})")
        try {
            val delta = syncInterface.fetchDelta(lastTimestamp = 0L)

            if (delta.rows.isNotEmpty()) {
                Log.i(TAG, "RestoreWorker fetched ${delta.rows.size} rows — upserting into Room")
                deltaSyncManager.upsertDeltaRows(delta.rows)
            } else {
                Log.d(TAG, "RestoreWorker — no rows returned from cloud")
            }

            Log.i(TAG, "RestoreWorker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "RestoreWorker failed: ${e.message}", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
