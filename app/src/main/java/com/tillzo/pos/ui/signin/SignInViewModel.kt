package com.tillzo.pos.ui.signin

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.remote.SheetsRemoteDataSource
import com.tillzo.pos.data.sync.options.token.OAuthTokenManager
import com.tillzo.pos.domain.setup.SheetSetupUseCase
import com.tillzo.pos.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val appSetupPrefs: AppSetupPrefs,
    private val tokenManager: OAuthTokenManager
) : BaseViewModel<SignInUiState>(SignInUiState.Idle) {

    fun buildSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(WEB_CLIENT_ID)
            .requestServerAuthCode(WEB_CLIENT_ID, true)
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

                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

                if (!task.isSuccessful) {
                    val apiEx = task.exception as? ApiException
                    val statusCode = apiEx?.statusCode ?: 0
                    Log.e("SignInViewModel", "Google Sign-In failed: statusCode=$statusCode", apiEx)
                    if (statusCode == 12501) {
                        updateState(SignInUiState.Idle)
                        return@launch
                    }
                    val message = when (statusCode) {
                        10 -> "Developer Error (10): SHA-1 mismatch or Client ID misconfiguration. Check Logcat."
                        12500 -> "Sign-in failed (12500): Check Google Cloud Console and SHA-1 fingerprint."
                        else -> apiEx?.localizedMessage ?: "Sign-in failed (code=$statusCode)"
                    }
                    updateState(SignInUiState.Error(message))
                    return@launch
                }

                val account = task.result as? GoogleSignInAccount
                if (account == null) {
                    Log.e("SignInViewModel", "Google Sign-In: task succeeded but account was null")
                    updateState(SignInUiState.Error("Sign-in returned empty account"))
                    return@launch
                }

                val email       = account.email ?: ""
                val displayName = account.displayName ?: "Shop Owner"
                val idToken     = account.idToken ?: "null"
                val serverAuthCode = account.serverAuthCode

                Log.d("SignInViewModel", "Sign-in success: email=$email, idToken present=${account.idToken != null}, serverAuthCode present=${serverAuthCode != null}")

                // Exchange server auth code for offline access + refresh tokens
                if (!serverAuthCode.isNullOrBlank()) {
                    Log.d("SignInViewModel", "Exchanging server auth code for tokens...")
                    val exchanged = tokenManager.exchangeAuthCode(serverAuthCode)
                    if (exchanged != null) {
                        Log.d("SignInViewModel", "Auth code exchange succeeded")
                    } else {
                        Log.w("SignInViewModel", "Auth code exchange returned null — falling back")
                    }
                } else {
                    Log.w("SignInViewModel", "No serverAuthCode returned — offline access not granted")
                }

                // Save user info
                appSetupPrefs.saveUser(email, displayName)

                // Skip auto check, let AppNavHost handle sheet_picker
                updateState(SignInUiState.Done)

            } catch (e: Exception) {
                val apiEx = e as? ApiException
                if (apiEx != null) {
                    Log.e("SignInViewModel", "ApiException: statusCode=${apiEx.statusCode}", apiEx)
                    if (apiEx.statusCode == 12501) {
                        updateState(SignInUiState.Idle)
                        return@launch
                    }
                    val message = when (apiEx.statusCode) {
                        10 -> "Developer Error (10): SHA-1 mismatch or Client ID misconfiguration."
                        12500 -> "Sign-in failed (12500): Check Google Cloud Console configuration."
                        else -> apiEx.localizedMessage ?: "Sign-in failed (code=${apiEx.statusCode})"
                    }
                    updateState(SignInUiState.Error(message))
                } else {
                    Log.e("SignInViewModel", "Unexpected sign-in error", e)
                    updateState(SignInUiState.Error(e.localizedMessage ?: "Sign-in failed"))
                }
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
        const val WEB_CLIENT_ID = "191290481305-3ag6k2hakgtdjkted28bulmig9eb1eaq.apps.googleusercontent.com"
    }
}
