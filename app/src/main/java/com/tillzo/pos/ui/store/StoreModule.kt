package com.tillzo.pos.ui.store

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tillzo.pos.ui.store.options.crm.CrmScreen
import com.tillzo.pos.ui.store.options.statement.StatementScreen
import com.tillzo.pos.ui.store.options.returns.ReturnsScreen
import com.tillzo.pos.ui.store.options.history.HistoryScreen
import com.tillzo.pos.ui.store.options.zreport.ZReportScreen
import com.tillzo.pos.ui.store.options.expense.ExpenseScreen

/**
 * M7 Store Operations Master Module
 * Handles internal navigation for CRM, Returns, Transaction History, Z-Reports, Expenses.
 */
@Composable
fun StoreModule(
    onNavigateBack: () -> Unit,
    startDestination: String = "crm_screen" // Allow passing direct deep link to specific M7 screen
) {
    val storeNavController = rememberNavController()

    NavHost(
        navController = storeNavController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        
        // M7.1 CRM & Khata Ledger
        composable("crm_screen") {
            CrmScreen(
                onBack = onNavigateBack,
                onNavigateToStatement = { customerId ->
                    storeNavController.navigate("statement_screen/$customerId")
                }
            )
        }

        // M7.2 WhatsApp Statement
        composable("statement_screen/{customerId}") { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: return@composable
            StatementScreen(
                customerId = customerId,
                onBack = { storeNavController.popBackStack() }
            )
        }

        // Missing route 2 — WhatsApp Statement
        composable(
            route = "statement/{customerId}",
            arguments = listOf(androidx.navigation.navArgument("customerId") { type = androidx.navigation.NavType.StringType })
        ) { backStack ->
            val customerId = backStack.arguments?.getString("customerId") ?: ""
            StatementScreen(
                customerId = customerId,
                onBack = { storeNavController.popBackStack() }
            )
        }

        // M7.3 Returns & Refunds
        composable("returns_screen") {
            ReturnsScreen(onBack = onNavigateBack)
        }

        // M7.4 Transaction History
        composable("history_screen") {
            HistoryScreen(onBack = onNavigateBack)
        }

        // M7.5 Z-Report & System Health
        composable("zreport_screen") {
            ZReportScreen(onBack = onNavigateBack)
        }

        // M7.6 Expense Tracking
        composable("expense_screen") {
            ExpenseScreen(onBack = onNavigateBack)
        }
    }
}
