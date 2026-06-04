package com.tillzo.pos.domain.usecase.security

import android.content.Context
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

import android.os.Build
import java.io.File

/**
 * M8.2: Root Detection Security UseCase.
 * Uses RootBeer to detect SU binaries, dangerous props, and test keys.
 */
class CheckRootStatusUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun execute(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= 35) { // VANILLA_ICE_CREAM
                // Android 15+ — use RootBeer but catch any alignment issues gracefully
                val rootBeer = RootBeer(context)
                rootBeer.isRootedWithoutBusyBoxCheck
            } else {
                val rootBeer = RootBeer(context)
                rootBeer.isRooted
            }
        } catch (e: Throwable) {
            // If native lib fails to load (e.g. UnsatisfiedLinkError for 16KB),
            // fall back to software-only checks
            checkRootSoftwareOnly(context)
        }
    }

    private fun checkRootSoftwareOnly(context: Context): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su",
            "/data/local/su", "/su/bin/su"
        )
        return paths.any { File(it).exists() }
    }
}
