package com.tillzo.pos.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.ui.setup.SheetPickerScreen
import com.tillzo.pos.ui.home.HomeScreen
import com.tillzo.pos.ui.home.HomeViewModel
import com.tillzo.pos.ui.home.SyncStatus
import com.tillzo.pos.ui.home.AdvancedMenuSheet
import com.tillzo.pos.ui.home.PosViewModel
import com.tillzo.pos.ui.home.ReceiptScreen
import com.tillzo.pos.ui.inventory.InventoryModule
import com.tillzo.pos.ui.store.StoreModule
import com.tillzo.pos.ui.settings.SettingsModule
import com.tillzo.pos.ui.security.RootBlockedScreen
import com.tillzo.pos.ui.security.RbacViewModel
import com.tillzo.pos.domain.auth.SessionGuardUseCase
import com.tillzo.pos.ui.till.TillOpenScreen
import com.tillzo.pos.ui.hardware.HardwareDiagnosticScreen
import com.tillzo.pos.ui.inventory.options.alerts.StockAlertsScreen
import com.tillzo.pos.ui.inventory.options.wastage.WastageLogScreen
import com.scottyab.rootbeer.RootBeer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
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
    onMenuDismiss: () -> Unit,
    appLogger: com.tillzo.pos.utils.AppLogger
) {
    val context = LocalContext.current
    val isRooted = remember {
        try {
            RootBeer(context).isRooted
        } catch (t: Throwable) {
            val suPaths = listOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su",
                "/su/bin/su"
            )
            suPaths.any { java.io.File(it).exists() }
        }
    }

    if (isRooted) {
        RootBlockedScreen()
        return
    }

    val navController = rememberNavController()

    androidx.compose.runtime.DisposableEffect(navController) {
        val listener = androidx.navigation.NavController.OnDestinationChangedListener { _, destination, arguments ->
            val argsString = arguments?.let { bundle ->
                bundle.keySet().joinToString(", ") { key -> "$key=${bundle.get(key)}" }
            } ?: ""
            appLogger.logInfo("NAVIGATION", "Navigated to: ${destination.route} | Args: $argsString")
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    val appSetupPrefs = remember { AppSetupPrefs(context) }
    val startDest = if (appSetupPrefs.spreadsheetId.isEmpty()) "sheet_picker" else "home"

    NavHost(
        navController = navController,
        startDestination = startDest,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        
        composable("sheet_picker") {
            // FIX (2026-08-06): prefer business name from onboarding for the sheet title
            val shopName = appSetupPrefs.businessName.ifBlank { appSetupPrefs.userDisplayName }
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
            // FIX (2026-08-22, GAP-2): PosViewModel is now ACTIVITY-scoped so
            // the full-screen barcode scanner route can add to the SAME cart.
            val activity = LocalContext.current as? androidx.activity.ComponentActivity
            val posViewModel: PosViewModel = if (activity != null) {
                hiltViewModel(viewModelStoreOwner = activity)
            } else {
                hiltViewModel()
            }
            // FIX (2026-08-22, DEF-32): RBAC gate — SessionGuardUseCase was
            // never called anywhere; wire admin-only menu routes through it.
            val rbacViewModel: RbacViewModel = hiltViewModel()

            val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
            val homeContext = LocalContext.current

            LaunchedEffect(Unit) {
                rbacViewModel.denied.collect { msg ->
                    android.widget.Toast.makeText(homeContext, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            }

            LaunchedEffect(homeState.syncStatus, homeState.syncMessage) {
                val message = homeState.syncMessage
                if (!message.isNullOrEmpty() && (homeState.syncStatus == SyncStatus.SUCCESS || homeState.syncStatus == SyncStatus.FAILED)) {
                    android.widget.Toast.makeText(homeContext, message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            if (homeState.syncStatus == SyncStatus.RUNNING) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { /* prevent dismissal */ }
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = homeState.syncMessage ?: "Syncing...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            HomeScreen(
                onOpenMenu = onOpenMenu,
                onNavigateToAnalytics = { navController.navigate("analytics_screen") },
                onNavigateToInventory = { navController.navigate("inventory_module") },
                onNavigateToReceipt = { invoiceId -> navController.navigate("receipt/$invoiceId") },
                onNavigateToTill = { navController.navigate("till_open") },
                // FIX (2026-08-22, GAP-2): full-screen barcode scanner route
                onNavigateToFullScanner = { navController.navigate("barcode_scanner") },
                viewModel = posViewModel
            )

            if (showAdvancedMenu) {
                AdvancedMenuSheet(
                    onDismiss = onMenuDismiss,
                    onNavigateToAnalytics = { onMenuDismiss(); navController.navigate("analytics_screen") },
                    onNavigateToInventory = { navController.navigate("inventory_module") },
                    onNavigateToCrm = { navController.navigate("store_module/crm_screen") },
                    onNavigateToReturns = { navController.navigate("store_module/returns_screen") },
                    onNavigateToHistory = { navController.navigate("store_module/history_screen") },
                    onNavigateToZReport = { navController.navigate("store_module/zreport_screen") },
                    onNavigateToExpense = {
                        rbacViewModel.requireAccess(SessionGuardUseCase.MODULE_EXPENSES) {
                            navController.navigate("store_module/expense_screen")
                        }
                    },
                    onNavigateToSettings = {
                        rbacViewModel.requireAccess(SessionGuardUseCase.MODULE_SETTINGS) {
                            navController.navigate("settings_module")
                        }
                    },
                    onNavigateToPoList = { navController.navigate("po_list") },
                    onNavigateToGrnList = { navController.navigate("grn_list") },
                    onNavigateToVendors = { onMenuDismiss(); navController.navigate("vendor_management") },
                    onNavigateToStockAdjustment = { onMenuDismiss(); navController.navigate("stock_adjustment") },
                    onNavigateToTill = { onMenuDismiss(); navController.navigate("till_open") },
                    onNavigateToVerifyQr = { onMenuDismiss(); navController.navigate("verify_qr") },
                    onNavigateToWastage = { onMenuDismiss(); navController.navigate("wastage_log") },
                    onNavigateToStockAlerts = { onMenuDismiss(); navController.navigate("stock_alerts") },
                    onNavigateToHardwareDiagnostics = { onMenuDismiss(); navController.navigate("hardware_diagnostics") },
                    onNavigateToSync = {
                        // OVERNIGHT-AUDIT Phase 2d: cooldown guard + confirm dialog
                        homeViewModel.requestSyncWithCooldown()
                    }
                )
            }

            // OVERNIGHT-AUDIT Phase 2d — 1h cooldown confirm popup (exact copy).
            val showSyncCooldown by homeViewModel.showSyncCooldownDialog.collectAsState()
            if (showSyncCooldown) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { homeViewModel.dismissSyncCooldownDialog() },
                    title = { Text("Sync") },
                    text = { Text(com.tillzo.pos.ui.home.HomeViewModel.COOLDOWN_DIALOG_TEXT) },
                    confirmButton = {
                        TextButton(onClick = {
                            homeViewModel.forceSyncNow()
                            onMenuDismiss()
                        }) { Text("Force Sync") }
                    },
                    dismissButton = {
                        TextButton(onClick = { homeViewModel.dismissSyncCooldownDialog() }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        // M4: Receipt Screen
        composable("receipt/{invoiceId}") { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId") ?: ""
            // FIX (2026-08-26, L6C-RECEIPT-LOOP): POS ViewModel scope mismatch.
            // Home route GAP-2 (2026-08-22) se ACTIVITY-scoped PosViewModel use
            // karta hai, lekin receipt abhi home-ENTRY-scoped instance le raha tha
            // -> do ALAG instances: sale VM-A (activity) pe complete hoti thi,
            // receipt VM-B (entry) pe saleResult=null dikhta tha (receipt ka
            // items/totals section ghaib — fallback mode), aur New Sale click par
            // VM-B.resetAfterSale() VM-A ka Success state kabhi null nahi karta tha
            // -> popBackStack("home") ke baad home ka LaunchedEffect(saleResult)
            // wapas Success dekh kar receipt/{invoiceId} pe RE-NAVIGATE kar deta
            // tha = infinite loop (L6C 'New Sale click no-op' bug).
            // FIX: receipt bhi wahi ACTIVITY-scoped instance share kare.
            val activity = LocalContext.current as? androidx.activity.ComponentActivity
            val posViewModel: PosViewModel = if (activity != null) {
                hiltViewModel(viewModelStoreOwner = activity)
            } else {
                hiltViewModel(
                    remember(backStackEntry) {
                        navController.getBackStackEntry("home")
                    }
                )
            }
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCategories = { navController.navigate("category_management") },
                onNavigateToUnits = { navController.navigate("product_units") },
                onNavigateToStockAlerts = { navController.navigate("stock_alerts") }
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
                onNavigateBack = { navController.popBackStack() },
                // FIX (2026-08-06): GRN cards now open their detail screen.
                onNavigateToGrnDetail = { grnId -> navController.navigate("grn_detail/$grnId") }
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

        // FIX (2026-08-06): receipt QR verification
        composable("verify_qr") {
            com.tillzo.pos.ui.store.options.verifyqr.VerifyQrScreen(
                onBack = { navController.popBackStack() }
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
                // FIX (2026-08-06): was navigating to "inventory" — a route that does
                // NOT exist (crash). The real route is the nested "inventory_module".
                onNavigateToInventory = { navController.navigate("inventory_module") }
            )
        }

        // Hardware Diagnostics Screen
        composable("hardware_diagnostics") {
            val context = LocalContext.current
            HardwareDiagnosticScreen(
                onBack = { navController.popBackStack() },
                tsplPrinter = com.tillzo.pos.utils.printer.TsplPrinter(),
                appSetupPrefs = remember { AppSetupPrefs(context) }
            )
        }

        // Business Analytics & Intelligence Dashboard
        composable("analytics_screen") {
            com.tillzo.pos.ui.analytics.AnalyticsScreen(
                onBack = { navController.popBackStack() }
            )
        }


        // FIX (2026-08-22, GAP-2): full-screen ML Kit barcode scanner was
        // ORPHANED — the screen existed but no route referenced it, so the
        // dedicated scan UX was unreachable. Wired from Home scanner card
        // ("Full Screen" button). Scanned product → added to cart directly,
        // then back to POS.
        composable("barcode_scanner") {
            // FIX (2026-08-22, GAP-2): activity-scoped PosViewModel — same
            // instance as the home route, so scanned products land in the
            // SAME cart (entry-scoped would create a fresh empty cart).
            val activity = LocalContext.current as? androidx.activity.ComponentActivity
            val scannerPosViewModel: PosViewModel = if (activity != null) {
                hiltViewModel(viewModelStoreOwner = activity)
            } else {
                hiltViewModel()
            }
            com.tillzo.pos.ui.hardware.scanner.BarcodeScannerScreen(
                onProductScanned = { product ->
                    scannerPosViewModel.addToCart(product, qty = 1.0)
                    navController.popBackStack()
                },
                onDismiss = { navController.popBackStack() }
            )
        }

    }
}
