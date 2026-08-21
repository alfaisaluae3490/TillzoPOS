package com.tillzo.pos.ui.store.options.returns

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnsScreen(
    onBack: () -> Unit,
    viewModel: ReturnsViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val foundInvoice by viewModel.foundInvoice.collectAsState()
    val returnStatus by viewModel.returnStatus.collectAsState()

    var showConfirmDialog by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val currencySymbol = remember { AppSetupPrefs(context).currencySymbol.ifBlank { "$" } } // holds reason string

    LaunchedEffect(returnStatus) {
        if (returnStatus != null) {
            // Give user time to read success message before it naturally clears (handled by logic/snackbars usually)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Returns & Refunds") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = { Text("Scan QR or Enter Invoice UUID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (returnStatus != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(returnStatus!!, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Button(onClick = { viewModel.clearStatus() }) {
                    Text("Dismiss")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (foundInvoice != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Invoice Found", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        Text("Date: ${formatter.format(Date(foundInvoice!!.timestamp))}")
                        Text("Total: $currencySymbol ${String.format("%.2f", foundInvoice!!.total)}", fontWeight = FontWeight.Bold)
                        Text("Payment Method: ${foundInvoice!!.paymentMethod}")
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Select Return Reason:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(
                                onClick = { showConfirmDialog = "Restock" },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Return to Inventory")
                            }
                            Button(
                                onClick = { showConfirmDialog = "Damaged/Wastage" },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
                            ) {
                                Text("Mark as Wastage")
                            }
                        }
                    }
                }
            } else if (searchQuery.isNotBlank() && returnStatus == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No invoice found with that ID.")
                }
            } else if (returnStatus == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Scan a customer receipt QR code to begin process.")
                }
            }
        }
    }

    if (showConfirmDialog != null) {
        val reason = showConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            title = { Text("Confirm Refund ($reason)") },
            text = { Text("This will issue a reverse transaction for $currencySymbol ${String.format("%.2f", foundInvoice?.total ?: 0.0)}. The items will ${if (reason == "Restock") "be returned to stock" else "NOT be returned to stock"}.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.processFullReturn(reason)
                    showConfirmDialog = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Issue Refund")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) { Text("Cancel") }
            }
        )
    }
}
