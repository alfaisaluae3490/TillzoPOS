package com.tillzo.pos.data.remote

import android.accounts.Account
import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TokenProvider — supplies fresh OAuth 2.0 Bearer tokens for Sheets REST API.
 *
 * Uses GoogleAuthUtil.getToken() which:
 *   - Returns a cached token if still valid
 *   - Auto-refreshes if expired (no manual refresh logic needed)
 *   - Works offline if a valid token is cached
 *
 * Scopes used:
 *   - drive.file    → create/access user's own Sheet
 *   - spreadsheets  → read/write sheet values
 */
@Singleton
class TokenProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSetupPrefs: AppSetupPrefs
) {
    companion object {
        private const val SCOPES =
            "oauth2:https://www.googleapis.com/auth/drive.file " +
            "https://www.googleapis.com/auth/spreadsheets"
    }

    /**
     * Returns a valid Bearer token. Auto-refreshes if expired.
     * Throws exception if user is not signed in.
     */
    suspend fun getToken(): String = withContext(Dispatchers.IO) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
            ?: throw IllegalStateException("User not signed in")
        val email = account.email ?: throw IllegalStateException("User email is missing")

        GoogleAuthUtil.getToken(
            context,
            Account(email, "com.google"),
            SCOPES
        )
    }

    /**
     * Invalidates cached token — call this if API returns 401.
     * Next getToken() call will fetch a fresh one.
     */
    suspend fun invalidateToken() = withContext(Dispatchers.IO) {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return@withContext
        val email = account.email ?: return@withContext
        GoogleAuthUtil.clearToken(
            context,
            GoogleAuthUtil.getToken(
                context,
                Account(email, "com.google"),
                SCOPES
            )
        )
    }
}
