package com.tillzo.pos.ui.hardware.printer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PrinterSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToScannerTesting: () -> Unit,
    viewModel: PrinterSettingsViewModel = hiltViewModel()
) {
    val ipAddress by viewModel.ipAddress.collectAsState()
    val macAddress by viewModel.macAddress.collectAsState()
    val status by viewModel.printStatus.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onNavigateBack) { Text("Back to POS") }
            Text("Hardware Settings", style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onNavigateToScannerTesting) { Text("Test ML Scanner") }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bluetooth ESC/POS / TSPL
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Bluetooth Printer (SPP)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = macAddress,
                    onValueChange = { viewModel.updateMacAddress(it) },
                    label = { Text("MAC Address (e.g. 00:11:22:33:44:55)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.testBluetoothPrint() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Test Bluetooth Connection")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Network ESC/POS
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Wi-Fi / Network Printer (Port 9100)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { viewModel.updateIpAddress(it) },
                    label = { Text("IP Address (e.g. 192.168.1.100)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.testNetworkPrint() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Test Network Connection")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Status Indicator
        Surface(
            color = if (status.contains("Failed")) MaterialTheme.colorScheme.errorContainer 
                    else if (status.contains("Success")) MaterialTheme.colorScheme.primaryContainer 
                    else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Status: $status",
                modifier = Modifier.padding(16.dp),
                color = if (status.contains("Failed")) MaterialTheme.colorScheme.onErrorContainer 
                        else if (status.contains("Success")) MaterialTheme.colorScheme.onPrimaryContainer 
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
