package com.tillzo.pos.data.sync.options.delta

import android.util.Log
import com.tillzo.pos.data.local.AppDatabase
import com.tillzo.pos.data.local.SyncLogEntity
import com.tillzo.pos.domain.sync.DataSyncInterface
import com.tillzo.pos.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DeltaSyncManager — M2.6 + M2.8
 *
 * Background polling: every 60 seconds, checks for remote updates from ALL terminals.
 *
 * M2.6 — Delta Sync:
 *   1. GET Settings tab → read last_updated_timestamp
 *   2. Compare with local SyncLogDao.getLastSyncedAt("delta_cursor")
 *   3. If remote > local → GET updated rows → UPSERT via system_row_id in Room
 *
 * M2.8 — Multi-POS Sync:
 *   Delta fetch includes ALL terminals' data (no terminal filter).
 *   Every device maintains a complete replica of all POS terminals.
 *   Terminal A can search/refund Terminal B's sale offline.
 *
 * Lifecycle:
 *   startPolling() → called from SignInViewModel after successful sign-in
 *   stopPolling()  → called on sign-out or app destruction
 */
@Singleton
class DeltaSyncManager @Inject constructor(
    private val syncInterface: DataSyncInterface,
    private val appDatabase: AppDatabase
) {
    companion object {
        private const val TAG = "DeltaSyncManager"
        /** Special "table name" used in SyncLogDao to track the delta cursor position. */
        private const val DELTA_CURSOR_KEY = "delta_cursor"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null

    /**
     * Starts the 60-second delta sync poll loop.
     * Safe to call multiple times — existing loop is cancelled before starting new one.
     */
    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            Log.i(TAG, "Delta sync polling started (every ${Constants.DELTA_SYNC_INTERVAL_MS / 1000}s)")

            // Register delta cursor in SyncLog if not already tracked
            appDatabase.syncLogDao().ensureTableRegistered(DELTA_CURSOR_KEY)

            while (isActive) {
                try {
                    pollOnce()
                } catch (e: Exception) {
                    Log.e(TAG, "Delta sync poll error: ${e.message}", e)
                }
                delay(Constants.DELTA_SYNC_INTERVAL_MS)
            }
        }
    }

    /**
     * Stops the polling loop. Called on sign-out or app destruction.
     */
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        Log.i(TAG, "Delta sync polling stopped")
    }

    /**
     * Performs a single delta sync cycle.
     */
    private suspend fun pollOnce() {
        // Step 1: Fetch remote Settings for last_updated_timestamp
        val settings = syncInterface.getSettings()
        val remoteTimestamp = settings.lastUpdatedTimestamp

        if (remoteTimestamp == 0L) {
            Log.d(TAG, "Remote timestamp=0 — Settings tab not yet populated. Skipping.")
            return
        }

        // Step 2: Compare with local cursor
        val localTimestamp = appDatabase.syncLogDao()
            .getLastSyncedAt(DELTA_CURSOR_KEY) ?: 0L

        if (remoteTimestamp <= localTimestamp) {
            Log.d(TAG, "No remote updates (remote=$remoteTimestamp, local=$localTimestamp)")
            return
        }

        Log.i(TAG, "Remote updates detected (remote=$remoteTimestamp > local=$localTimestamp)")

        // Step 3: Fetch delta rows (ALL terminals — M2.8 Multi-POS replica sync)
        val delta = syncInterface.fetchDelta(lastTimestamp = localTimestamp)

        if (delta.rows.isEmpty()) {
            Log.d(TAG, "Delta fetch returned 0 rows")
        } else {
            Log.i(TAG, "Fetched ${delta.rows.size} delta rows from all terminals")

            // Step 4: UPSERT delta rows into Room via system_row_id
            // M3+: Each module registers its own UPSERT handler here
            // e.g., saleDao.upsertBySysId(rows.filter { it["table"] == "sales" }.map { ... })
            upsertDeltaRows(delta.rows)
        }

        // Step 5: Update local cursor to remote timestamp
        appDatabase.syncLogDao().upsertSyncLog(
            SyncLogEntity(
                table_name     = DELTA_CURSOR_KEY,
                lastSyncedAt   = remoteTimestamp,
                lastSyncStatus = "synced"
            )
        )
    }

    /**
     * UPSERT delta rows into Room.
     *
     * Blueprint M2.7 — Immutable UUID PK:
     *   Upsert keyed on system_row_id (UUID, never changes).
     *   barcode_id / product_name changes do NOT create new rows — they update existing.
     *
     * Blueprint M2.8 — Multi-POS:
     *   Rows from ALL terminals are merged — every device becomes a complete replica.
     *
     * NOTE: M3+ modules inject their DAOs here.
     * For M2 with no entity DAOs yet, this is a framework with log output only.
     */
    private suspend fun upsertDeltaRows(rows: List<Map<String, Any>>) {
        Log.i(TAG, "UPSERT framework: ${rows.size} rows by system_row_id")
        Log.i(TAG, "M3+: wire saleDao.upsertBySysId / inventoryDao.upsertBySysId here")

        // Example M3 wiring (not active yet):
        // rows.groupBy { it["sheet_tab"] as? String }
        //     .forEach { (tab, tabRows) ->
        //         when {
        //             tab?.startsWith("Sales_") == true →
        //                 saleDao.upsertAll(tabRows.map { SaleEntity.fromMap(it) })
        //             tab == "Inventory" →
        //                 inventoryDao.upsertAll(tabRows.map { InventoryItemEntity.fromMap(it) })
        //         }
        //     }
    }
}
