package com.tillzo.pos.ui.signin

import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.domain.setup.SheetSetupUseCase
import com.tillzo.pos.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.tillzo.pos.data.remote.SheetsRemoteDataSource

sealed class SignInUiState {
    object Idle : SignInUiState()
    object SigningIn : SignInUiState()
    object SearchingExistingSheets : SignInUiState()
    data class MultipleSheets(val sheets: List<SheetsRemoteDataSource.ExistingSheetInfo>) : SignInUiState()
    object CreatingSheet : SignInUiState()
    object Done : SignInUiState()
    data class Error(val message: String) : SignInUiState()
}

/**
 * SignInViewModel — Google Sign-In + Auto Sheet Setup.
 *
 * Flow:
 *   1. User taps "Continue with Google"
 *   2. Google account picker dikhta hai (ek baar only)
 *   3. OAuth token milta hai (drive.file + spreadsheets scope)
 *   4. SheetSetupUseCase:
 *      a. Agar pehli baar → Google Sheet create karo + tabs + headers
 *      b. Agar pehle se hai → skip
 *   5. SpreadsheetId AppSetupPrefs mein save
 *   6. HomeScreen khulta hai
 *
 * User ko kuch bhi manually nahi karna — zero setup.
 */
@HiltViewModel
class SignInViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sheetSetupUseCase: SheetSetupUseCase,
    private val appSetupPrefs: AppSetupPrefs
) : BaseViewModel<SignInUiState>(SignInUiState.Idle) {

    fun buildSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                // Blueprint Security Rule: sirf drive.file — broader scope kabhi nahi
                Scope("https://www.googleapis.com/auth/drive.file")
            )
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun onSignInResult(result: ActivityResult) {
        viewModelScope.launch {
            try {
                updateState(SignInUiState.SigningIn)

                val task    = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.result as? GoogleSignInAccount
                    ?: run { updateState(SignInUiState.Error("Sign-in cancelled")); return@launch }

                val email       = account.email ?: ""
                val displayName = account.displayName ?: "Shop Owner"

                // Save user info
                appSetupPrefs.saveUser(email, displayName)

                // Skip auto check, let AppNavHost handle sheet_picker
                updateState(SignInUiState.Done)

            } catch (e: Exception) {
                updateState(SignInUiState.Error(e.message ?: "Sign-in failed"))
            }
        }
    }

    fun selectExistingSheet(spreadsheetId: String) {
        viewModelScope.launch {
            updateState(SignInUiState.CreatingSheet) // Using CreatingSheet as a generic "Loading" state for UI
            appSetupPrefs.saveProvisioningResult(spreadsheetId)
            updateState(SignInUiState.Done)
        }
    }

    fun createNewSheet() {
        viewModelScope.launch {
            updateState(SignInUiState.CreatingSheet)
            val displayName = appSetupPrefs.userDisplayName.ifBlank { "Shop Owner" }
            val setupResult = sheetSetupUseCase.execute(shopName = displayName, forceCreate = true)
            
            if (setupResult is SheetSetupUseCase.SetupResult.NewSheetCreated) {
                updateState(SignInUiState.Done)
            } else if (setupResult is SheetSetupUseCase.SetupResult.Error) {
                updateState(SignInUiState.Error("Sheet setup failed: ${setupResult.message}"))
            }
        }
    }

    fun retrySignIn() { updateState(SignInUiState.Idle) }

    companion object {
        // Web Client ID from Google Cloud Console
        // APIs & Services → Credentials → OAuth 2.0 → Web Client → Client ID
        const val WEB_CLIENT_ID = "191290481305-3m583fdj0hq5je8mnj34frqih33lssqc.apps.googleusercontent.com"
    }
}
