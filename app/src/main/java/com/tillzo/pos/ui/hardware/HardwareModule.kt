package com.tillzo.pos.ui.hardware

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tillzo.pos.ui.hardware.printer.PrinterSettingsScreen
import com.tillzo.pos.ui.hardware.scanner.BarcodeScannerScreen

/**
 * M5 Hardware Module Master Wiring
 * Strict Rule: Only handles navigation, no business logic.
 */

@Composable
fun HardwareNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "printer_settings") {
        
        // M5.3 & M5.4 Hardware Printers
        composable("printer_settings") {
            PrinterSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScannerTesting = { navController.navigate("scanner_testing") }
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
    }
}
