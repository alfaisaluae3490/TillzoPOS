package com.tillzo.pos.ui.store.options.crm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.entity.CustomerEntity
import com.tillzo.pos.data.local.entity.KhataEventEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CRM & Khata Ledger — FIX (2026-08-06): complete mobile-first layout rebuild.
 *
 * Before: cramped 1:2 split Row (broken on phones), raw transaction rows,
 * 5 stat boxes overflowing, hardcoded "Rs.".
 * Now: single column — full customer list when nothing selected, compact
 * chip row + rich detail pane when a customer is selected. Stats in a 2×2
 * grid, transactions as proper cards, currency symbol respected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmScreen(
    onBack: () -> Unit,
    onNavigateToStatement: (String) -> Unit,
    viewModel: CrmViewModel = hiltViewModel()
) {
    val customers by viewModel.customers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()

    val totalUdhaar by viewModel.totalUdhaar.collectAsState()
    val totalJama by viewModel.totalJama.collectAsState()
    val baqaya by viewModel.baqaya.collectAsState()

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<CustomerEntity?>(null) }
    var showAddEventDialog by remember { mutableStateOf<String?>(null) } // "UDHAAR" or "JAMA"

    val context = LocalContext.current
    val currencySymbol = remember(context) {
        com.tillzo.pos.data.local.prefs.AppSetupPrefs(context).currencySymbol.ifBlank { "Rs" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers & Ledger") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        customerToEdit = null
                        showAddCustomerDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Customer")
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
                .padding(horizontal = 14.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = { Text("Search Customers") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            val selected = selectedCustomer
            if (selected == null) {
                // ── Full customer list (nothing selected yet) ──────────────────
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (customers.isEmpty()) {
                        item { EmptyCustomersBox() }
                    } else {
                        items(customers, key = { it.system_row_id }) { customer ->
                            CustomerListCard(customer = customer) {
                                viewModel.selectCustomer(customer)
                            }
                        }
                    }
                }
            } else {
                // ── Compact customer chips (selected mode) ─────────────────────
                if (customers.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(customers, key = { it.system_row_id }) { customer ->
                            CustomerChip(
                                customer = customer,
                                selected = customer.system_row_id == selected.system_row_id
                            ) {
                                viewModel.selectCustomer(customer)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                KhataDetail(
                    customer = selected,
                    currencySymbol = currencySymbol,
                    totalUdhaar = totalUdhaar,
                    totalJama = totalJama,
                    baqaya = baqaya,
                    viewModel = viewModel,
                    onEdit = {
                        customerToEdit = selected
                        showAddCustomerDialog = true
                    },
                    onDelete = {
                        viewModel.deleteCustomer(selected)
                    },
                    onWhatsApp = { onNavigateToStatement(it) },
                    onAddUdhaar = { showAddEventDialog = "UDHAAR" },
                    onAddJama = { showAddEventDialog = "JAMA" }
                )
            }
        }
    }

    if (showAddCustomerDialog) {
        AddEditCustomerDialog(
            existingCustomer = customerToEdit,
            onDismiss = { showAddCustomerDialog = false },
            onSave = { name, phone, whatsapp, email, address ->
                viewModel.saveCustomer(customerToEdit, name, phone, whatsapp, email, address)
                showAddCustomerDialog = false
            }
        )
    }

    if (showAddEventDialog != null) {
        val isUdhaar = showAddEventDialog == "UDHAAR"
        AddKhataEventDialog(
            type = showAddEventDialog!!,
            onDismiss = { showAddEventDialog = null },
            onSave = { amount, note ->
                viewModel.addKhataEvent(amount, showAddEventDialog!!, note)
                showAddEventDialog = null
            }
        )
    }
}

// ── Customer list item (full list mode) ───────────────────────────────────────

@Composable
private fun CustomerListCard(customer: CustomerEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.take(1).uppercase(Locale.US),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(customer.name, fontWeight = FontWeight.SemiBold)
                Text(
                    customer.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyCustomersBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No customers yet", fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Tap + to add your first customer",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Customer chip (selected mode) ─────────────────────────────────────────────

@Composable
private fun CustomerChip(
    customer: CustomerEntity,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = customer.name,
                color = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

// ── Khata detail pane ─────────────────────────────────────────────────────────

@Composable
private fun KhataDetail(
    customer: CustomerEntity,
    currencySymbol: String,
    totalUdhaar: Double,
    totalJama: Double,
    baqaya: Double,
    viewModel: CrmViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onWhatsApp: (String) -> Unit,
    onAddUdhaar: () -> Unit,
    onAddJama: () -> Unit
) {
    val events by viewModel.getEventsForCustomer(customer.system_row_id)
        .collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                customer.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                customer.phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!customer.email.isNullOrBlank()) {
                                Text(
                                    customer.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Customer",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Customer",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onWhatsApp(customer.system_row_id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WhatsApp Statement")
                    }
                }
            }
        }

        // Stats 2×2 grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox("Total Credit", totalUdhaar, Color(0xFFF44336), currencySymbol, Modifier.weight(1f))
                    StatBox("Total Paid", totalJama, Color(0xFF4CAF50), currencySymbol, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(
                                            "Balance Due",
                        baqaya,
                        if (baqaya > 0) Color(0xFFF44336) else Color(0xFF4CAF50),
                        currencySymbol,
                        Modifier.weight(1f)
                    )
                    StatBox("Loyalty Pts", customer.loyalty_points, Color(0xFFFFC107), "", Modifier.weight(1f))
                }
            }
        }

        // Action buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddUdhaar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text("Add Credit (-)")
                }
                Button(
                    onClick = onAddJama,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Record Payment (+)")
                }
            }
        }

        // Section title
        item {
            Text(
                "Transaction History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        if (events.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No transactions yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(events, key = { it.system_row_id }) { event ->
                KhataEventCard(event = event, currencySymbol = currencySymbol, dateFormat = dateFormat)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun KhataEventCard(
    event: KhataEventEntity,
    currencySymbol: String,
    dateFormat: SimpleDateFormat
) {
    val isUdhaar = event.event_type == "UDHAAR"
    val color = if (isUdhaar) Color(0xFFF44336) else Color(0xFF4CAF50)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUdhaar) "Credit" else "Payment",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "$currencySymbol ${formatAmount(event.amount)}",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            if (!event.note.isNullOrBlank()) {
                Text(
                    event.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                dateFormat.format(Date(event.created_at)),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// ── Stat box (2×2 grid cell) ──────────────────────────────────────────────────

@Composable
private fun StatBox(
    label: String,
    value: Double,
    color: Color,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (currencySymbol.isEmpty()) formatAmount(value)
                else "$currencySymbol ${formatAmount(value)}",
                style = MaterialTheme.typography.titleLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatAmount(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString()
    else String.format("%.2f", value)

// ── Dialogs (unchanged) ───────────────────────────────────────────────────────

@Composable
fun AddEditCustomerDialog(
    existingCustomer: CustomerEntity? = null,
    onSave: (name: String, phone: String, whatsapp: String,
             email: String, address: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(existingCustomer?.name ?: "") }
    var phone by remember { mutableStateOf(existingCustomer?.phone ?: "") }
    var whatsapp by remember { mutableStateOf(existingCustomer?.whatsapp ?: "") }
    var email by remember { mutableStateOf(existingCustomer?.email ?: "") }
    var address by remember { mutableStateOf(existingCustomer?.address ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingCustomer == null) "Add Customer" else "Edit Customer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Triple("Name *", name, { v: String -> name = v }),
                    Triple("Phone *", phone, { v: String -> phone = v }),
                    Triple("WhatsApp", whatsapp, { v: String -> whatsapp = v }),
                    Triple("Email", email, { v: String -> email = v }),
                    Triple("Address", address, { v: String -> address = v })
                ).forEach { (label, value, onChange) ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = onChange,
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank())
                        onSave(name, phone, whatsapp, email, address)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5)
                )
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun AddKhataEventDialog(
    type: String, // "UDHAAR" or "JAMA"
    onDismiss: () -> Unit,
    onSave: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (type == "UDHAAR") "Add Credit" else "Record Payment") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                    label = { Text("Amount") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note (Optional)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                amount.toDoubleOrNull()?.let { onSave(it, note) }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
