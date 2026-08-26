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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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

    /** OVERNIGHT-AUDIT Phase 1c — FLAG_SECURE controller (set in onCreate). */
    var screenSecurityController: com.tillzo.pos.utils.security.ScreenSecurityController? = null
        private set

    /** Lazy prefs access for screen-capture setting (avoid heavy init in field). */
    private val appSetupPrefs by lazy {
        dagger.hilt.android.EntryPointAccessors.fromApplication(
            this,
            AppSetupPrefsEntryPoint::class.java,
        ).appSetupPrefs()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    /**
     * FIX (2026-08-07): Issue 11 — Tamper detection (was log-only).
     * OVERNIGHT-AUDIT Phase 1a/1b (2026-08-23): replaced with REAL enforcement.
     * SecurityGuard.enforce() = signature allow-list + debugger + root checks;
     * mismatch/compromise -> immediate exitProcess BEFORE any business logic.
     * Old log-only verifyApkSignature() removed (dead code).
     */

    override fun onCreate() {
        super.onCreate()

        // OVERNIGHT-AUDIT Phase 1a/1b — hard integrity enforcement at cold start.
        // Order matters: signature first, then debugger, then root. Any failure
        // exits the process before WorkManager/sync/network can touch data.
        com.tillzo.pos.utils.security.SecurityGuard.initPackageManager(this)
        com.tillzo.pos.utils.security.SecurityGuard.initDebuggableFlag(this)
        com.tillzo.pos.utils.security.SecurityGuard.enforce(this)

        // OVERNIGHT-AUDIT Phase 1c — screen capture blocking (FLAG_SECURE).
        // Default ON (bank-level); Settings > Privacy toggle flips it at runtime.
        screenSecurityController = com.tillzo.pos.utils.security.ScreenSecurityController(
            application = this,
            enabled = appSetupPrefs.blockScreenCapture,
        ).also { it.register() }

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
            // FIX (2026-08-25, overnight audit BUG#2): runBlocking bridges the
            // crashing (main) thread to Dispatchers.IO so Room's
            // assertNotMainThread() doesn't kill crash-logging itself.
            runBlocking {
                withContext(Dispatchers.IO) {
                    logDao.insertLog(
                        AppLogEntity(
                            tag = "APP_CRASH",
                            logLevel = "FATAL",
                            message = deviceInfo
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("CrashHandler", "Failed to log crash: ${e.message}")
        }
        defaultHandler?.uncaughtException(thread, throwable)
    }
}
