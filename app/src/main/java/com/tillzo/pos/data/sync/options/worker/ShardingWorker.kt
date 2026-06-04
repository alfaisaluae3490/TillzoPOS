package com.tillzo.pos.data.sync.options.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.utils.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * ShardingWorker — M2.2
 *
 * Monthly tab lifecycle management:
 *   - Runs daily (WorkManager PeriodicWorkRequest, 1 day interval)
 *   - Skips if today is NOT the 1st of the month (unless forced by row limit)
 *   - On 1st of month:
 *       1. Renames current Sales tab → ARCH_Sales_MMM_YYYY (archive)
 *       2. Creates new Sales_MMM_YYYY tab for current month
 *   - Row guard: if current Sales tab has >= 18,000 rows → force early sharding
 *
 * Tab naming:
 *   Active:   Sales_Mar_2026
 *   Archived: ARCH_Sales_Feb_2026
 */
@HiltWorker
class ShardingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val sheetsRepository: SheetsRepository
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ShardingWorker"

        fun currentSalesTabName(): String =
            "Sales_${SimpleDateFormat("MMM_yyyy", Locale.ENGLISH).format(Calendar.getInstance().time)}"

        fun previousSalesTabName(): String {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -1)
            return "Sales_${SimpleDateFormat("MMM_yyyy", Locale.ENGLISH).format(cal.time)}"
        }

        fun archiveName(tabName: String) = "ARCH_$tabName"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "ShardingWorker started")

        return try {
            val todayIsFistOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1
            val currentTab = currentSalesTabName()
            val prevTab    = previousSalesTabName()

            // ── Check row limit even if not 1st of month (18k guard) ─────────
            val rowCount = sheetsRepository.getRowCount(currentTab)
            val forcedByRowLimit = rowCount >= Constants.MAX_ROWS_PER_SHARD

            if (!todayIsFistOfMonth && !forcedByRowLimit) {
                Log.d(TAG, "Not 1st of month, row count=$rowCount — skipping sharding")
                return Result.success()
            }

            val reason = if (forcedByRowLimit) "row limit ($rowCount >= ${Constants.MAX_ROWS_PER_SHARD})" else "1st of month"
            Log.i(TAG, "Sharding triggered by: $reason")

            // ── Step 1: Archive previous tab ──────────────────────────────────
            val tabToArchive = if (todayIsFistOfMonth) prevTab else currentTab
            val archiveName  = archiveName(tabToArchive)

            val archived = sheetsRepository.renameTab(tabToArchive, archiveName)
            if (!archived) {
                Log.w(TAG, "Could not archive tab '$tabToArchive' — may not exist yet. Will create new tab.")
            } else {
                Log.i(TAG, "Archived '$tabToArchive' → '$archiveName'")
            }

            // ── Step 2: Create new current tab ───────────────────────────────
            val newTab    = if (forcedByRowLimit) generateForcedTabName(currentTab) else currentTab
            val created   = sheetsRepository.createTab(newTab)

            if (!created) {
                Log.e(TAG, "Failed to create new Sales tab '$newTab'")
                return Result.retry()
            }

            Log.i(TAG, "Created new Sales tab: $newTab")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "ShardingWorker error: ${e.message}", e)
            Result.retry()
        }
    }

    /** Generates a forced tab name with suffix when row limit is hit mid-month. */
    private fun generateForcedTabName(currentTab: String): String {
        val timestamp = SimpleDateFormat("dd", Locale.ENGLISH).format(Calendar.getInstance().time)
        return "${currentTab}_Part$timestamp"
    }
}
