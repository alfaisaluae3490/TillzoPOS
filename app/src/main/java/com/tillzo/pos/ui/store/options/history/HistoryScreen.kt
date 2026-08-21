package com.tillzo.pos.ui.store.options.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val sales by viewModel.sales.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val printStatus by viewModel.printStatus.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val context = LocalContext.current
    val currencySymbol = remember { AppSetupPrefs(context).currencySymbol.ifBlank { "$" } }

    LaunchedEffect(printStatus) {
        if (printStatus != null) {
            // Give user time to read success message before it naturally clears (handled by logic/snackbars usually)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History") },
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
                label = { Text("Search by Invoice ID or QR UUID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            val filters = listOf("All", "Today", "Yesterday", "Last 7 Days")
            var selectedFilter by remember { mutableIntStateOf(0) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEachIndexed { index, title ->
                    FilterChip(
                        selected = selectedFilter == index,
                        onClick = {
                            selectedFilter = index
                            val cal = Calendar.getInstance()
                            val now = System.currentTimeMillis()
                            val range = when(index) {
                                0 -> null
                                1 -> { 
                                    cal.set(Calendar.HOUR_OF_DAY, 0)
                                    cal.set(Calendar.MINUTE, 0)
                                    cal.set(Calendar.SECOND, 0)
                                    Pair(cal.timeInMillis, now)
                                }
                                2 -> {
                                    cal.set(Calendar.HOUR_OF_DAY, 0)
                                    cal.set(Calendar.MINUTE, 0)
                                    cal.set(Calendar.SECOND, 0)
                                    val end = cal.timeInMillis - 1
                                    cal.add(Calendar.DAY_OF_YEAR, -1)
                                    Pair(cal.timeInMillis, end)
                                }
                                3 -> {
                                    val end = now
                                    cal.add(Calendar.DAY_OF_YEAR, -7)
                                    Pair(cal.timeInMillis, end)
                                }
                                else -> null
                            }
                            viewModel.setDateRange(range)
                        },
                        label = { Text(title) }
                    )
                }
            }

            if (printStatus != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(printStatus!!, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Button(onClick = { viewModel.clearPrintStatus() }) {
                    Text("Dismiss")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sales) { sale ->
                    val isRefund = sale.total < 0
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isRefund) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Invoice: ${sale.invoiceId.take(8).uppercase()}", 
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRefund) Color.Red else Color.Unspecified
                                )
                                Text(formatter.format(Date(sale.timestamp)), style = MaterialTheme.typography.bodySmall)
                                Text("Payment: ${sale.paymentMethod}", style = MaterialTheme.typography.bodySmall)
                                
                                if (isRefund) {
                                    Text("REFUNDED", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "$currencySymbol ${String.format("%.2f", sale.total)}", 
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRefund) Color.Red else Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                IconButton(onClick = { viewModel.printDuplicateReceipt(sale) }) {
                                    Icon(Icons.Default.Print, contentDescription = "Print Duplicate", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                if (hasMore || isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoadingMore) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                TextButton(onClick = { viewModel.loadMore() }) {
                                    Text("Load More")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
