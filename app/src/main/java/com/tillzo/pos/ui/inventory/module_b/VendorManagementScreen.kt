package com.tillzo.pos.ui.inventory.module_b

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import java.text.SimpleDateFormat
import java.util.*

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
                if (vendor.contractFileUrl.isNotEmpty())
                    Text("📎 Contract attached", color = Color(0xFF4CAF50).copy(alpha=0.7f), fontSize = 11.sp)
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

    // Geographic
    var city by remember(existing) { mutableStateOf(existing?.city ?: "") }
    var province by remember(existing) { mutableStateOf(existing?.province ?: "") }
    var country by remember(existing) { mutableStateOf(existing?.country ?: "") }
    var billingAddress by remember(existing) { mutableStateOf(existing?.billingAddress ?: "") }
    var ownerName by remember(existing) { mutableStateOf(existing?.ownerName ?: "") }

    // Financial / Tax
    var bankAccountTitle by remember(existing) { mutableStateOf(existing?.bankAccountTitle ?: "") }
    var bankName by remember(existing) { mutableStateOf(existing?.bankName ?: "") }
    var bankAccountNumber by remember(existing) { mutableStateOf(existing?.bankAccountNumber ?: "") }
    var bankIban by remember(existing) { mutableStateOf(existing?.bankIban ?: "") }
    var bankSwiftCode by remember(existing) { mutableStateOf(existing?.bankSwiftCode ?: "") }
    var bankBranch by remember(existing) { mutableStateOf(existing?.bankBranch ?: "") }
    var paymentTerms by remember(existing) { mutableStateOf(existing?.paymentTerms ?: "") }
    var preferredCurrency by remember(existing) { mutableStateOf(existing?.preferredCurrency ?: "") }
    var creditLimit by remember(existing) { mutableStateOf(existing?.creditLimit?.toString() ?: "0.0") }
    var registrationNumber by remember(existing) { mutableStateOf(existing?.registrationNumber ?: "") }
    var ntnNumber by remember(existing) { mutableStateOf(existing?.ntnNumber ?: "") }
    var cnicNumber by remember(existing) { mutableStateOf(existing?.cnicNumber ?: "") }
    var trnNumber by remember(existing) { mutableStateOf(existing?.trnNumber ?: "") }
    var tradeLicenseNumber by remember(existing) { mutableStateOf(existing?.tradeLicenseNumber ?: "") }
    var tradeLicenseExpiryDate by remember(existing) { mutableStateOf(existing?.tradeLicenseExpiryDate ?: "") }

    // Primary Manager
    var primaryManagerName by remember(existing) { mutableStateOf(existing?.primaryManagerName ?: "") }
    var primaryManagerPhone by remember(existing) { mutableStateOf(existing?.primaryManagerPhone ?: "") }
    var primaryManagerEmail by remember(existing) { mutableStateOf(existing?.primaryManagerEmail ?: "") }

    // Tech Support
    var techSupportName by remember(existing) { mutableStateOf(existing?.techSupportName ?: "") }
    var techSupportPhone by remember(existing) { mutableStateOf(existing?.techSupportPhone ?: "") }
    var techSupportEmail by remember(existing) { mutableStateOf(existing?.techSupportEmail ?: "") }

    // Billing Contact
    var billingContactName by remember(existing) { mutableStateOf(existing?.billingContactName ?: "") }
    var billingContactPhone by remember(existing) { mutableStateOf(existing?.billingContactPhone ?: "") }
    var billingContactEmail by remember(existing) { mutableStateOf(existing?.billingContactEmail ?: "") }

    // Escalation L1
    var escalationL1Name by remember(existing) { mutableStateOf(existing?.escalationL1Name ?: "") }
    var escalationL1Phone by remember(existing) { mutableStateOf(existing?.escalationL1Phone ?: "") }
    var escalationL1Email by remember(existing) { mutableStateOf(existing?.escalationL1Email ?: "") }

    // Escalation L2
    var escalationL2Name by remember(existing) { mutableStateOf(existing?.escalationL2Name ?: "") }
    var escalationL2Phone by remember(existing) { mutableStateOf(existing?.escalationL2Phone ?: "") }
    var escalationL2Email by remember(existing) { mutableStateOf(existing?.escalationL2Email ?: "") }

    // Escalation L3
    var escalationL3Name by remember(existing) { mutableStateOf(existing?.escalationL3Name ?: "") }
    var escalationL3Phone by remember(existing) { mutableStateOf(existing?.escalationL3Phone ?: "") }
    var escalationL3Email by remember(existing) { mutableStateOf(existing?.escalationL3Email ?: "") }

    // SLA & Warranty
    var contractStartDate by remember(existing) { mutableStateOf(existing?.contractStartDate ?: "") }
    var contractExpiryDate by remember(existing) { mutableStateOf(existing?.contractExpiryDate ?: "") }
    var slaResponseTimes by remember(existing) { mutableStateOf(existing?.slaResponseTimes ?: "") }
    var warrantyTerms by remember(existing) { mutableStateOf(existing?.warrantyTerms ?: "") }
    var complianceCertificates by remember(existing) { mutableStateOf(existing?.complianceCertificates ?: "") }

    // File attachment state
    var attachedFileName by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            // Extract filename from URI
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) attachedFileName = it.getString(nameIndex)
                }
            } ?: run { attachedFileName = "selected_file" }
            cursor?.close()
        }
    }

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

    // Collapsible section states
    var basicExpanded by remember { mutableStateOf(true) }
    var geoExpanded by remember { mutableStateOf(false) }
    var financialExpanded by remember { mutableStateOf(false) }
    var taxExpanded by remember { mutableStateOf(false) }
    var primaryContactExpanded by remember { mutableStateOf(false) }
    var techSupportExpanded by remember { mutableStateOf(false) }
    var billingContactExpanded by remember { mutableStateOf(false) }
    var escalationL1Expanded by remember { mutableStateOf(false) }
    var escalationL2Expanded by remember { mutableStateOf(false) }
    var escalationL3Expanded by remember { mutableStateOf(false) }
    var slaExpanded by remember { mutableStateOf(false) }
    var docsExpanded by remember { mutableStateOf(false) }

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

                // Scrollable form content with accordion sections
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // ── Section 1: Basic Info ──────────────────────────────────
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

                    // ── Section 2: Geographic ──────────────────────────────────
                    AccordionSection(
                        title = "Geographic Details",
                        icon = Icons.Default.LocationOn,
                        expanded = geoExpanded,
                        onToggle = { geoExpanded = !geoExpanded }
                    ) {
                        OutlinedTextField(value = city, onValueChange = { city = it },
                            label = { Text("City") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = province, onValueChange = { province = it },
                            label = { Text("Province / State") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = country, onValueChange = { country = it },
                            label = { Text("Country") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = billingAddress, onValueChange = { billingAddress = it },
                            label = { Text("Billing Address") }, modifier = Modifier.fillMaxWidth(),
                            minLines = 2, colors = vendorFormFieldColors())
                        OutlinedTextField(value = ownerName, onValueChange = { ownerName = it },
                            label = { Text("Owner Name") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                    }

                    // ── Section 3: Financial Details ───────────────────────────
                    AccordionSection(
                        title = "Financial Details",
                        icon = Icons.Default.AccountBalance,
                        expanded = financialExpanded,
                        onToggle = { financialExpanded = !financialExpanded }
                    ) {
                        OutlinedTextField(value = bankAccountTitle, onValueChange = { bankAccountTitle = it },
                            label = { Text("Bank Account Title") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = bankName, onValueChange = { bankName = it },
                            label = { Text("Bank Name") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = bankAccountNumber, onValueChange = { bankAccountNumber = it },
                            label = { Text("Bank Account Number") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = bankIban, onValueChange = { bankIban = it },
                            label = { Text("IBAN") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = bankSwiftCode, onValueChange = { bankSwiftCode = it },
                            label = { Text("SWIFT Code") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = bankBranch, onValueChange = { bankBranch = it },
                            label = { Text("Bank Branch") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = paymentTerms, onValueChange = { paymentTerms = it },
                            label = { Text("Payment Terms") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = preferredCurrency, onValueChange = { preferredCurrency = it },
                            label = { Text("Preferred Currency") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = creditLimit, onValueChange = { creditLimit = it },
                            label = { Text("Credit Limit") }, modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = vendorFormFieldColors())
                    }

                    // ── Section 4: Tax & Registration ──────────────────────────
                    AccordionSection(
                        title = "Tax & Registration",
                        icon = Icons.Default.Description,
                        expanded = taxExpanded,
                        onToggle = { taxExpanded = !taxExpanded }
                    ) {
                        OutlinedTextField(value = registrationNumber, onValueChange = { registrationNumber = it },
                            label = { Text("Registration Number") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = ntnNumber, onValueChange = { ntnNumber = it },
                            label = { Text("NTN Number") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = cnicNumber, onValueChange = { cnicNumber = it },
                            label = { Text("CNIC Number") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = trnNumber, onValueChange = { trnNumber = it },
                            label = { Text("TRN Number") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = tradeLicenseNumber, onValueChange = { tradeLicenseNumber = it },
                            label = { Text("Trade License Number") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = tradeLicenseExpiryDate, onValueChange = { tradeLicenseExpiryDate = it },
                            label = { Text("Trade License Expiry Date") }, modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("YYYY-MM-DD", color = Color.White.copy(alpha = 0.3f)) },
                            colors = vendorFormFieldColors())
                    }

                    // ── Section 5: Primary Contact ─────────────────────────────
                    AccordionSection(
                        title = "Primary Manager",
                        icon = Icons.Default.PersonOutline,
                        expanded = primaryContactExpanded,
                        onToggle = { primaryContactExpanded = !primaryContactExpanded }
                    ) {
                        OutlinedTextField(value = primaryManagerName, onValueChange = { primaryManagerName = it },
                            label = { Text("Name") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = primaryManagerPhone, onValueChange = { primaryManagerPhone = it },
                            label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = primaryManagerEmail, onValueChange = { primaryManagerEmail = it },
                            label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                    }

                    // ── Section 6: Tech Support ────────────────────────────────
                    AccordionSection(
                        title = "Tech Support",
                        icon = Icons.Default.SupportAgent,
                        expanded = techSupportExpanded,
                        onToggle = { techSupportExpanded = !techSupportExpanded }
                    ) {
                        OutlinedTextField(value = techSupportName, onValueChange = { techSupportName = it },
                            label = { Text("Name") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = techSupportPhone, onValueChange = { techSupportPhone = it },
                            label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = techSupportEmail, onValueChange = { techSupportEmail = it },
                            label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                    }

                    // ── Section 7: Billing Contact ─────────────────────────────
                    AccordionSection(
                        title = "Billing Contact",
                        icon = Icons.Default.Receipt,
                        expanded = billingContactExpanded,
                        onToggle = { billingContactExpanded = !billingContactExpanded }
                    ) {
                        OutlinedTextField(value = billingContactName, onValueChange = { billingContactName = it },
                            label = { Text("Name") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = billingContactPhone, onValueChange = { billingContactPhone = it },
                            label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = billingContactEmail, onValueChange = { billingContactEmail = it },
                            label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                    }

                    // ── Section 8: Escalation L1 ───────────────────────────────
                    AccordionSection(
                        title = "Escalation L1",
                        icon = Icons.Default.Notifications,
                        expanded = escalationL1Expanded,
                        onToggle = { escalationL1Expanded = !escalationL1Expanded }
                    ) {
                        OutlinedTextField(value = escalationL1Name, onValueChange = { escalationL1Name = it },
                            label = { Text("Name") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = escalationL1Phone, onValueChange = { escalationL1Phone = it },
                            label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = escalationL1Email, onValueChange = { escalationL1Email = it },
                            label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                    }

                    // ── Section 9: Escalation L2 ───────────────────────────────
                    AccordionSection(
                        title = "Escalation L2",
                        icon = Icons.Default.Notifications,
                        expanded = escalationL2Expanded,
                        onToggle = { escalationL2Expanded = !escalationL2Expanded }
                    ) {
                        OutlinedTextField(value = escalationL2Name, onValueChange = { escalationL2Name = it },
                            label = { Text("Name") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = escalationL2Phone, onValueChange = { escalationL2Phone = it },
                            label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = escalationL2Email, onValueChange = { escalationL2Email = it },
                            label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                    }

                    // ── Section 10: Escalation L3 ──────────────────────────────
                    AccordionSection(
                        title = "Escalation L3",
                        icon = Icons.Default.Notifications,
                        expanded = escalationL3Expanded,
                        onToggle = { escalationL3Expanded = !escalationL3Expanded }
                    ) {
                        OutlinedTextField(value = escalationL3Name, onValueChange = { escalationL3Name = it },
                            label = { Text("Name") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = escalationL3Phone, onValueChange = { escalationL3Phone = it },
                            label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = escalationL3Email, onValueChange = { escalationL3Email = it },
                            label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                            colors = vendorFormFieldColors())
                    }

                    // ── Section 11: SLA & Warranty ─────────────────────────────
                    AccordionSection(
                        title = "SLA & Warranty",
                        icon = Icons.Default.Verified,
                        expanded = slaExpanded,
                        onToggle = { slaExpanded = !slaExpanded }
                    ) {
                        OutlinedTextField(value = contractStartDate, onValueChange = { contractStartDate = it },
                            label = { Text("Contract Start Date") }, modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("YYYY-MM-DD", color = Color.White.copy(alpha = 0.3f)) },
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = contractExpiryDate, onValueChange = { contractExpiryDate = it },
                            label = { Text("Contract Expiry Date") }, modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("YYYY-MM-DD", color = Color.White.copy(alpha = 0.3f)) },
                            colors = vendorFormFieldColors())
                        OutlinedTextField(value = slaResponseTimes, onValueChange = { slaResponseTimes = it },
                            label = { Text("SLA Response Times") }, modifier = Modifier.fillMaxWidth(),
                            minLines = 2, colors = vendorFormFieldColors())
                        OutlinedTextField(value = warrantyTerms, onValueChange = { warrantyTerms = it },
                            label = { Text("Warranty Terms") }, modifier = Modifier.fillMaxWidth(),
                            minLines = 2, colors = vendorFormFieldColors())
                        OutlinedTextField(value = complianceCertificates, onValueChange = { complianceCertificates = it },
                            label = { Text("Compliance Certificates") }, modifier = Modifier.fillMaxWidth(),
                            minLines = 2, colors = vendorFormFieldColors())
                    }

                    // ── Section 12: Documents ──────────────────────────────────
                    AccordionSection(
                        title = "Documents",
                        icon = Icons.Default.AttachFile,
                        expanded = docsExpanded,
                        onToggle = { docsExpanded = !docsExpanded }
                    ) {
                        // File picker button
                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            enabled = saveState !is SaveState.Saving
                        ) {
                            Icon(Icons.Default.UploadFile, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (attachedFileName.isNotEmpty()) "Change File" else "Attach Contract / Document")
                        }

                        // Show selected filename
                        if (attachedFileName.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.InsertDriveFile, null,
                                    tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(attachedFileName,
                                    color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                            }
                        }

                        // Show existing attachment / view button
                        if (!existing?.contractFileUrl.isNullOrEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(existing!!.contractFileUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("View Attachment")
                            }
                        }

                        // Saving progress indicator
                        if (saveState is SaveState.Saving) {
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF1E88E5)
                            )
                            Text("Saving vendor...",
                                color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
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
                                    isActive = isActive,
                                    name = name, phone = phone,
                                    whatsapp = whatsapp, email = email, address = address,
                                    city = city, province = province, country = country,
                                    billingAddress = billingAddress, ownerName = ownerName,
                                    bankAccountTitle = bankAccountTitle, bankName = bankName,
                                    bankAccountNumber = bankAccountNumber, bankIban = bankIban,
                                    bankSwiftCode = bankSwiftCode, bankBranch = bankBranch,
                                    paymentTerms = paymentTerms, preferredCurrency = preferredCurrency,
                                    creditLimit = creditLimit.toDoubleOrNull() ?: 0.0,
                                    registrationNumber = registrationNumber, ntnNumber = ntnNumber,
                                    cnicNumber = cnicNumber, trnNumber = trnNumber,
                                    tradeLicenseNumber = tradeLicenseNumber,
                                    tradeLicenseExpiryDate = tradeLicenseExpiryDate,
                                    primaryManagerName = primaryManagerName,
                                    primaryManagerPhone = primaryManagerPhone,
                                    primaryManagerEmail = primaryManagerEmail,
                                    techSupportName = techSupportName,
                                    techSupportPhone = techSupportPhone,
                                    techSupportEmail = techSupportEmail,
                                    billingContactName = billingContactName,
                                    billingContactPhone = billingContactPhone,
                                    billingContactEmail = billingContactEmail,
                                    escalationL1Name = escalationL1Name,
                                    escalationL1Phone = escalationL1Phone,
                                    escalationL1Email = escalationL1Email,
                                    escalationL2Name = escalationL2Name,
                                    escalationL2Phone = escalationL2Phone,
                                    escalationL2Email = escalationL2Email,
                                    escalationL3Name = escalationL3Name,
                                    escalationL3Phone = escalationL3Phone,
                                    escalationL3Email = escalationL3Email,
                                    contractStartDate = contractStartDate,
                                    contractExpiryDate = contractExpiryDate,
                                    slaResponseTimes = slaResponseTimes,
                                    warrantyTerms = warrantyTerms,
                                    complianceCertificates = complianceCertificates,
                                    fileUri = selectedFileUri,
                                    context = context
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
