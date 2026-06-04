package com.tillzo.pos.data.sync.options.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.domain.sync.DataSyncInterface
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * DisasterWorker — M2.9
 *
 * Daily full backup at ~11:59 PM.
 *
 * Flow:
 *   1. Fetch all data tabs via DataSyncInterface.fetchDelta(0) — full snapshot
 *   2. Write to backup_spreadsheet_id from AppSettings.backupSheetUrl
 *   3. Skip if backupSheetUrl is empty (user hasn't configured backup yet)
 *
 * Scheduled by WorkerModule as a daily PeriodicWorkRequest.
 * WorkManager will fire it approximately once per day — exact timing uses
 * setInitialDelay calculated to target ~23:59 on first run.
 */
@HiltWorker
class DisasterWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncInterface: DataSyncInterface,
    private val appSetupPrefs: AppSetupPrefs
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "DisasterWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "DisasterWorker started — daily backup")

        return try {
            // Step 1: Fetch app settings to get backup Sheet ID
            val settings = syncInterface.getSettings()

            val backupSheetUrl = settings.backupSheetUrl
            if (backupSheetUrl.isBlank()) {
                Log.i(TAG, "Backup Sheet URL not configured — skipping disaster recovery")
                return Result.success()  // Not an error — backup is optional
            }

            // Step 2: Full data snapshot (fetchDelta with timestamp=0 = all rows)
            val delta = syncInterface.fetchDelta(lastTimestamp = 0L)

            if (delta.rows.isEmpty()) {
                Log.i(TAG, "No data to back up yet")
                return Result.success()
            }

            Log.i(TAG, "Disaster backup: ${delta.rows.size} rows fetched, writing to backup sheet")

            // Step 3: Write to backup Sheet
            // Backup sheet ID is extracted from backupSheetUrl
            // URL format: https://docs.google.com/spreadsheets/d/{spreadsheetId}/edit
            val backupId = extractSpreadsheetId(backupSheetUrl)
            if (backupId.isNullOrBlank()) {
                Log.e(TAG, "Could not extract spreadsheet ID from backupSheetUrl: $backupSheetUrl")
                return Result.failure()
            }

            // TODO: Write delta.rows to backupId via REST API
            // This requires a SheetsRepository method that accepts a custom spreadsheetId
            // Will be wired in M3 when SaleEntity exists and data volumes are real
            Log.i(TAG, "Disaster backup framework ready — backupId=$backupId, rows=${delta.rows.size}")
            Log.i(TAG, "M3+: Wire actual write call here via sheetsRepository.writeToCrossSheet(backupId, rows)")

            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "DisasterWorker error: ${e.message}", e)
            Result.retry()
        }
    }

    /**
     * Extracts the spreadsheet ID from a Google Sheets URL.
     * Handles formats:
     *   https://docs.google.com/spreadsheets/d/{id}/edit
     *   https://docs.google.com/spreadsheets/d/{id}
     *   Just the raw ID
     */
    private fun extractSpreadsheetId(url: String): String? {
        if (!url.contains("spreadsheets")) return url  // Assume it's already a raw ID
        return Regex("/d/([a-zA-Z0-9_-]+)").find(url)?.groupValues?.getOrNull(1)
    }
}
