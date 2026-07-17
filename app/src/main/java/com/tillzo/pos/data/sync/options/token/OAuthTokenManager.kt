package com.tillzo.pos.data.sync.options.token

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.tillzo.pos.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OAuthTokenManager — M2.10
 *
 * Single source of truth for OAuth 2.0 access tokens.
 *
 * Storage: EncryptedSharedPreferences (AES-256-GCM key, AES-256-SIV value)
 * Refresh:  POST https://oauth2.googleapis.com/token (standard OAuth refresh flow)
 * Fallback: If refresh fails → broadcasts RE_AUTH_NEEDED → MainActivity shows sign-in screen
 *
 * Architecture Law:
 *   SheetsApiClient.bearerInterceptor calls OAuthTokenManager.getValidToken().
 *   Nothing outside data layer touches this class directly.
 */
@Singleton
class OAuthTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "OAuthTokenManager"
        private const val PREFS_FILE = "tillzo_oauth_prefs"
        private const val KEY_ACCESS_TOKEN  = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRY_MS     = "token_expiry_ms"

        // Google OAuth token refresh endpoint
        private const val TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"

        // Broadcast action — MainActivity listens and shows sign-in screen
        const val ACTION_RE_AUTH_NEEDED = "com.tillzo.pos.RE_AUTH_NEEDED"

        // Token validity buffer — refresh 5 minutes before actual expiry
        private const val EXPIRY_BUFFER_MS = 5 * 60 * 1000L
    }

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Plain OkHttpClient for token refresh — does NOT go through SheetsApiClient
    // (avoids circular dependency: SheetsApiClient → OAuthTokenManager → SheetsApiClient)
    private val tokenHttpClient = OkHttpClient()

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Returns a valid (non-expired) access token.
     *
     * Order:
     *   1. Return cached token if not expired
     *   2. Refresh via GoogleAuthUtil (uses Google's cached account credentials)
     *   3. If GoogleAuthUtil fails → try manual POST refresh with stored refresh_token
     *   4. If all fail → broadcast RE_AUTH_NEEDED, return null
     */
    suspend fun getValidToken(): String? = withContext(Dispatchers.IO) {
        // 1. Cached token still valid?
        val cached    = prefs.getString(KEY_ACCESS_TOKEN, null)
        val expiryMs  = prefs.getLong(KEY_EXPIRY_MS, 0L)
        if (!cached.isNullOrBlank() && System.currentTimeMillis() < expiryMs - EXPIRY_BUFFER_MS) {
            return@withContext cached
        }

        // 2. Try GoogleAuthUtil (fastest — uses Google's internal caching)
        val googleToken = fetchViaGoogleAuthUtil()
        if (googleToken != null) {
            cacheToken(googleToken, expiryMs = System.currentTimeMillis() + 3600_000L)
            return@withContext googleToken
        }

        // 3. Try manual refresh with stored refresh_token
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
        if (!refreshToken.isNullOrBlank()) {
            val refreshed = refreshWithToken(refreshToken)
            if (refreshed != null) return@withContext refreshed
        }

        // 4. All attempts failed → user must re-authenticate
        Log.e(TAG, "Token refresh failed — broadcasting RE_AUTH_NEEDED")
        broadcastReAuthNeeded()
        null
    }

    /**
     * Exchanges a server auth code for access + refresh tokens.
     * Called from SignInViewModel after a successful Google Sign-In
     * that included requestServerAuthCode().
     */
    suspend fun exchangeAuthCode(authCode: String): String? = withContext(Dispatchers.IO) {
        try {
            val body = FormBody.Builder()
                .add("client_id", Constants.WEB_CLIENT_ID)
                .add("code", authCode)
                .add("grant_type", "authorization_code")
                .build()

            val request = Request.Builder().url(TOKEN_ENDPOINT).post(body).build()
            val response = tokenHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.w(TAG, "Auth code exchange HTTP ${response.code}")
                return@withContext null
            }

            val json = JSONObject(response.body?.string() ?: return@withContext null)
            val accessToken = json.optString("access_token").takeIf { it.isNotBlank() } ?: return@withContext null
            val expiresIn = json.optLong("expires_in", 3600L) * 1000L
            val refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() }

            cacheToken(accessToken, System.currentTimeMillis() + expiresIn)
            if (refreshToken != null) {
                prefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply()
            }

            Log.i(TAG, "Auth code exchanged successfully — refresh_token=${refreshToken != null}")
            accessToken
        } catch (e: Exception) {
            Log.e(TAG, "Auth code exchange failed: ${e.message}", e)
            null
        }
    }

    /**
     * Stores refresh_token after a fresh sign-in (called from SignInViewModel).
     * Access token is cached from GoogleAuthUtil on first use.
     */
    fun storeRefreshToken(refreshToken: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply()
    }

    /**
     * Clears all stored tokens — called on sign-out.
     */
    fun invalidateTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_EXPIRY_MS)
            .apply()
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private fun fetchViaGoogleAuthUtil(): String? {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
            val email   = account.email ?: return null
            GoogleAuthUtil.getToken(
                context,
                android.accounts.Account(email, "com.google"),
                "oauth2:https://www.googleapis.com/auth/drive.file"
            )
        } catch (e: Exception) {
            Log.w(TAG, "GoogleAuthUtil.getToken failed: ${e.message}")
            null
        }
    }

    private fun refreshWithToken(refreshToken: String): String? {
        return try {
            val body = FormBody.Builder()
                .add("client_id",     Constants.WEB_CLIENT_ID)
                .add("refresh_token", refreshToken)
                .add("grant_type",    "refresh_token")
                .build()

            val request  = Request.Builder().url(TOKEN_ENDPOINT).post(body).build()
            val response = tokenHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.w(TAG, "Token refresh HTTP ${response.code}")
                return null
            }

            val json        = JSONObject(response.body?.string() ?: return null)
            val newToken    = json.optString("access_token").takeIf { it.isNotBlank() } ?: return null
            val expiresIn   = json.optLong("expires_in", 3600L) * 1000L
            val newRefresh  = json.optString("refresh_token").takeIf { it.isNotBlank() }

            cacheToken(newToken, System.currentTimeMillis() + expiresIn)
            if (newRefresh != null) prefs.edit().putString(KEY_REFRESH_TOKEN, newRefresh).apply()

            newToken
        } catch (e: IOException) {
            Log.e(TAG, "Token refresh network error: ${e.message}")
            null
        }
    }

    private fun cacheToken(accessToken: String, expiryMs: Long) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putLong(KEY_EXPIRY_MS, expiryMs)
            .apply()
    }

    private fun broadcastReAuthNeeded() {
        val intent = Intent(ACTION_RE_AUTH_NEEDED)
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }
}
