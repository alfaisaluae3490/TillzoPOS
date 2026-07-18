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
import okhttp3.logging.HttpLoggingInterceptor
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
    private val tokenManager: OAuthTokenManager
) {
    companion object {
        const val BASE_URL = "https://sheets.googleapis.com/v4/"
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

            val newToken = runBlocking {
                tokenManager.invalidateTokens()
                tokenManager.getValidToken()
            } ?: return null

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .header("Authorization-Retry", "true")
                .build()
        }
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
                .authenticator(tokenAuthenticator)
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                                else HttpLoggingInterceptor.Level.NONE
                    }
                )
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /** Creates Retrofit API service instance. */
    inline fun <reified T> createService(): T = retrofit.create(T::class.java)
}
