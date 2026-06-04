package com.tillzo.pos.ui.pos.options.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * M4.6 Multi-Tender Checkout Screen
 */
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onCheckoutComplete: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val totalDue by viewModel.totalDue.collectAsState()
    val cash by viewModel.cashInput.collectAsState()
    val card by viewModel.cardInput.collectAsState()
    val wallet by viewModel.walletInput.collectAsState()
    val udhaar by viewModel.udhaarInput.collectAsState()

    // Print Toggle (M4.7)
    var printEnabled by remember { mutableStateOf(true) }
    
    // WhatsApp Input (M4.8)
    var whatsappNumber by remember { mutableStateOf("") }
    
    // Assuming we passed 1500 as dummy due amount for demonstration
    LaunchedEffect(Unit) {
        viewModel.loadCartData(1500.0)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Text("Checkout", style = MaterialTheme.typography.headlineMedium)
            
            // Print Toggle UI
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Print Receipt")
                Switch(checked = printEnabled, onCheckedChange = { printEnabled = it })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Total Due: Rs $totalDue", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Text("Payment Split (Multi-Tender)", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = cash,
            onValueChange = { viewModel.updateTender("CASH", it) },
            label = { Text("Cash") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = card,
            onValueChange = { viewModel.updateTender("CARD", it) },
            label = { Text("Card") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = wallet,
            onValueChange = { viewModel.updateTender("WALLET", it) },
            label = { Text("Wallet (EasyPaisa/JazzCash)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        
        OutlinedTextField(
            value = udhaar,
            onValueChange = { viewModel.updateTender("UDHAAR", it) },
            label = { Text("Udhaar (Credit)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        // WhatsApp Field (M4.8)
        Text("Digital Receipt", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = whatsappNumber,
            onValueChange = { whatsappNumber = it },
            label = { Text("WhatsApp Number (e.g. +923001234567)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { 
                viewModel.completeCheckout(
                    printEnabled = printEnabled,
                    whatsappNumber = whatsappNumber,
                    onSuccess = onCheckoutComplete 
                ) 
            },
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            Text("Complete Sale", style = MaterialTheme.typography.titleLarge)
        }
    }
}
