package com.tillzo.pos.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tillzo.pos.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import net.openid.appauth.*
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
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
            "169970921764-1l1b6it59oojkigk92f80h59nsl4c194.apps.googleusercontent.com", // Example Client ID. Make sure to retrieve this from buildConfig or resources dynamically later
            ResponseTypeValues.CODE,
            Uri.parse("com.tillzo.pos:/oauth2redirect")
        ).setScopes("openid", "email", "https://www.googleapis.com/auth/drive.file")

        val authRequest = authRequestBuilder.build()
        return authService.getAuthorizationRequestIntent(authRequest)
    }

    override suspend fun loginWithOAuth(authCode: String): Result<Unit> {
        // Implementation for exchanging auth code for tokens using AppAuth
        // M3.1: To be implemented fully when integrating the AppAuth flow.
        return Result.success(Unit)
    }

    override suspend fun logout() {
        sharedPrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    override fun getAccessToken(): String? {
        return sharedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    override fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    override suspend fun setPIN(pin: String) {
        sharedPrefs.edit().putString(KEY_APP_PIN, pin).apply()
    }

    override fun verifyPIN(pin: String): Boolean {
        val storedPin = sharedPrefs.getString(KEY_APP_PIN, null)
        return storedPin == pin
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
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_APP_PIN = "app_pin"
    }
}
