package com.tillzo.pos.data.remote

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

data class PosSheetInfo(
    val spreadsheetId: String,
    val name: String,
    val createdTime: String,
    val modifiedTime: String,
    val isTagged: Boolean
)

@Singleton
class DriveSearchHelper @Inject constructor(
    private val sheetsApiClient: SheetsApiClient
) {
    suspend fun searchPosSheets(accessToken: String): List<PosSheetInfo> {
        return withContext(Dispatchers.IO) {
            try {
                // Search 1: By appProperty tag (sheets created by this app)
                val taggedUrl = buildDriveSearchUrl(
                    query = "appProperties has { key='isTillzoPosSheet' and value='true' } and trashed=false"
                )
                val taggedResults = fetchDriveFiles(taggedUrl)

                // Search 2: By name (catches sheets created before tagging fix)
                val namedUrl = buildDriveSearchUrl(
                    query = "name contains 'Tillzo POS' and mimeType='application/vnd.google-apps.spreadsheet' and trashed=false"
                )
                val namedResults = fetchDriveFiles(namedUrl)

                // Merge, deduplicate by id
                (taggedResults + namedResults)
                    .distinctBy { it.spreadsheetId }
                    .sortedByDescending { it.modifiedTime }

            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun buildDriveSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return "https://www.googleapis.com/drive/v3/files?q=$encoded&fields=files(id,name,createdTime,modifiedTime,appProperties)&orderBy=modifiedTime+desc&pageSize=20"
    }

    private suspend fun fetchDriveFiles(url: String): List<PosSheetInfo> {
        return try {
            // SheetsApiClient's internal okhttp client has the Bearer interceptor!
            val okHttpClient = sheetsApiClient.retrofit.callFactory() as OkHttpClient
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val responseBody = response.body?.string() ?: return emptyList()
            val driveResponse = Gson().fromJson(responseBody, DriveResponse::class.java)
            
            driveResponse.files?.mapNotNull { file ->
                val id = file.id ?: return@mapNotNull null
                val name = file.name ?: return@mapNotNull null
                val created = file.createdTime ?: ""
                val modified = file.modifiedTime ?: ""
                val isTagged = file.appProperties?.get("isTillzoPosSheet") == "true"

                PosSheetInfo(
                    spreadsheetId = id,
                    name = name,
                    createdTime = created,
                    modifiedTime = modified,
                    isTagged = isTagged
                )
            } ?: emptyList()

        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchFolders(): List<PosSheetInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val url = buildDriveFolderSearchUrl()
                fetchDriveFiles(url)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun createFolder(folderName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val body = mapOf<String, Any>(
                    "name" to folderName,
                    "mimeType" to "application/vnd.google-apps.folder"
                )
                val resp = sheetsApiClient.createService<SheetsApiService>().createDriveFolder(body)
                if (!resp.isSuccessful) return@withContext null
                val id = resp.body()?.get("id") as? String ?: return@withContext null
                id
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun buildDriveFolderSearchUrl(): String {
        val query = "mimeType='application/vnd.google-apps.folder' and trashed=false"
        val encoded = URLEncoder.encode(query, "UTF-8")
        return "https://www.googleapis.com/drive/v3/files?q=$encoded&fields=files(id,name,createdTime,modifiedTime,appProperties)&orderBy=modifiedTime+desc&pageSize=20"
    }

    private data class DriveResponse(
        @SerializedName("files") val files: List<DriveFile>?
    )

    private data class DriveFile(
        @SerializedName("id") val id: String?,
        @SerializedName("name") val name: String?,
        @SerializedName("createdTime") val createdTime: String?,
        @SerializedName("modifiedTime") val modifiedTime: String?,
        @SerializedName("appProperties") val appProperties: Map<String, String>?
    )
}
