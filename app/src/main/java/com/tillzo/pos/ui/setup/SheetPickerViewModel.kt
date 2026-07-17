package com.tillzo.pos.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.remote.DriveSearchHelper
import com.tillzo.pos.data.remote.PosSheetInfo
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.data.sync.options.delta.DeltaSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun loadExistingSheets(accessToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(UiState.Ready(emptyList(), isSearching = true))
            try {
                // Token handled directly via interceptor
                val sheets = driveSearchHelper.searchPosSheets("")
                _uiState.emit(UiState.Ready(sheets, isSearching = false))
            } catch (e: Exception) {
                // Still show UI even if search fails
                _uiState.emit(UiState.Ready(emptyList(), isSearching = false))
            }
        }
    }

    fun selectSheet(sheet: PosSheetInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            appSetupPrefs.saveProvisioningResult(sheet.spreadsheetId)
            deltaSyncManager.triggerImmediatePoll()
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
                // Create workspace creates sheet, adds tabs and headers
                val result = sheetsRepository.createWorkspace(shopName)
                
                if (result.success) {
                    val newId = result.spreadsheetId
                    // Tag it so future installs can find it
                    sheetsRemoteDataSource.tagSheetAsPosSheet(newId, shopName)
                    appSetupPrefs.saveProvisioningResult(newId)
                    withContext(Dispatchers.Main) { onDone(newId) }
                } else {
                    _uiState.emit(UiState.Error("Failed: ${result.error}"))
                }
            } catch (e: Exception) {
                _uiState.emit(UiState.Error("Failed: ${e.message}"))
            }
        }
    }
}
