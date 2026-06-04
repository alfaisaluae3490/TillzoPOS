package com.tillzo.pos.domain.sync

/**
 * The single contract both cloud backends must fulfill.
 *
 * Architecture Law (Blueprint G-5 / Section 8):
 * - RestApiSyncImpl implements this.
 * - Single backend: Sheets REST API.
 */
interface DataSyncInterface {

    /**
     * Upload a batch of pending local rows to the cloud.
     * Called by SyncWorker via WorkManager.
     * Returns [SyncResult] to determine ACK state:
     *   Success  → mark rows as sync_status = "synced"
     *   anything else → keep "pending", apply exponential backoff
     */
    suspend fun uploadBatch(payload: SyncPayload): SyncResult

    /**
     * Fetch delta rows from cloud modified after [lastTimestamp].
     * Used by Delta Sync polling (every 60s) and Force Sync button.
     */
    suspend fun fetchDelta(lastTimestamp: Long): DeltaResult
    
    /**
     * Delete a row by its index from a specific table name.
     */
    suspend fun deleteRow(tableName: String, sheetRowIndex: Int): Boolean

    /**
     * Fetch app-level settings from Sheet's Settings tab.
     * Used by: ForceUpdateChecker, BackupConfig reader, Delta Sync trigger check.
     */
    suspend fun getSettings(): AppSettings
}
