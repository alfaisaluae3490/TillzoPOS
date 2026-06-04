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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.domain.update.ForceUpdateState
import com.tillzo.pos.ui.signin.SignInScreen
import com.tillzo.pos.ui.theme.TillzoPOSTheme
import com.tillzo.pos.ui.update.ForceUpdateScreen
import com.tillzo.pos.ui.update.ForceUpdateViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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

    private val forceUpdateViewModel: ForceUpdateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Portrait lock in code (manifest attribute triggers Android 16 + Chrome OS warnings)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()

        setContent {
            TillzoPOSTheme {
                TillzoPOSApp(
                    appSetupPrefs       = appSetupPrefs,
                    forceUpdateViewModel = forceUpdateViewModel
                )
            }
        }
    }
}


@Composable
private fun TillzoPOSApp(
    appSetupPrefs: AppSetupPrefs,
    forceUpdateViewModel: ForceUpdateViewModel
) {
    // Track sign-in / provisioning status
    var isProvisioned by remember { mutableStateOf(appSetupPrefs.isProvisioned) }
    var showAdvancedMenu by remember { mutableStateOf(false) }
    var dismissedCountdown by remember { mutableStateOf(false) }

    // A: Runtime Permissions
    PermissionRequestHandler()

    val updateState by forceUpdateViewModel.uiState.collectAsStateWithLifecycle()

    if (!isProvisioned) {
        // ── Not signed in yet — show SignInScreen ─────────────────────────
        SignInScreen(
            onSignInComplete = {
                isProvisioned = true
                // Trigger force update check now that we're online
                forceUpdateViewModel.checkUpdate()
            }
        )
        return
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
                        onMenuDismiss = { showAdvancedMenu = false }
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
                        onMenuDismiss = { showAdvancedMenu = false }
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
