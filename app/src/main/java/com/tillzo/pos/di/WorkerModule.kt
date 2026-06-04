package com.tillzo.pos.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * WorkerModule — reserved for any future WorkManager-related DI bindings.
 *
 * IMPORTANT: WorkManager is NOT provided as a Hilt singleton here.
 *
 * Why: WorkManager.getInstance(context) during Hilt initialization calls
 * Configuration.Provider.getWorkManagerConfiguration() on TillzoPOSApp.
 * That method accesses TillzoPOSApp.workerFactory (@Inject lateinit var),
 * which is NOT yet injected at that point → UninitializedPropertyAccessException.
 *
 * Solution: SyncOrchestrator gets WorkManager via lazy delegate:
 *   private val workManager by lazy { WorkManager.getInstance(context) }
 * This defers access until scheduleAll() is called from TillzoPOSApp.onCreate(),
 * AFTER super.onCreate() has completed all Hilt injections.
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkerModule
// Intentionally empty — WorkManager accessed lazily in SyncOrchestrator
