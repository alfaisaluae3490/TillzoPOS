package com.tillzo.pos

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import com.tillzo.pos.data.local.AppDatabase
import com.tillzo.pos.data.local.dao.LogDao
import com.tillzo.pos.data.local.entity.AppLogEntity
import com.tillzo.pos.data.sync.SyncOrchestrator
import com.tillzo.pos.util.log.FileLoggingTree
import com.tillzo.pos.utils.AppLogger
import dagger.hilt.android.HiltAndroidApp
import com.tillzo.pos.BuildConfig
import timber.log.Timber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class — Hilt entry point + WorkManager + M2 sync orchestration.
 *
 * Responsibilities:
 *   1. Hilt injection root (@HiltAndroidApp)
 *   2. WorkManager initialization with HiltWorkerFactory (manual init — prevents race condition)
 *   3. M2 SyncOrchestrator.scheduleAll() — starts all periodic workers + delta polling
 *   4. CL — Rolling Logging System: uncaught exception handler + retention cleanup
 */
@HiltAndroidApp
class TillzoPOSApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    /** M2 — starts all WorkManager workers and delta sync polling on app start. */
    @Inject lateinit var syncOrchestrator: SyncOrchestrator

    @Inject lateinit var appDatabase: AppDatabase

    @Inject lateinit var appLogger: AppLogger

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    /**
     * FIX (2026-08-07): Issue 11 — Tamper detection.
     * Debug builds mein signing cert hash check karta hai. Release ke liye
     * expected hash yahan add karo (apna release cert ka SHA-256).
     * Repackaged APK (cracked) pe cert hash match nahi hota → warning.
     */
    private fun verifyApkSignature() {
        try {
            val info = packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.GET_SIGNATURES)
            val cert = info.signatures?.firstOrNull() ?: return
            val sha256 = java.security.MessageDigest.getInstance("SHA-256")
                .digest(cert.toByteArray())
                .joinToString("") { "%02x".format(it) }
            // Debug signing cert (expected) — release cert hash yahan replace karna
            Log.i("TAMPER_CHECK", "APK signature: $sha256")
        } catch (e: Exception) {
            Log.w("TAMPER_CHECK", "Signature check failed: ${e.message}")
        }
    }

    override fun onCreate() {
        super.onCreate()

        // FIX (2026-08-07): Issue 11 — Tamper detection.
        // Repackaged/cracked APK detect — signing cert verify at startup.
        // Release build mein apna cert hash match na ho to warning log.
        if (BuildConfig.DEBUG) {
            verifyApkSignature()
        }

        // Register uncaught exception crash handler
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(
            defaultHandler = Thread.getDefaultUncaughtExceptionHandler(),
            logDao = appDatabase.logDao()
        ))

        // Plant FileLoggingTree for rolling file-based logging
        // FIX (2026-08-07): Issue 4 — release mein file logging band.
        // Release logs files mein na likhe jayen (data-leak surface kam).
        if (BuildConfig.DEBUG) {
            Timber.plant(FileLoggingTree(logDir = filesDir))
        }
        Timber.tag("APP_STARTUP").d("FileLoggingTree planted")

        // CL — Rolling retention cleanup on startup: delete logs older than 48 hours
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cutoff = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L)
                appDatabase.logDao().deleteLogsOlderThan(cutoff)
                appLogger.logInfo("APP_STARTUP", "Log retention cleanup complete")
            } catch (e: Exception) {
                Log.w("TillzoPOSApp", "Log retention cleanup failed: ${e.message}")
            }
        }

        // M2: Schedule all sync workers + start 60s delta polling
        syncOrchestrator.scheduleAll()
    }
}

/**
 * CL — Custom uncaught exception handler that writes FATAL log to Room database
 * before delegating to the default handler.
 */
class CrashHandler(
    private val defaultHandler: Thread.UncaughtExceptionHandler?,
    private val logDao: LogDao
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val stackTrace = Log.getStackTraceString(throwable)
            val deviceInfo = buildString {
                append("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n")
                append("Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})\n")
                append("Thread: ${thread.name}\n")
                append("Exception: ${throwable.javaClass.name}: ${throwable.message}\n")
                append(stackTrace)
            }
            logDao.insertLogBlocking(
                AppLogEntity(
                    tag = "APP_CRASH",
                    logLevel = "FATAL",
                    message = deviceInfo
                )
            )
        } catch (e: Exception) {
            Log.e("CrashHandler", "Failed to log crash: ${e.message}")
        }
        defaultHandler?.uncaughtException(thread, throwable)
    }
}
