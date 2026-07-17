package com.tillzo.pos.ui.auth.options.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.openid.appauth.AuthorizationResponse

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val resp = AuthorizationResponse.fromIntent(data)
            viewModel.handleAuthResult(resp?.authorizationCode)
        } else {
            viewModel.handleAuthResult(null)
        }
    }

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> onLoginSuccess()
            is LoginState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (loginState as LoginState.Error).message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tillzo POS Login", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            when (loginState) {
                is LoginState.Loading -> CircularProgressIndicator()
                is LoginState.Error -> {
                    Text(
                        text = (loginState as LoginState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { launcher.launch(viewModel.getAuthIntent()) }) {
                        Text("Try Again")
                    }
                }
                else -> {
                    Button(onClick = { launcher.launch(viewModel.getAuthIntent()) }) {
                        Text("Login with Google")
                    }
                }
            }
        }
    }
}
