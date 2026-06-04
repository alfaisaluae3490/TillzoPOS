package com.tillzo.pos.data.local.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UpdatePrefs — SharedPreferences wrapper for force-update tracking.
 *
 * Stores the timestamp when the app was first detected as outdated.
 * This timestamp is used to calculate the 3-day grace period countdown.
 */
@Singleton
class UpdatePrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("tillzo_update_prefs", Context.MODE_PRIVATE)
    }

    private companion object {
        const val KEY_OUTDATED_FIRST_DETECTED = "outdated_first_detected_at"
    }

    /**
     * Returns the timestamp when outdated was first detected.
     * If no timestamp stored yet, saves current time and returns it.
     */
    fun getOrSetOutdatedTimestamp(): Long {
        val existing = prefs.getLong(KEY_OUTDATED_FIRST_DETECTED, -1L)
        return if (existing != -1L) {
            existing
        } else {
            val now = System.currentTimeMillis()
            prefs.edit().putLong(KEY_OUTDATED_FIRST_DETECTED, now).apply()
            now
        }
    }

    /**
     * Clear the stored timestamp — called when app is up to date.
     */
    fun clearOutdatedTimestamp() {
        prefs.edit().remove(KEY_OUTDATED_FIRST_DETECTED).apply()
    }
}
