package com.tillzo.pos.domain.setup

import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.data.repository.SheetsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SheetSetupUseCase — called once on first sign-in.
 * 
 * Flow:
 * 1. Check local AppSetupPrefs and verify access
 * 2. If no local or access denied, search Drive API for existing POS sheet
 * 3. Return result (Load existing, Create new, or Prompt user)
 */
@Singleton
class SheetSetupUseCase @Inject constructor(
    private val sheetsRepository: SheetsRepository,
    private val appSetupPrefs: AppSetupPrefs
) {
    sealed class SetupResult {
        data class ExistingSheetLoaded(val spreadsheetId: String) : SetupResult()
        data class NewSheetCreated(val spreadsheetId: String) : SetupResult()
        data class MultipleSheetFound(val sheets: List<SheetsRemoteDataSource.ExistingSheetInfo>) : SetupResult()
        data class Error(val message: String) : SetupResult()
    }

    suspend fun execute(shopName: String, forceCreate: Boolean = false): SetupResult {
        if (forceCreate) {
            val result = sheetsRepository.createWorkspace(shopName)
            return if (result.success) {
                SetupResult.NewSheetCreated(result.spreadsheetId)
            } else {
                SetupResult.Error(result.error)
            }
        }

        // Step 1: Check local storage first
        val localId = appSetupPrefs.spreadsheetId
        if (localId.isNotEmpty()) {
            val exists = sheetsRepository.verifySheetAccess(localId)
            if (exists) {
                return SetupResult.ExistingSheetLoaded(localId)
            }
            // If inaccessible, clear it and fall through to search
            appSetupPrefs.clearSpreadsheetId()
        }

        // Step 2: Search Drive for existing POS sheet
        val existingSheets = sheetsRepository.searchExistingPosSheets()
        
        return when {
            existingSheets.isEmpty() -> {
                // Step 3A: No existing sheet — create new one
                val result = sheetsRepository.createWorkspace(shopName)
                if (result.success) {
                    SetupResult.NewSheetCreated(result.spreadsheetId)
                } else {
                    SetupResult.Error(result.error)
                }
            }
            existingSheets.size == 1 -> {
                // Step 3B: Exactly one found — use it automatically
                val sheet = existingSheets.first()
                appSetupPrefs.saveProvisioningResult(sheet.spreadsheetId)
                SetupResult.ExistingSheetLoaded(sheet.spreadsheetId)
            }
            else -> {
                // Step 3C: Multiple sheets found — ask user to choose
                SetupResult.MultipleSheetFound(existingSheets)
            }
        }
    }
}
