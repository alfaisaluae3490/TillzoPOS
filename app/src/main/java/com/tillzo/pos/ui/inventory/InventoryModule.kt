package com.tillzo.pos.ui.inventory

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tillzo.pos.ui.inventory.options.crud.InventoryCrudScreen
import com.tillzo.pos.ui.inventory.options.ocr.OcrEntryScreen
import com.tillzo.pos.ui.inventory.options.qr.QrGeneratorScreen

/**
 * M6.1 Inventory Module Master Wiring
 *
 * Rules: Navigation + Wiring ONLY. No Business Logic here.
 */
@Composable
fun InventoryModule(
    onNavigateBack: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "inventory_crud") {
        composable("inventory_crud") {
            InventoryCrudScreen(
                onNavigateBack = onNavigateBack,
                onNavigateToOcr = { navController.navigate("ocr_entry") },
                onNavigateToQr = { barcode -> navController.navigate("qr_generator/$barcode") },
                navController = navController
            )
        }
        composable("ocr_entry") {
            OcrEntryScreen(
                onNavigateBack = { navController.popBackStack() },
                onTextScanned = { weightUnit, rawText ->
                    // Set the result in the previous back stack entry's saved state handle
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("ocr_scanned_weight", weightUnit)
                }
            )
        }
        composable("qr_generator/{barcode_id}") { backStackEntry ->
            val barcode = backStackEntry.arguments?.getString("barcode_id") ?: ""
            QrGeneratorScreen(
                barcodeId = barcode,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
