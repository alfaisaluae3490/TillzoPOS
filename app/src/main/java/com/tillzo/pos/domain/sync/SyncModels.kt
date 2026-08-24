package com.tillzo.pos.domain.sync

/**
 * Payload sent to cloud during a sync batch upload.
 * Contains lists of all pending local rows grouped by table name.
 */
data class SyncPayload(
    val tableName: String,
    val rows: List<Map<String, Any>>,
    val posTerminalId: String,
    val batchTimestamp: Long = System.currentTimeMillis()
)

/**
 * Result returned after a cloud upload attempt.
 */
sealed class SyncResult {
    /** Upload succeeded — HTTP 200 OK received. */
    data class Success(val syncedCount: Int) : SyncResult()

    /** Rate limited (HTTP 429) — caller should apply exponential backoff. */
    object RateLimited : SyncResult()

    /** Server error (HTTP 500) or generic failure — apply backoff. */
    data class ServerError(val code: Int, val message: String) : SyncResult()

    /** Network timeout — treat as pending, apply backoff. */
    object Timeout : SyncResult()
}

/**
 * Delta rows fetched from cloud since [lastTimestamp].
 */
data class DeltaResult(
    val rows: List<Map<String, Any>>,
    val fetchedAt: Long = System.currentTimeMillis()
)

/**
 * App-level settings fetched from Google Sheet Settings tab.
 * Used for forced update check and backup configuration.
 */
data class AppSettings(
    val lastUpdatedTimestamp: Long,
    val minAppVersion: Int,
    val backupSheetUrl: String,
    val shopName: String,
    val shopPhone: String,
    val businessFolderId: String = "",
    val businessFolderName: String = "",
    val grnFolderId: String = "",
    val grnFolderName: String = "",
    val countryCode: String = "OTHER",
    val taxNumber: String = "",
    val taxLabel: String = "VAT",
    val defaultTaxRate: Double = 0.0,
    val taxInclusive: Boolean = true
)
