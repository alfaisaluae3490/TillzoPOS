package com.tillzo.pos

import android.app.Application
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import com.tillzo.pos.data.sync.SyncOrchestrator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class — Hilt entry point + WorkManager + M2 sync orchestration.
 *
 * Responsibilities:
 *   1. Hilt injection root (@HiltAndroidApp)
 *   2. WorkManager initialization with HiltWorkerFactory (manual init — prevents race condition)
 *   3. M2 SyncOrchestrator.scheduleAll() — starts all periodic workers + delta polling
 */
@HiltAndroidApp
class TillzoPOSApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    /** M2 — starts all WorkManager workers and delta sync polling on app start. */
    @Inject lateinit var syncOrchestrator: SyncOrchestrator

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // M2: Schedule all sync workers + start 60s delta polling
        syncOrchestrator.scheduleAll()
    }
}

