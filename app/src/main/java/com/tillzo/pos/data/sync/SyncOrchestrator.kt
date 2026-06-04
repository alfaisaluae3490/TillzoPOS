package com.tillzo.pos.data.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import androidx.work.BackoffPolicy
import com.tillzo.pos.data.sync.options.delta.DeltaSyncManager
import com.tillzo.pos.data.sync.options.worker.DisasterWorker
import com.tillzo.pos.data.sync.options.worker.ShardingWorker
import com.tillzo.pos.data.sync.options.worker.SyncWorker
import com.tillzo.pos.data.sync.options.worker.ExpiryCheckWorker
import com.tillzo.pos.domain.sync.usecase.SchemaGuardUseCase
import com.tillzo.pos.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SyncOrchestrator — M2 master coordinator.
 *
 * Called from TillzoPOSApp.onCreate() to schedule all periodic workers
 * and start the DeltaSyncManager polling loop.
 *
 * WorkManager is accessed via `lazy` — NOT injected via Hilt —
 * to avoid UninitializedPropertyAccessException on TillzoPOSApp.workerFactory.
 */
@Singleton
class SyncOrchestrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deltaSyncManager: DeltaSyncManager,
    private val schemaGuardUseCase: SchemaGuardUseCase
) {
    companion object {
        private const val TAG = "SyncOrchestrator"
    }

    // Lazy: ensures WorkManager.getInstance() is deferred until after Hilt injection
    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

    /**
     * Schedules all WorkManager workers and starts delta polling.
     * Called from TillzoPOSApp.onCreate() — always AFTER super.onCreate() (Hilt done).
     */
    fun scheduleAll() {
        try {
            scheduleSyncWorkers()
            scheduleShardingWorker()
            scheduleDisasterWorker()
            scheduleExpiryCheckWorker()
        } catch (e: Exception) {
            Log.e(TAG, "Worker scheduling failed (non-fatal): ${e.message}", e)
            // Non-fatal — app continues normally. Workers retry on next launch.
        }
        // M2.3 Schema validation on App Startup explicitly runs here
        CoroutineScope(Dispatchers.IO).launch {
            try {
                schemaGuardUseCase()
            } catch (e: Exception) {
                Log.e(TAG, "M2.3 SchemaGuard application startup check failed: ${e.message}")
            }
        }
        
        // Delta polling is independent of WorkManager — always starts
        deltaSyncManager.startPolling()
    }

    fun stopAll() {
        deltaSyncManager.stopPolling()
    }

    /** M6.6 - Force an immediate sync (User clicked 'Force Sync') */
    fun triggerManualSync() {
        Log.i(TAG, "User requested Force Sync. Enqueuing OneTimeWorkRequest.")
        val manualSync = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
            
        workManager.enqueueUniqueWork(
            "manual_sync_override",
            ExistingWorkPolicy.REPLACE,
            manualSync
        )
    }

    // ── SyncWorker — Standard Auto Sync (Every 15 mins) ───────────────────
    // Note: WorkManager minimum interval is strictly 15 minutes by OS design.

    private fun scheduleSyncWorkers() {
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // Only require internet
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "AUTO_SYNC_WORKER",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    // ── ShardingWorker — Daily month check (M2.2) ────────────────────────────

    private fun scheduleShardingWorker() {
        val request = PeriodicWorkRequestBuilder<ShardingWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            Constants.WORK_NAME_SHARDING,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    // ── DisasterWorker — 23:59 daily backup (M2.9) ──────────────────────────

    private fun scheduleDisasterWorker() {
        val request = PeriodicWorkRequestBuilder<DisasterWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInitialDelay(calculateDelayToMidnight(), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            Constants.WORK_NAME_DISASTER,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun calculateDelayToMidnight(): Long {
        val now    = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) target.add(Calendar.DAY_OF_YEAR, 1)
        return (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
    }

    // ── ExpiryCheckWorker — Daily expiry notification (D/K) ──────────────────

    private fun scheduleExpiryCheckWorker() {
        val request = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateDelayToMidnight(), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            ExpiryCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
