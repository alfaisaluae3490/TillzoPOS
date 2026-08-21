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
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.sync.options.delta.DeltaSyncManager
import com.tillzo.pos.data.sync.options.worker.SyncWorker
import com.tillzo.pos.data.sync.options.worker.ExpiryCheckWorker
import com.tillzo.pos.data.sync.options.worker.MonthlyShardWorker
import com.tillzo.pos.data.sync.options.worker.NightlyBackupWorker
import com.tillzo.pos.data.sync.options.worker.AutoLocalBackupWorker
import com.tillzo.pos.domain.sync.usecase.SchemaGuardUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private val schemaGuardUseCase: SchemaGuardUseCase,
    private val appSetupPrefs: AppSetupPrefs
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
            scheduleExpiryCheckWorker()
            scheduleMonthlyShardWorker()
            scheduleNightlyBackupWorker()
            scheduleAutoLocalBackupWorker()
        } catch (e: Exception) {
            Log.e(TAG, "Worker scheduling failed (non-fatal): ${e.message}", e)
            // Non-fatal — app continues normally. Workers retry on next launch.
        }
        if (appSetupPrefs.spreadsheetId.isBlank()) {
            Log.w(TAG, "spreadsheetId not configured — skipping schema guard and delta sync. Setup not completed.")
            return
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

    /** Exposes WorkInfo flow for the manual sync request to observe its state. */
    fun getManualSyncWorkInfo(): kotlinx.coroutines.flow.Flow<List<androidx.work.WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkFlow("manual_sync_override")
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

    private fun calculateDelayToMidnight(): Long {
        val now    = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) target.add(java.util.Calendar.DAY_OF_YEAR, 1)
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

    // FIX (2026-08-06): M2.2 Monthly Sharding + M2.9 Nightly Backup workers —
    // previously only referenced in comments/docstrings, never registered.
    private fun scheduleMonthlyShardWorker() {
        // Runs daily; first run delayed to the 1st of next month at 00:01.
        val now = java.util.Calendar.getInstance()
        val nextMonth1st = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.MONTH, 1)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 1)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val delay = (nextMonth1st.timeInMillis - now.timeInMillis).coerceAtLeast(60_000L)
        val request = PeriodicWorkRequestBuilder<MonthlyShardWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            MonthlyShardWorker::class.java.simpleName,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun scheduleNightlyBackupWorker() {
        val request = PeriodicWorkRequestBuilder<NightlyBackupWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateDelayToMidnight(), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            NightlyBackupWorker::class.java.simpleName,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    // FIX (2026-08-06): Faisal's requirement — local backup copy that survives
    // uninstall/reinstall. Runs 15 min after midnight (00:15), public Documents.
    private fun scheduleAutoLocalBackupWorker() {
        val now = java.util.Calendar.getInstance()
        val next = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 15)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        if (next.timeInMillis <= now.timeInMillis) {
            next.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        val delay = next.timeInMillis - now.timeInMillis
        val request = PeriodicWorkRequestBuilder<AutoLocalBackupWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            AutoLocalBackupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
