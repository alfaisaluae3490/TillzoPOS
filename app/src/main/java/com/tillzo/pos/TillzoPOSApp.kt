package com.tillzo.pos

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import com.tillzo.pos.data.local.AppDatabase
import com.tillzo.pos.data.local.dao.LogDao
import com.tillzo.pos.data.local.entity.AppLogEntity
import com.tillzo.pos.data.sync.SyncOrchestrator
import com.tillzo.pos.utils.AppLogger
import dagger.hilt.android.HiltAndroidApp
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

    override fun onCreate() {
        super.onCreate()

        // Register uncaught exception crash handler
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(
            defaultHandler = Thread.getDefaultUncaughtExceptionHandler(),
            logDao = appDatabase.logDao()
        ))

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
