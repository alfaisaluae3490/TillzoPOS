package com.tillzo.pos.ui.store.options.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
fun ExpenseScreen(
    onBack: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val expenses by viewModel.expenses.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<com.tillzo.pos.data.local.entity.ExpenseEntity?>(null) }
    val context = LocalContext.current
    val currencySymbol = remember { AppSetupPrefs(context).currencySymbol.ifBlank { "$" } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Expenses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Expense")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

            if (expenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No expenses recorded recently.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(expenses) { exp ->
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
                                    Text(exp.category, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(exp.description, style = MaterialTheme.typography.bodyMedium)
                                    Text(formatter.format(Date(exp.timestamp)), style = MaterialTheme.typography.bodySmall)
                                }
                                Text(
                                    "- $currencySymbol ${String.format("%.2f", exp.amount)}",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Row {
                                    IconButton(onClick = { editingExpense = exp; showAddDialog = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Expense", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteExpense(exp.system_row_id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Expense", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        val editing = editingExpense
        var category by remember { mutableStateOf(editing?.category ?: "Rent") }
        var amount by remember { mutableStateOf(editing?.amount?.toString() ?: "") }
        var description by remember { mutableStateOf(editing?.description ?: "") }

        val categories = listOf("Rent", "Electricity", "Wages", "Maintenance", "Misc")

        AlertDialog(
            onDismissRequest = { showAddDialog = false; editingExpense = null },
            title = { Text(if (editing == null) "Log New Expense" else "Edit Expense") },
            text = {
                Column {
                    // Simple category selector using buttons for quick entry
                    Text("Category:", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        categories.take(3).forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, maxLines = 1) }
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        categories.drop(3).forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, maxLines = 1) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                        label = { Text("Amount") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    amount.toDoubleOrNull()?.let {
                        if (editing != null) {
                            viewModel.updateExpense(editing.system_row_id, category, it, description)
                        } else {
                            viewModel.addExpense(category, it, description)
                        }
                        showAddDialog = false
                        editingExpense = null
                    }
                }) { Text("Save Expense") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; editingExpense = null }) { Text("Cancel") }
            }
        )
    }
}
