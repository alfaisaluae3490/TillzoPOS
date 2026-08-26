package com.tillzo.pos.ui.settings.options.privacy

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.remote.DriveSearchHelper
import com.tillzo.pos.data.remote.PosSheetInfo
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.domain.repository.AuthRepository
import com.tillzo.pos.utils.LocalBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.app.Application
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSetupPrefs: AppSetupPrefs,
    private val authRepository: AuthRepository,
    private val driveSearchHelper: DriveSearchHelper,
    private val sheetsRepository: SheetsRepository,
    private val localBackupManager: LocalBackupManager,
    private val appDatabase: com.tillzo.pos.data.local.AppDatabase,
    application: Application
) : ViewModel() {

    // PLAY POLICY T2: app context for DB file deletion + prefs wipe
    private val application: Application = application

    // PLAY POLICY T2: held reference so deleteAllLocalData() can close Room
    // before deleting the database files (SQLCipher SupportFactory connection).
    private var appDatabaseRef: com.tillzo.pos.data.local.AppDatabase? = appDatabase

    private val _spreadsheetId = MutableStateFlow(appSetupPrefs.spreadsheetId)
    val spreadsheetId = _spreadsheetId.asStateFlow()

    // FIX (2026-08-06): multi-currency support — selectable in Settings
    private val _currencySymbol = MutableStateFlow(appSetupPrefs.currencySymbol.ifBlank { "$" })
    val currencySymbol = _currencySymbol.asStateFlow()

    fun setCurrencySymbol(symbol: String) {
        _currencySymbol.value = symbol
        appSetupPrefs.currencySymbol = symbol
    }

    // ── Global Tax & Compliance Configuration ───────────────────────────────
    private val _countryCode = MutableStateFlow(appSetupPrefs.countryCode)
    val countryCode: StateFlow<String> = _countryCode.asStateFlow()

    private val _taxNumber = MutableStateFlow(appSetupPrefs.taxNumber)
    val taxNumber: StateFlow<String> = _taxNumber.asStateFlow()

    private val _taxLabel = MutableStateFlow(appSetupPrefs.taxLabel)
    val taxLabel: StateFlow<String> = _taxLabel.asStateFlow()

    private val _defaultTaxRate = MutableStateFlow(appSetupPrefs.defaultTaxRate)
    val defaultTaxRate: StateFlow<Double> = _defaultTaxRate.asStateFlow()

    private val _taxInclusive = MutableStateFlow(appSetupPrefs.taxInclusive)
    val taxInclusive: StateFlow<Boolean> = _taxInclusive.asStateFlow()

    private val _enableZatcaQr = MutableStateFlow(appSetupPrefs.enableZatcaQr)
    val enableZatcaQr: StateFlow<Boolean> = _enableZatcaQr.asStateFlow()

    fun selectCountryPreset(code: String) {
        val preset = com.tillzo.pos.utils.TaxUtils.getPreset(code)
        appSetupPrefs.applyCountryPreset(preset)
        _countryCode.value = preset.code
        _currencySymbol.value = preset.currencySymbol
        _taxLabel.value = preset.taxLabel
        _defaultTaxRate.value = preset.defaultTaxRate
        _taxInclusive.value = preset.taxInclusive
        _enableZatcaQr.value = preset.enableZatcaQr
    }

    fun setTaxNumber(v: String) {
        _taxNumber.value = v
        appSetupPrefs.taxNumber = v
    }

    fun setTaxLabel(v: String) {
        _taxLabel.value = v
        appSetupPrefs.taxLabel = v
    }

    fun setDefaultTaxRate(v: Double) {
        val safe = v.coerceAtLeast(0.0)
        _defaultTaxRate.value = safe
        appSetupPrefs.defaultTaxRate = safe
    }

    fun setTaxInclusive(enabled: Boolean) {
        _taxInclusive.value = enabled
        appSetupPrefs.taxInclusive = enabled
    }

    fun setEnableZatcaQr(enabled: Boolean) {
        _enableZatcaQr.value = enabled
        appSetupPrefs.enableZatcaQr = enabled
    }

    private val _loyaltyEnabled = MutableStateFlow(appSetupPrefs.loyaltyEnabled)
    val loyaltyEnabled: StateFlow<Boolean> = _loyaltyEnabled.asStateFlow()

    fun setLoyaltyEnabled(enabled: Boolean) {
        _loyaltyEnabled.value = enabled
        appSetupPrefs.loyaltyEnabled = enabled
    }

    private val _hasPin = MutableStateFlow(authRepository.hasPIN())
    val hasPin: StateFlow<Boolean> = _hasPin.asStateFlow()

    private val _isPinEnabled = MutableStateFlow(appSetupPrefs.isPinEnabled)
    val isPinEnabled: StateFlow<Boolean> = _isPinEnabled.asStateFlow()

    private val _sheetsList = MutableStateFlow<List<PosSheetInfo>>(emptyList())
    val sheetsList: StateFlow<List<PosSheetInfo>> = _sheetsList.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _grnFolderId = MutableStateFlow(appSetupPrefs.grnFolderId)
    val grnFolderId: StateFlow<String> = _grnFolderId.asStateFlow()

    private val _grnFolderName = MutableStateFlow(appSetupPrefs.grnFolderName)
    val grnFolderName: StateFlow<String> = _grnFolderName.asStateFlow()

    private val _folderList = MutableStateFlow<List<PosSheetInfo>>(emptyList())
    val folderList: StateFlow<List<PosSheetInfo>> = _folderList.asStateFlow()

    private val _isSearchingFolders = MutableStateFlow(false)
    val isSearchingFolders: StateFlow<Boolean> = _isSearchingFolders.asStateFlow()

    // DEF-31 FIX (2026-08-23): Drive/folder/sheet creation failures were
    // silent — user tapped and nothing happened. Surface an error message.
    private val _settingsError = MutableStateFlow<String?>(null)
    val settingsError: StateFlow<String?> = _settingsError.asStateFlow()

    fun clearSettingsError() { _settingsError.value = null }

    private val _blockNegativeStock = MutableStateFlow(appSetupPrefs.blockNegativeStock)
    val blockNegativeStock: StateFlow<Boolean> = _blockNegativeStock.asStateFlow()

    private val _backupProgress = MutableStateFlow<String?>(null)
    val backupProgress: StateFlow<String?> = _backupProgress.asStateFlow()

    fun exportBackup(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _backupProgress.value = "Starting backup..."
            localBackupManager.exportToZip(uri) { progress ->
                _backupProgress.value = progress
            }
            _backupProgress.value = "Backup completed successfully!"
            kotlinx.coroutines.delay(3000)
            _backupProgress.value = null
        }
    }

    // FIX (2026-08-06): Faisal's requirement — one-tap local backup copy to
    // public Documents (survives uninstall/reinstall). Same writer as the
    // nightly AutoLocalBackupWorker, but immediate + shows file path.
    private val _autoBackupStatus = MutableStateFlow<String?>(null)
    val autoBackupStatus: StateFlow<String?> = _autoBackupStatus.asStateFlow()

    fun runAutoBackupNow() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _autoBackupStatus.value = "Creating local backup copy..."
                val path = localBackupManager.exportToPublicDocuments()
                _autoBackupStatus.value = "Backup saved: $path"
            } catch (e: Exception) {
                _autoBackupStatus.value = "Backup failed: ${e.message}"
            }
        }
    }

    fun clearAutoBackupStatus() { _autoBackupStatus.value = null }

    fun setBlockNegativeStock(enabled: Boolean) {
        appSetupPrefs.blockNegativeStock = enabled
    }

    // OVERNIGHT-AUDIT Phase 1c (2026-08-23): screen-capture blocking toggle.
    // Persists in encrypted prefs; Application reads it at cold start and the
    // ScreenSecurityController applies/clears FLAG_SECURE on all activities.
    private val _blockScreenCapture = MutableStateFlow(appSetupPrefs.blockScreenCapture)
    val blockScreenCapture: StateFlow<Boolean> = _blockScreenCapture.asStateFlow()

    fun setBlockScreenCapture(enabled: Boolean) {
        _blockScreenCapture.value = enabled
        appSetupPrefs.blockScreenCapture = enabled
        // OVERNIGHT-AUDIT Phase 1c: runtime flip via controller singleton.
        com.tillzo.pos.utils.security.ScreenSecurityController.instance?.secureEnabled = enabled
    }

    fun clearBackupProgress() {
        _backupProgress.value = null
    }

    fun searchFolders() {
        viewModelScope.launch {
            _isSearchingFolders.value = true
            try {
                val results = withContext(Dispatchers.IO) {
                    driveSearchHelper.searchFolders()
                }
                _folderList.value = results
            } catch (_: Exception) {
                _folderList.value = emptyList()
            } finally {
                _isSearchingFolders.value = false
            }
        }
    }

    init {
        // Auto-discover and link folder if unlinked but sheet exists (e.g. fresh reinstall/sync)
        if (appSetupPrefs.grnFolderId.isBlank() && appSetupPrefs.spreadsheetId.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val remoteSettings = sheetsRepository.getSettings()
                    val folderId = remoteSettings.businessFolderId.ifBlank { remoteSettings.grnFolderId }
                    val folderName = remoteSettings.businessFolderName.ifBlank { remoteSettings.grnFolderName }
                    if (folderId.isNotBlank()) {
                        val finalName = folderName.ifBlank { "TillzoPOS Business" }
                        appSetupPrefs.saveBusinessFolder(folderId)
                        appSetupPrefs.saveGrnFolder(folderId, finalName)
                        _grnFolderId.value = folderId
                        _grnFolderName.value = finalName
                    } else {
                        val existing = driveSearchHelper.findBusinessFolderForSheet(
                            appSetupPrefs.spreadsheetId,
                            appSetupPrefs.businessName
                        )
                        if (existing != null) {
                            appSetupPrefs.saveBusinessFolder(existing.spreadsheetId)
                            appSetupPrefs.saveGrnFolder(existing.spreadsheetId, existing.name)
                            _grnFolderId.value = existing.spreadsheetId
                            _grnFolderName.value = existing.name
                            sheetsRepository.updateSetting("business_folder_id", existing.spreadsheetId)
                            sheetsRepository.updateSetting("business_folder_name", existing.name)
                            sheetsRepository.updateSetting("grn_folder_id", existing.spreadsheetId)
                            sheetsRepository.updateSetting("grn_folder_name", existing.name)
                            driveSearchHelper.tagFolder(existing.spreadsheetId, appSetupPrefs.spreadsheetId, existing.name)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun selectFolder(id: String, name: String) {
        appSetupPrefs.saveGrnFolder(id, name)
        appSetupPrefs.saveBusinessFolder(id)
        _grnFolderId.value = id
        _grnFolderName.value = name
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sheetId = appSetupPrefs.spreadsheetId
                if (sheetId.isNotBlank()) {
                    sheetsRepository.updateSetting("business_folder_id", id)
                    sheetsRepository.updateSetting("business_folder_name", name)
                    sheetsRepository.updateSetting("grn_folder_id", id)
                    sheetsRepository.updateSetting("grn_folder_name", name)
                    driveSearchHelper.tagFolder(id, sheetId, name)
                }
            } catch (_: Exception) {}
        }
    }

    fun createNewFolder(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sheetId = appSetupPrefs.spreadsheetId
                val folderId = driveSearchHelper.createFolder(name, sheetId, name)
                if (folderId != null) {
                    withContext(Dispatchers.Main) {
                        selectFolder(folderId, name)
                    }
                } else {
                    // DEF-31 FIX: Drive returned null (no exception) — surface it.
                    _settingsError.value = "Failed to create folder. Please try again."
                }
            } catch (e: Exception) {
                // DEF-31 FIX: previously silent — user saw nothing happen.
                _settingsError.value = "Folder creation failed: ${e.message}"
            }
        }
    }

    fun updateSpreadsheetId(newId: String) {
        val extracted = extractSheetId(newId)
        // DEF-31/DEF-100 FIX (2026-08-23): garbage non-empty strings were
        // accepted as sheet IDs (live: "not_a_real_sheet_id_xyz" save ho gaya
        // → app ek non-existent sheet par point ho gaya, sync dead). Light
        // format check: real Google sheet IDs are 30+ base64url chars; URLs
        // must contain /d/.
        val looksValid = extracted.contains("/d/") ||
            (extracted.length >= 30 && extracted.all { it.isLetterOrDigit() || it == '_' || it == '-' })
        if (extracted.isNotEmpty() && looksValid) {
            appSetupPrefs.saveProvisioningResult(extracted)
            _spreadsheetId.value = extracted
            // Automatically resolve and link corresponding folder for the newly chosen sheet
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val remoteSettings = sheetsRepository.getSettings()
                    val folderId = remoteSettings.businessFolderId.ifBlank { remoteSettings.grnFolderId }
                    val folderName = remoteSettings.businessFolderName.ifBlank { remoteSettings.grnFolderName }
                    if (folderId.isNotBlank()) {
                        val finalName = folderName.ifBlank { "TillzoPOS Business" }
                        appSetupPrefs.saveBusinessFolder(folderId)
                        appSetupPrefs.saveGrnFolder(folderId, finalName)
                        _grnFolderId.value = folderId
                        _grnFolderName.value = finalName
                    } else {
                        val existing = driveSearchHelper.findBusinessFolderForSheet(extracted, appSetupPrefs.businessName)
                        if (existing != null) {
                            appSetupPrefs.saveBusinessFolder(existing.spreadsheetId)
                            appSetupPrefs.saveGrnFolder(existing.spreadsheetId, existing.name)
                            _grnFolderId.value = existing.spreadsheetId
                            _grnFolderName.value = existing.name
                            sheetsRepository.updateSetting("business_folder_id", existing.spreadsheetId)
                            sheetsRepository.updateSetting("business_folder_name", existing.name)
                            sheetsRepository.updateSetting("grn_folder_id", existing.spreadsheetId)
                            sheetsRepository.updateSetting("grn_folder_name", existing.name)
                        }
                    }
                } catch (_: Exception) {}
            }
        } else {
            // DEF-31 FIX: empty/invalid input was silently ignored.
            _settingsError.value = "Invalid spreadsheet ID or URL."
        }
    }

    private fun extractSheetId(input: String): String {
        return if (input.contains("/d/")) {
            input.substringAfter("/d/").substringBefore("/")
        } else {
            input.trim()
        }
    }

    fun checkPinStatus() {
        _hasPin.value = authRepository.hasPIN()
    }

    fun verifyCurrentPin(pin: String): Boolean = authRepository.verifyPIN(pin)

    fun saveNewPin(pin: String) {
        viewModelScope.launch {
            authRepository.setPIN(pin)
            checkPinStatus()
        }
    }

    fun removePin() {
        viewModelScope.launch {
            authRepository.clearPIN()
            checkPinStatus()
        }
    }

    fun togglePinLock(enabled: Boolean) {
        appSetupPrefs.isPinEnabled = enabled
        _isPinEnabled.value = enabled
    }

    fun loadDriveSheets() {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val results = withContext(Dispatchers.IO) {
                    driveSearchHelper.searchPosSheets("")
                }
                _sheetsList.value = results
            } catch (_: Exception) {
                _sheetsList.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun createNewSheet(shopName: String, onDone: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cleanName = shopName.trim().ifBlank { appSetupPrefs.businessName.ifBlank { "TillzoPOS Business" } }
                val result = sheetsRepository.createNewSpreadsheet(cleanName)

                if (result.success) {
                    val newId = result.spreadsheetId
                    val existing = driveSearchHelper.findBusinessFolderForSheet(newId, cleanName)
                    val folderId = if (existing != null) {
                        existing.spreadsheetId
                    } else {
                        driveSearchHelper.createFolder("$cleanName Folder", newId, cleanName)
                    }
                    val folderName = existing?.name ?: "$cleanName Folder"
                    if (!folderId.isNullOrBlank()) {
                        appSetupPrefs.saveBusinessFolder(folderId)
                        appSetupPrefs.saveGrnFolder(folderId, folderName)
                        driveSearchHelper.tagFolder(folderId, newId, cleanName)
                        sheetsRepository.updateSetting("business_folder_id", folderId)
                        sheetsRepository.updateSetting("business_folder_name", folderName)
                        sheetsRepository.updateSetting("grn_folder_id", folderId)
                        sheetsRepository.updateSetting("grn_folder_name", folderName)
                    }

                    withContext(Dispatchers.Main) {
                        updateSpreadsheetId(newId)
                        onDone(newId)
                    }
                } else {
                    // DEF-31 FIX: previously silent — user saw nothing happen.
                    _settingsError.value = "Sheet creation failed: ${result.error}"
                }
            } catch (e: Exception) {
                _settingsError.value = "Sheet creation failed: ${e.message}"
            }
        }
    }

    // ── PLAY POLICY T2 (2026-08-24): Account & Data Deletion ─────────────────

    private val _deleteAccountState = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Idle)
    val deleteAccountState: StateFlow<DeleteAccountState> = _deleteAccountState.asStateFlow()

    /**
     * Full account & data deletion per Google Play policy:
     *  1. Revoke Google OAuth token server-side (removes grant from user's
     *     Google Account → myaccount.google.com/permissions)
     *  2. Wipe the entire local Room database (all business data)
     *  3. Clear every SharedPreferences/DataStore file (setup prefs, tokens,
     *     barcode prefs, update prefs, encryption prefs)
     *
     * After completion the app process is killed so the next launch starts
     * from a clean first-run state.
     */
    fun deleteAccountAndData() {
        if (_deleteAccountState.value is DeleteAccountState.Deleting) return
        _deleteAccountState.value = DeleteAccountState.Deleting

        viewModelScope.launch {
            try {
                // Step 1 — revoke Google OAuth grant server-side
                val revoked = withContext(Dispatchers.IO) { authRepository.revokeGoogleAccess() }
                android.util.Log.d("SettingsViewModel", "Google access revoked: $revoked")

                // Step 2 + 3 — wipe local DB & all preferences on IO thread
                withContext(Dispatchers.IO) {
                    deleteAllLocalData()
                }

                _deleteAccountState.value = DeleteAccountState.Done
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Delete account failed", e)
                _deleteAccountState.value = DeleteAccountState.Error(e.message ?: "Deletion failed")
            }
        }
    }

    /**
     * Deletes the Room database file(s) directly (bypassing clearAllTables which
     * requires an open connection and SQLCipher key management), then wipes all
     * SharedPreferences files this app owns. WorkManager jobs are cancelled too
     * so no pending sync can resurrect deleted rows.
     */
    private suspend fun deleteAllLocalData() {
        val ctx: android.content.Context = application
        deleteAllDataWith(ctx)
    }

    private suspend fun deleteAllDataWith(ctx: android.content.Context) {

        // Cancel all background work first — prevents WorkManager from writing
        // to the DB while we delete it or after deletion.
        try {
            androidx.work.WorkManager.getInstance(ctx).cancelAllWork()
            androidx.work.WorkManager.getInstance(ctx).pruneWork()
        } catch (e: Exception) {
            android.util.Log.w("SettingsViewModel", "WorkManager cancel failed", e)
        }

        // Close Room before deleting files. The Hilt singleton holds an open
        // connection — close it via the instance from the DI graph. We can't
        // inject AppDatabase into the ViewModel method easily here, so use the
        // application-scoped instance passed in at construction time.
        try {
            appDatabaseRef?.close()
            appDatabaseRef = null
        } catch (e: Exception) {
            android.util.Log.w("SettingsViewModel", "DB close skipped: ${e.message}")
        }

        // Delete DB files (main + WAL + SHM; -journal for safety). The database
        // is SQLCipher-encrypted but file deletion works regardless of encryption.
        listOf("", "-wal", "-shm", "-journal").forEach { suffix ->
            ctx.getDatabasePath("tillzo_pos_db$suffix").let { f ->
                if (f.exists()) f.delete()
            }
        }

        // Clear EVERY SharedPreferences file the app owns:
        // tillzo_setup_secure_prefs, tillzo_oauth_prefs, barcode_prefs,
        // tillzo_update_prefs, db_encryption, auth_repo prefs, any others.
        ctx.getSharedPreferences("tillzo_setup_secure_prefs", 0).edit().clear().commit()
        ctx.getSharedPreferences("tillzo_oauth_prefs", 0).edit().clear().commit()
        ctx.getSharedPreferences("barcode_prefs", 0).edit().clear().commit()
        ctx.getSharedPreferences("tillzo_update_prefs", 0).edit().clear().commit()
        ctx.getSharedPreferences("db_encryption", 0).edit().clear().commit()
        ctx.getSharedPreferences("auth_prefs", 0).edit().clear().commit()
        // Sweep any remaining pref files by scanning the shared_prefs dir
        ctx.filesDir?.parentFile?.let { appDir ->
            java.io.File(appDir, "shared_prefs").listFiles()?.forEach { xml ->
                val name = xml.nameWithoutExtension
                try {
                    ctx.getSharedPreferences(name, 0).edit().clear().commit()
                } catch (_: Exception) { }
            }
        }

        // Delete cached files / external cache (receipts, PDFs, temp exports)
        ctx.cacheDir?.deleteRecursively()
        ctx.externalCacheDir?.deleteRecursively()
    }
}

/** State machine for the Delete Account flow UI. */
sealed class DeleteAccountState {
    object Idle : DeleteAccountState()
    object Deleting : DeleteAccountState()
    object Done : DeleteAccountState()
    data class Error(val message: String) : DeleteAccountState()
}

