package com.tillzo.pos.utils.security

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.WindowManager

/**
 * OVERNIGHT-AUDIT Phase 1c — bank-level screen-capture blocking.
 *
 * FIX (2026-08-23): no FLAG_SECURE anywhere in the app — screenshots and screen
 * recording captured POS data freely. Now a single setting ("Block screenshots &
 * screen recording", default ON) drives an ActivityLifecycleCallbacks hook that
 * applies/clears WindowManager.LayoutParams.FLAG_SECURE on every activity as it
 * resumes. When ON: recents thumbnail + screen record + screenshot all show a
 * blank surface. When OFF: normal behaviour (e.g. for receipt sharing demos).
 */
class ScreenSecurityController(
    private val application: Application,
    private var enabled: Boolean,
) {

    companion object {
        /** Set on construction; lets Settings VM flip FLAG_SECURE without Hilt plumbing. */
        @Volatile
        var instance: ScreenSecurityController? = null
            private set
    }

    init { instance = this }

    /** Toggle from Settings; applies immediately to the resumed activity too. */
    @Volatile
    var secureEnabled: Boolean = enabled
        set(value) {
            field = value
            currentActivity?.let { apply(it) }
        }

    private var currentActivity: Activity? = null

    fun register() {
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            currentActivity = activity
            apply(activity)
        }

        override fun onActivityPaused(activity: Activity) {
            if (currentActivity === activity) currentActivity = null
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }

    private fun apply(activity: Activity) {
        val window = activity.window ?: return
        if (secureEnabled) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE,
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
