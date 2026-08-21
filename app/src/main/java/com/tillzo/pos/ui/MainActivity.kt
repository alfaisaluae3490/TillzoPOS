package com.tillzo.pos.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.sync.options.token.OAuthTokenManager
import com.tillzo.pos.domain.update.ForceUpdateState
import com.tillzo.pos.ui.auth.options.session.PINUnlockScreen
import com.tillzo.pos.ui.signin.SignInScreen
import com.tillzo.pos.ui.theme.TillzoPOSTheme
import com.tillzo.pos.ui.update.ForceUpdateScreen
import com.tillzo.pos.ui.update.ForceUpdateViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.compose.ui.platform.LocalContext

/**
 * MainActivity — single-activity entry point.
 *
 * Navigation flow:
 *   Not provisioned → SignInScreen (auto-provisions on sign-in) → HomeScreen
 *   Already provisioned → ForceUpdateCheck → HomeScreen
 *
 * Architecture Law: Only navigation wiring here. Zero business logic.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appSetupPrefs: AppSetupPrefs

    @Inject
    lateinit var authRepository: com.tillzo.pos.domain.repository.AuthRepository

    @Inject
    lateinit var appLogger: com.tillzo.pos.utils.AppLogger

    private val forceUpdateViewModel: ForceUpdateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Portrait lock in code (manifest attribute triggers Android 16 + Chrome OS warnings)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        // FIX (2026-08-07): Issue 6 — FLAG_SECURE (screenshot/recents protection)
        // App switcher/recents mein sales data screenshot leak se bachao
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            TillzoPOSTheme {
                TillzoPOSApp(
                    appSetupPrefs       = appSetupPrefs,
                    authRepository      = authRepository,
                    forceUpdateViewModel = forceUpdateViewModel,
                    appLogger            = appLogger
                )
            }
        }
    }
}


@Composable
private fun TillzoPOSApp(
    appSetupPrefs: AppSetupPrefs,
    authRepository: com.tillzo.pos.domain.repository.AuthRepository,
    forceUpdateViewModel: ForceUpdateViewModel,
    appLogger: com.tillzo.pos.utils.AppLogger
) {
    // Track sign-in / provisioning status
    var isProvisioned by remember { mutableStateOf(appSetupPrefs.isProvisioned) }
    var isUnlocked by rememberSaveable { mutableStateOf(false) }
    var showAdvancedMenu by remember { mutableStateOf(false) }
    var dismissedCountdown by remember { mutableStateOf(false) }

    // A: Runtime Permissions
    PermissionRequestHandler()

    val updateState by forceUpdateViewModel.uiState.collectAsStateWithLifecycle()

    if (!isProvisioned) {
        // ── FIX (2026-08-07): Gmail sign-in FIRST (Faisal's requirement) ──
        // Login pehle, phir business check (SheetPicker) decide karta hai:
        // existing user → simple login + cloud restore; naya user → onboarding.
        SignInScreen(
            onSignInComplete = {
                isProvisioned = true
                // Trigger force update check now that we're online
                forceUpdateViewModel.checkUpdate()
            }
        )
        return
    }

    // FIX (2026-08-06): PIN gate must only apply when a PIN actually EXISTS.
    // Old code checked appSetupPrefs.isPinEnabled (default TRUE) so a brand-new
    // user who never set a PIN got stuck on the PIN unlock screen after sign-in.
    val isPinEnabled = appSetupPrefs.isPinEnabled && authRepository.hasPIN()
    if (isPinEnabled && !isUnlocked) {
        PINUnlockScreen(
            onUnlockSuccess = { isUnlocked = true },
            onNeedsLogin = { isProvisioned = false; isUnlocked = false }
        )
        return
    }

    // FIX (2026-08-06): listen for RE_AUTH_NEEDED — when the OAuth token can no
    // longer be refreshed (expired/revoked), drop back to the SignInScreen so the
    // user can do a fresh interactive Google sign-in. Without this receiver the
    // broadcast was dead code and the app silently kept 401ing forever.
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                android.util.Log.i("RE_AUTH", "received: ${intent?.action}")
                if (intent?.action == OAuthTokenManager.ACTION_RE_AUTH_NEEDED) {
                    isProvisioned = false
                    isUnlocked = false
                }
            }
        }
        val filter = IntentFilter(OAuthTokenManager.ACTION_RE_AUTH_NEEDED)
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    // ── Signed in — check force update then show Home ─────────────────────
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val state = updateState) {
                is ForceUpdateState.HardBlock -> {
                    ForceUpdateScreen(daysRemaining = null, onDismiss = null)
                }

                is ForceUpdateState.CountdownActive -> {
                    AppNavHost(
                        onOpenMenu = { showAdvancedMenu = true },
                        showAdvancedMenu = showAdvancedMenu,
                        onMenuDismiss = { showAdvancedMenu = false },
                        appLogger = appLogger
                    )
                    if (!dismissedCountdown) {
                        ForceUpdateScreen(
                            daysRemaining = state.daysRemaining,
                            onDismiss = { dismissedCountdown = true }
                        )
                    }
                }

                else -> {
                    // UpToDate, FetchError — show HomeScreen normally
                    AppNavHost(
                        onOpenMenu = { showAdvancedMenu = true },
                        showAdvancedMenu = showAdvancedMenu,
                        onMenuDismiss = { showAdvancedMenu = false },
                        appLogger = appLogger
                    )
                }
            }
        }
    }
}

/**
 * Group A — Runtime Permission Handler.
 * Fires once on first app launch (checked via PermissionChecker).
 * CAMERA, BLUETOOTH_CONNECT (API 31+), POST_NOTIFICATIONS (API 33+).
 * Never blocks launch — graceful degradation if denied.
 */
@Composable
private fun PermissionRequestHandler() {
    val context = LocalContext.current

    val permissions = buildList {
        add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Results are handled by the system — no blocking logic needed */ }

    LaunchedEffect(Unit) {
        val missing = permissions.filter { perm ->
            ContextCompat.checkSelfPermission(context, perm) != PermissionChecker.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            launcher.launch(missing.toTypedArray())
        }
    }
}
