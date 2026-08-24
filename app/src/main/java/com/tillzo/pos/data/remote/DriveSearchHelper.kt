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

                // Search 2: By name (TillzoPOS / Tillzo POS / Tillzo / POS)
                val namedUrl = buildDriveSearchUrl(
                    query = "(name contains 'TillzoPOS' or name contains 'Tillzo POS' or name contains 'Tillzo' or name contains 'POS') and mimeType='application/vnd.google-apps.spreadsheet' and trashed=false"
                )
                val namedResults = fetchDriveFiles(namedUrl)

                // Search 3: All Google Spreadsheets in user's Drive so nothing is ever missed
                val allSheetsUrl = buildDriveSearchUrl(
                    query = "mimeType='application/vnd.google-apps.spreadsheet' and trashed=false"
                )
                val allSheetsResults = fetchDriveFiles(allSheetsUrl)

                // Merge, prioritize tagged/Tillzo sheets first, deduplicate by id
                (taggedResults + namedResults + allSheetsResults)
                    .distinctBy { it.spreadsheetId }
                    .sortedWith(compareByDescending<PosSheetInfo> { it.isTagged }
                        .thenByDescending { it.name.contains("Tillzo", ignoreCase = true) }
                        .thenByDescending { it.modifiedTime })

            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun buildDriveSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return "https://www.googleapis.com/drive/v3/files?q=$encoded&fields=files(id,name,createdTime,modifiedTime,appProperties)&orderBy=modifiedTime+desc&pageSize=100"
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

    suspend fun findBusinessFolderForSheet(spreadsheetId: String, shopName: String): PosSheetInfo? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Search by exact spreadsheetId tag in appProperties
                if (spreadsheetId.isNotBlank()) {
                    val taggedQuery = "mimeType='application/vnd.google-apps.folder' and appProperties has { key='spreadsheetId' and value='$spreadsheetId' } and trashed=false"
                    val taggedResults = fetchDriveFiles(buildDriveFolderSearchUrl(taggedQuery))
                    if (taggedResults.isNotEmpty()) {
                        return@withContext taggedResults.first()
                    }
                }

                // 2. Search by shopName in folder name or appProperties
                val cleanName = shopName.trim()
                if (cleanName.isNotBlank()) {
                    // Check appProperties with shopName
                    val propQuery = "mimeType='application/vnd.google-apps.folder' and appProperties has { key='isTillzoBusinessFolder' and value='true' } and name contains '$cleanName' and trashed=false"
                    val propResults = fetchDriveFiles(buildDriveFolderSearchUrl(propQuery))
                    if (propResults.isNotEmpty()) {
                        return@withContext propResults.first()
                    }

                    // Check exact folder names ($cleanName Folder, $cleanName Receipts, $cleanName)
                    val nameQuery = "mimeType='application/vnd.google-apps.folder' and (name='$cleanName Folder' or name='$cleanName Receipts' or name='$cleanName' or name contains '$cleanName') and trashed=false"
                    val nameResults = fetchDriveFiles(buildDriveFolderSearchUrl(nameQuery))
                    if (nameResults.isNotEmpty()) {
                        return@withContext nameResults.first()
                    }
                }

                // 3. Fallback search for any existing generic TillzoPOS folders
                val genericQuery = "mimeType='application/vnd.google-apps.folder' and (name='TillzoPOS Business' or name='Tillzo POS Uploads') and trashed=false"
                val genericResults = fetchDriveFiles(buildDriveFolderSearchUrl(genericQuery))
                if (genericResults.isNotEmpty()) {
                    return@withContext genericResults.first()
                }

                null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun verifyFolderExists(folderId: String): Boolean {
        if (folderId.isBlank()) return false
        return withContext(Dispatchers.IO) {
            try {
                val okHttpClient = sheetsApiClient.retrofit.callFactory() as OkHttpClient
                val url = "https://www.googleapis.com/drive/v3/files/$folderId?fields=id,trashed"
                val request = Request.Builder().url(url).build()
                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) return@withContext false
                val bodyStr = response.body?.string() ?: return@withContext false
                val map = Gson().fromJson(bodyStr, Map::class.java)
                val trashed = map["trashed"] as? Boolean ?: false
                !trashed
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun tagFolder(folderId: String, spreadsheetId: String, shopName: String): Boolean {
        if (folderId.isBlank()) return false
        return withContext(Dispatchers.IO) {
            try {
                val props = mutableMapOf(
                    "isTillzoBusinessFolder" to "true",
                    "createdByApp" to "TillzoPOS"
                )
                if (spreadsheetId.isNotBlank()) props["spreadsheetId"] = spreadsheetId
                if (shopName.isNotBlank()) props["shopName"] = shopName
                val body = mapOf("appProperties" to props)
                val resp = sheetsApiClient.createService<SheetsApiService>().updateAppProperties(folderId, body)
                resp.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun createFolder(
        folderName: String,
        spreadsheetId: String? = null,
        shopName: String? = null
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val appProps = mutableMapOf(
                    "isTillzoBusinessFolder" to "true",
                    "createdByApp" to "TillzoPOS"
                )
                if (!spreadsheetId.isNullOrBlank()) appProps["spreadsheetId"] = spreadsheetId
                if (!shopName.isNullOrBlank()) appProps["shopName"] = shopName

                val body = mapOf<String, Any>(
                    "name" to folderName,
                    "mimeType" to "application/vnd.google-apps.folder",
                    "appProperties" to appProps
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

    private fun buildDriveFolderSearchUrl(query: String = "mimeType='application/vnd.google-apps.folder' and trashed=false"): String {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return "https://www.googleapis.com/drive/v3/files?q=$encoded&fields=files(id,name,createdTime,modifiedTime,appProperties)&orderBy=modifiedTime+desc&pageSize=100"
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
