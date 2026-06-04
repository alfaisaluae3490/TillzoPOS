package com.tillzo.pos.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tillzo.pos.ui.theme.*

/**
 * AdvancedMenuSheet — Bottom sheet containing ALL hidden advanced features.
 *
 * Blueprint OPT-2: Cashier only sees Home screen. Advanced features
 * are hidden behind the hamburger menu (≡) in this sheet.
 *
 * Features listed here (navigation hooks to be wired in M3/M4/M7):
 *  - Wastage Entry     (M7)
 *  - Returns & Refunds (M7)
 *  - CRM / Khata       (M7)
 *  - Delta Sync        (M2)
 *  - Z-Report          (M7)
 *  - Admin Dashboard   (M3)
 *  - Settings          (M3)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedMenuSheet(
    onDismiss: () -> Unit,
    onNavigateToWastage: () -> Unit = {},
    onNavigateToReturns: () -> Unit = {},
    onNavigateToCrm: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSync: () -> Unit = {},
    onNavigateToZReport: () -> Unit = {},
    onNavigateToExpense: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToPoList: () -> Unit = {},
    onNavigateToGrnList: () -> Unit = {},
    onNavigateToVendors: () -> Unit = {},
    onNavigateToStockAdjustment: () -> Unit = {},
    onNavigateToTill: () -> Unit = {},
    onNavigateToStockAlerts: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Advanced Options",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            MenuItemRow(
                icon = Icons.Default.DeleteSweep,
                label = "Wastage Entry",
                description = "Log spoiled or wasted stock",
                onClick = { onDismiss(); onNavigateToWastage() }
            )
            MenuItemRow(
                icon = Icons.Default.AssignmentReturn,
                label = "Returns & Refunds",
                description = "Process customer returns",
                onClick = { onDismiss(); onNavigateToReturns() }
            )
            MenuItemRow(
                icon = Icons.Default.People,
                label = "CRM / Khata",
                description = "Customer accounts & udhaar",
                onClick = { onDismiss(); onNavigateToCrm() }
            )
            MenuItemRow(
                icon = Icons.Default.Inventory,
                label = "Inventory & AI Entry",
                description = "Manage stock, smart OCR, QR codes",
                onClick = { onDismiss(); onNavigateToInventory() }
            )
            MenuItemRow(
                icon = Icons.Default.StoreMallDirectory,
                label = "Vendors",
                description = "Manage supplier contacts",
                onClick = { onNavigateToVendors() }
            )
            MenuItemRow(
                icon = Icons.Default.Tune,
                label = "Stock Adjustment",
                description = "Correct, damage, or return stock",
                onClick = { onNavigateToStockAdjustment() }
            )
            MenuItemRow(
                icon = Icons.Default.NotificationsActive,
                label = "Stock Alerts",
                description = "Low stock, out of stock & expiry",
                onClick = { onDismiss(); onNavigateToStockAlerts() }
            )
            MenuItemRow(
                icon = Icons.Default.ShoppingCart,
                label = "Purchase Orders",
                description = "Create & manage vendor POs",
                onClick = { onDismiss(); onNavigateToPoList() }
            )
            MenuItemRow(
                icon = Icons.Default.Receipt,
                label = "Goods Receive Notes",
                description = "GRN History & receiving",
                onClick = { onDismiss(); onNavigateToGrnList() }
            )
            MenuItemRow(
                icon = Icons.Default.History,
                label = "Transaction History",
                description = "View & reprint past sales",
                onClick = { onDismiss(); onNavigateToHistory() }
            )
            MenuItemRow(
                icon = Icons.Default.Sync,
                label = "Force Sync",
                description = "Upload pending data now",
                onClick = { onDismiss(); onNavigateToSync() }
            )
            MenuItemRow(
                icon = Icons.Default.Assessment,
                label = "Z-Report / Day Close",
                description = "End-of-day sales summary",
                onClick = { onDismiss(); onNavigateToZReport() }
            )
            MenuItemRow(
                icon = Icons.Default.AccountBalanceWallet,
                label = "Open / Close Till",
                description = "Start shift, enter opening cash",
                accentColor = AccentBlue,
                onClick = { onDismiss(); onNavigateToTill() }
            )

            Divider(color = SurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(4.dp))

            MenuItemRow(
                icon = Icons.Default.AdminPanelSettings,
                label = "Admin Dashboard",
                description = "P&L, user management, reports",
                accentColor = AccentBlue,
                onClick = { onDismiss(); onNavigateToAdmin() }
            )
            MenuItemRow(
                icon = Icons.Default.Settings,
                label = "Settings",
                description = "Shop info, printer, sync config",
                onClick = { onDismiss(); onNavigateToSettings() }
            )
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    label: String,
    description: String,
    accentColor: androidx.compose.ui.graphics.Color = TextSecondary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = accentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
