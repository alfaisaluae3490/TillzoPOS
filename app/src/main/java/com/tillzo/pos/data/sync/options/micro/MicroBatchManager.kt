package com.tillzo.pos.data.sync.options.micro

import android.util.Log
import com.tillzo.pos.domain.sync.DataSyncInterface
import com.tillzo.pos.domain.sync.SyncPayload
import com.tillzo.pos.domain.sync.SyncResult
import com.tillzo.pos.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MicroBatchManager — M2.5
 *
 * 20-second micro-batching to avoid Google Sheets API 60 req/min quota.
 *
 * How it works:
 *   1. M4 SaleRepository calls enqueue(tableName, row) after each local Room save
 *   2. MicroBatchManager collects rows in memory for 20 seconds
 *   3. After 20s with no new enqueues — fires a single POST with all collected rows
 *   4. Result: 60 sales in 20s = 1 API call instead of 60
 *
 * Blueprint M2.5:
 *   "Sales debounce timer: 20s after first sale.
 *    All sales in window → single JSON array → one POST /values/{range}:append"
 */
@Singleton
class MicroBatchManager @Inject constructor(
    private val syncInterface: DataSyncInterface
) {
    companion object {
        private const val TAG = "MicroBatchManager"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Pending rows per table: tableName → list of rows
    private val pendingRows = mutableMapOf<String, MutableList<Map<String, Any>>>()

    // Debounce timer job per table — reset on each new enqueue
    private val debounceJobs = mutableMapOf<String, Job?>()

    // Terminal ID — set from SignInViewModel after sign-in
    var posTerminalId: String = "terminal_1"

    /**
     * Enqueue a row for deferred batched upload.
     * Called by M4 SaleRepository immediately after local Room save.
     *
     * @param tableName  The target Google Sheet tab (e.g., "Sales_Mar_2026")
     * @param row        The row data map with all required columns
     */
    fun enqueue(tableName: String, row: Map<String, Any>) {
        synchronized(pendingRows) {
            pendingRows.getOrPut(tableName) { mutableListOf() }.add(row)
        }

        // Cancel existing debounce timer for this table and start a fresh 20s window
        debounceJobs[tableName]?.cancel()
        debounceJobs[tableName] = scope.launch {
            Log.d(TAG, "Table $tableName: 20s window started, pending=${pendingRows[tableName]?.size}")
            delay(Constants.MICRO_BATCH_WINDOW_MS)
            flush(tableName)
        }
    }

    /**
     * Immediately flushes all pending rows for a table (e.g., on app background/close).
     * Called from lifecycle-aware component when app goes to background.
     */
    fun flushAll() {
        val tables = synchronized(pendingRows) { pendingRows.keys.toList() }
        tables.forEach { tableName ->
            debounceJobs[tableName]?.cancel()
            scope.launch { flush(tableName) }
        }
    }

    /**
     * Flush rows for a specific table — fires the actual API call.
     */
    private suspend fun flush(tableName: String) {
        val rowsToUpload = synchronized(pendingRows) {
            val rows = pendingRows[tableName]?.toList() ?: return
            pendingRows.remove(tableName)
            rows
        }

        if (rowsToUpload.isEmpty()) return

        Log.i(TAG, "Flushing $tableName: ${rowsToUpload.size} rows in single batch POST")

        val payload = SyncPayload(
            tableName     = tableName,
            rows          = rowsToUpload,
            posTerminalId = posTerminalId
        )

        val result = try {
            syncInterface.uploadBatch(payload)
        } catch (e: Exception) {
            Log.e(TAG, "Micro-batch upload error: ${e.message}", e)
            SyncResult.ServerError(-1, e.message ?: "Unknown")
        }

        when (result) {
            is SyncResult.Success     -> Log.i(TAG, "Batch uploaded: ${result.syncedCount} rows")
            is SyncResult.RateLimited -> {
                Log.w(TAG, "Rate limited — re-queuing rows for retry")
                // Re-enqueue rows individually to trigger a new 20s window
                rowsToUpload.forEach { enqueue(tableName, it) }
            }
            is SyncResult.ServerError -> Log.e(TAG, "Server error ${result.code}: ${result.message}")
            is SyncResult.Timeout     -> Log.w(TAG, "Timeout — rows lost. SyncWorker will recover from Room.")
        }
    }
}
