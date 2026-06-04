package com.tillzo.pos.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tillzo.pos.ui.auth.options.login.LoginScreen
import com.tillzo.pos.ui.auth.options.permissions.PermissionManagerScreen
import com.tillzo.pos.ui.auth.options.session.PINUnlockScreen
import com.tillzo.pos.ui.auth.options.usermanagement.UserManagementScreen

/**
 * MASTER FILE: Auth Module
 * Handles all navigation and wiring for M3 (Auth & RBAC).
 * No business logic should reside here.
 */
@Composable
fun AuthNavHost(
    navController: NavHostController = rememberNavController(),
    onAuthSuccess: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "pin_unlock" // Or logic to check if OAuth login needed
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("pin_unlock") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("pin_unlock") {
            PINUnlockScreen(
                onUnlockSuccess = onAuthSuccess,
                onNeedsLogin = {
                    navController.navigate("login") {
                        popUpTo("pin_unlock") { inclusive = true }
                    }
                }
            )
        }
        
        composable("user_management") {
            UserManagementScreen(
                onBack = { navController.popBackStack() },
                onManagePermissions = { systemRowId -> 
                    navController.navigate("permission_manager/$systemRowId")
                }
            )
        }
        
        composable("permission_manager/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            PermissionManagerScreen(
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
