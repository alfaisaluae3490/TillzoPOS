package com.tillzo.pos.domain.repository

import android.content.Intent

interface AuthRepository {
    fun getAuthIntent(): Intent
    suspend fun loginWithOAuth(authCode: String): Result<Unit>
    suspend fun logout()
    fun getAccessToken(): String?
    fun isLoggedIn(): Boolean
    suspend fun setPIN(pin: String)
    fun verifyPIN(pin: String): Boolean
    fun hasPIN(): Boolean
    fun clearPIN()
}
