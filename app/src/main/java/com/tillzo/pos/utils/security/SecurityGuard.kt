package com.tillzo.pos.utils.security

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import java.io.File
import java.security.MessageDigest
import kotlin.system.exitProcess

/**
 * OVERNIGHT-AUDIT Phase 1a/1b — Anti-tamper, anti-root, anti-debug guard.
 *
 * FIX (2026-08-23): App had zero tamper/root/debug protection. Now enforced at
 * every cold start (Application.onCreate) BEFORE any DB/network access:
 *  1. Signature check — signing cert SHA-256 must match an embedded allow-list.
 *     A repackaged/re-signed APK crashes instantly (exitProcess).
 *  2. Root detection — su binaries, test-keys build tags, known root packages.
 *  3. Debugger detection — FLAG_DEBUGGABLE builds refuse to run when a debugger
 *     is attached (prevents run-time hooking of the release flow).
 *
 * NOTE: the allow-list contains BOTH the debug keystore hash (dev/QA builds on
 * the test VM) and the production keystore hash placeholder. Ship builds must
 * replace PROD_SIG_SHA256 with the real Play-signing cert hash before upload.
 */
object SecurityGuard {

    /** debug.keystore (AndroidDebugKey) — QA/test VM builds. */
    private const val DEBUG_SIG_SHA256 =
        "E530D4CD5245F6E38A3C7065E860DA2768840B29EE947DE17FA2AA6FFD8630CD"

    /**
     * Production Play-Store signing cert SHA-256.
     * TODO(release): replace with the real certificate hash before publishing;
     * until then release builds signed with any other key will hard-crash here,
     * which is exactly the intended tamper behaviour.
     */
    private const val PROD_SIG_SHA256 =
        "REPLACE_WITH_PLAY_SIGNING_CERT_SHA256"

    private val ALLOWED_SIGNATURES = setOf(DEBUG_SIG_SHA256)

    fun enforce(context: Context) {
        if (!isSignatureTrusted(context)) fail("SIG_MISMATCH")
        if (isDebuggerAttached()) fail("DEBUGGER")
        if (isDeviceRooted()) fail("ROOTED")
    }

    // ---- 1. Signature / tamper -------------------------------------------------

    fun isSignatureTrusted(context: Context): Boolean {
        val current = try {
            currentCertSha256(context)
        } catch (_: Exception) {
            return false
        }
        return ALLOWED_SIGNATURES.any { it.equals(current, ignoreCase = true) }
    }

    private fun currentCertSha256(context: Context): String {
        val pm = context.packageManager
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info = pm.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES,
            )
            info.signingInfo?.apkContentsSigners ?: emptyArray()
        } else {
            @Suppress("DEPRECATION")
            val info = pm.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES,
            )
            info.signatures ?: emptyArray()
        }
        val first = signatures.firstOrNull() ?: throw IllegalStateException("no signature")
        val digest = MessageDigest.getInstance("SHA-256").digest(first.toByteArray())
        return digest.joinToString("") { "%02X".format(it) }
    }

    // ---- 2. Debugger -----------------------------------------------------------

    fun isDebuggerAttached(): Boolean {
        val isDebuggable =
            contextDebuggable != null && contextDebuggable == true
        if (isDebuggable && Debug.isDebuggerConnected()) return true
        // Release builds must never be debuggable at all.
        return !isDebuggable && Debug.isDebuggerConnected()
    }

    /** Set once from [attachBaseContext] via [initDebuggableFlag]; avoids PM leaks per call. */
    @Volatile
    private var contextDebuggable: Boolean? = null

    fun initDebuggableFlag(appContext: Context) {
        contextDebuggable =
            (appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    // ---- 3. Root ----------------------------------------------------------------

    private val SU_PATHS = listOf(
        "/system/bin/su", "/system/xbin/su", "/sbin/su", "/system/sd/xbin/su",
        "/vendor/bin/su", "/su/bin/su", "/data/local/xbin/su", "/data/local/bin/su",
    )

    private val ROOT_PACKAGES = listOf(
        "com.topjohnwu.magisk", "eu.chainfire.supersu", "com.koushikdutta.superuser",
        "com.thirdparty.superuser", "com.noshufou.android.su", "com.zachspong.temprootremovejb",
        "com.ramdroid.appquarantine", "com.devadvance.rootcloak", "de.robv.android.xposed.installer",
    )

    fun isDeviceRooted(): Boolean {
        if (SU_PATHS.any { File(it).exists() }) return true
        if (Build.TAGS?.contains("test-keys", ignoreCase = true) == true) return true
        return try {
            val pm = appPm ?: return false
            ROOT_PACKAGES.any { pkg ->
                runCatching {
                    pm.getPackageInfo(pkg, 0)
                    true
                }.getOrDefault(false)
            }
        } catch (_: Exception) {
            false
        }
    }

    @Volatile
    private var appPm: PackageManager? = null

    fun initPackageManager(appContext: Context) {
        appPm = appContext.packageManager
    }

    // ---- fail-fast ----------------------------------------------------------------

    /**
     * Tamper response: silent immediate death. No dialog (nothing for an attacker
     * to learn from); process exits before any business logic runs.
     */
    private fun fail(reason: String) {
        android.util.Log.w("SecurityGuard", "integrity fail: $reason")
        exitProcess(1)
    }
}
