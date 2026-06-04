package com.tillzo.pos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.ui.setup.SheetPickerScreen
import com.tillzo.pos.ui.home.HomeScreen
import com.tillzo.pos.ui.home.HomeViewModel
import com.tillzo.pos.ui.home.AdvancedMenuSheet
import com.tillzo.pos.ui.home.PosViewModel
import com.tillzo.pos.ui.home.ReceiptScreen
import com.tillzo.pos.ui.inventory.InventoryModule
import com.tillzo.pos.ui.store.StoreModule
import com.tillzo.pos.ui.settings.SettingsModule
import com.tillzo.pos.ui.security.RootBlockedScreen
import com.tillzo.pos.ui.till.TillOpenScreen
import com.tillzo.pos.ui.inventory.options.alerts.StockAlertsScreen
import com.tillzo.pos.ui.inventory.options.wastage.WastageLogScreen
import com.scottyab.rootbeer.RootBeer
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Top-Level App Navigation Host.
 * Routes between the main POS screen and advanced modules.
 *
 * M4: Added receipt/{invoiceId} route.
 */
@Composable
fun AppNavHost(
    onOpenMenu: () -> Unit,
    showAdvancedMenu: Boolean,
    onMenuDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isRooted = remember { RootBeer(context).isRooted }

    if (isRooted) {
        RootBlockedScreen()
        return
    }

    val navController = rememberNavController()
    val appSetupPrefs = remember { AppSetupPrefs(context) }
    val startDest = if (appSetupPrefs.spreadsheetId.isEmpty()) "sheet_picker" else "home"

    NavHost(navController = navController, startDestination = startDest) {
        
        composable("sheet_picker") {
            val shopName = appSetupPrefs.userDisplayName
            SheetPickerScreen(
                accessToken = "",
                shopName = shopName,
                onSheetReady = { spreadsheetId ->
                    navController.navigate("home") {
                        popUpTo("sheet_picker") { inclusive = true }
                    }
                }
            )
        }

        // M4: Main POS Screen
        composable("home") {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val posViewModel: PosViewModel = hiltViewModel()

            HomeScreen(
                onOpenMenu = onOpenMenu,
                onNavigateToInventory = { navController.navigate("inventory_module") },
                onNavigateToReceipt = { invoiceId -> navController.navigate("receipt/$invoiceId") },
                viewModel = posViewModel
            )

            if (showAdvancedMenu) {
                AdvancedMenuSheet(
                    onDismiss = onMenuDismiss,
                    onNavigateToInventory = { navController.navigate("inventory_module") },
                    onNavigateToCrm = { navController.navigate("store_module/crm_screen") },
                    onNavigateToReturns = { navController.navigate("store_module/returns_screen") },
                    onNavigateToHistory = { navController.navigate("store_module/history_screen") },
                    onNavigateToZReport = { navController.navigate("store_module/zreport_screen") },
                    onNavigateToExpense = { navController.navigate("store_module/expense_screen") },
                    onNavigateToSettings = { navController.navigate("settings_module") },
                    onNavigateToPoList = { navController.navigate("po_list") },
                    onNavigateToGrnList = { navController.navigate("grn_list") },
                    onNavigateToVendors = { onMenuDismiss(); navController.navigate("vendor_management") },
                    onNavigateToStockAdjustment = { onMenuDismiss(); navController.navigate("stock_adjustment") },
                    onNavigateToTill = { onMenuDismiss(); navController.navigate("till_open") },
                    onNavigateToWastage = { onMenuDismiss(); navController.navigate("wastage_log") },
                    onNavigateToStockAlerts = { onMenuDismiss(); navController.navigate("stock_alerts") },
                    onNavigateToSync = {
                        homeViewModel.forceSync()
                        onMenuDismiss()
                    }
                )
            }
        }

        // M4: Receipt Screen
        composable("receipt/{invoiceId}") { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId") ?: ""
            // Share the same PosViewModel from the home back-stack entry so saleResult is preserved
            val posViewModel: PosViewModel = hiltViewModel(
                remember(backStackEntry) {
                    navController.getBackStackEntry("home")
                }
            )
            ReceiptScreen(
                invoiceId = invoiceId,
                onNewSale = {
                    navController.popBackStack("home", inclusive = false)
                },
                viewModel = posViewModel
            )
        }

        // M6: Inventory & Smart AI Entry Module
        composable("inventory_module") {
            InventoryModule(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // M7: Store Operations Module (CRM, Returns, Z-Report, etc)
        composable("store_module/{startDest}") { backStackEntry ->
            val startDest = backStackEntry.arguments?.getString("startDest") ?: "crm_screen"
            StoreModule(
                onNavigateBack = { navController.popBackStack() },
                startDestination = startDest
            )
        }

        // M8.3: Settings & Billing Module
        composable("settings_module") {
            SettingsModule(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Module B: Purchase Orders
        composable("po_list") {
            com.tillzo.pos.ui.inventory.module_b.PurchaseOrderListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreatePo = { navController.navigate("create_po") },
                onNavigateToPoDetail = { poId -> navController.navigate("po_detail/$poId") }
            )
        }

        composable("create_po") {
            com.tillzo.pos.ui.inventory.module_b.CreatePurchaseOrderScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("po_detail/{poId}") { backStackEntry ->
            val poId = backStackEntry.arguments?.getString("poId") ?: ""
            com.tillzo.pos.ui.inventory.module_b.PODetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateGrn = { pId -> navController.navigate("create_grn/$pId") },
                onNavigateToGrnDetail = { grnId -> navController.navigate("grn_detail/$grnId") }
            )
        }

        // Module C: GRN
        composable("grn_list") {
            com.tillzo.pos.ui.inventory.module_c.GrnListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("create_grn/{poId}") { backStackEntry ->
            val poId = backStackEntry.arguments?.getString("poId") ?: ""
            com.tillzo.pos.ui.inventory.module_c.CreateGrnScreen(
                poId = poId,
                onNavigateBack = { navController.popBackStack("po_list", inclusive = false) },
                onNavigateToSuccess = { grnId, newProducts, newBatches, updatedBatches ->
                    navController.popBackStack("po_list", inclusive = false)
                    navController.navigate("grn_success/$grnId/$newProducts/$newBatches/$updatedBatches")
                }
            )
        }

        composable(
            route = "grn_success/{grnId}/{newProductsCreated}/{batchesAdded}/{batchesUpdated}",
            arguments = listOf(
                androidx.navigation.navArgument("newProductsCreated") { type = androidx.navigation.NavType.IntType },
                androidx.navigation.navArgument("batchesAdded") { type = androidx.navigation.NavType.IntType },
                androidx.navigation.navArgument("batchesUpdated") { type = androidx.navigation.NavType.IntType }
            )
        ) { backStackEntry ->
            val grnId = backStackEntry.arguments?.getString("grnId") ?: ""
            val newProducts = backStackEntry.arguments?.getInt("newProductsCreated") ?: 0
            val newBatches = backStackEntry.arguments?.getInt("batchesAdded") ?: 0
            val updatedBatches = backStackEntry.arguments?.getInt("batchesUpdated") ?: 0
            
            com.tillzo.pos.ui.inventory.module_c.GrnSuccessScreen(
                grnId = grnId,
                newProductsCreated = newProducts,
                batchesAdded = newBatches,
                batchesUpdated = updatedBatches,
                onNavigateHome = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }

        // ── New screens added in audit pass ──────────────────────────────────

        composable("vendor_management") {
            com.tillzo.pos.ui.inventory.module_b.VendorManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("stock_adjustment") {
            com.tillzo.pos.ui.inventory.StockAdjustmentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("category_management") {
            com.tillzo.pos.ui.inventory.CategoryManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("product_units") {
            com.tillzo.pos.ui.inventory.ProductUnitsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "grn_detail/{grnId}",
            arguments = listOf(androidx.navigation.navArgument("grnId") {
                type = androidx.navigation.NavType.StringType
            })
        ) {
            com.tillzo.pos.ui.inventory.module_c.GrnDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Missing route 1 — QR Generator
        composable(
            route = "qr/{barcodeId}",
            arguments = listOf(androidx.navigation.navArgument("barcodeId") { type = androidx.navigation.NavType.StringType })
        ) { backStack ->
            val barcodeId = backStack.arguments?.getString("barcodeId") ?: ""
            com.tillzo.pos.ui.inventory.options.qr.QrGeneratorScreen(
                barcodeId = barcodeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // M-Till: Open/Close Till (Shift Management)
        composable("till_open") {
            TillOpenScreen(
                onTillOpened = {
                    navController.navigate("home") {
                        popUpTo("till_open") { inclusive = true }
                    }
                }
            )
        }

        // E1: Wastage Log Screen
        composable("wastage_log") {
            WastageLogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // D: Stock Alerts Screen (Low Stock + Out of Stock + Expiring)
        composable("stock_alerts") {
            StockAlertsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInventory = { navController.navigate("inventory") }
            )
        }
    }
}
