package com.tillzo.pos.ui.settings.options.privacy

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tillzo.pos.data.remote.PosSheetInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToSystemLogs: () -> Unit = {},
    onNavigateToDataViewer: () -> Unit = {},
    onNavigateToPrinterSettings: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val spreadsheetId by viewModel.spreadsheetId.collectAsStateWithLifecycle()
    val hasPinStatus by viewModel.hasPin.collectAsStateWithLifecycle()
    val isPinEnabled by viewModel.isPinEnabled.collectAsStateWithLifecycle()
    val sheetsList by viewModel.sheetsList.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val grnFolderId by viewModel.grnFolderId.collectAsStateWithLifecycle()
    val grnFolderName by viewModel.grnFolderName.collectAsStateWithLifecycle()
    val folderList by viewModel.folderList.collectAsStateWithLifecycle()
    val isSearchingFolders by viewModel.isSearchingFolders.collectAsStateWithLifecycle()
    val backupProgress by viewModel.backupProgress.collectAsStateWithLifecycle()
    val blockNegativeStock by viewModel.blockNegativeStock.collectAsStateWithLifecycle()
    val countryCode by viewModel.countryCode.collectAsStateWithLifecycle()
    val taxNumber by viewModel.taxNumber.collectAsStateWithLifecycle()
    val taxLabel by viewModel.taxLabel.collectAsStateWithLifecycle()
    val defaultTaxRate by viewModel.defaultTaxRate.collectAsStateWithLifecycle()
    val taxInclusive by viewModel.taxInclusive.collectAsStateWithLifecycle()
    val enableZatcaQr by viewModel.enableZatcaQr.collectAsStateWithLifecycle()

    var showCountryDialog by remember { mutableStateOf(false) }
    var showTaxEditDialog by remember { mutableStateOf(false) }

    // DEF-31 FIX: Drive/folder/sheet failures ab snackbar se dikhte hain
    val settingsError by viewModel.settingsError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(settingsError) {
        settingsError?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSettingsError()
        }
    }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportBackup(uri)
        }
    }
    var showPinConfigDialog by remember { mutableStateOf(false) }
    var showGrnFolderDialog by remember { mutableStateOf(false) }
    var grnNewFolderName by remember { mutableStateOf("") }
    var showCreateGrnFolder by remember { mutableStateOf(false) }
    var manualFolderIdInput by remember { mutableStateOf("") }
    var showManualFolderEntry by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var currentPinInput by remember { mutableStateOf("") }
    var changeNewPinInput by remember { mutableStateOf("") }
    var changeConfirmPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showSheetSelectionDialog by remember { mutableStateOf(false) }
    var manualSheetIdInput by remember { mutableStateOf("") }
    var showManualEntry by remember { mutableStateOf(false) }
    var showCreateSheetForm by remember { mutableStateOf(false) }
    var newSheetShopName by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings & Privacy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Account & Billing", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCard(
                icon = Icons.Default.Payment,
                title = "Subscription & Licenses",
                subtitle = "Manage your Tillzo POS Plus subscription",
                onClick = onNavigateToBilling
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Security & Device Lock", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("App Security PIN", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            if (hasPinStatus) "PIN Lock is Active" else "Quick PIN lock is Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isPinEnabled,
                        onCheckedChange = { checked ->
                            if (checked && !hasPinStatus) {
                                newPinInput = ""
                                pinError = null
                                showPinConfigDialog = true
                            } else {
                                viewModel.togglePinLock(checked)
                            }
                        }
                    )
                }
            }

            if (hasPinStatus) {
                TextButton(
                    onClick = {
                        newPinInput = ""
                        currentPinInput = ""
                        changeNewPinInput = ""
                        changeConfirmPinInput = ""
                        pinError = null
                        showPinConfigDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage PIN", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Store Settings", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Block Negative Stock", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Prevent selling more than available stock",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = blockNegativeStock,
                        onCheckedChange = { viewModel.setBlockNegativeStock(it) }
                    )
                }
            }

            // OVERNIGHT-AUDIT Phase 1c (2026-08-23): screen-capture blocking.
            // FLAG_SECURE on every activity when ON — screenshots, screen recording
            // and recents thumbnail all blocked (bank-level security).
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                val blockCapture by viewModel.blockScreenCapture.collectAsStateWithLifecycle()
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Block Screenshots & Recording", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Bank-level: blocks screenshots, screen recording and recents preview",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = blockCapture,
                        onCheckedChange = { viewModel.setBlockScreenCapture(it) }
                    )
                }
            }

            // FIX (2026-08-06): multi-currency selector (industry-standard feature)
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                val currencySymbol by viewModel.currencySymbol.collectAsState()
                var currencyMenu by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Currency Symbol", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Applies to POS, receipts, reports (e.g. $, USD, EUR, AED, SAR, INR)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box {
                        OutlinedButton(onClick = { currencyMenu = true }) { Text(currencySymbol.ifBlank { "Rs" }) }
                        DropdownMenu(expanded = currencyMenu, onDismissRequest = { currencyMenu = false }) {
                            listOf("$", "USD", "EUR", "AED", "SAR", "INR", "GBP", "QAR", "OMR", "PKR").forEach { sym ->
                                DropdownMenuItem(
                                    text = { Text(sym) },
                                    onClick = {
                                        currencyMenu = false
                                        viewModel.setCurrencySymbol(sym)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ── Global Tax & Regional Compliance Section ────────────────────
            val currentPreset = com.tillzo.pos.utils.TaxUtils.getPreset(countryCode)
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tax & Regional Compliance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text("Configure country tax laws, TRN/GSTIN, and invoice rules", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Country Selection Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCountryDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Business Country", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("${currentPreset.flag} ${currentPreset.name} (${currentPreset.currencySymbol})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 4.dp))

                    // Tax ID / TRN Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTaxEditDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(currentPreset.taxIdLabel, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(taxNumber.ifBlank { "Not configured (Tap to add)" }, style = MaterialTheme.typography.bodySmall, color = if (taxNumber.isNotBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 4.dp))

                    // Default Tax Rate & Label
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTaxEditDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Default Tax Rate", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("$taxLabel $defaultTaxRate% (Standard store rate)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 4.dp))

                    // Tax Inclusive Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tax-Inclusive Pricing", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("On: prices include tax (VAT model). Off: tax added at checkout (Sales Tax model).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = taxInclusive,
                            onCheckedChange = { viewModel.setTaxInclusive(it) }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 4.dp))

                    // ZATCA / Compliance QR Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Compliance E-Invoice QR", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Print official ZATCA/Tax QR on thermal & digital receipts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = enableZatcaQr,
                            onCheckedChange = { viewModel.setEnableZatcaQr(it) }
                        )
                    }
                }
            }

            // FIX (2026-08-06): loyalty program toggle (industry-standard)
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                val loyaltyEnabled by viewModel.loyaltyEnabled.collectAsState()
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Loyalty Program", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Customers earn points on every sale (shown in CRM / Accounts)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = loyaltyEnabled,
                        onCheckedChange = { viewModel.setLoyaltyEnabled(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Data & Privacy (Google Play Compliance)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Data Safe Promise", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "This app reads and writes exclusively to your personal Google Sheet using the `drive.file` scope. " +
                        "Tillzo POS DOES NOT transmit, log, or store your inventory or customer data on any external developer servers. " +
                        "Your financial data stays entirely on your Google Drive.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Data Sheet",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Connected Sheet ID:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        if (spreadsheetId.length > 20) spreadsheetId.take(20) + "..." else spreadsheetId,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            manualSheetIdInput = ""
                            showManualEntry = false
                            showSheetSelectionDialog = true
                            viewModel.loadDriveSheets()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Connect Different Sheet",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Goods Receipt Drive Folder",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (grnFolderName.isNotEmpty()) "Folder: $grnFolderName" else "No folder selected",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            grnNewFolderName = ""
                            showCreateGrnFolder = false
                            showGrnFolderDialog = true
                            viewModel.searchFolders()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.TableChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (grnFolderId.isEmpty()) "Select Receipt Folder" else "Change Receipt Folder",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsCard(
                icon = Icons.Default.Policy,
                title = "Privacy Policy",
                subtitle = "Read our full privacy commitments online",
                onClick = {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://tillzopos.com/privacy"))
                    context.startActivity(browserIntent)
                }
            )

            // ── PLAY POLICY T2 (2026-08-24): Account & Data Deletion ─────────
            Spacer(modifier = Modifier.height(12.dp))
            val deleteState by viewModel.deleteAccountState.collectAsStateWithLifecycle()
            var showDeleteConfirm by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFD32F2F))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Delete Account & Data", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Permanently deletes your local Tillzo POS database, all app preferences and " +
                        "revokes this app's access to your Google Account. This cannot be undone. " +
                        "Your Google Sheet on your own Drive is NOT deleted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6D4C41)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showDeleteConfirm = true },
                        enabled = deleteState !is DeleteAccountState.Deleting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (deleteState is DeleteAccountState.Deleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Deleting...")
                        } else {
                            Text("Delete Account & All Data")
                        }
                    }
                }
            }

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Delete everything?") },
                    text = {
                        Text(
                            "This will permanently erase ALL local data (products, sales, customers, " +
                            "expenses, ledgers), reset the app to first-run state, and revoke its Google " +
                            "Account access. Your Google Sheet itself stays on your Drive.\n\nAre you sure?"
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteConfirm = false
                            viewModel.deleteAccountAndData()
                        }) {
                            Text("Yes, delete everything", color = Color(0xFFD32F2F))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                    }
                )
            }

            // On success: kill process so next launch starts fresh (first-run state)
            LaunchedEffect(deleteState) {
                if (deleteState is DeleteAccountState.Done) {
                    kotlinx.coroutines.delay(1500)
                    android.os.Process.killProcess(android.os.Process.myPid())
                }
            }

            if (deleteState is DeleteAccountState.Error) {
                LaunchedEffect(deleteState) {
                    snackbarHostState.showSnackbar(
                        "Deletion failed: ${(deleteState as DeleteAccountState.Error).message}"
                    )
                    viewModel.clearSettingsError()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("App Info", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCard(
                icon = Icons.Default.Info,
                title = "Version",
                subtitle = "Tillzo POS ${com.tillzo.pos.BuildConfig.VERSION_NAME} (Build ${com.tillzo.pos.BuildConfig.VERSION_CODE}) - Protected by RootBeer",
                onClick = { }
            )

            SettingsCard(
                icon = Icons.Default.FilterList,
                title = "System Logs",
                subtitle = "View and export app logs (rolling 48-hour buffer)",
                onClick = onNavigateToSystemLogs
            )

            // FIX (2026-08-22, GAP-1): PrinterSettingsScreen was orphaned —
            // no nav route existed, so the "No printer configured. Set MAC in
            // Printer Settings." snackbar pointed at an unreachable screen.
            // Bluetooth/Wi-Fi printing could NEVER be configured. Entry point
            // added here (App Info section).
            SettingsCard(
                icon = Icons.Default.Payments,
                title = "Printer Settings",
                subtitle = "Configure Bluetooth / Wi-Fi ESC-POS printer (MAC or IP)",
                onClick = onNavigateToPrinterSettings
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsCard(
                icon = Icons.Default.CloudUpload,
                title = "Local Backup",
                subtitle = if (backupProgress != null) backupProgress!! else "Export all data to a ZIP file",
                onClick = {
                    val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                    backupLauncher.launch("TillzoPOS_Backup_$timestamp.zip")
                }
            )

            // FIX (2026-08-06): Faisal's requirement — view what's stored on this phone
            SettingsCard(
                icon = Icons.Default.Storage,
                title = "Stored Data (This Phone)",
                subtitle = "View all data saved on this device",
                onClick = { onNavigateToDataViewer() }
            )

            // FIX (2026-08-06): one-tap local backup copy to Documents (survives reinstall)
            val autoBackupStatus by viewModel.autoBackupStatus.collectAsState()
            SettingsCard(
                icon = Icons.Default.Save,
                title = "Back Up Now",
                subtitle = autoBackupStatus ?: "Save a backup copy to Documents (safe even if you uninstall)",
                onClick = { viewModel.runAutoBackupNow() }
            )

            if (backupProgress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showSheetSelectionDialog) {
        Dialog(
            onDismissRequest = { showSheetSelectionDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Select Data Sheet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "Choose a Google Sheet to store your POS data.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.loadDriveSheets() }
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh Sheets",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    if (isSearching) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Searching your Google Drive...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isSearching && sheetsList.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.SearchOff, null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "No existing worksheets found",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 14.sp
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Create a new one below or paste ID",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        items(sheetsList, key = { it.spreadsheetId }) { sheet ->
                            SheetOptionCard(
                                sheet = sheet,
                                onClick = {
                                    viewModel.updateSpreadsheetId(sheet.spreadsheetId)
                                    showSheetSelectionDialog = false
                                }
                            )
                        }

                        if (!isSearching) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HorizontalDivider(modifier = Modifier.weight(1f))
                                    Text(
                                        "  OR  ",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                    HorizontalDivider(modifier = Modifier.weight(1f))
                                }
                            }

                            item {
                                TextButton(
                                    onClick = { showCreateSheetForm = !showCreateSheetForm },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        if (showCreateSheetForm) "Hide create form" else "+ Create New Sheet",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            if (showCreateSheetForm) {
                                item {
                                    OutlinedTextField(
                                        value = newSheetShopName,
                                        onValueChange = { newSheetShopName = it },
                                        label = { Text("Business / Shop Name") },
                                        placeholder = { Text("e.g. My Shop") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val name = newSheetShopName.trim().ifBlank { "TillzoPOS Business" }
                                            viewModel.createNewSheet(
                                                shopName = name,
                                                onDone = { showSheetSelectionDialog = false }
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Create & Connect Sheet") }
                                }
                            }

                            item {
                                Spacer(Modifier.height(4.dp))
                                TextButton(
                                    onClick = { showManualEntry = !showManualEntry },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        if (showManualEntry) "Hide manual entry" else "Paste Sheet ID or URL manually",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            if (showManualEntry) {
                                item {
                                    OutlinedTextField(
                                        value = manualSheetIdInput,
                                        onValueChange = { manualSheetIdInput = it },
                                        label = { Text("Sheet ID or URL") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.updateSpreadsheetId(manualSheetIdInput)
                                            showSheetSelectionDialog = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Connect") }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = { showSheetSelectionDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancel") }
                }
            }
        }
    }

    if (showGrnFolderDialog) {
        Dialog(
            onDismissRequest = { showGrnFolderDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Select Receipt Folder",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "Choose a Google Drive folder for attachments.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.searchFolders() }
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh Folders",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    if (isSearchingFolders) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Searching folders...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isSearchingFolders && folderList.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.SearchOff, null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "No folders found",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        items(folderList, key = { it.spreadsheetId }) { folder ->
                            SheetOptionCard(
                                sheet = folder,
                                onClick = {
                                    viewModel.selectFolder(folder.spreadsheetId, folder.name)
                                    showGrnFolderDialog = false
                                }
                            )
                        }

                        if (!isSearchingFolders) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HorizontalDivider(modifier = Modifier.weight(1f))
                                    Text(
                                        "  OR  ",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                    HorizontalDivider(modifier = Modifier.weight(1f))
                                }
                            }

                            item {
                                TextButton(
                                    onClick = { showCreateGrnFolder = !showCreateGrnFolder },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        if (showCreateGrnFolder) "Hide create form" else "Create new folder",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            if (showCreateGrnFolder) {
                                item {
                                    OutlinedTextField(
                                        value = grnNewFolderName,
                                        onValueChange = { grnNewFolderName = it },
                                        label = { Text("Folder name") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (grnNewFolderName.isNotBlank()) {
                                                viewModel.createNewFolder(grnNewFolderName.trim())
                                                showGrnFolderDialog = false
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Create & Select") }
                                }
                            }

                            item {
                                TextButton(
                                    onClick = { showManualFolderEntry = !showManualFolderEntry },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        if (showManualFolderEntry) "Hide manual folder entry" else "Paste Folder ID or URL manually",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            if (showManualFolderEntry) {
                                item {
                                    OutlinedTextField(
                                        value = manualFolderIdInput,
                                        onValueChange = { manualFolderIdInput = it },
                                        label = { Text("Folder ID or Drive URL") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val extracted = if (manualFolderIdInput.contains("/folders/")) {
                                                manualFolderIdInput.substringAfter("/folders/").substringBefore("?").substringBefore("/")
                                            } else {
                                                manualFolderIdInput.trim()
                                            }
                                            if (extracted.isNotBlank()) {
                                                viewModel.selectFolder(extracted, "Selected Drive Folder")
                                                showGrnFolderDialog = false
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Connect Folder") }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = { showGrnFolderDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancel") }
                }
            }
        }
    }

    if (showPinConfigDialog) {
        if (!hasPinStatus) {
            AlertDialog(
                onDismissRequest = {
                    showPinConfigDialog = false
                    if (!isPinEnabled) {
                        viewModel.togglePinLock(false)
                    }
                },
                title = { Text("Set Security PIN") },
                text = {
                    Column {
                        Text(
                            "Create a 4-digit PIN for quick access to Tillzo POS.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = {
                                if (it.all { c -> c.isDigit() } && it.length <= 4) {
                                    newPinInput = it
                                    pinError = null
                                }
                            },
                            label = { Text("New 4-Digit PIN") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (pinError != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = pinError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPinInput.length == 4) {
                                viewModel.saveNewPin(newPinInput)
                                viewModel.togglePinLock(true)
                                showPinConfigDialog = false
                            } else {
                                pinError = "PIN must be exactly 4 digits."
                            }
                        }
                    ) { Text("Save PIN") }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showPinConfigDialog = false
                            if (!isPinEnabled) {
                                viewModel.togglePinLock(false)
                            }
                        }
                    ) { Text("Cancel") }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showPinConfigDialog = false },
                title = { Text("Security PIN Settings") },
                text = {
                    Column {
                        TextButton(
                            onClick = {
                                viewModel.removePin()
                                showPinConfigDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Remove PIN", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Change PIN",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = currentPinInput,
                            onValueChange = {
                                if (it.all { c -> c.isDigit() } && it.length <= 4) {
                                    currentPinInput = it
                                    pinError = null
                                }
                            },
                            label = { Text("Current PIN") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = changeNewPinInput,
                            onValueChange = {
                                if (it.all { c -> c.isDigit() } && it.length <= 4) {
                                    changeNewPinInput = it
                                    pinError = null
                                }
                            },
                            label = { Text("New PIN") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = changeConfirmPinInput,
                            onValueChange = {
                                if (it.all { c -> c.isDigit() } && it.length <= 4) {
                                    changeConfirmPinInput = it
                                    pinError = null
                                }
                            },
                            label = { Text("Confirm New PIN") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (pinError != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = pinError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (!viewModel.verifyCurrentPin(currentPinInput)) {
                                pinError = "Incorrect current PIN. Please try again."
                            } else if (changeNewPinInput.length != 4) {
                                pinError = "New PIN must be exactly 4 digits."
                            } else if (changeNewPinInput != changeConfirmPinInput) {
                                pinError = "New PIN and confirm PIN do not match."
                            } else {
                                viewModel.saveNewPin(changeNewPinInput)
                                showPinConfigDialog = false
                            }
                        }
                    ) { Text("Change PIN") }
                },
                dismissButton = {
                    TextButton(onClick = { showPinConfigDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // ── Country Selection Dialog ────────────────────────────────────────
        if (showCountryDialog) {
            AlertDialog(
                onDismissRequest = { showCountryDialog = false },
                title = { Text("Select Business Country", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.tillzo.pos.utils.TaxUtils.PRESETS.forEach { preset ->
                            val isSelected = preset.code.equals(countryCode, ignoreCase = true)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectCountryPreset(preset.code)
                                        showCountryDialog = false
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(preset.flag, fontSize = 22.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(preset.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text(
                                            "${preset.currencySymbol} • ${preset.taxLabel} ${preset.defaultTaxRate}% (${if (preset.taxInclusive) "Inclusive" else "Exclusive"})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCountryDialog = false }) { Text("Close") }
                }
            )
        }

        // ── Tax Registration & Rate Edit Dialog ──────────────────────────────
        if (showTaxEditDialog) {
            val preset = com.tillzo.pos.utils.TaxUtils.getPreset(countryCode)
            var tempTaxNumber by remember(taxNumber) { mutableStateOf(taxNumber) }
            var tempTaxLabel by remember(taxLabel) { mutableStateOf(taxLabel) }
            var tempTaxRate by remember(defaultTaxRate) { mutableStateOf(defaultTaxRate.toString()) }

            AlertDialog(
                onDismissRequest = { showTaxEditDialog = false },
                title = { Text("Tax & Compliance Details", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = tempTaxNumber,
                            onValueChange = { tempTaxNumber = it },
                            label = { Text("${preset.taxIdLabel} / Tax ID") },
                            placeholder = { Text("e.g. 100234567890003") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = tempTaxLabel,
                            onValueChange = { tempTaxLabel = it },
                            label = { Text("Tax Name / Label") },
                            placeholder = { Text("e.g. VAT, GST, Sales Tax") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = tempTaxRate,
                            onValueChange = { tempTaxRate = it },
                            label = { Text("Default Tax Rate (%)") },
                            placeholder = { Text("e.g. 5.0, 15.0, 18.0") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.setTaxNumber(tempTaxNumber.trim())
                            viewModel.setTaxLabel(tempTaxLabel.trim().ifBlank { "VAT" })
                            viewModel.setDefaultTaxRate(tempTaxRate.toDoubleOrNull() ?: 0.0)
                            showTaxEditDialog = false
                        }
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showTaxEditDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun SheetOptionCard(
    sheet: PosSheetInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (sheet.isTagged)
                Color(0xFF4CAF50).copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFF4CAF50).copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.TableChart, null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    sheet.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Modified: ${formatDriveDate(sheet.modifiedTime)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                if (sheet.isTagged) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Verified, null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Tillzo POS backup",
                            color = Color(0xFF4CAF50),
                            fontSize = 11.sp
                        )
                    }
                }
            }
            Icon(
                Icons.Default.ChevronRight, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SettingsCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun formatDriveDate(isoDate: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()
        )
        val outputFormat = java.text.SimpleDateFormat(
            "dd MMM yyyy", java.util.Locale.getDefault()
        )
        val date = inputFormat.parse(isoDate)
        outputFormat.format(date ?: return isoDate)
    } catch (e: Exception) {
        isoDate.take(10)
    }
}
