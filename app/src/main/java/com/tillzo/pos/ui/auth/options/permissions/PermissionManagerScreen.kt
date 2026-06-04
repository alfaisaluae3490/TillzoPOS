package com.tillzo.pos.ui.auth.options.permissions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PermissionManagerScreen(
    userId: String,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Permission Manager for $userId", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
