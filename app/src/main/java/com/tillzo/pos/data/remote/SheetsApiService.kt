package com.tillzo.pos.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.PATCH

/**
 * SheetsApiService — Retrofit interface for Google Sheets REST API v4.
 *
 * Used by SheetsRemoteDataSource (DataSource layer only).
 * Architecture Law: Never injected into ViewModel or UseCase directly.
 *
 * Two batchUpdate endpoints exist by design:
 *   batchUpdate (values)         → POST /spreadsheets/{id}/values:batchUpdate  (cell data)
 *   sheetsBatchUpdate (structural) → POST /spreadsheets/{id}:batchUpdate  (add/rename/hide tabs)
 */
interface SheetsApiService {

    /** Create a new Google Spreadsheet. */
    @POST("spreadsheets")
    suspend fun createSpreadsheet(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    /** Read a range of values. */
    @GET("spreadsheets/{spreadsheetId}/values/{range}")
    suspend fun getValues(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range", encoded = false) range: String
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    /**
     * Append rows to a sheet.
     * valueInputOption=RAW → values stored as-is (no formula parsing).
     * insertDataOption=INSERT_ROWS → always inserts, never overwrites.
     */
    @POST("spreadsheets/{spreadsheetId}/values/{range}:append")
    suspend fun appendValues(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range", encoded = false) range: String,
        @Query("valueInputOption") valueInputOption: String = "RAW",
        @Query("insertDataOption") insertDataOption: String = "INSERT_ROWS",
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    /** Batch update cell values across multiple ranges. */
    @POST("spreadsheets/{spreadsheetId}/values:batchUpdate")
    suspend fun batchUpdate(
        @Path("spreadsheetId") spreadsheetId: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    /**
     * Structural batch update — add/rename/hide/delete sheet tabs.
     * POST /spreadsheets/{id}:batchUpdate  ← no '/values/' in URL!
     *
     * Requests body: { "requests": [ { "addSheet": {...} }, { "updateSheetProperties": {...} } ] }
     */
    @POST("spreadsheets/{spreadsheetId}:batchUpdate")
    suspend fun sheetsBatchUpdate(
        @Path("spreadsheetId") spreadsheetId: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    /**
     * Get spreadsheet metadata — sheet IDs, titles, hidden status.
     * Used by M2.3 schema check and M2.2 sharding row count.
     */
    @GET("spreadsheets/{spreadsheetId}")
    suspend fun getSpreadsheet(
        @Path("spreadsheetId") spreadsheetId: String,
        @Query("fields") fields: String = "sheets.properties"
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    /** Search for Google Drive files by query string (e.g. appProperties) */
    @GET("https://www.googleapis.com/drive/v3/files")
    suspend fun searchDriveFiles(
        @Query("q") query: String,
        @Query("fields") fields: String = "files(id,name,createdTime,modifiedTime,appProperties)",
        @Query("spaces") spaces: String = "drive"
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    /** Patch a file's appProperties in Google Drive */
    @PATCH("https://www.googleapis.com/drive/v3/files/{fileId}")
    suspend fun updateAppProperties(
        @Path("fileId") fileId: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, @JvmSuppressWildcards Any>>
}
