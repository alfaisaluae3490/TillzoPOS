package com.tillzo.pos.domain.provisioning

import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.repository.SheetsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * ProvisionWorkspaceUseCase
 *
 * Orchestrates the full provisioning check + auto-deploy:
 *   1. If already provisioned → emit Done
 *   2. If not provisioned → delegate to SheetsRepository to create Workspace
 *   3. On Done → save result to AppSetupPrefs
 *
 * Architecture Law: Only injected via Hilt. UI never calls Repository directly.
 */
class ProvisionWorkspaceUseCase @Inject constructor(
    private val sheetsRepository: SheetsRepository,
    private val appSetupPrefs: AppSetupPrefs
) {
    /**
     * @param accessToken  OAuth2 Bearer token from Google Sign-In (no longer needed directly here, managed by SheetsApiClient interceptor)
     * @param userEmail    Will be saved to AppSetupPrefs
     * @param displayName  Used as sheet name suffix + saved to prefs
     */
    fun execute(
        accessToken: String,
        userEmail: String,
        displayName: String
    ): Flow<ProvisioningState> = flow {

        // Save user info always
        appSetupPrefs.saveUser(email = userEmail, displayName = displayName)

        emit(ProvisioningState.Checking)

        // Already provisioned?
        if (appSetupPrefs.isProvisioned && appSetupPrefs.spreadsheetId.isNotBlank()) {
            emit(ProvisioningState.Done(appSetupPrefs.spreadsheetId))
            return@flow
        }

        emit(ProvisioningState.CreatingSheet)
        
        val shopName = displayName.ifBlank { userEmail }
        val result = sheetsRepository.createWorkspace(shopName)
        
        if (result.success) {
            appSetupPrefs.saveProvisioningResult(result.spreadsheetId)
            emit(ProvisioningState.Done(result.spreadsheetId))
        } else {
            emit(ProvisioningState.Failed(result.error))
        }
    }
}
