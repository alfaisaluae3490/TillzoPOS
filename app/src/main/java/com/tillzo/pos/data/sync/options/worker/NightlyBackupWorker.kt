package com.tillzo.pos.data.sync.options.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.repository.SheetsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * M2.9 — Nightly Backup Worker (FIX 2026-08-06: newly implemented).
 *
 * Blueprint spec: every day at 23:59, read all tabs from the primary sheet
 * and append them to a backup spreadsheet (ID from Settings.backup_sheet_url).
 * Zero server dependency — pure client-side Drive/Sheets calls.
 *
 * Previously only EmbeddedAppsScript.dailyBackup() (dead JS) mentioned this —
 * no worker existed in the app.
 */
class NightlyBackupWorker @Inject constructor(
    @ApplicationContext context: Context,
    private val params: WorkerParameters,
    private val sheetsRepository: SheetsRepository,
    private val sheetsRemoteDataSource: com.tillzo.pos.data.remote.SheetsRemoteDataSource,
    private val appSetupPrefs: AppSetupPrefs
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "NightlyBackupWorker"
    }

    override suspend fun doWork(): Result {
        val spreadsheetId = appSetupPrefs.spreadsheetId
        if (spreadsheetId.isBlank()) return Result.success()
        return try {
            val backupId = sheetsRepository.getSettings().backupSheetUrl
                .substringAfter("/spreadsheets/d/", "")
                .substringBefore("/")
                .ifBlank { null }
            if (backupId == null || backupId == spreadsheetId) {
                Log.i(TAG, "No separate backup sheet configured — skipping backup")
                return Result.success()
            }

            val metadata = sheetsRemoteDataSource.getSheetMetadata()
            var backedUp = 0
            for (tab in metadata.keys) {
                if (tab.startsWith("SYS_DB") || tab.startsWith("ARCH_")) continue
                val rows = sheetsRepository.readTabRows(tab)
                if (rows.isNotEmpty()) {
                    val ok = sheetsRepository.appendToBackup(backupId, tab, rows)
                    if (ok) backedUp++
                }
            }
            Log.i(TAG, "Nightly backup complete: $backedUp tabs → $backupId")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Nightly backup failed: ${e.message}", e)
            Result.retry()
        }
    }
}
