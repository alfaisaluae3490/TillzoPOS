package com.tillzo.pos.ui.inventory.options.crud

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.data.local.entity.CategoryEntity
import com.tillzo.pos.domain.usecase.inventory.ProductFilter
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.ui.inventory.options.alerts.LowStockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryCrudScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToUnits: () -> Unit = {},
    onNavigateToStockAlerts: () -> Unit = {},
    onNavigateToOcr: () -> Unit,
    onNavigateToQr: (String) -> Unit,
    viewModel: InventoryCrudViewModel = hiltViewModel(),
    alertViewModel: LowStockViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val items by viewModel.allItems.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val lowCount by alertViewModel.lowStockCount.collectAsState()
    val outCount by alertViewModel.outOfStockCount.collectAsState()
    val expiringCount by alertViewModel.expiringCount.collectAsState()

    val context = LocalContext.current
    val prefs = remember { AppSetupPrefs(context) }
    val currencySymbol = prefs.currencySymbol

    var showFormDialog by remember { mutableStateOf(false) }
    var selectedItemForBatches by remember { mutableStateOf<InventoryEntity?>(null) }
    var itemToPrint by remember { mutableStateOf<InventoryEntity?>(null) }

    // M6.2 Read OCR scanned values returned from OcrEntryScreen
    val backStackEntry = navController?.currentBackStackEntryAsState()?.value
    val scannedWeight = backStackEntry?.savedStateHandle?.getLiveData<String>("ocr_scanned_weight")?.observeAsState()

    LaunchedEffect(scannedWeight?.value) {
        if (!scannedWeight?.value.isNullOrBlank()) {
            viewModel.selectItem(null) // New item
            showFormDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = onNavigateToCategories,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Categories",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onNavigateToUnits,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Units",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onNavigateToOcr) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Smart AI Entry (OCR)")
                    }
                    // OVERNIGHT-AUDIT Phase 2b: CSV bulk import (Excel/Sheets export)
                    val importPicker = androidx.activity.compose.rememberLauncherForActivityResult(
                        androidx.activity.result.contract.ActivityResultContracts.GetContent()
                    ) { uri ->
                        if (uri != null) {
                            context.contentResolver.openInputStream(uri)?.use { stream ->
                                viewModel.importInventoryCsv(stream.readBytes())
                            }
                        }
                    }
                    IconButton(onClick = { importPicker.launch("text/*") }) {
                        Icon(Icons.Default.UploadFile, contentDescription = "Import CSV (bulk upload)")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.selectItem(null)
                showFormDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        // OVERNIGHT-AUDIT Phase 2b: bulk import result banner
        val importResult by viewModel.importResult.collectAsState()
        if (importResult != null) {
            val importMsg = importResult ?: ""
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        importMsg,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    androidx.compose.material3.TextButton(onClick = { viewModel.clearImportResult() }) {
                        Text("OK")
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Alert Badge Row
            if (lowCount > 0 || outCount > 0 || expiringCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (lowCount > 0) {
                        SuggestionChip(
                            onClick = onNavigateToStockAlerts,
                            label = { Text("⚠️ $lowCount Low", fontSize = 12.sp) }
                        )
                    }
                    if (outCount > 0) {
                        SuggestionChip(
                            onClick = onNavigateToStockAlerts,
                            label = { Text("🚫 $outCount Out", fontSize = 12.sp) }
                        )
                    }
                    if (expiringCount > 0) {
                        SuggestionChip(
                            onClick = onNavigateToStockAlerts,
                            label = { Text("⏰ $expiringCount Expiring", fontSize = 12.sp) }
                        )
                    }
                }
            }
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search items...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ProductFilter.entries) { filter ->
                    FilterChip(
                        selected = currentFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.name.replace("_", " ")) }
                    )
                }
            }

            // Products List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.system_row_id }) { item ->
                    InventoryItemCard(
                        item = item,
                        currencySymbol = currencySymbol,
                        onClick = {
                            viewModel.selectItem(item)
                            showFormDialog = true
                        },
                        onDelete = { viewModel.deleteItem(item.system_row_id) },
                        onPrintQr = { navController?.navigate("barcode_print_settings/${item.system_row_id}") },
                        onViewBatches = { selectedItemForBatches = item }
                    )
                }
            }
        }
    }

    if (showFormDialog) {
        InventoryFormDialog(
            item = selectedItem,
            preFilledWeight = scannedWeight?.value,
            viewModel = viewModel,
            onDismiss = {
                navController?.currentBackStackEntry?.savedStateHandle?.remove<String>("ocr_scanned_weight")
                showFormDialog = false
            },
            onSave = { name, category, gtinsList, unit, price, stock, threshold, sku, desc, cost, tax, batch, exp, mfg, expAlert, dmgStock, dmgQty ->
                viewModel.saveItem(
                    itemName = name,
                    category = category,
                    gtins = gtinsList,
                    unit = unit,
                    pricePerUnit = price,
                    currentStock = stock,
                    lowStockThreshold = threshold,
                    sku = sku,
                    description = desc,
                    costPrice = cost,
                    taxPercent = tax,
                    batchNumber = batch,
                    expiryDate = exp,
                    manufacturingDate = mfg,
                    expiryAlertDays = expAlert,
                    isDamaged = dmgStock,
                    damagedQty = dmgQty
                )
                navController?.currentBackStackEntry?.savedStateHandle?.remove<String>("ocr_scanned_weight")
                showFormDialog = false
            },
            onNavigateToUnits = onNavigateToUnits
        )
    }

    selectedItemForBatches?.let { item ->
        val batches by viewModel.getBatchesForProduct(item.system_row_id)
            .collectAsState(initial = emptyList())
        
        com.tillzo.pos.ui.inventory.module_a.BatchListBottomSheet(
            product = item,
            batches = batches,
            onAddNewBatch = { 
                viewModel.showAddBatchDialog(item.system_row_id)
            },
            onDismiss = { selectedItemForBatches = null },
            onEditBatch = { batch, batchNumber, mfgDate, expiryDate, stockQty, sellingPrice ->
                viewModel.updateBatch(batch, batchNumber, mfgDate, expiryDate, stockQty, sellingPrice)
            }
        )
    }

    // FIX (2026-08-06): Add-batch dialog — was an empty stub, now functional.
    val addBatchProductId by viewModel.addBatchProductId.collectAsState()
    addBatchProductId?.let { productId ->
        com.tillzo.pos.ui.inventory.module_a.AddBatchDialog(
            onDismiss = { viewModel.dismissAddBatchDialog() },
            onSave = { batchNumber, mfgDate, expiryDate, stockQty, costPrice, sellingPrice ->
                viewModel.addBatch(
                    productId, batchNumber, mfgDate, expiryDate,
                    stockQty, costPrice, sellingPrice
                ) {
                    viewModel.dismissAddBatchDialog()
                }
            }
        )
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryEntity,
    currencySymbol: String = "Rs",
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onPrintQr: () -> Unit,
    onViewBatches: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val today = dateFormat.format(Date())
    
    val badgeColor = when {
        item.current_stock <= 0.0 || (item.expiry_date.isNotBlank() && item.expiry_date < today) -> Color.Red
        item.current_stock <= item.low_stock_threshold -> Color.Yellow
        item.expiry_date.isNotBlank() && isNearExpiry(item.expiry_date) -> Color(0xFFFFA500) // Orange
        else -> Color.Green
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.item_name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    // Colored Badge Indicator
                    Surface(shape = MaterialTheme.shapes.small, color = badgeColor, modifier = Modifier.size(12.dp)) {}
                }
                
                Text("${item.sku} • ${item.category}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                        Text("$currencySymbol ${item.price_per_unit} / ${item.unit}", color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                        Text("Stock: ${item.current_stock}", color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
                Text("Barcode: ${item.barcode_id}", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                if (item.hasBatches) {
                    TextButton(onClick = onViewBatches, modifier = Modifier.padding(top = 4.dp)) {
                        Text("View Batches (${item.totalStock})", 
                             color = Color(0xFF1E88E5), fontSize = 12.sp)
                    }
                }
            }

            IconButton(onClick = onPrintQr) {
                Icon(Icons.Default.QrCode, contentDescription = "Print QR Code")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun isNearExpiry(expiryDate: String): Boolean {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return try {
        val exp = dateFormat.parse(expiryDate) ?: return false
        val today = Date()
        val diff = exp.time - today.time
        val days = diff / (1000 * 60 * 60 * 24)
        days in 1..30
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryFormDialog(
    item: InventoryEntity?,
    preFilledWeight: String? = null,
    viewModel: InventoryCrudViewModel,
    onDismiss: () -> Unit,
    onSave: (String, String, List<String>, String, Double, Double, Double, String, String, Double, Double, String, String, String, Int, Boolean, Double) -> Unit,
    onNavigateToUnits: () -> Unit = {}
) {
    val categories by viewModel.allCategories.collectAsState()
    var showCategoryManager by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(item?.item_name ?: "") }
    var sku by remember { mutableStateOf(item?.sku ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "") }
    var desc by remember { mutableStateOf(item?.description ?: "") }
    
    var gtins by remember { mutableStateOf(item?.barcode_id?.let { listOf(it) } ?: emptyList()) }
    var currentGtinInput by remember { mutableStateOf("") }
    var gtinError by remember { mutableStateOf<String?>(null) } // FIX (2026-08-22, DEF-08)
    var costPrice by remember { mutableStateOf(item?.cost_price?.takeIf { it > 0.0 }?.toString() ?: "") }
    var price by remember { mutableStateOf(item?.price_per_unit?.takeIf { it > 0.0 }?.toString() ?: "") }
    val context = LocalContext.current
    val appSetupPrefs = remember { com.tillzo.pos.data.local.prefs.AppSetupPrefs(context) }
    var taxPercent by remember { mutableStateOf(item?.tax_percent?.takeIf { it > 0.0 }?.toString() ?: if (item == null && appSetupPrefs.defaultTaxRate > 0.0) appSetupPrefs.defaultTaxRate.toString() else "") }
    
    var stock by remember { mutableStateOf(item?.current_stock?.takeIf { it > 0.0 }?.toString() ?: "") }
    var threshold by remember { mutableStateOf(item?.low_stock_threshold?.takeIf { it > 0.0 }?.toString() ?: "") }
    var unit by remember { mutableStateOf(item?.unit ?: "PC") }
    
    var batch by remember { mutableStateOf(item?.batch_number ?: "") }
    var expDate by remember { mutableStateOf(item?.expiry_date ?: "") }
    var mfgDate by remember { mutableStateOf(item?.manufacturing_date ?: "") }
    var expAlert by remember { mutableStateOf(item?.expiry_alert_days?.toString() ?: "30") }
    // OVERNIGHT-AUDIT FIX (2026-08-23): silent validation failure UX bug — surface errors
    var saveError by remember { mutableStateOf<String?>(null) }
    
    var isDamaged by remember { mutableStateOf(item?.is_damaged_stock ?: false) }
    var dmgQty by remember { mutableStateOf(item?.damaged_qty?.takeIf { it > 0.0 }?.toString() ?: "") }

    // Hierarchical Category Selection State
    var selectedMainCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var selectedSubCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    // DatePicker State
    var showMfgDatePicker by remember { mutableStateOf(false) }
    var showExpDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Common TextField Colors for Dark Theme Visibility
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = Color.Gray,
        focusedLabelColor = MaterialTheme.colorScheme.primary
    )

    // Filtered category lists based on hierarchy
    val mainCategoriesList = remember(categories) { categories.filter { it.parent_category_id.isNullOrBlank() } }
    val subCategoriesList = remember(categories) { categories.filter { !it.parent_category_id.isNullOrBlank() } }

    LaunchedEffect(preFilledWeight) {
        if (!preFilledWeight.isNullOrBlank() && name.isBlank()) {
            name = "$preFilledWeight Product"
        }
    }

    // Restore category selection state when editing an existing product
    LaunchedEffect(categories, item) {
        if (item != null) {
            val matchedCategory = categories.find { it.category_name == item.category }
            if (matchedCategory != null) {
                if (!matchedCategory.parent_category_id.isNullOrBlank()) {
                    // It's a subcategory
                    selectedSubCategory = matchedCategory
                    selectedMainCategory = categories.find { it.system_row_id == matchedCategory.parent_category_id }
                } else {
                    // It's a main category
                    selectedMainCategory = matchedCategory
                    selectedSubCategory = null
                }
            }
        }
    }

    if (showMfgDatePicker || showExpDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showMfgDatePicker = false
                showExpDatePicker = false
            },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        val formattedDate = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        if (showMfgDatePicker) mfgDate = formattedDate
                        if (showExpDatePicker) expDate = formattedDate
                    }
                    showMfgDatePicker = false
                    showExpDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showMfgDatePicker = false
                    showExpDatePicker = false
                }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCategoryManager) {
        var newCategoryName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCategoryManager = false },
            title = { Text("Manage Categories") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("New Category") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = { viewModel.addCategory(newCategoryName); newCategoryName = "" }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Category")
                        }
                    }
                    LazyColumn(modifier = Modifier.padding(top = 8.dp).heightIn(max = 200.dp)) {
                        items(categories) { cat ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(cat.category_name)
                                IconButton(onClick = { viewModel.deleteCategory(cat.system_row_id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Category", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryManager = false }) { Text("Close") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add Product" else "Edit Product") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // OVERNIGHT-AUDIT FIX: show validation errors instead of silent failure
                if (saveError != null) {
                    Text(
                        text = saveError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Section 1: Basic Info
                Text("Basic Info", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name *") }, colors = textFieldColors, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU *") }, colors = textFieldColors, singleLine = true, modifier = Modifier.weight(1f))
                    if (item == null) {
                        Button(
                            onClick = { sku = "SKU-${System.currentTimeMillis().toString().takeLast(6)}" },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Generate")
                        }
                    }
                }
                
                // Category - Main Category Dropdown
                var expandingMainCat by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandingMainCat,
                    onExpandedChange = { expandingMainCat = !expandingMainCat }
                ) {
                    OutlinedTextField(
                        value = selectedMainCategory?.category_name ?: "Select Main Category",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Main Category *") },
                        colors = textFieldColors,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandingMainCat) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandingMainCat,
                        onDismissRequest = { expandingMainCat = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Manage Categories...", color = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                expandingMainCat = false
                                showCategoryManager = true
                            }
                        )
                        HorizontalDivider()
                        mainCategoriesList.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.category_name) },
                                onClick = {
                                    selectedMainCategory = cat
                                    selectedSubCategory = null
                                    category = cat.category_name
                                    expandingMainCat = false
                                }
                            )
                        }
                    }
                }

                // Category - Subcategory Dropdown (only if main category selected and has subcategories)
                if (selectedMainCategory != null) {
                    val availableSubCategories = subCategoriesList.filter { it.parent_category_id == selectedMainCategory?.system_row_id }
                    if (availableSubCategories.isNotEmpty()) {
                        var expandingSubCat by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expandingSubCat,
                            onExpandedChange = { expandingSubCat = !expandingSubCat }
                        ) {
                            OutlinedTextField(
                                value = selectedSubCategory?.category_name ?: "Select Subcategory",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Subcategory") },
                                colors = textFieldColors,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandingSubCat) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandingSubCat,
                                onDismissRequest = { expandingSubCat = false }
                            ) {
                                availableSubCategories.forEach { sub ->
                                    DropdownMenuItem(
                                        text = { Text(sub.category_name) },
                                        onClick = {
                                            selectedSubCategory = sub
                                            category = sub.category_name
                                            expandingSubCat = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, colors = textFieldColors, modifier = Modifier.fillMaxWidth())

                // Section 2: GTINs (Barcodes)
                Text("GTINs (Barcodes)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    gtins.forEachIndexed { index, gtin ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(gtin, modifier = Modifier.weight(1f))
                            IconButton(onClick = { gtins = gtins.filterIndexed { i, _ -> i != index } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove GTIN", tint = Color.Red)
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = currentGtinInput, 
                            onValueChange = { currentGtinInput = it }, 
                            label = { Text("Add GTIN (Leave blank for Auto)") }, 
                            colors = textFieldColors, 
                            singleLine = true, 
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (currentGtinInput.isNotBlank()) {
                                    // FIX (2026-08-22, DEF-08): GTIN validation —
                                    // was accept-anything. Invalid barcodes
                                    // (letters, wrong length, bad EAN-13 check
                                    // digit) got saved and then never scanned
                                    // at any retail POS. Now: 8-14 digits
                                    // (EAN-8/UPC-A/EAN-13/GTIN-14), and a
                                    // 13-digit code must pass the EAN checksum.
                                    val clean = currentGtinInput.filter { it.isDigit() }
                                    when {
                                        clean.length !in 8..14 -> {
                                            gtinError = "GTIN must be 8-14 digits (EAN-8, UPC-A, EAN-13, GTIN-14)"
                                        }
                                        clean.length == 13 && !com.tillzo.pos.utils.BarcodeUtils.isValidEan13(clean) -> {
                                            gtinError = "Invalid EAN-13 check digit"
                                        }
                                        gtins.contains(clean) -> {
                                            gtinError = "GTIN already added"
                                        }
                                        else -> {
                                            gtins = gtins + clean
                                            currentGtinInput = ""
                                            gtinError = null
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Add")
                        }
                    }
                    gtinError?.let { err ->
                        Text(err, color = Color(0xFFF44336), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                // Section 3: Pricing & Tax
                Text("Pricing & Tax", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = costPrice, onValueChange = { costPrice = it }, label = { Text("Cost Price") }, colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Selling Price") }, colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = taxPercent, onValueChange = { taxPercent = it }, label = { Text("Tax %") }, colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())

                // Section 4: Stock
                Text("Stock", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Current Stock") }, colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = threshold, onValueChange = { threshold = it }, label = { Text("Low Alert") }, colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                }
                var unitExpanded by remember { mutableStateOf(false) }
                val units by viewModel.productUnits.collectAsState()

                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded }
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit (KG/ML/PC)") },
                        colors = textFieldColors,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Manage Units...", color = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                unitExpanded = false
                                onNavigateToUnits()
                            }
                        )
                        HorizontalDivider()
                        units.forEach { u ->
                            DropdownMenuItem(
                                text = { Text("${u.unitName} (${u.abbreviation})") },
                                onClick = {
                                    unit = u.abbreviation
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }

                // Section 5: Batch & Expiry
                Text("Batch & Expiry", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = batch, onValueChange = { batch = it }, label = { Text("Batch Number *") }, colors = textFieldColors, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = mfgDate, 
                            onValueChange = { }, 
                            label = { Text("Mfg Date") }, 
                            colors = textFieldColors,
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Invisible overlay to securely capture clicks on the field
                        Box(modifier = Modifier.matchParentSize().clickable { showMfgDatePicker = true })
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = expDate, 
                            onValueChange = { }, 
                            label = { Text("Exp Date *") }, 
                            colors = textFieldColors,
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showExpDatePicker = true })
                    }
                }
                OutlinedTextField(value = expAlert, onValueChange = { expAlert = it }, label = { Text("Expiry Alert (Days Before)") }, colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())


                // Section 6: Damaged Stock (Only show when editing an existing item)
                if (item != null) {
                    Text("Damaged Stock", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDamaged = !isDamaged }
                            .padding(vertical = 4.dp)
                    ) {
                        Switch(
                            checked = isDamaged, 
                            onCheckedChange = { isDamaged = it },
                            colors = SwitchDefaults.colors(
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color.DarkGray,
                                uncheckedBorderColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark as having damaged items", color = MaterialTheme.colorScheme.onSurface)
                    }
                    if (isDamaged) {
                        OutlinedTextField(
                            value = dmgQty, 
                            onValueChange = { dmgQty = it }, 
                            label = { Text("Damaged Qty") }, 
                            colors = textFieldColors, 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                            singleLine = true, 
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            // OVERNIGHT-AUDIT FIX (2026-08-23): extracted handler + semantic description.
            // Synthetic taps (adb/uiautomator2) on this Button were silently dropped —
            // contentDescription gives a11y tools a direct ACTION_CLICK target, and the
            // Snackbar surfaces validation failures instead of failing silently.
            val onSaveClick: () -> Unit = {
                val priceVal = price.toDoubleOrNull() ?: 0.0
                val costVal = costPrice.toDoubleOrNull() ?: 0.0
                val stockVal = stock.toDoubleOrNull() ?: 0.0
                val thresholdVal = threshold.toDoubleOrNull() ?: 0.0

                if (name.isNotBlank() && sku.isNotBlank() &&
                    category.isNotBlank() && batch.isNotBlank() && expDate.isNotBlank() &&
                    priceVal >= 0 && costVal >= 0 && stockVal >= 0 && thresholdVal >= 0) {
                    
                    // Include the current input if the user forgot to hit "Add"
                    val finalGtins = if (currentGtinInput.isNotBlank()) gtins + currentGtinInput else gtins
                    
                    onSave(
                        name, category, finalGtins, unit,
                        priceVal, stockVal, thresholdVal,
                        sku, desc, costVal,
                        taxPercent.toDoubleOrNull() ?: 0.0,
                        batch, expDate, mfgDate,
                        expAlert.toIntOrNull() ?: 30,
                        isDamaged,
                        dmgQty.toDoubleOrNull() ?: 0.0
                    )
                } else {
                    saveError = when {
                        batch.isBlank() -> "Batch Number is required"
                        expDate.isBlank() -> "Expiry Date is required"
                        category.isBlank() -> "Category is required"
                        else -> "Please fill all required fields"
                    }
                }
            }
            Button(
                onClick = onSaveClick,
                modifier = Modifier.semantics { contentDescription = "Save Product" }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
