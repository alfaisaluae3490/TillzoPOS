package com.tillzo.pos.ui.settings.options.billing

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.billing.SubscriptionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    onBack: () -> Unit,
    viewModel: BillingViewModel = hiltViewModel()
) {
    val status by viewModel.subscriptionStatus.collectAsState()
    val errorString by viewModel.billingError.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tillzo POS Subscription") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Error banner
            if (errorString != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Text(
                        text = errorString!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (status) {
                SubscriptionStatus.LOADING -> {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Connecting to Google Play...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                SubscriptionStatus.ACTIVE -> {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(100.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Subscription Active", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "You are currently on the Tillzo POS Pro plan. All data sync features are enabled.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                SubscriptionStatus.EXPIRED, SubscriptionStatus.ERROR -> {
                    Icon(
                        if (status == SubscriptionStatus.ERROR) Icons.Default.Error else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (status == SubscriptionStatus.ERROR) MaterialTheme.colorScheme.error else Color(0xFFFF9800),
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(if (status == SubscriptionStatus.ERROR) "Connection Error" else "Trial Expired", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "To continue syncing data to Google Sheets, please renew your subscription via Google Play.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = {
                            val activity = context as? Activity
                            if (activity != null) {
                                viewModel.billingManager.initiatePurchaseFlow(activity)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Subscribe with Google Play", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Billing is securely managed by Google Play. We do not store any payment information locally.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
