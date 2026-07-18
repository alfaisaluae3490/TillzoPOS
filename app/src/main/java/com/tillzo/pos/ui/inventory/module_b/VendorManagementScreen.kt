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
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.errorChannel.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var editingVendor by remember { mutableStateOf<VendorEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<VendorEntity?>(null) }

    val displayList = if (searchQuery.isBlank()) vendors else searchResults

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Vendor", color = Color.White) },
            text = {
                Text(
                    "Permanently delete ${showDeleteConfirm?.name}? This will delete the vendor locally and remove them from your spreadsheet on next sync.",
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendors", color = Color.White, fontWeight = FontWeight.Bold) },
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayList, key = { it.vendorId }) { vendor ->
                        VendorCard(
                            vendor = vendor,
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
private fun VendorCard(vendor: VendorEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
                    vendor.name.first().uppercaseChar().toString(),
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
                    Text(vendor.phone, color = Color.White.copy(alpha=0.5f), fontSize = 13.sp)
                if (vendor.address.isNotEmpty())
                    Text(vendor.address, color = Color.White.copy(alpha=0.35f), fontSize = 12.sp,
                        maxLines = 1)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, tint = Color(0xFF1E88E5).copy(alpha = 0.7f))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = Color(0xFFE53935).copy(alpha = 0.7f))
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
