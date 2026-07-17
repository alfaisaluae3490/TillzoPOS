package com.tillzo.pos.ui.settings.options.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.remote.DriveSearchHelper
import com.tillzo.pos.data.remote.PosSheetInfo
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSetupPrefs: AppSetupPrefs,
    private val authRepository: AuthRepository,
    private val driveSearchHelper: DriveSearchHelper,
    private val sheetsRepository: SheetsRepository,
    private val sheetsRemoteDataSource: SheetsRemoteDataSource
) : ViewModel() {

    private val _spreadsheetId = MutableStateFlow(appSetupPrefs.spreadsheetId)
    val spreadsheetId = _spreadsheetId.asStateFlow()

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

    fun selectFolder(id: String, name: String) {
        appSetupPrefs.saveGrnFolder(id, name)
        _grnFolderId.value = id
        _grnFolderName.value = name
    }

    fun createNewFolder(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val folderId = driveSearchHelper.createFolder(name)
                if (folderId != null) {
                    withContext(Dispatchers.Main) {
                        selectFolder(folderId, name)
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun updateSpreadsheetId(newId: String) {
        val extracted = extractSheetId(newId)
        if (extracted.isNotEmpty()) {
            appSetupPrefs.saveProvisioningResult(extracted)
            _spreadsheetId.value = extracted
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
                val curTab = currentSalesTab()
                val sheetDefs = listOf(
                    curTab, "Inventory", "Customers", "Khata_Events",
                    "Expenses", "Categories", "Returns", "Wastage_Ledger", "Users_Permissions",
                    "Purchase_Orders", "PO_Items",
                    "GRN_Headers", "GRN_Items",
                    "Vendors", "Product_Batches",
                    "Settings", "Sync_Log", "Dashboard", "SYS_DB_DO_NOT_TOUCH"
                ).mapIndexed { idx, title ->
                    mapOf("properties" to mapOf("title" to title, "index" to idx))
                }

                val result = sheetsRemoteDataSource.createSpreadsheet(
                    title = "$shopName \u2014 TillzoPOS",
                    sheetDefs = sheetDefs
                )

                if (result.success) {
                    sheetsRemoteDataSource.tagSheetAsPosSheet(result.spreadsheetId, shopName)
                    withContext(Dispatchers.Main) {
                        updateSpreadsheetId(result.spreadsheetId)
                        onDone(result.spreadsheetId)
                    }
                }
            } catch (_: Exception) { }
        }
    }

    private fun currentSalesTab(): String {
        val months = arrayOf("Jan","Feb","Mar","Apr","May","Jun",
                             "Jul","Aug","Sep","Oct","Nov","Dec")
        val cal = Calendar.getInstance()
        return "Sales_${months[cal.get(Calendar.MONTH)]}_${cal.get(Calendar.YEAR)}"
    }
}
