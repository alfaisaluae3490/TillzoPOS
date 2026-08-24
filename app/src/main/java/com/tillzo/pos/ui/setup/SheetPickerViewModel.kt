package com.tillzo.pos.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.remote.DriveSearchHelper
import com.tillzo.pos.data.remote.PosSheetInfo
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.data.sync.options.delta.DeltaSyncManager
import com.tillzo.pos.data.sync.options.delta.DeltaSyncManager.RestoreState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SheetPickerViewModel @Inject constructor(
    private val appSetupPrefs: AppSetupPrefs,
    private val driveSearchHelper: DriveSearchHelper,
    private val sheetsRepository: SheetsRepository,
    private val sheetsRemoteDataSource: SheetsRemoteDataSource,
    private val deltaSyncManager: DeltaSyncManager
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Ready(
            val sheets: List<PosSheetInfo>,
            val isSearching: Boolean = false
        ) : UiState()
        object CreatingNewSheet : UiState()
        data class CreationSuccess(val sheetName: String, val folderName: String) : UiState()
        data class Error(val msg: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val restoreState: StateFlow<RestoreState> = deltaSyncManager.restoreState

    private val _navigateToHome = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigateToHome: SharedFlow<String> = _navigateToHome.asSharedFlow()

    private var selectedSheetId: String = ""

    // FIX (2026-08-06): expose the selected sheet id for navigation once
    // the async restore worker completes.
    val spreadsheetIdOrEmpty: String
        get() = selectedSheetId

    fun loadExistingSheets(accessToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(UiState.Ready(emptyList(), isSearching = true))
            try {
                val sheets = driveSearchHelper.searchPosSheets("")
                _uiState.emit(UiState.Ready(sheets, isSearching = false))
            } catch (e: Exception) {
                _uiState.emit(UiState.Ready(emptyList(), isSearching = false))
            }
        }
    }

    fun selectSheet(sheet: PosSheetInfo) {
        selectedSheetId = sheet.spreadsheetId
        viewModelScope.launch(Dispatchers.IO) {
            appSetupPrefs.saveProvisioningResult(sheet.spreadsheetId)
            resolveAndLinkBusinessFolder(sheet.spreadsheetId, sheet.name)
            // FIX (2026-08-06): navigate IMMEDIATELY — do not wait for the
            // restore worker. RestoreWorker (scheduleRestoreWorker path) never
            // sets RestoreState.Success (it only logs + upserts), so any
            // Success-gated navigation stays on the picker forever. The worker
            // + delta polling continue restoring in the background and the
            // home screen shows data as it arrives.
            deltaSyncManager.scheduleRestoreWorker()
            _navigateToHome.emit(sheet.spreadsheetId)
            // FIX (2026-08-06): delta polling never started after fresh sign-in
            // (startPolling was only called from SyncOrchestrator at app start,
            // gated on spreadsheetId). Start it now that a sheet is selected.
            deltaSyncManager.startPolling()
        }
    }

    fun createNewSheet(
        accessToken: String,
        shopName: String,
        onDone: (spreadsheetId: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(UiState.CreatingNewSheet)
            try {
                val result = sheetsRepository.createWorkspace(shopName)

                if (result.success) {
                    val newId = result.spreadsheetId
                    appSetupPrefs.saveProvisioningResult(newId)

                    // Pair existing or create new business/GRN folder
                    val existing = driveSearchHelper.findBusinessFolderForSheet(newId, shopName)
                    val folderId = if (existing != null) {
                        existing.spreadsheetId
                    } else {
                        driveSearchHelper.createFolder("$shopName Folder", newId, shopName)
                    }
                    val folderName = existing?.name ?: "$shopName Folder"

                    if (!folderId.isNullOrBlank()) {
                        appSetupPrefs.saveBusinessFolder(folderId)
                        appSetupPrefs.saveGrnFolder(folderId, folderName)
                        driveSearchHelper.tagFolder(folderId, newId, shopName)
                        sheetsRemoteDataSource.tagSheetAsPosSheet(newId, shopName, folderId)
                        sheetsRepository.updateSetting("business_folder_id", folderId)
                        sheetsRepository.updateSetting("business_folder_name", folderName)
                        sheetsRepository.updateSetting("grn_folder_id", folderId)
                        sheetsRepository.updateSetting("grn_folder_name", folderName)
                    } else {
                        sheetsRemoteDataSource.tagSheetAsPosSheet(newId, shopName)
                    }

                    _uiState.emit(UiState.CreationSuccess("$shopName — TillzoPOS", folderName))
                    kotlinx.coroutines.delay(1200)
                    withContext(Dispatchers.Main) { onDone(newId) }
                } else {
                    _uiState.emit(UiState.Error("Failed: ${result.error}"))
                }
            } catch (e: Exception) {
                _uiState.emit(UiState.Error("Failed: ${e.message}"))
            }
        }
    }

    fun retryRestore() {
        viewModelScope.launch(Dispatchers.IO) {
            deltaSyncManager.resetRestoreState()
            deltaSyncManager.scheduleRestoreWorker()
            // FIX (2026-08-06): navigate immediately (same reason as selectSheet
            // — restore worker never sets Success state).
            if (selectedSheetId.isNotBlank()) {
                _navigateToHome.emit(selectedSheetId)
            }
            deltaSyncManager.startPolling()
        }
    }

    /**
     * Resolves the matching Drive folder for the chosen Sheet on reinstall or multi-device login.
     * Prevents duplicate folder creation by checking:
     * 1. Sheet Settings tab (remote cloud source of truth)
     * 2. Google Drive appProperties tag (spreadsheetId tag)
     * 3. Matching folder name on Drive
     * Fallback only creates a new folder if no existing folder exists anywhere.
     */
    private suspend fun resolveAndLinkBusinessFolder(spreadsheetId: String, sheetTitle: String) {
        try {
            val cleanShopName = sheetTitle
                .substringBefore(" —")
                .substringBefore(" -")
                .trim()
                .ifBlank { appSetupPrefs.businessName.ifBlank { "TillzoPOS Business" } }

            // 1. Try reading folder from Sheet's Settings tab
            val remoteSettings = try { sheetsRepository.getSettings() } catch (e: Exception) { null }
            val remoteFolderId = remoteSettings?.businessFolderId?.ifBlank { remoteSettings.grnFolderId } ?: ""
            val remoteFolderName = remoteSettings?.businessFolderName?.ifBlank { remoteSettings.grnFolderName } ?: ""

            if (remoteFolderId.isNotBlank()) {
                val exists = driveSearchHelper.verifyFolderExists(remoteFolderId)
                if (exists) {
                    val finalName = remoteFolderName.ifBlank { "$cleanShopName Folder" }
                    appSetupPrefs.saveBusinessFolder(remoteFolderId)
                    appSetupPrefs.saveGrnFolder(remoteFolderId, finalName)
                    driveSearchHelper.tagFolder(remoteFolderId, spreadsheetId, cleanShopName)
                    return
                }
            }

            // 2. Search Drive for existing folder tagged or named after this business/sheet
            val existingFolder = driveSearchHelper.findBusinessFolderForSheet(spreadsheetId, cleanShopName)
            if (existingFolder != null) {
                val folderId = existingFolder.spreadsheetId
                val folderName = existingFolder.name
                appSetupPrefs.saveBusinessFolder(folderId)
                appSetupPrefs.saveGrnFolder(folderId, folderName)
                driveSearchHelper.tagFolder(folderId, spreadsheetId, cleanShopName)
                sheetsRepository.updateSetting("business_folder_id", folderId)
                sheetsRepository.updateSetting("business_folder_name", folderName)
                sheetsRepository.updateSetting("grn_folder_id", folderId)
                sheetsRepository.updateSetting("grn_folder_name", folderName)
                return
            }

            // 3. Fallback: Create only if absolutely no folder exists
            val newFolderName = "$cleanShopName Folder"
            val newFolderId = driveSearchHelper.createFolder(newFolderName, spreadsheetId, cleanShopName)
            if (!newFolderId.isNullOrBlank()) {
                appSetupPrefs.saveBusinessFolder(newFolderId)
                appSetupPrefs.saveGrnFolder(newFolderId, newFolderName)
                sheetsRepository.updateSetting("business_folder_id", newFolderId)
                sheetsRepository.updateSetting("business_folder_name", newFolderName)
                sheetsRepository.updateSetting("grn_folder_id", newFolderId)
                sheetsRepository.updateSetting("grn_folder_name", newFolderName)
            }
        } catch (e: Exception) {
            // Non-fatal — folder can be configured later from Settings
        }
    }
}
