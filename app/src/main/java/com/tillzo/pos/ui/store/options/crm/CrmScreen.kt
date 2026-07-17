package com.tillzo.pos.ui.store.options.crm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.entity.CustomerEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.Edit

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CRM & Khata Ledger") },
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Left Panel: Customer List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    label = { Text("Search Customers") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(customers) { customer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.selectCustomer(customer) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedCustomer?.system_row_id == customer.system_row_id) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(customer.name, fontWeight = FontWeight.Bold)
                                Text(customer.phone, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Right Panel: Khata Details
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                if (selectedCustomer != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(selectedCustomer!!.name, style = MaterialTheme.typography.headlineSmall)
                                        IconButton(onClick = {
                                            customerToEdit = selectedCustomer
                                            showAddCustomerDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Customer", modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Text(selectedCustomer!!.phone)
                                }
                                Button(onClick = { onNavigateToStatement(selectedCustomer!!.system_row_id) }) {
                                    Icon(Icons.Default.Message, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("WhatsApp Statement")
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                StatisticBox("Total Udhaar", totalUdhaar, Color.Red)
                                StatisticBox("Total Jama", totalJama, Color(0xFF4CAF50))
                                StatisticBox("Baqaya (Balance)", baqaya, if (baqaya > 0) Color.Red else Color(0xFF4CAF50))
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { showAddEventDialog = "UDHAAR" },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text("Give Udhaar (-)")
                                }
                                Button(
                                    onClick = { showAddEventDialog = "JAMA" },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Text("Accept Jama (+)")
                                }
                            }

                            val events by viewModel.getEventsForCustomer(selectedCustomer!!.system_row_id)
                                .collectAsState(initial = emptyList())

                            Text(
                                "Transaction History",
                                color = Color(0xFF1E88E5),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            if (events.isEmpty()) {
                                Text("No transactions yet",
                                    color = Color.LightGray.copy(alpha = 0.4f), fontSize = 12.sp)
                            } else {
                                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                    items(events, key = { it.system_row_id }) { event ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = event.event_type,
                                                color = if (event.event_type == "UDHAAR")
                                                    Color(0xFFF44336) else Color(0xFF4CAF50),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                "Rs. ${event.amount}",
                                                color = Color.LightGray,
                                                fontSize = 13.sp
                                            )
                                            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                                            Text(
                                                dateFormat.format(Date(event.created_at)),
                                                color = Color.LightGray.copy(alpha = 0.4f),
                                                fontSize = 11.sp
                                            )
                                        }
                                        Divider(color = Color.LightGray.copy(alpha = 0.08f))
                                    }
                                }
                            }

                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Select a customer from the left to view Khata")
                    }
                }
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

@Composable
fun StatisticBox(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(String.format("Rs %.2f", value), style = MaterialTheme.typography.headlineSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

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
        title = { Text(if (type == "UDHAAR") "Give Udhaar" else "Accept Jama") },
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
