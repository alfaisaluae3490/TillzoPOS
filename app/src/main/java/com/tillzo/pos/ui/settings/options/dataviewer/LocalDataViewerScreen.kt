package com.tillzo.pos.ui.settings.options.dataviewer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Local Data Viewer (FIX 2026-08-06 — Faisal's requirement).
 * Shows all data stored on this phone (Room DB) — so the user can always
 * see what's saved locally, regardless of cloud sync state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalDataViewerScreen(
    onBack: () -> Unit,
    viewModel: LocalDataViewerViewModel = hiltViewModel()
) {
    val summary by viewModel.summary.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val khataEvents by viewModel.khataEvents.collectAsState()
    val tillSessions by viewModel.tillSessions.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }

    var expandedSection by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stored Data (This Phone)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Info card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "All data below is stored ON THIS PHONE",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                "It is safe even offline. Cloud sync adds a second copy.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Summary grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    summary.entries.chunked(3).forEach { rowEntries ->
                        rowEntries.forEach { (label, count) ->
                            SummaryCard(label, count, Modifier.weight(1f))
                        }
                    }
                }
            }

            // Expandable sections
            item { SectionHeader("Inventory Items (${inventory.size})", expandedSection == "inv") { expandedSection = if (expandedSection == "inv") "" else "inv" } }
            if (expandedSection == "inv") {
                items(inventory, key = { it.system_row_id }) { item ->
                    DataRow(
                        title = item.item_name,
                        subtitle = "Stock: ${item.current_stock} ${item.unit} • Price: ${item.price_per_unit} • SKU: ${item.sku}"
                    )
                }
            }

            item { SectionHeader("Sales (${sales.size})", expandedSection == "sales") { expandedSection = if (expandedSection == "sales") "" else "sales" } }
            if (expandedSection == "sales") {
                items(sales, key = { it.system_row_id }) { sale ->
                    DataRow(
                        title = "Invoice ${sale.sync_uuid.take(8).uppercase()}",
                        subtitle = "${dateFormat.format(Date(sale.timestamp))} • ${sale.payment_method} • ${sale.total} • ${sale.sync_status}"
                    )
                }
            }

            item { SectionHeader("Customers (${customers.size})", expandedSection == "cust") { expandedSection = if (expandedSection == "cust") "" else "cust" } }
            if (expandedSection == "cust") {
                items(customers, key = { it.system_row_id }) { customer ->
                    DataRow(
                        title = customer.name,
                        subtitle = "${customer.phone} • Loyalty: ${customer.loyalty_points} pts"
                    )
                }
            }

            item { SectionHeader("Expenses (${expenses.size})", expandedSection == "exp") { expandedSection = if (expandedSection == "exp") "" else "exp" } }
            if (expandedSection == "exp") {
                items(expenses, key = { it.system_row_id }) { expense ->
                    DataRow(
                        title = expense.category,
                        subtitle = "${expense.amount} • ${expense.description} • ${dateFormat.format(Date(expense.timestamp))}"
                    )
                }
            }

            item { SectionHeader("Khata Events (${khataEvents.size})", expandedSection == "kha") { expandedSection = if (expandedSection == "kha") "" else "kha" } }
            if (expandedSection == "kha") {
                items(khataEvents, key = { it.system_row_id }) { event ->
                    DataRow(
                        title = "${event.event_type} ${event.amount}",
                        subtitle = "Customer: ${event.customer_id.take(8)} • ${dateFormat.format(Date(event.created_at))}"
                    )
                }
            }

            item { SectionHeader("Till Sessions (${tillSessions.size})", expandedSection == "til") { expandedSection = if (expandedSection == "til") "" else "til" } }
            if (expandedSection == "til") {
                items(tillSessions, key = { it.sessionId }) { session ->
                    DataRow(
                        title = "Session ${session.sessionId.take(8)}",
                        subtitle = "${session.status} • Net: ${session.netCash} • ${dateFormat.format(Date(session.openedAt))}"
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SummaryCard(label: String, count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, expanded: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(if (expanded) "▾" else "▸", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DataRow(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text(
                subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
