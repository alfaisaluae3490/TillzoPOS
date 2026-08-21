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
                // FIX (2026-08-07): exactly ONE business found on this Gmail →
                // auto-select it (simple login + cloud restore). User ko manual
                // pick ki zaroorat nahi — existing business wapas mil gaya.
                if (sheets.size == 1) {
                    selectSheet(sheets.first())
                }
            } catch (e: Exception) {
                _uiState.emit(UiState.Ready(emptyList(), isSearching = false))
            }
        }
    }

    fun selectSheet(sheet: PosSheetInfo) {
        selectedSheetId = sheet.spreadsheetId
        viewModelScope.launch(Dispatchers.IO) {
            appSetupPrefs.saveProvisioningResult(sheet.spreadsheetId)
            ensureBusinessFolder()
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
                    sheetsRemoteDataSource.tagSheetAsPosSheet(newId, shopName)
                    appSetupPrefs.saveProvisioningResult(newId)
                    ensureBusinessFolder()
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
     * FIX (2026-08-06): Faisal's requirement — a Google Drive folder named
     * after the business, created once and permanently remembered. Also reused
     * as the GRN/backup folder. No-op if already created.
     */
    private suspend fun ensureBusinessFolder() {
        try {
            if (appSetupPrefs.businessFolderId.isNotBlank()) return
            val folderName = appSetupPrefs.businessName.ifBlank { "TillzoPOS Business" }
            val folderId = driveSearchHelper.createFolder(folderName)
            if (!folderId.isNullOrBlank()) {
                appSetupPrefs.saveBusinessFolder(folderId)
                appSetupPrefs.saveGrnFolder(folderId, folderName)
            }
        } catch (e: Exception) {
            // Non-fatal — folder can be created later from Settings
        }
    }
}
