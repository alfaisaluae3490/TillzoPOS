package com.tillzo.pos.ui.inventory.options.wastage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.entity.WastageEntity
import com.tillzo.pos.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WastageLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: WastageViewModel = hiltViewModel()
) {
    val filtered by viewModel.filteredWastage.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val totalLossToday by viewModel.totalLossToday.collectAsState()
    val totalLossMonth by viewModel.totalLossMonth.collectAsState()
    val allWastage by viewModel.allWastage.collectAsState()
    val context = LocalContext.current
    val currencySymbol = remember { AppSetupPrefs(context).currencySymbol.ifBlank { "$" } }

    var showLogDialog by remember { mutableStateOf(false) }
    // GAP-4 FIX (2026-08-22): delete confirm dialog state
    var entryToDelete by remember { mutableStateOf<WastageEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wastage Log", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showLogDialog = true },
                containerColor = AccentBlue
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Wastage", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary Cards
            WastageSummaryCard(
                totalToday = totalLossToday,
                totalMonth = totalLossMonth,
                countToday = allWastage.count { it.wastageDate == java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()) }
            )

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { viewModel.setFilter(null) },
                        label = { Text("All") }
                    )
                }
                items(WastageReason.values().toList()) { reason ->
                    FilterChip(
                        selected = selectedFilter == reason,
                        onClick = { viewModel.setFilter(if (selectedFilter == reason) null else reason) },
                        label = { Text(reason.label) }
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No wastage records", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.wastageId }) { entry ->
                        WastageEntryCard(entry = entry, onDelete = { entryToDelete = entry })
                    }
                }
            }
        }
    }

    if (showLogDialog) {
        LogWastageDialog(
            viewModel = viewModel,
            onDismiss = { showLogDialog = false }
        )
    }

    // GAP-4 FIX (2026-08-22): confirm + soft-delete wastage entry
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Wastage Entry", fontWeight = FontWeight.Bold) },
            text = { Text("Delete \"${entry.productName}\" (${entry.quantity} ${entry.unit}, ${entry.reason}) from the log? Sheet audit trail unchanged rahega.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWastage(entry)
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun WastageSummaryCard(totalToday: Double, totalMonth: Double, countToday: Int) {
    val context = LocalContext.current
    val currencySymbol = remember { AppSetupPrefs(context).currencySymbol.ifBlank { "$" } }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem("Today's Loss", "$currencySymbol %.2f".format(totalToday))
            VerticalDivider(modifier = Modifier.height(40.dp))
            SummaryItem("Month Loss", "$currencySymbol %.2f".format(totalMonth))
            VerticalDivider(modifier = Modifier.height(40.dp))
            SummaryItem("Today's Items", "$countToday wasted")
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onErrorContainer)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f))
    }
}

@Composable
private fun WastageEntryCard(entry: WastageEntity, onDelete: () -> Unit) {
    val context = LocalContext.current
    val currencySymbol = remember { AppSetupPrefs(context).currencySymbol.ifBlank { "$" } }
    val reasonColor = when (entry.reason) {
        "EXPIRED"  -> Color(0xFFE53935)
        "DAMAGED"  -> Color(0xFFFF6F00)
        "THEFT"    -> Color(0xFF6A1B9A)
        else       -> Color(0xFF00897B)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = MaterialTheme.shapes.small,
                color = reasonColor
            ) {}
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.productName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                Text("${entry.quantity} ${entry.unit}  •  ${entry.wastageDate}", fontSize = 12.sp, color = TextSecondary)
                if (entry.notes.isNotBlank()) Text(entry.notes, fontSize = 11.sp, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(color = reasonColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                    Text(entry.reason, color = reasonColor, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Loss: $currencySymbol %.2f".format(entry.totalLoss), color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                // GAP-4 FIX (2026-08-22): entry delete action (pehle UI mein tha hi nahi)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete entry",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogWastageDialog(
    viewModel: WastageViewModel,
    onDismiss: () -> Unit
) {
    val productResults by viewModel.productSearchResults.collectAsState()
    var productSearch by remember { mutableStateOf("") }
    var selectedProductId by remember { mutableStateOf("") }
    var selectedProductName by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("") }
    var selectedCostPrice by remember { mutableStateOf(0.0) }
    var quantity by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf(WastageReason.DAMAGED) }
    var reasonExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Wastage", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product search
                OutlinedTextField(
                    value = productSearch,
                    onValueChange = {
                        productSearch = it
                        viewModel.searchProducts(it)
                    },
                    label = { Text("Search Product") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (productResults.isNotEmpty() && selectedProductId.isEmpty()) {
                    Card(colors = CardDefaults.cardColors(containerColor = SurfaceVariant)) {
                        Column {
                            productResults.take(5).forEach { item ->
                                Text(
                                    text = "${item.item_name} (${item.unit}) — Stock: ${item.current_stock}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedProductId   = item.system_row_id
                                            selectedProductName = item.item_name
                                            selectedUnit        = item.unit
                                            selectedCostPrice   = item.cost_price
                                            productSearch       = item.item_name
                                        }
                                        .padding(12.dp),
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                                Divider(color = SurfaceDark)
                            }
                        }
                    }
                }

                // Reason dropdown
                ExposedDropdownMenuBox(
                    expanded = reasonExpanded,
                    onExpandedChange = { reasonExpanded = !reasonExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedReason.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Reason") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(reasonExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = reasonExpanded,
                        onDismissRequest = { reasonExpanded = false }
                    ) {
                        WastageReason.values().forEach { reason ->
                            DropdownMenuItem(
                                text = { Text(reason.label) },
                                onClick = { selectedReason = reason; reasonExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity ($selectedUnit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toDoubleOrNull() ?: 0.0
                    if (selectedProductId.isNotBlank() && qty > 0.0) {
                        viewModel.logWastage(
                            productId   = selectedProductId,
                            productName = selectedProductName,
                            batchId     = null,
                            quantity    = qty,
                            unit        = selectedUnit,
                            costPrice   = selectedCostPrice,
                            reason      = selectedReason,
                            notes       = notes,
                            loggedBy    = viewModel.currentUserId(),
                            posTerminalId = viewModel.currentTerminalId()
                        )
                        onDismiss()
                    }
                },
                enabled = selectedProductId.isNotBlank() && quantity.toDoubleOrNull() != null
            ) { Text("Log Wastage") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
