package com.tillzo.pos.ui.pos.options.casio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * M4.1 Casio UI Screen.
 * Left Side: Cart & Total
 * Right Side Top: Quick Access Grid (M4.2)
 * Right Side Bottom: Giant Numpad (M4.3)
 */
@Composable
fun CasioScreen(
    onNavigateToCheckout: () -> Unit,
    onNavigateToMenu: () -> Unit,
    viewModel: CasioViewModel = hiltViewModel()
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val numpadInput by viewModel.numpadInput.collectAsState()
    val quickGridItems = viewModel.getQuickGridItems()

    // Calculate total on the fly for UI
    val totalAmount = cartItems.sumOf { it.total }

    // M5.1 Universal HID Scanner
    val focusRequester = remember { FocusRequester() }
    var barcodeBuffer by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF5F5F5))
        .focusRequester(focusRequester)
        .focusable()
        .onKeyEvent { event ->
            if (event.type == KeyEventType.KeyUp) {
                if (event.key == Key.Enter || event.key == Key.NumPadEnter) {
                    if (barcodeBuffer.isNotEmpty()) {
                        viewModel.onBarcodeScanned(barcodeBuffer)
                        barcodeBuffer = "" // Reset
                    }
                    true
                } else {
                    val char = event.utf16CodePoint.toChar()
                    if (char.isLetterOrDigit()) {
                        barcodeBuffer += char
                        true
                    } else false
                }
            } else false
        }
    ) {
        // Left Column: Cart & Menu
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateToMenu) {
                    Text("☰", style = MaterialTheme.typography.headlineMedium)
                }
                Text("Tillzo POS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = { viewModel.clearCart() }) {
                    Text("Clear Cart", color = MaterialTheme.colorScheme.error)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Cart Items
            Column(modifier = Modifier.weight(1f)) {
                if (cartItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Cart is empty", color = Color.Gray)
                    }
                } else {
                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.name} x ${item.quantity}")
                            Text("Rs ${item.total}")
                        }
                    }
                }
            }

            // Total & Checkout
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Rs $totalAmount", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToCheckout,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                enabled = cartItems.isNotEmpty(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("CHECKOUT", style = MaterialTheme.typography.titleLarge)
            }
        }

        // Right Column: Grid and Numpad
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            // Top: Quick Access Grid (M4.2)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickGridItems) { item ->
                    Card(
                        modifier = Modifier
                            .height(80.dp)
                            .clickable { viewModel.addItemToCart(item) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(item.name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Input Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = if (numpadInput.isEmpty()) "1" else numpadInput,
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (numpadInput.isEmpty()) Color.Gray else Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom: Giant Numpad (M4.3)
            val numpadRows = listOf(
                listOf("7", "8", "9"),
                listOf("4", "5", "6"),
                listOf("1", "2", "3"),
                listOf("0", ".", "C")
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                numpadRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { digit ->
                            Button(
                                onClick = {
                                    if (digit == "C") viewModel.clearNumpad()
                                    else viewModel.appendNumpad(digit)
                                },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (digit == "C") MaterialTheme.colorScheme.errorContainer else Color.White,
                                    contentColor = if (digit == "C") MaterialTheme.colorScheme.onErrorContainer else Color.Black
                                )
                            ) {
                                Text(digit, style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
