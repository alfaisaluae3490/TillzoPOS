package com.tillzo.pos.ui.store.options.zreport

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tillzo.pos.ui.till.TillViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZReportScreen(
    onBack: () -> Unit,
    viewModel: ZReportViewModel = hiltViewModel(),
    tillViewModel: TillViewModel = hiltViewModel()
) {
    val totalSales by viewModel.totalSalesToday.collectAsStateWithLifecycle()
    val totalExpenses by viewModel.totalExpensesToday.collectAsStateWithLifecycle()
    val netDrawer by viewModel.netCashDrawer.collectAsStateWithLifecycle()
    val pendingSync by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val status by viewModel.reportStatus.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var physicalCount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Z-Report & System Health") },
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
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (status != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (status!!.startsWith("Error"))
                            MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Text(
                        text = status!!,
                        modifier = Modifier.padding(16.dp),
                        color = if (status!!.startsWith("Error"))
                            MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Sync Health Warning
            if (pendingSync > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SyncProblem, contentDescription = null, tint = Color(0xFFFF9800))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Unsynced Data Pending", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            Text(
                                "You have $pendingSync items waiting. Cannot close till until uploaded.",
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }
            }

            // Financial Summary Block
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Today's Summary", style = MaterialTheme.typography.titleLarge)
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Sales:")
                        Text("Rs ${String.format("%.2f", totalSales)}", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Expenses:")
                        Text("- Rs ${String.format("%.2f", totalExpenses)}", color = Color.Red, fontWeight = FontWeight.SemiBold)
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Expected Cash in Drawer:", fontWeight = FontWeight.Bold)
                        Text(
                            "Rs ${String.format("%.2f", netDrawer)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Till / Shift Summary ──────────────────────────────────────────
            activeSession?.let { session ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Till / Cash Drawer",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))

                        TillRow(
                            label = "Opened At",
                            value = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                .format(Date(session.openedAt))
                        )
                        TillRow(
                            label = "Opening Cash",
                            value = "Rs ${String.format("%.2f", session.openingCash)}"
                        )
                        TillRow(
                            label = "Cash Sales",
                            value = "Rs ${String.format("%.2f", session.totalCashSales)}"
                        )
                        TillRow(
                            label = "Expected in Drawer",
                            value = "Rs ${String.format("%.2f", session.expectedCash)}",
                            isHighlighted = true
                        )
                        TillRow(
                            label = "Total Transactions",
                            value = "${session.totalSalesCount}"
                        )

                        Spacer(Modifier.height(14.dp))

                        OutlinedTextField(
                            value = physicalCount,
                            onValueChange = { physicalCount = it },
                            label = { Text("Physical Cash Count (Rs.)", color = Color.White.copy(alpha = 0.7f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF1E88E5),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                cursorColor = Color(0xFF1E88E5)
                            )
                        )

                        // Live variance indicator
                        physicalCount.toDoubleOrNull()?.let { counted ->
                            val variance = counted - session.expectedCash
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Variance:", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                                Text(
                                    text = "${if (variance >= 0) "+" else ""}${String.format("%.2f", variance)}",
                                    color = if (variance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick = {
                                tillViewModel.closeTill(
                                    sessionId = session.sessionId,
                                    physicalCashCount = physicalCount.toDoubleOrNull() ?: 0.0
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                            enabled = pendingSync == 0 && physicalCount.isNotBlank()
                        ) {
                            Text(
                                "Close Till & End Shift",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showConfirmDialog = true },
                enabled = pendingSync == 0,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.LockClock, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("CLOSE DAY (Z-REPORT)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showConfirmDialog) {
        var dayClosePhysicalCount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Day Close") },
            text = {
                Column {
                    Text("Count the cash in your drawer and enter the total below.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dayClosePhysicalCount,
                        onValueChange = { dayClosePhysicalCount = it },
                        label = { Text("Physical Cash Count (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    dayClosePhysicalCount.toDoubleOrNull()?.let { counted ->
                        val variance = counted - netDrawer
                        Text(
                            "Expected: Rs ${String.format("%.2f", netDrawer)}",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Variance: ${if (variance >= 0) "+" else ""}${String.format("%.2f", variance)}",
                            color = if (variance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("This action cannot be undone.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.executeDayClose(dayClosePhysicalCount.toDoubleOrNull() ?: netDrawer)
                        showConfirmDialog = false
                    },
                    enabled = dayClosePhysicalCount.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm & Close")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TillRow(label: String, value: String, isHighlighted: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
        Text(
            value,
            color = if (isHighlighted) Color(0xFF1E88E5) else Color.White,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}
