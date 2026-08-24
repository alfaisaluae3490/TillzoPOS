package com.tillzo.pos.ui.settings

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tillzo.pos.ui.settings.options.billing.BillingScreen
import com.tillzo.pos.ui.settings.options.logviewer.SystemLogsScreen
import com.tillzo.pos.ui.settings.options.privacy.SettingsScreen

/**
 * Settings Module Navigation
 * Handles routing within the settings & billing ecosystem.
 */
@Composable
fun SettingsModule(
    onNavigateBack: () -> Unit
) {
    val settingsNavController = rememberNavController()

    NavHost(
        navController = settingsNavController,
        startDestination = "settings_main",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        
        composable("settings_main") {
            SettingsScreen(
                onBack = onNavigateBack,
                onNavigateToBilling = { settingsNavController.navigate("billing_screen") },
                onNavigateToSystemLogs = { settingsNavController.navigate("system_logs") },
                // FIX (2026-08-06): local data viewer
                onNavigateToDataViewer = { settingsNavController.navigate("data_viewer") },
                // FIX (2026-08-22, GAP-1): printer settings route — was orphaned
                onNavigateToPrinterSettings = { settingsNavController.navigate("printer_settings") }
            )
        }

        // FIX (2026-08-22, GAP-1): PrinterSettingsScreen was unreachable —
        // no route existed anywhere. Hardware printing config (Bluetooth MAC /
        // Wi-Fi IP) is now accessible from Settings → App Info → Printer Settings.
        composable("printer_settings") {
            com.tillzo.pos.ui.hardware.printer.PrinterSettingsScreen(
                onNavigateBack = { settingsNavController.popBackStack() },
                onNavigateToScannerTesting = {
                    // FIX (2026-08-22, GAP-1): no ML-scanner test screen exists;
                    // keep navigation safe — stay on printer settings.
                    settingsNavController.popBackStack()
                },
                onNavigateToDiagnostics = {}
            )
        }

        composable("billing_screen") {
            BillingScreen(
                onBack = { settingsNavController.popBackStack() }
            )
        }

        composable("system_logs") {
            SystemLogsScreen(
                onBack = { settingsNavController.popBackStack() }
            )
        }

        // FIX (2026-08-06): view all data stored on this phone
        composable("data_viewer") {
            com.tillzo.pos.ui.settings.options.dataviewer.LocalDataViewerScreen(
                onBack = { settingsNavController.popBackStack() }
            )
        }
    }
}
