package com.tillzo.pos.ui.auth.options.session

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PINUnlockScreen(
    onUnlockSuccess: () -> Unit,
    onNeedsLogin: () -> Unit,
    viewModel: PINUnlockViewModel = hiltViewModel()
) {
    val unlockState by viewModel.unlockState.collectAsState()
    var pinInput by remember { mutableStateOf("") }

    LaunchedEffect(unlockState) {
        when (unlockState) {
            is PINUnlockState.NeedsOAuthLogin -> onNeedsLogin()
            is PINUnlockState.Success -> onUnlockSuccess()
            else -> {}
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        if (unlockState is PINUnlockState.NeedsPINSetup) {
            Text("Create Your PIN", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = pinInput,
                onValueChange = { pinInput = it },
                label = { Text("Enter New 4-Digit PIN") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.setPIN(pinInput) }) {
                Text("Save PIN")
            }
            return
        }

        Text("Tillzo Quick Access", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (unlockState is PINUnlockState.Error) {
            Text(
                text = (unlockState as PINUnlockState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = pinInput,
            onValueChange = { pinInput = it },
            label = { Text("Enter 4-Digit PIN") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.verifyPIN(pinInput) }) {
            Text("Unlock")
        }
        TextButton(onClick = { viewModel.logout() }) {
            Text("Logout & Login via OAuth")
        }
    }
}
