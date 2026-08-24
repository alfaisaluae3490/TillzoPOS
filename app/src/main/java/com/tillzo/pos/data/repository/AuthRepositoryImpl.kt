@file:Suppress("DEPRECATION")

package com.tillzo.pos.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Tasks
import com.tillzo.pos.data.sync.options.token.OAuthTokenManager
import com.tillzo.pos.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.*
import javax.inject.Inject
import kotlin.coroutines.resume

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: OAuthTokenManager
) : AuthRepository {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val authService = AuthorizationService(context)

    override fun getAuthIntent(): Intent {
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("https://accounts.google.com/o/oauth2/v2/auth"),
            Uri.parse("https://oauth2.googleapis.com/token")
        )

        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,
            WEB_CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse("com.tillzo.pos:/oauth2redirect")
        ).setScopes("openid", "email", "https://www.googleapis.com/auth/drive.file")

        val authRequest = authRequestBuilder.build()
        return authService.getAuthorizationRequestIntent(authRequest)
    }

    override suspend fun loginWithOAuth(authCode: String): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            val serviceConfig = AuthorizationServiceConfiguration(
                Uri.parse("https://accounts.google.com/o/oauth2/v2/auth"),
                Uri.parse("https://oauth2.googleapis.com/token")
            )

            val tokenRequest = TokenRequest.Builder(
                serviceConfig,
                WEB_CLIENT_ID
            ).setAuthorizationCode(authCode)
                .setRedirectUri(Uri.parse("com.tillzo.pos:/oauth2redirect"))
                .build()

            authService.performTokenRequest(tokenRequest) { response, ex ->
                if (ex != null) {
                    Log.e("AuthRepository", "Token exchange failed", ex)
                    continuation.resume(Result.failure(ex))
                } else if (response != null) {
                    saveTokens(response.accessToken ?: "", response.refreshToken)
                    Log.d("AuthRepository", "Token exchange success")
                    continuation.resume(Result.success(Unit))
                } else {
                    val err = Exception("Token response was null")
                    Log.e("AuthRepository", "Token exchange: null response", err)
                    continuation.resume(Result.failure(err))
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun logout() {
        tokenManager.invalidateTokens()
        sharedPrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
        val googleSignInClient = GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)
        Tasks.await(googleSignInClient.signOut())
    }

    override fun getAccessToken(): String? {
        // FIX (2026-08-06): legacy prefs first; tokenManager valid-token lookup is
        // suspend — read its cached token synchronously when prefs is empty.
        val fromPrefs = sharedPrefs.getString(KEY_ACCESS_TOKEN, null)
        if (fromPrefs != null) return fromPrefs
        return runCatching {
            kotlinx.coroutines.runBlocking { tokenManager.getValidToken() }
        }.getOrNull()
    }

    @Suppress("DEPRECATION")
    override fun isLoggedIn(): Boolean {
        if (getAccessToken() != null) return true
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    override suspend fun setPIN(pin: String) {
        sharedPrefs.edit().putString(KEY_APP_PIN, pin).apply()
    }

    override fun verifyPIN(pin: String): Boolean {
        // FIX (2026-08-22, DEF-07): unlimited PIN attempts = brute-forceable
        // (4-digit PIN = 10k combos, automated guessing takes minutes). Now:
        // 5 failed attempts → 30s lockout, 10+ → 5min lockout. Attempt count
        // and lock deadline persist in prefs so process death doesn't reset it.
        val now = System.currentTimeMillis()
        val lockUntil = sharedPrefs.getLong(KEY_PIN_LOCK_UNTIL, 0L)
        if (now < lockUntil) {
            val remaining = ((lockUntil - now) / 1000) + 1
            android.util.Log.w("AuthRepo", "PIN locked for ${remaining}s")
            return false
        }

        val storedPin = sharedPrefs.getString(KEY_APP_PIN, null)
        val ok = storedPin == pin
        if (ok) {
            sharedPrefs.edit().putInt(KEY_PIN_ATTEMPTS, 0).apply()
        } else {
            val attempts = sharedPrefs.getInt(KEY_PIN_ATTEMPTS, 0) + 1
            val lockMs = when {
                attempts >= 10 -> 300_000L  // 5 min
                attempts >= 5  -> 30_000L   // 30 sec
                else           -> 0L
            }
            sharedPrefs.edit()
                .putInt(KEY_PIN_ATTEMPTS, attempts)
                .apply()
            if (lockMs > 0) {
                sharedPrefs.edit().putLong(KEY_PIN_LOCK_UNTIL, now + lockMs).apply()
                android.util.Log.w("AuthRepo", "PIN locked for ${lockMs / 1000}s after $attempts failed attempts")
            }
        }
        return ok
    }

    override fun hasPIN(): Boolean {
        return sharedPrefs.getString(KEY_APP_PIN, null) != null
    }

    override fun clearPIN() {
        sharedPrefs.edit().remove(KEY_APP_PIN).apply()
    }

    fun saveTokens(accessToken: String, refreshToken: String?) {
        sharedPrefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            if (refreshToken != null) putString(KEY_REFRESH_TOKEN, refreshToken)
        }.apply()
        // FIX (2026-08-06): keep OAuthTokenManager (the store SheetsApiClient uses)
        // in sync — this was the "2 token stores" bug.
        if (refreshToken != null) {
            runCatching { tokenManager.storeRefreshToken(refreshToken) }
        }
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_APP_PIN = "app_pin"
        // FIX (2026-08-22, DEF-07): PIN brute-force lockout state
        private const val KEY_PIN_ATTEMPTS = "pin_attempts"
        private const val KEY_PIN_LOCK_UNTIL = "pin_lock_until"

        // FIX (2026-08-06): single source of truth — WEB client ID from
        // Constants (matches default_web_client_id). Old Android client ID
        // caused 400 invalid_grant on token exchange.
        private const val WEB_CLIENT_ID = com.tillzo.pos.utils.Constants.WEB_CLIENT_ID
    }
}
