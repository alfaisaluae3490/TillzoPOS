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
    private val localBackupManager: LocalBackupManager
) : ViewModel() {

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
}
