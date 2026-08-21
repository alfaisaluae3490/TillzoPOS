package com.tillzo.pos.data.sync.options.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.repository.SheetsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * M2.2 — Monthly Sharding Worker (FIX 2026-08-06: newly implemented).
 *
 * Blueprint spec: on the 1st of every month at 00:01, create the new
 * Sales_MMM_YYYY tab and archive the previous month (rename to
 * ARCH_Sales_MMM_YYYY). Also enforces the 18,000-row hard limit — if the
 * current month tab exceeds it, an overflow tab is force-created.
 *
 * Previously this existed only as comments/docstrings — no worker was ever
 * registered, so the 18k row check NEVER ran and tabs were never archived.
 */
class MonthlyShardWorker @Inject constructor(
    @ApplicationContext context: Context,
    private val params: WorkerParameters,
    private val sheetsRepository: SheetsRepository,
    private val sheetsRemoteDataSource: com.tillzo.pos.data.remote.SheetsRemoteDataSource,
    private val appSetupPrefs: AppSetupPrefs
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "MonthlyShardWorker"
        private const val ROW_LIMIT = 18_000
    }

    override suspend fun doWork(): Result {
        if (appSetupPrefs.spreadsheetId.isBlank()) return Result.success()
        return try {
            val now = Calendar.getInstance()
            val currentMonth = SimpleDateFormat("MMM_yyyy", Locale.US).format(now.time)

            // 1. Ensure current month tab exists
            val metadata = sheetsRemoteDataSource.getSheetMetadata()
            val currentTab = "Sales_$currentMonth"
            if (!metadata.containsKey(currentTab)) {
                val created = sheetsRepository.createTab(currentTab)
                Log.i(TAG, "Created sales tab $currentTab: $created")
            }

            // 2. Archive previous month if it exists and hasn't been archived
            val prev = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
            val prevMonth = SimpleDateFormat("MMM_yyyy", Locale.US).format(prev.time)
            val prevTab = "Sales_$prevMonth"
            val archTab = "ARCH_Sales_$prevMonth"
            if (metadata.containsKey(prevTab) && !metadata.containsKey(archTab)) {
                val renamed = sheetsRepository.renameTab(prevTab, archTab)
                Log.i(TAG, "Archived $prevTab → $archTab: $renamed")
            }

            // 3. Row-limit check on the active tab
            val rowCount = sheetsRepository.getRowCount(currentTab)
            if (rowCount >= ROW_LIMIT) {
                val overflow = "Sales_${currentMonth}_OVF"
                if (!metadata.containsKey(overflow)) {
                    sheetsRepository.createTab(overflow)
                    Log.w(TAG, "Row limit ($ROW_LIMIT) exceeded — created overflow tab $overflow")
                }
            }

            Log.i(TAG, "Monthly sharding complete (rows=$rowCount)")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sharding failed: ${e.message}", e)
            Result.retry()
        }
    }
}
