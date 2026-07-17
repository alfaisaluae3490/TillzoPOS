package com.tillzo.pos.ui.inventory.module_b

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.VendorDao
import com.tillzo.pos.data.local.entity.VendorEntity
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.remote.DriveSearchHelper
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VendorManagementViewModel @Inject constructor(
    private val vendorDao: VendorDao,
    private val sheetsRemoteDataSource: SheetsRemoteDataSource,
    private val appSetupPrefs: AppSetupPrefs,
    private val driveSearchHelper: DriveSearchHelper
) : ViewModel() {

    companion object {
        private const val TAG = "VendorVM"
    }

    val vendors: StateFlow<List<VendorEntity>> = vendorDao.getAllVendors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchResults = MutableStateFlow<List<VendorEntity>>(emptyList())
    val searchResults: StateFlow<List<VendorEntity>> = _searchResults.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private val _errorChannel = Channel<String>(Channel.BUFFERED)
    val errorChannel: Flow<String> = _errorChannel.receiveAsFlow()

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    fun search(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _searchResults.value = if (query.isBlank()) emptyList()
                else vendorDao.searchVendors(query)
            } catch (e: Exception) {
                _errorChannel.send(e.localizedMessage ?: "Failed to search vendors")
            }
        }
    }

    fun save(
        existing: VendorEntity?,
        name: String, phone: String, whatsapp: String, email: String, address: String,
        city: String, province: String, country: String, billingAddress: String, ownerName: String,
        bankAccountTitle: String, bankName: String, bankAccountNumber: String,
        bankIban: String, bankSwiftCode: String, bankBranch: String,
        paymentTerms: String, preferredCurrency: String, creditLimit: Double,
        registrationNumber: String, ntnNumber: String, cnicNumber: String,
        trnNumber: String, tradeLicenseNumber: String, tradeLicenseExpiryDate: String,
        primaryManagerName: String, primaryManagerPhone: String, primaryManagerEmail: String,
        techSupportName: String, techSupportPhone: String, techSupportEmail: String,
        billingContactName: String, billingContactPhone: String, billingContactEmail: String,
        escalationL1Name: String, escalationL1Phone: String, escalationL1Email: String,
        escalationL2Name: String, escalationL2Phone: String, escalationL2Email: String,
        escalationL3Name: String, escalationL3Phone: String, escalationL3Email: String,
        contractStartDate: String, contractExpiryDate: String,
        slaResponseTimes: String, warrantyTerms: String, complianceCertificates: String,
        fileUri: Uri? = null, context: Context? = null,
        isActive: Boolean = true
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _saveState.value = SaveState.Saving
            try {
                val now = System.currentTimeMillis()
                val targetVendorId = existing?.vendorId ?: UUID.randomUUID().toString()

                var contractFileId = existing?.contractFileId ?: ""
                var contractFileUrl = existing?.contractFileUrl ?: ""

                if (fileUri != null && context != null) {
                    // Read filename from URI
                    val cursor = context.contentResolver.query(fileUri, null, null, null, null)
                    val filename = cursor?.use {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        it.moveToFirst()
                        if (nameIndex >= 0) it.getString(nameIndex) else "document.pdf"
                    } ?: "document.pdf"
                    cursor?.close()

                    // Read MIME type
                    val mimeType = context.contentResolver.getType(fileUri) ?: "application/octet-stream"

                    // Read file bytes
                    val fileBytes = context.contentResolver.openInputStream(fileUri)?.use {
                        it.readBytes()
                    } ?: throw Exception("Failed to read file")

                    // Validate file size (max 20MB)
                    if (fileBytes.size > 20 * 1024 * 1024) {
                        throw Exception("File too large. Maximum size is 20MB")
                    }

                    // Resolve target folder
                    val parentId = resolveUploadFolderId()

                    // Upload to Drive
                    val dot = filename.lastIndexOf('.')
                    val extension = if (dot >= 0) filename.substring(dot + 1) else "pdf"
                    val result = sheetsRemoteDataSource.uploadDocument(
                        filename = "Vendor_Contract_${targetVendorId}_${System.currentTimeMillis()}.$extension",
                        mimeType = mimeType,
                        fileBytes = fileBytes,
                        parentFolderId = parentId
                    )

                    if (result != null) {
                        val (fileId, webViewLink) = result
                        contractFileId = fileId
                        contractFileUrl = webViewLink
                        Log.d(TAG, "Contract uploaded: $fileId -> $webViewLink")
                    } else {
                        throw Exception("Upload failed — server returned no data")
                    }
                }

                if (existing == null) {
                    vendorDao.insertVendor(
                        VendorEntity(
                            vendorId = targetVendorId,
                            isActive = isActive,
                            name = name.trim(), phone = phone.trim(),
                            whatsapp = whatsapp.trim(), email = email.trim(),
                            address = address.trim(),
                            city = city.trim(), province = province.trim(),
                            country = country.trim(), billingAddress = billingAddress.trim(),
                            ownerName = ownerName.trim(),
                            bankAccountTitle = bankAccountTitle.trim(),
                            bankName = bankName.trim(),
                            bankAccountNumber = bankAccountNumber.trim(),
                            bankIban = bankIban.trim(),
                            bankSwiftCode = bankSwiftCode.trim(),
                            bankBranch = bankBranch.trim(),
                            paymentTerms = paymentTerms.trim(),
                            preferredCurrency = preferredCurrency.trim(),
                            creditLimit = creditLimit,
                            registrationNumber = registrationNumber.trim(),
                            ntnNumber = ntnNumber.trim(),
                            cnicNumber = cnicNumber.trim(),
                            trnNumber = trnNumber.trim(),
                            tradeLicenseNumber = tradeLicenseNumber.trim(),
                            tradeLicenseExpiryDate = tradeLicenseExpiryDate.trim(),
                            primaryManagerName = primaryManagerName.trim(),
                            primaryManagerPhone = primaryManagerPhone.trim(),
                            primaryManagerEmail = primaryManagerEmail.trim(),
                            techSupportName = techSupportName.trim(),
                            techSupportPhone = techSupportPhone.trim(),
                            techSupportEmail = techSupportEmail.trim(),
                            billingContactName = billingContactName.trim(),
                            billingContactPhone = billingContactPhone.trim(),
                            billingContactEmail = billingContactEmail.trim(),
                            escalationL1Name = escalationL1Name.trim(),
                            escalationL1Phone = escalationL1Phone.trim(),
                            escalationL1Email = escalationL1Email.trim(),
                            escalationL2Name = escalationL2Name.trim(),
                            escalationL2Phone = escalationL2Phone.trim(),
                            escalationL2Email = escalationL2Email.trim(),
                            escalationL3Name = escalationL3Name.trim(),
                            escalationL3Phone = escalationL3Phone.trim(),
                            escalationL3Email = escalationL3Email.trim(),
                            contractStartDate = contractStartDate.trim(),
                            contractExpiryDate = contractExpiryDate.trim(),
                            slaResponseTimes = slaResponseTimes.trim(),
                            warrantyTerms = warrantyTerms.trim(),
                            complianceCertificates = complianceCertificates.trim(),
                            contractFileId = contractFileId,
                            contractFileUrl = contractFileUrl,
                            syncStatus = "pending",
                            createdAt = now, updatedAt = now
                        )
                    )
                } else {
                    vendorDao.updateVendor(
                        existing.copy(
                            isActive = isActive,
                            name = name.trim(), phone = phone.trim(),
                            whatsapp = whatsapp.trim(), email = email.trim(),
                            address = address.trim(),
                            city = city.trim(), province = province.trim(),
                            country = country.trim(), billingAddress = billingAddress.trim(),
                            ownerName = ownerName.trim(),
                            bankAccountTitle = bankAccountTitle.trim(),
                            bankName = bankName.trim(),
                            bankAccountNumber = bankAccountNumber.trim(),
                            bankIban = bankIban.trim(),
                            bankSwiftCode = bankSwiftCode.trim(),
                            bankBranch = bankBranch.trim(),
                            paymentTerms = paymentTerms.trim(),
                            preferredCurrency = preferredCurrency.trim(),
                            creditLimit = creditLimit,
                            registrationNumber = registrationNumber.trim(),
                            ntnNumber = ntnNumber.trim(),
                            cnicNumber = cnicNumber.trim(),
                            trnNumber = trnNumber.trim(),
                            tradeLicenseNumber = tradeLicenseNumber.trim(),
                            tradeLicenseExpiryDate = tradeLicenseExpiryDate.trim(),
                            primaryManagerName = primaryManagerName.trim(),
                            primaryManagerPhone = primaryManagerPhone.trim(),
                            primaryManagerEmail = primaryManagerEmail.trim(),
                            techSupportName = techSupportName.trim(),
                            techSupportPhone = techSupportPhone.trim(),
                            techSupportEmail = techSupportEmail.trim(),
                            billingContactName = billingContactName.trim(),
                            billingContactPhone = billingContactPhone.trim(),
                            billingContactEmail = billingContactEmail.trim(),
                            escalationL1Name = escalationL1Name.trim(),
                            escalationL1Phone = escalationL1Phone.trim(),
                            escalationL1Email = escalationL1Email.trim(),
                            escalationL2Name = escalationL2Name.trim(),
                            escalationL2Phone = escalationL2Phone.trim(),
                            escalationL2Email = escalationL2Email.trim(),
                            escalationL3Name = escalationL3Name.trim(),
                            escalationL3Phone = escalationL3Phone.trim(),
                            escalationL3Email = escalationL3Email.trim(),
                            contractStartDate = contractStartDate.trim(),
                            contractExpiryDate = contractExpiryDate.trim(),
                            slaResponseTimes = slaResponseTimes.trim(),
                            warrantyTerms = warrantyTerms.trim(),
                            complianceCertificates = complianceCertificates.trim(),
                            contractFileId = contractFileId,
                            contractFileUrl = contractFileUrl,
                            syncStatus = "pending",
                            updatedAt = now
                        )
                    )
                }
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                Log.e(TAG, "Save error: ${e.message}", e)
                _saveState.value = SaveState.Error(e.localizedMessage ?: "Failed to save vendor")
            }
        }
    }
    fun deleteVendor(vendorId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                vendorDao.softDeleteVendor(vendorId, System.currentTimeMillis())
            } catch (e: Exception) {
                Log.e(TAG, "Delete error: ${e.message}", e)
                _errorChannel.send(e.localizedMessage ?: "Failed to delete vendor")
            }
        }
    }

    private suspend fun resolveUploadFolderId(): String? {
        val saved = appSetupPrefs.grnFolderId
        if (saved.isNotBlank()) return saved

        val folders = driveSearchHelper.searchFolders()
        val target = folders.find { it.name == "Tillzo POS Uploads" }
        if (target != null) {
            appSetupPrefs.saveGrnFolder(target.spreadsheetId, target.name)
            return target.spreadsheetId
        }

        val newId = driveSearchHelper.createFolder("Tillzo POS Uploads")
        if (newId != null) {
            appSetupPrefs.saveGrnFolder(newId, "Tillzo POS Uploads")
        }
        return newId
    }
}

sealed class SaveState {
    data object Idle : SaveState()
    data object Saving : SaveState()
    data object Success : SaveState()
    data class Error(val message: String) : SaveState()
}
