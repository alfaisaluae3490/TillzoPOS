package com.tillzo.pos.ui.signin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tillzo.pos.ui.theme.*

@Composable
fun SignInScreen(
    onSignInComplete: () -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onSignInResult(result)
    }

    LaunchedEffect(uiState) {
        if (uiState is SignInUiState.Done) onSignInComplete()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is SignInUiState.MultipleSheets -> {
                SheetSelectionScreen(
                    sheets = state.sheets,
                    onSheetSelected = { id -> viewModel.selectExistingSheet(id) },
                    onCreateNew = { viewModel.createNewSheet() }
                )
            }
            is SignInUiState.Idle -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(40.dp)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null,
                        tint = AccentBlue, modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(20.dp))
                    Text("TillzoPOS", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Smart POS for your shop", color = TextSecondary, fontSize = 16.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(60.dp))

                    Button(
                        onClick = { signInLauncher.launch(viewModel.buildSignInClient().signInIntent) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Continue with Google", color = TextPrimary,
                            fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Your data stays in your own Google account.\nWe set everything up automatically.",
                        color = TextSecondary, fontSize = 13.sp,
                        textAlign = TextAlign.Center, lineHeight = 20.sp
                    )
                }
            }

            is SignInUiState.SigningIn -> ProgressContent("Signing in with Google...")
            is SignInUiState.SearchingExistingSheets -> ProgressContent(
                title = "Looking for existing data...",
                subtitle = "Checking your Google Drive"
            )
            is SignInUiState.CreatingSheet -> ProgressContent(
                title    = "Setting up your database...",
                subtitle = "Preparing your Google Sheet"
            )
            is SignInUiState.Done -> ProgressContent("Opening POS...")

            is SignInUiState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(40.dp)
                ) {
                    Text("Something went wrong", color = ErrorRed,
                        fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(state.message, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.retrySignIn() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Try Again", color = TextPrimary) }
                }
            }
        }
    }
}

@Composable
private fun ProgressContent(title: String, subtitle: String = "") {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
        CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(24.dp))
        Text(title, color = TextPrimary, fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        if (subtitle.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(subtitle, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}
