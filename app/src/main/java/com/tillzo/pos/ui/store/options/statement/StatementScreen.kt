package com.tillzo.pos.ui.store.options.statement

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementScreen(
    customerId: String,
    onBack: () -> Unit,
    viewModel: StatementViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currencySymbol = remember { AppSetupPrefs(context).currencySymbol }
    val customer by viewModel.customer.collectAsState()
    val events by viewModel.events.collectAsState()
    val baqaya by viewModel.baqaya.collectAsState()

    LaunchedEffect(customerId) {
        viewModel.loadData(customerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Statement") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val phone = customer?.phone ?: return@ExtendedFloatingActionButton
                    val msg = viewModel.generateWhatsAppMessage()
                    
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(msg)}")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Handle WhatsApp not installed
                    }
                },
                icon = { Icon(Icons.Default.Send, contentDescription = null) },
                text = { Text("Send via WhatsApp") },
                containerColor = Color(0xFF25D366) // WhatsApp Green
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (customer != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(customer!!.name, style = MaterialTheme.typography.titleLarge)
                        Text(customer!!.phone, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Net Balance: $currencySymbol ${String.format("%.2f", baqaya)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (baqaya < 0) Color.Red else Color(0xFF4CAF50)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Transaction History", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(events) { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    if (event.event_type == "UDHAAR") "Credit" else "Payment", 
                                    fontWeight = FontWeight.Bold,
                                    color = if (event.event_type == "UDHAAR") Color.Red else Color(0xFF4CAF50)
                                )
                                Text(formatter.format(Date(event.created_at)), style = MaterialTheme.typography.bodySmall)
                                if (!event.note.isNullOrBlank()) {
                                    Text("Note: ${event.note}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Text(
                                "$currencySymbol ${Math.abs(event.amount)}", 
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
