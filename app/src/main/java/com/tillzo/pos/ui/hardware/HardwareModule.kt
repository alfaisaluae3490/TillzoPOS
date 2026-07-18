package com.tillzo.pos.ui.hardware

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.ui.hardware.printer.PrinterSettingsScreen
import com.tillzo.pos.ui.hardware.scanner.BarcodeScannerScreen
import com.tillzo.pos.utils.printer.TsplPrinter

/**
 * M5 Hardware Module Master Wiring
 * Strict Rule: Only handles navigation, no business logic.
 */

@Composable
fun HardwareNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "printer_settings",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        
        // M5.3 & M5.4 Hardware Printers
        composable("printer_settings") {
            PrinterSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScannerTesting = { navController.navigate("scanner_testing") },
                onNavigateToDiagnostics = { navController.navigate("hardware_diagnostics") }
            )
        }

        // M5.2 ML Kit Scanner Testing (Standalone UI routing)
        composable("scanner_testing") {
            BarcodeScannerScreen(
                onProductScanned = { product ->
                    // Handle standalone barcode result, route back or show Toast
                    navController.popBackStack()
                },
                onDismiss = { navController.popBackStack() }
            )
        }

        // Hardware Diagnostic Screen
        composable("hardware_diagnostics") {
            val context = LocalContext.current
            val prefs = remember { AppSetupPrefs(context) }
            HardwareDiagnosticScreen(
                onBack = { navController.popBackStack() },
                tsplPrinter = TsplPrinter(),
                appSetupPrefs = prefs
            )
        }
    }
}
