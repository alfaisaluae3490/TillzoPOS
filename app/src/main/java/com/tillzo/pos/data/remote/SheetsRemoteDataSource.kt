package com.tillzo.pos.data.remote

import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SheetsRemoteDataSource — DataSource layer for Google Sheets REST API.
 *
 * Architecture Law (v3 Blueprint M1.3):
 *   Uses SheetsApiClient (Retrofit2) for all HTTP calls.
 *   Never called directly from UseCase or ViewModel — only via SheetsRepository.
 *
 * Chain: UseCase → SheetsRepository → SheetsRemoteDataSource → SheetsApiClient
 */
@Singleton
class SheetsRemoteDataSource @Inject constructor(
    private val apiClient: SheetsApiClient,
    private val appSetupPrefs: AppSetupPrefs
) {
    private val api: SheetsApiService = apiClient.createService<SheetsApiService>()

    private val spreadsheetId: String get() = appSetupPrefs.spreadsheetId

    // ── Drive Search & Tagging ───────────────────────────────────────────────

    data class ExistingSheetInfo(
        val spreadsheetId: String,
        val name: String,
        val createdTime: String,
        val modifiedTime: String,
        val isPosSheet: Boolean
    )

    @Suppress("UNCHECKED_CAST")
    suspend fun searchExistingPosSheets(): List<ExistingSheetInfo> =
        withContext(Dispatchers.IO) {
            try {
                val query = "appProperties has { key='isTillzoPosSheet' and value='true' } and trashed=false"
                val resp = api.searchDriveFiles(query = query)
                if (!resp.isSuccessful) return@withContext emptyList()

                val files = resp.body()?.get("files") as? List<Map<String, Any>> ?: return@withContext emptyList()
                
                files.mapNotNull { file ->
                    val id = file["id"] as? String ?: return@mapNotNull null
                    val name = file["name"] as? String ?: return@mapNotNull null
                    val created = file["createdTime"] as? String ?: ""
                    val modified = file["modifiedTime"] as? String ?: ""
                    val appProperties = file["appProperties"] as? Map<String, String>
                    
                    ExistingSheetInfo(
                        spreadsheetId = id,
                        name = name,
                        createdTime = created,
                        modifiedTime = modified,
                        isPosSheet = appProperties?.get("isTillzoPosSheet") == "true"
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun tagSheetAsPosSheet(sheetId: String, shopName: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val body = mapOf(
                    "appProperties" to mapOf(
                        "isTillzoPosSheet" to "true",
                        "shopName" to shopName,
                        "createdByApp" to "TillzoPOS",
                        "version" to "1"
                    )
                )
                val resp = api.updateAppProperties(sheetId, body)
                resp.isSuccessful
            } catch (e: Exception) {
                false
            }
        }

    suspend fun verifySheetAccess(sheetId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.getSpreadsheet(sheetId)
                resp.isSuccessful
            } catch (e: Exception) {
                false
            }
        }

    // ── Create Spreadsheet ───────────────────────────────────────────────────

    data class CreateSheetResult(
        val success: Boolean,
        val spreadsheetId: String = "",
        val error: String = ""
    )

    @Suppress("UNCHECKED_CAST")
    suspend fun createSpreadsheet(
        title: String,
        sheetDefs: List<Map<String, Any>>
    ): CreateSheetResult = withContext(Dispatchers.IO) {
        try {
            val resp = api.createSpreadsheet(
                mapOf("properties" to mapOf("title" to title), "sheets" to sheetDefs)
            )
            if (!resp.isSuccessful)
                return@withContext CreateSheetResult(false, error = "${resp.code()}: ${resp.message()}")

            val id = resp.body()?.get("spreadsheetId") as? String
                ?: return@withContext CreateSheetResult(false, error = "No spreadsheetId in response")

            CreateSheetResult(success = true, spreadsheetId = id)
        } catch (e: Exception) {
            CreateSheetResult(false, error = e.message ?: "Unknown")
        }
    }

    // ── Batch Write ──────────────────────────────────────────────────────────

    suspend fun batchWrite(valueRanges: List<Map<String, Any>>): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.batchUpdate(
                    spreadsheetId,
                    mapOf("valueInputOption" to "RAW", "data" to valueRanges)
                )
                resp.isSuccessful
            } catch (e: Exception) { false }
        }

    // ── Append Rows ──────────────────────────────────────────────────────────

    suspend fun appendRows(range: String, rows: List<List<Any>>): Boolean =
        withContext(Dispatchers.IO) {
            // Guard: spreadsheetId empty = user not signed in yet
            if (spreadsheetId.isEmpty()) return@withContext false
            try {
                val resp = api.appendValues(
                    spreadsheetId,
                    range,
                    body = mapOf(
                        "range"          to range,
                        "majorDimension" to "ROWS",
                        "values"         to rows
                    )
                )
                resp.isSuccessful
            } catch (e: Exception) { false }
        }

    // ── Read Range ───────────────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    suspend fun readRange(range: String): List<List<String>> =
        withContext(Dispatchers.IO) {
            // Guard: spreadsheetId empty = user not signed in yet
            if (spreadsheetId.isEmpty()) return@withContext emptyList()
            try {
                val resp = api.getValues(spreadsheetId, range)
                if (!resp.isSuccessful) return@withContext emptyList()

                val values = resp.body()?.get("values") as? List<List<Any>>
                    ?: return@withContext emptyList()

                values.map { row -> row.map { it.toString() } }
            } catch (e: Exception) { emptyList() }
        }

    // ── Read Single Cell (for ForceUpdate min_version) ───────────────────────

    @Suppress("UNCHECKED_CAST")
    suspend fun readCell(range: String): String? =
        withContext(Dispatchers.IO) {
            // Guard: spreadsheetId empty = user not signed in yet
            if (spreadsheetId.isEmpty()) return@withContext null
            try {
                val resp = api.getValues(spreadsheetId, range)
                if (!resp.isSuccessful) return@withContext null
                val values = resp.body()?.get("values") as? List<List<Any>>
                values?.firstOrNull()?.firstOrNull()?.toString()
            } catch (e: Exception) { null }
        }

    // ── M2 Sheet Management ──────────────────────────────────────────────────

    /** Adds a new sheet tab. Returns true on HTTP 200. */
    suspend fun addSheet(tabName: String): Boolean =
        withContext(Dispatchers.IO) {
            if (spreadsheetId.isEmpty()) return@withContext false
            try {
                val resp = api.sheetsBatchUpdate(
                    spreadsheetId,
                    mapOf("requests" to listOf(
                        mapOf("addSheet" to mapOf("properties" to mapOf("title" to tabName)))
                    ))
                )
                resp.isSuccessful
            } catch (e: Exception) { false }
        }

    /** Renames an existing sheet tab. Requires sheetId (numeric). */
    suspend fun renameSheet(sheetId: Int, newTitle: String): Boolean =
        withContext(Dispatchers.IO) {
            if (spreadsheetId.isEmpty()) return@withContext false
            try {
                val resp = api.sheetsBatchUpdate(
                    spreadsheetId,
                    mapOf("requests" to listOf(mapOf(
                        "updateSheetProperties" to mapOf(
                            "properties" to mapOf("sheetId" to sheetId, "title" to newTitle),
                            "fields"     to "title"
                        )
                    )))
                )
                resp.isSuccessful
            } catch (e: Exception) { false }
        }

    /** Sets hidden flag on a sheet tab. */
    suspend fun setSheetHidden(sheetId: Int, hidden: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            if (spreadsheetId.isEmpty()) return@withContext false
            try {
                val resp = api.sheetsBatchUpdate(
                    spreadsheetId,
                    mapOf("requests" to listOf(mapOf(
                        "updateSheetProperties" to mapOf(
                            "properties" to mapOf("sheetId" to sheetId, "hidden" to hidden),
                            "fields"     to "hidden"
                        )
                    )))
                )
                resp.isSuccessful
            } catch (e: Exception) { false }
        }

    /** Deletes a specific row by index in a given sheet tab. */
    suspend fun deleteRow(sheetId: Int, rowIndex: Int): Boolean =
        withContext(Dispatchers.IO) {
            if (spreadsheetId.isEmpty()) return@withContext false
            try {
                val resp = api.sheetsBatchUpdate(
                    spreadsheetId,
                    mapOf("requests" to listOf(mapOf(
                        "deleteDimension" to mapOf(
                            "range" to mapOf(
                                "sheetId" to sheetId,
                                "dimension" to "ROWS",
                                "startIndex" to rowIndex - 1, // 0-based index
                                "endIndex" to rowIndex
                            )
                        )
                    )))
                )
                resp.isSuccessful
            } catch (e: Exception) { false }
        }

    /**
     * Fetches spreadsheet metadata — all sheet titles + sheetIds.
     * Returns map of title → sheetId.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun getSheetMetadata(): Map<String, Int> =
        withContext(Dispatchers.IO) {
            if (spreadsheetId.isEmpty()) return@withContext emptyMap()
            try {
                val resp = api.getSpreadsheet(spreadsheetId)
                if (!resp.isSuccessful) return@withContext emptyMap()

                val sheets = resp.body()?.get("sheets") as? List<Map<String, Any>>
                    ?: return@withContext emptyMap()

                sheets.mapNotNull { sheet ->
                    val props = sheet["properties"] as? Map<String, Any> ?: return@mapNotNull null
                    val title   = props["title"] as? String             ?: return@mapNotNull null
                    val sheetId = (props["sheetId"] as? Number)?.toInt() ?: return@mapNotNull null
                    title to sheetId
                }.toMap()
            } catch (e: Exception) { emptyMap() }
        }

    /**
     * Gets the row count for a specific sheet tab.
     * Used by ShardingWorker M2.2 to check 18k row limit.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun getRowCount(tabName: String): Int =
        withContext(Dispatchers.IO) {
            if (spreadsheetId.isEmpty()) return@withContext 0
            try {
                val resp = api.getValues(spreadsheetId, "$tabName!A:A")
                if (!resp.isSuccessful) return@withContext 0
                val values = resp.body()?.get("values") as? List<List<Any>>
                (values?.size ?: 1) - 1  // subtract header row
            } catch (e: Exception) { 0 }
        }
}

