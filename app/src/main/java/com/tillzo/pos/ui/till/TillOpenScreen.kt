package com.tillzo.pos.ui.till

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TillOpenScreen(
    onTillOpened: () -> Unit,
    viewModel: TillViewModel = hiltViewModel()
) {
    var openingCash by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFF1A1A1A),
        topBar = {
            TopAppBar(
                title = { Text("Open Till / Start Shift", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Color(0xFF1E88E5),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Enter Opening Cash",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Count your drawer and enter the total",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = openingCash,
                onValueChange = { openingCash = it },
                label = { Text("Opening Cash (Rs.)", color = Color.White.copy(alpha = 0.7f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = Color(0xFF1E88E5)
                ),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)", color = Color.White.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                )
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.openTill(
                        openingCash = openingCash.toDoubleOrNull() ?: 0.0,
                        notes = notes
                    )
                    onTillOpened()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .navigationBarsPadding(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Open Till & Start Selling",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
