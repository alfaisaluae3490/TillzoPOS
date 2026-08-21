package com.tillzo.pos.ui.settings.options.privacy

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.remote.DriveSearchHelper
import com.tillzo.pos.data.remote.PosSheetInfo
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.domain.repository.AuthRepository
import com.tillzo.pos.utils.LocalBackupManager
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
    private val sheetsRemoteDataSource: SheetsRemoteDataSource,
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

    // FIX (2026-08-06): tax + loyalty configuration
    private val _taxInclusive = MutableStateFlow(appSetupPrefs.taxInclusive)
    val taxInclusive: StateFlow<Boolean> = _taxInclusive.asStateFlow()

    fun setTaxInclusive(enabled: Boolean) {
        _taxInclusive.value = enabled
        appSetupPrefs.taxInclusive = enabled
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
        _blockNegativeStock.value = enabled
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
