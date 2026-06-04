package com.tillzo.pos.ui.pos

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tillzo.pos.ui.pos.options.casio.CasioScreen
import com.tillzo.pos.ui.pos.options.checkout.CheckoutScreen

/**
 * M4 POS Module Master Wiring
 * Strict Rule: Only handles navigation, no business logic.
 */

@Composable
fun PosNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "casio_screen") {
        
        // M4.1 Casio UI
        composable("casio_screen") {
            CasioScreen(
                onNavigateToCheckout = { 
                    navController.navigate("checkout_screen") 
                },
                onNavigateToMenu = {
                    // Drawer logic or external router
                }
            )
        }

        // M4.6 Checkout Tenders
        composable("checkout_screen") {
            CheckoutScreen(
                onBack = { navController.popBackStack() },
                onCheckoutComplete = {
                    navController.navigate("casio_screen") {
                        popUpTo("casio_screen") { inclusive = true }
                    }
                }
            )
        }
    }
}
