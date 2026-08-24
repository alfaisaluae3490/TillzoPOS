package com.tillzo.pos.ui.inventory.module_b

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.entity.VendorEntity

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: VendorManagementViewModel = hiltViewModel()
) {
    val vendors by viewModel.vendors.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val vendorBalances by viewModel.vendorBalances.collectAsState()
    val context = LocalContext.current
    val currencySymbol = remember { com.tillzo.pos.data.local.prefs.AppSetupPrefs(context).currencySymbol.ifBlank { "$" } }

    LaunchedEffect(Unit) {
        viewModel.errorChannel.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var editingVendor by remember { mutableStateOf<VendorEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<VendorEntity?>(null) }

    var payingVendor by remember { mutableStateOf<VendorEntity?>(null) }
    var debitNoteVendor by remember { mutableStateOf<VendorEntity?>(null) }
    var ledgerVendor by remember { mutableStateOf<VendorEntity?>(null) }

    val displayList = if (searchQuery.isBlank()) vendors else searchResults

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Vendor", color = Color.White) },
            text = {
                Text(
                    "Delete ${showDeleteConfirm?.name}? This marks the vendor as deleted locally and removes them from your spreadsheet on next sync.",
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm?.let { viewModel.deleteVendor(it.vendorId) }
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            },
            containerColor = Color(0xFF2A2A2A)
        )
    }

    if (showDialog) {
        VendorFormDialog(
            existing = editingVendor,
            viewModel = viewModel,
            onDismiss = { showDialog = false; editingVendor = null },
            onSaved = { showDialog = false; editingVendor = null }
        )
    }

    // ── Pay Vendor Dialog ───────────────────────────────────────────────────
    if (payingVendor != null) {
        val targetVendor = payingVendor!!
        val currentBal = vendorBalances[targetVendor.vendorId] ?: 0.0
        PayVendorDialog(
            vendor = targetVendor,
            currentBalance = currentBal,
            currencySymbol = currencySymbol,
            onDismiss = { payingVendor = null },
            onConfirmPayment = { amount, method, note ->
                viewModel.recordVendorPayment(targetVendor.vendorId, targetVendor.name, amount, method, note)
                payingVendor = null
                Toast.makeText(context, "Payment of $currencySymbol%.2f recorded".format(amount), Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ── Debit Note Dialog ───────────────────────────────────────────────────
    if (debitNoteVendor != null) {
        val targetVendor = debitNoteVendor!!
        val currentBal = vendorBalances[targetVendor.vendorId] ?: 0.0
        DebitNoteDialog(
            vendor = targetVendor,
            currentBalance = currentBal,
            currencySymbol = currencySymbol,
            onDismiss = { debitNoteVendor = null },
            onConfirmDebitNote = { amount, reason ->
                viewModel.recordDebitNote(targetVendor.vendorId, targetVendor.name, amount, reason)
                debitNoteVendor = null
                Toast.makeText(context, "Debit Note of $currencySymbol%.2f recorded".format(amount), Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ── Ledger / Statement Dialog ───────────────────────────────────────────
    if (ledgerVendor != null) {
        val targetVendor = ledgerVendor!!
        val currentBal = vendorBalances[targetVendor.vendorId] ?: 0.0
        VendorLedgerDialog(
            vendor = targetVendor,
            currentBalance = currentBal,
            currencySymbol = currencySymbol,
            viewModel = viewModel,
            onDismiss = { ledgerVendor = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendors & Payables (AP)", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingVendor = null; showDialog = true },
                containerColor = Color(0xFF1E88E5), contentColor = Color.White
            ) { Icon(Icons.Default.Add, "Add Vendor") }
        },
        containerColor = Color(0xFF1A1A1A)
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it; viewModel.search(it) },
                label = { Text("Search vendors...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF1E88E5)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                colors = vendorFormFieldColors()
            )

            if (displayList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.StoreMallDirectory, null,
                            tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No vendors yet", color = Color.White.copy(alpha=0.4f), fontSize = 16.sp)
                        Text("Tap + to add your first vendor",
                            color = Color.White.copy(alpha=0.25f), fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayList, key = { it.vendorId }) { vendor ->
                        val balance = vendorBalances[vendor.vendorId] ?: 0.0
                        VendorCard(
                            vendor = vendor,
                            balance = balance,
                            currencySymbol = currencySymbol,
                            onPay = { payingVendor = vendor },
                            onDebitNote = { debitNoteVendor = vendor },
                            onViewLedger = { ledgerVendor = vendor },
                            onEdit = { editingVendor = vendor; showDialog = true },
                            onDelete = { showDeleteConfirm = vendor }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorCard(
    vendor: VendorEntity,
    balance: Double,
    currencySymbol: String,
    onPay: () -> Unit,
    onDebitNote: () -> Unit,
    onViewLedger: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF242424)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(44.dp)
                        .background(
                            if (vendor.isActive) Color(0xFF1E88E5).copy(alpha = 0.15f)
                            else Color(0xFF757575).copy(alpha = 0.15f),
                            RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        vendor.name.firstOrNull()?.uppercaseChar()?.toString() ?: "V",
                        color = if (vendor.isActive) Color(0xFF1E88E5) else Color(0xFF757575),
                        fontWeight = FontWeight.Bold, fontSize = 18.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(vendor.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (vendor.isActive) "Active" else "Inactive",
                            color = if (vendor.isActive) Color(0xFF4CAF50) else Color(0xFF757575),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (vendor.phone.isNotEmpty())
                        Text(vendor.phone, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                    if (vendor.address.isNotEmpty())
                        Text(vendor.address, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, maxLines = 1)
                }

                // Balance Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (balance > 0) Color(0xFF3E1F1F) else Color(0xFF1F3A24),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            if (balance > 0) "Owed (AP)" else "Cleared",
                            color = if (balance > 0) Color(0xFFFF8A80) else Color(0xFF81C784),
                            fontSize = 10.sp
                        )
                        Text(
                            "$currencySymbol%.2f".format(balance),
                            color = if (balance > 0) Color(0xFFFF5252) else Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Divider(color = Color(0xFF333333))
            Spacer(Modifier.height(8.dp))

            // Action Row: [Pay] [Debit Note] [Ledger] [Edit] [Delete]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onPay,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Payment, null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Pay", fontSize = 11.sp, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onDebitNote,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB74D)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Debit Note", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onViewLedger,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64B5F6)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Ledger", fontSize = 11.sp)
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, null, tint = Color(0xFF1E88E5).copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Color(0xFFE53935).copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// ── Vendor Form Dialog (Full-Screen with Collapsible Accordions) ───────────────

@Composable
private fun VendorFormDialog(
    existing: VendorEntity?,
    viewModel: VendorManagementViewModel,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val saveState by viewModel.saveState.collectAsState()

    // Basic Info
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var phone by remember(existing) { mutableStateOf(existing?.phone ?: "") }
    var whatsapp by remember(existing) { mutableStateOf(existing?.whatsapp ?: "") }
    var email by remember(existing) { mutableStateOf(existing?.email ?: "") }
    var address by remember(existing) { mutableStateOf(existing?.address ?: "") }
    var isActive by remember(existing) { mutableStateOf(existing?.isActive ?: true) }
    var city by remember(existing) { mutableStateOf(existing?.city ?: "") }
    var creditLimit by remember(existing) { mutableStateOf(existing?.creditLimit?.toString() ?: "0.0") }

    // Handle save state changes
    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Success -> {
                viewModel.resetSaveState()
                onSaved()
            }
            is SaveState.Error -> {
                Toast.makeText(context, (saveState as SaveState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    var basicExpanded by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2A2A2A)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Title bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.StoreMallDirectory, null,
                        tint = Color(0xFF1E88E5), modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (existing == null) "Add Vendor" else "Edit Vendor",
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp
                    )
                }

                // Scrollable form content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // ── Basic Info ──────────────────────────────────
                    AccordionSection(
                        title = "Basic Info",
                        icon = Icons.Default.Person,
                        expanded = basicExpanded,
                        onToggle = { basicExpanded = !basicExpanded }
                    ) {
                        OutlinedTextField(value = name, onValueChange = { name = it },
                            label = { Text("Name *") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = phone, onValueChange = { phone = it },
                            label = { Text("Phone *") }, modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it },
                            label = { Text("WhatsApp") }, modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = email, onValueChange = { email = it },
                            label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = address, onValueChange = { address = it },
                            label = { Text("Address") }, modifier = Modifier.fillMaxWidth(),
                            minLines = 2, colors = vendorFormFieldColors())
                        OutlinedTextField(value = city, onValueChange = { city = it },
                            label = { Text("City") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = creditLimit, onValueChange = { creditLimit = it },
                            label = { Text("Credit Limit") }, modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = vendorFormFieldColors())
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Active Status", color = Color.White, fontSize = 14.sp)
                            Switch(
                                checked = isActive,
                                onCheckedChange = { isActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF1E88E5),
                                    checkedTrackColor = Color(0xFF1E88E5).copy(alpha = 0.3f),
                                    uncheckedThumbColor = Color(0xFF757575),
                                    uncheckedTrackColor = Color(0xFF757575).copy(alpha = 0.3f)
                                )
                            )
                        }
                    }

                    if (saveState is SaveState.Saving) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF1E88E5)
                        )
                        Text("Saving vendor...",
                            color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                } // end scrollable content

                // Bottom buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                viewModel.save(
                                    existing = existing,
                                    name = name, phone = phone,
                                    whatsapp = whatsapp, email = email, address = address,
                                    city = city, creditLimit = creditLimit.toDoubleOrNull() ?: 0.0,
                                    isActive = isActive
                                )
                            }
                        },
                        enabled = saveState !is SaveState.Saving,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                    ) {
                        if (saveState is SaveState.Saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Save")
                    }
                }
            }
        }
    }
}

// ── Accordion Section Component ─────────────────────────────────────────────────

@Composable
private fun AccordionSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3A3A3A)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = Color(0xFF1E88E5), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = Color.White.copy(alpha = 0.5f)
                )
            }

            // Content
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    content = content
                )
            }
        }
    }
}

// ── Theme Colors ───────────────────────────────────────────────────────────────

@Composable
private fun vendorFormFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color(0xFF1E88E5),
    focusedBorderColor = Color(0xFF1E88E5),
    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
    focusedLabelColor = Color(0xFF1E88E5),
    unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
)

// ── Vendor AP Dialogs ─────────────────────────────────────────────────────────

@Composable
fun PayVendorDialog(
    vendor: VendorEntity,
    currentBalance: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirmPayment: (amount: Double, method: String, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf(if (currentBalance > 0) "%.2f".format(currentBalance) else "") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Pay Vendor (AP)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(vendor.name, color = Color(0xFF64B5F6), fontSize = 14.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = Color(0xFF332A1D),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Current Outstanding:", color = Color(0xFFFFB74D), fontSize = 13.sp)
                        Text("$currencySymbol%.2f".format(currentBalance), color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vendorFormFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Payment Method", color = Color.Gray, fontSize = 12.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("CASH", "BANK_TRANSFER", "CHEQUE", "CARD").forEach { method ->
                        val isSel = paymentMethod == method
                        FilterChip(
                            selected = isSel,
                            onClick = { paymentMethod = method },
                            label = { Text(method.replace("_", " "), fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2E7D32),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Reference / Note (Optional)") },
                    colors = vendorFormFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0.0) {
                        onConfirmPayment(amount, paymentMethod, noteText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Confirm Payment", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        },
        containerColor = Color(0xFF2A2A2A)
    )
}

@Composable
fun DebitNoteDialog(
    vendor: VendorEntity,
    currentBalance: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirmDebitNote: (amount: Double, reason: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var reasonText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Create Debit Note", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(vendor.name, color = Color(0xFFFFB74D), fontSize = 14.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "A Debit Note reduces the amount you owe to the vendor (e.g. for returned damaged stock or price dispute).",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Debit Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vendorFormFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Reason / Return Details") },
                    placeholder = { Text("e.g. Returned 5 expired items from GRN-002", color = Color.Gray) },
                    colors = vendorFormFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0.0) {
                        onConfirmDebitNote(amount, reasonText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
            ) {
                Text("Apply Debit Note", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        },
        containerColor = Color(0xFF2A2A2A)
    )
}

@Composable
fun VendorLedgerDialog(
    vendor: VendorEntity,
    currentBalance: Double,
    currencySymbol: String,
    viewModel: VendorManagementViewModel,
    onDismiss: () -> Unit
) {
    val payments by viewModel.getPaymentsForVendor(vendor.vendorId).collectAsState(initial = emptyList())
    val sdf = remember { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF242424)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Vendor Statement / Ledger", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(vendor.name, color = Color(0xFF64B5F6), fontSize = 14.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Summary Card
                Surface(
                    color = Color(0xFF332A1D),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Outstanding Balance", color = Color.Gray, fontSize = 11.sp)
                            Text(
                                "$currencySymbol%.2f".format(currentBalance),
                                color = if (currentBalance > 0) Color(0xFFFF5252) else Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Text("${payments.size} Transactions", color = Color.LightGray, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Divider(color = Color(0xFF3A3A3A))
                Spacer(Modifier.height(8.dp))

                if (payments.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No payment or bill transactions yet.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(payments, key = { it.paymentId }) { payment ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E2E)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val badgeColor = when (payment.type) {
                                                "BILL" -> Color(0xFFE53935)
                                                "PAYMENT" -> Color(0xFF4CAF50)
                                                "DEBIT_NOTE" -> Color(0xFF1E88E5)
                                                else -> Color(0xFFFF9800)
                                            }
                                            Surface(
                                                color = badgeColor.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    payment.type.replace("_", " "),
                                                    color = badgeColor,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                sdf.format(java.util.Date(payment.createdAt)),
                                                color = Color.Gray,
                                                fontSize = 11.sp
                                            )
                                        }
                                        if (payment.note.isNotEmpty()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(payment.note, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                        }
                                        if (payment.dueDate.isNotEmpty()) {
                                            Text("Due: ${payment.dueDate}", color = Color(0xFFFFB74D), fontSize = 11.sp)
                                        }
                                    }

                                    Text(
                                        (if (payment.type in listOf("BILL", "CREDIT_NOTE")) "+ " else "- ") + "$currencySymbol%.2f".format(payment.amount),
                                        color = if (payment.type in listOf("BILL", "CREDIT_NOTE")) Color(0xFFFF5252) else Color(0xFF81C784),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                ) {
                    Text("Close Statement")
                }
            }
        }
    }
}
