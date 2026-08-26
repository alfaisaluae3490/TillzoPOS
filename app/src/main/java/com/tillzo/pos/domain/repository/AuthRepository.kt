package com.tillzo.pos.domain.repository

import android.content.Intent

interface AuthRepository {
    fun getAuthIntent(): Intent
    suspend fun loginWithOAuth(authCode: String): Result<Unit>
    suspend fun logout()
    /**
     * PLAY POLICY (2026-08-24, T2): Account & Data Deletion requirement.
     * Revokes the Google OAuth token server-side (POST /revoke) so the app
     * disappears from the user's Google Account "Apps with access" page,
     * then clears all locally stored tokens.
     */
    suspend fun revokeGoogleAccess(): Boolean
    fun getAccessToken(): String?
    fun isLoggedIn(): Boolean
    suspend fun setPIN(pin: String)
    fun verifyPIN(pin: String): Boolean
    fun hasPIN(): Boolean
    fun clearPIN()
}

