package com.tillzo.pos.ui.signin

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
    val context = LocalContext.current
    var showDisclosure by remember { mutableStateOf(false) }

    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onSignInResult(result)
    }

    LaunchedEffect(uiState) {
        if (uiState is SignInUiState.Done) onSignInComplete()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).systemBarsPadding(),
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
                        onClick = { showDisclosure = true },
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

                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Privacy Policy",
                        color = AccentBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://tillzopos.com/privacy"))
                            context.startActivity(intent)
                        }
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

    if (showDisclosure) {
        AlertDialog(
            onDismissRequest = { showDisclosure = false },
            title = {
                Text(
                    text = "Backup & Sync Consent",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "Tillzo POS requires access to your personal Google Drive (via drive.file scope) to create and synchronize a secure database spreadsheet. This sheet stores your sales, inventory, and expense data so you can access it across your devices.\n\nSyncing happens in the background to ensure data consistency. Your data remains stored purely locally on your device and inside your own personal Google Drive. Tillzo POS developers do NOT collect, access, transfer, or sell your data.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDisclosure = false
                        signInLauncher.launch(viewModel.buildSignInClient().signInIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Accept & Sync", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDisclosure = false }
                ) {
                    Text("Cancel", color = AccentBlue)
                }
            },
            containerColor = BackgroundDark,
            shape = RoundedCornerShape(16.dp)
        )
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
