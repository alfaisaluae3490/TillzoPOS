package com.tillzo.pos.ui.auth.options.usermanagement

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UserManagementScreen(
    onBack: () -> Unit,
    onManagePermissions: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("User Management", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { onManagePermissions("dummy_user_id") }) {
            Text("Manage User Permissions")
        }
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
