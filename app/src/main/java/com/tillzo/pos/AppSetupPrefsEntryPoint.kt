package com.tillzo.pos

import android.content.Context
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors

/**
 * OVERNIGHT-AUDIT Phase 1c — EntryPoint so the Application class can read
 * AppSetupPrefs (encrypted prefs need a Context that only exists after super
 *.onCreate(); @Inject fields are not yet usable for pre-init ordering needs).
 */
@EntryPoint
@InstallIn(dagger.hilt.components.SingletonComponent::class)
interface AppSetupPrefsEntryPoint {
    fun appSetupPrefs(): AppSetupPrefs
}
