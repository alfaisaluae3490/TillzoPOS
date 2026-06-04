package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tillzo.pos.data.local.SyncLogEntity

/**
 * SyncLogDao — tracks last successful sync timestamp per table.
 *
 * Used by:
 *   SyncWorker (M2.1): reads which tables have pending rows
 *   DeltaSyncManager (M2.6): reads/writes last_synced_at per table
 *
 * Architecture Law: Only accessed via SheetsRepository, never from ViewModel/UseCase directly.
 */
@Dao
interface SyncLogDao {

    /** Get the last-synced timestamp for a specific table. Returns 0L if never synced. */
    @Query("SELECT last_synced_at FROM sync_log WHERE table_name = :tableName")
    suspend fun getLastSyncedAt(tableName: String): Long?

    /** Get all tracked table names (used by SyncWorker to iterate tables). */
    @Query("SELECT table_name FROM sync_log")
    suspend fun getAllTrackedTables(): List<String>

    /** Upsert (insert or replace) a sync log entry — updates the last_synced_at timestamp. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncLog(entity: SyncLogEntity)

    /** Ensure a table is registered in the sync log (first use). */
    @Query(
        "INSERT OR IGNORE INTO sync_log (table_name, last_synced_at, last_sync_status) " +
        "VALUES (:tableName, 0, 'never')"
    )
    suspend fun ensureTableRegistered(tableName: String)

    /** Mark a table sync as completed with current timestamp. */
    @Query(
        "UPDATE sync_log SET last_synced_at = :timestamp, last_sync_status = 'synced' " +
        "WHERE table_name = :tableName"
    )
    suspend fun markTableSynced(tableName: String, timestamp: Long)

    /** Mark a table sync as failed. */
    @Query(
        "UPDATE sync_log SET last_sync_status = 'failed' WHERE table_name = :tableName"
    )
    suspend fun markTableFailed(tableName: String)
}
