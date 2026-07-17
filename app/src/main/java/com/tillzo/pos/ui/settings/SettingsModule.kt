package com.tillzo.pos.ui.settings

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tillzo.pos.ui.settings.options.billing.BillingScreen
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
                onNavigateToBilling = { settingsNavController.navigate("billing_screen") }
            )
        }

        composable("billing_screen") {
            BillingScreen(
                onBack = { settingsNavController.popBackStack() }
            )
        }
    }
}
