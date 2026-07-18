package com.tillzo.pos.ui.hardware

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.utils.printer.TsplPrinter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareDiagnosticScreen(
    onBack: () -> Unit,
    tsplPrinter: TsplPrinter,
    appSetupPrefs: AppSetupPrefs
) {
    var printerTestResult by remember { mutableStateOf<String?>(null) }
    var isTestingPrinter by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val printerMac = appSetupPrefs.printerMac

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hardware Diagnostics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                "Printer Diagnostics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Configured Printer MAC: ${printerMac.ifBlank { "Not configured" }}",
                color = if (printerMac.isBlank()) MaterialTheme.colorScheme.error else Color.Unspecified,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bluetooth Printer Test", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Sends a connection test command to the configured thermal printer.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isTestingPrinter = true
                            printerTestResult = null
                            scope.launch {
                                try {
                                    val success = tsplPrinter.printBarcodeLabel(
                                        printerMac,
                                        "DIAGNOSTIC",
                                        "TSPL DIAGNOSTIC TEST\nConnection OK"
                                    )
                                    printerTestResult = if (success) {
                                        "Printer test successful. Check device for output."
                                    } else {
                                        "Printer test failed. Check MAC address and Bluetooth."
                                    }
                                } catch (e: Exception) {
                                    printerTestResult = "Error: ${e.message}"
                                } finally {
                                    isTestingPrinter = false
                                }
                            }
                        },
                        enabled = !isTestingPrinter && printerMac.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isTestingPrinter) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Icon(Icons.Default.Print, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isTestingPrinter) "Testing..." else "Test Printer Connection")
                    }

                    if (printerTestResult != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            printerTestResult!!,
                            color = if (printerTestResult!!.contains("successful", ignoreCase = true))
                                Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Scanner Diagnostics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Camera / Barcode Scanner", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Scanner testing is available from the Scanner screen.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "To test: Go to Settings > Hardware > Scanner Testing",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "System Info",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Printer MAC", printerMac.ifBlank { "Not set" })
                    InfoRow("Bluetooth Available", android.bluetooth.BluetoothAdapter.getDefaultAdapter()?.isEnabled?.toString() ?: "N/A")
                    InfoRow("Camera Available", "Via ML Kit barcode scanner")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
