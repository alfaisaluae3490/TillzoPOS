package com.tillzo.pos.data.remote

import android.content.Context
import com.tillzo.pos.BuildConfig
import com.tillzo.pos.data.sync.options.token.OAuthTokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SheetsApiClient — M1.3 (v3 Blueprint)
 *
 * Retrofit2 + OkHttp singleton for Google Sheets REST API.
 * Base URL: https://sheets.googleapis.com/v4/
 *
 * Features:
 *  - BearerInterceptor: auto-attaches "Authorization: Bearer {token}" to every request
 *  - TokenAuthenticator: auto-refreshes token on 401 response (one retry)
 *  - Injected via Hilt as Singleton
 *
 * Architecture Law: Only SheetsRemoteDataSource calls this client.
 * All higher layers go through Repository → DataSource → SheetsApiClient.
 */
@Singleton
class SheetsApiClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: OAuthTokenManager,
    private val appLogger: com.tillzo.pos.utils.AppLogger
) {
    companion object {
        const val BASE_URL = "https://sheets.googleapis.com/v4/"
    }

    // ── Error Logging Interceptor ─────────────────────────────────────────────
    private val errorLoggingInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            val isCancellation = chain.call().isCanceled() ||
                e is java.net.UnknownHostException ||
                e is java.net.ConnectException ||
                (e is java.io.IOException && e.message?.contains("Canceled") == true)
            if (isCancellation) {
                appLogger.logInfo("HTTP_API_CANCEL", "Request canceled or device offline: ${request.method} ${request.url}")
            } else {
                appLogger.logError("HTTP_API_ERROR", "Network request failed: ${request.method} ${request.url}", e)
            }
            throw e
        }

        if (!response.isSuccessful) {
            val errorBodyPreview = try {
                val source = response.body?.source()
                source?.request(Long.MAX_VALUE)
                val buffer = source?.buffer
                buffer?.clone()?.readString(Charsets.UTF_8)?.take(300) ?: ""
            } catch (e: Exception) { "" }

            appLogger.logError("HTTP_API_ERROR", "API returned HTTP ${response.code} ${response.message} for ${request.method} ${request.url} | ErrorBody: $errorBodyPreview")
        }

        response
    }

    // ── Bearer Interceptor ────────────────────────────────────────────────────

    /**
     * Attaches Bearer token from OAuthTokenManager to every outgoing request.
     * Uses runBlocking to bridge the coroutine-based tokenManager into OkHttp's sync interceptor.
     */
    private val bearerInterceptor = Interceptor { chain ->
        val token = runBlocking { tokenManager.getValidToken() }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    // ── 401 Authenticator ─────────────────────────────────────────────────────

    /**
     * Called by OkHttp automatically when server returns 401.
     * Invalidates stale tokens, gets fresh one, retries request once.
     * Returns null after first retry to prevent infinite loop.
     */
    private val tokenAuthenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            if (response.request.header("Authorization-Retry") != null) return null

            // FIX (2026-08-06): do NOT invalidateTokens() first — that wiped the
            // refresh token, so getValidToken()'s manual-refresh fallback could
            // never succeed after the first 401. getValidToken() now handles the
            // full chain (cached → GoogleAuthUtil → refresh_token) and only
            // broadcasts RE_AUTH_NEEDED if everything fails.
            val newToken = runBlocking { tokenManager.getValidToken() } ?: return null

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .header("Authorization-Retry", "true")
                .build()
        }
    }

    // ── Sanitized HTTP Logging Interceptor ────────────────────────────────────

    /**
     * Custom logging interceptor that redacts the Authorization header to prevent
     * credential leakage, while still logging useful HTTP request/response info.
     */
    private val sanitizedLoggingInterceptor = Interceptor { chain ->
        val request = chain.request()
        if (BuildConfig.DEBUG) {
            val sanitizedHeaders = request.headers.map { (name, value) ->
                if (name.equals("Authorization", ignoreCase = true)) "$name: Bearer [REDACTED]" else "$name: $value"
            }
            appLogger.logInfo("HTTP", "${request.method} ${request.url}")
            sanitizedHeaders.forEach { appLogger.logInfo("HTTP", "  $it") }
        }
        val response = chain.proceed(request)
        if (BuildConfig.DEBUG) {
            appLogger.logInfo("HTTP", "${response.code} ${response.message} (${response.body?.contentLength() ?: 0}b)")
        }
        response
    }

    // ── Retrofit Instance ─────────────────────────────────────────────────────

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(bearerInterceptor)
                .addInterceptor(errorLoggingInterceptor)
                .addInterceptor(sanitizedLoggingInterceptor)
                .authenticator(tokenAuthenticator)
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /** Creates Retrofit API service instance. */
    inline fun <reified T> createService(): T = retrofit.create(T::class.java)
}
