package com.tillzo.pos.ui.inventory.module_c

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.entity.GrnItemEntity
import com.tillzo.pos.data.local.entity.ProductBatchEntity
import com.tillzo.pos.ui.inventory.module_c.viewmodel.CreateGrnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGrnScreen(
    poId: String,
    onNavigateBack: () -> Unit,
    onNavigateToSuccess: (String, Int, Int, Int) -> Unit,
    viewModel: CreateGrnViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val po by viewModel.selectedPO.collectAsState()
    val confirmResult by viewModel.confirmResult.collectAsState()
    val confirmedGrnId by viewModel.confirmedGrnId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val attachedFileUri by viewModel.attachedFileUri.collectAsState()
    val attachedFileName by viewModel.attachedFileName.collectAsState()

    val context = LocalContext.current
    val currencySymbol = remember { AppSetupPrefs(context).currencySymbol.ifBlank { "$" } }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val cursor = context.contentResolver.query(it, null, null, null, null)
            val fileName = cursor?.use { c ->
                val nameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                c.moveToFirst()
                if (nameIndex >= 0) c.getString(nameIndex) else "attachment"
            } ?: "attachment"
            viewModel.setAttachedFile(it, fileName)
        }
    }

    var notes by remember { mutableStateOf("") }
    var expandedItemId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(poId) {
        viewModel.loadPO(poId)
    }

    LaunchedEffect(confirmResult) {
        confirmResult?.let {
            if (it.success) {
                val grnId = confirmedGrnId ?: "unknown"
                onNavigateToSuccess(grnId, it.newProductsCreated, it.batchesAdded, it.batchesUpdated)
                viewModel.resetConfirmResult()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receive Goods") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveAndConfirmGRN(notes) }, enabled = !isLoading) {
                        Icon(Icons.Default.Check, contentDescription = "Confirm", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E88E5),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1A1A1A)
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1E88E5))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                // Header Info
                Text("PO: ${po?.poNumber ?: "-"}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                Text("Vendor: ${po?.vendorName ?: "Unknown"}", color = Color.White, style = MaterialTheme.typography.titleMedium)
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Delivery Notes") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF1E88E5),
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E88E5))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (attachedFileName.isNotEmpty()) attachedFileName else "Attach Document")
                }
                if (attachedFileUri != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "File selected",
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Received Items (${items.size})", style = MaterialTheme.typography.titleMedium, color = Color(0xFF1E88E5))
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(Modifier.fillMaxSize()) {
                    items(items, key = { it.grnItemId }) { item ->
                        val batches by viewModel.getBatchesForProduct(item.productId)
                            .collectAsState(initial = emptyList())
                        GrnItemAccordion(
                            item = item,
                            batches = batches,
                            isExpanded = expandedItemId == item.grnItemId,
                            onToggle = { 
                                expandedItemId = if (expandedItemId == item.grnItemId) null else item.grnItemId 
                            },
                            onQtyChange = { viewModel.updateItemReceivedQty(item.grnItemId, it) },
                            onBatchInfoChange = { batch, mfg, exp -> viewModel.updateItemBatchInfo(item.grnItemId, batch, mfg, exp) },
                            onActionChange = { action, isNew -> viewModel.updateItemInventoryAction(item.grnItemId, action, isNew) },
                            onCategoryBrandChange = { cat, brand -> viewModel.updateItemCategoryAndBrand(item.grnItemId, cat, brand) },
                            onSellingPriceChange = { price -> viewModel.updateItemSellingPrice(item.grnItemId, price) },
                            onBatchSelected = { batchId, batchNumber, mfg, exp ->
                                viewModel.updateItemBatchSelection(item.grnItemId, batchId, batchNumber, mfg, exp)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GrnItemAccordion(
    item: GrnItemEntity,
    batches: List<ProductBatchEntity> = emptyList(),
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onQtyChange: (Double) -> Unit,
    onBatchInfoChange: (String, String, String) -> Unit,
    onActionChange: (String, Boolean) -> Unit,
    onCategoryBrandChange: (String, String) -> Unit,
    onSellingPriceChange: (Double) -> Unit,
    onBatchSelected: (String, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    val context = LocalContext.current
    val currencySymbol = remember { AppSetupPrefs(context).currencySymbol.ifBlank { "$" } }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onToggle() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))
    ) {
        Column(Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(item.productName, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text("Recv: ${item.receivedQty} ${item.unit} | Cost: $currencySymbol ${item.unitCostPrice}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand",
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(Modifier.padding(top = 16.dp)) {
                    Divider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = item.receivedQty.toString(),
                        onValueChange = { onQtyChange(it.toDoubleOrNull() ?: 0.0) },
                        label = { Text("Received Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Inventory Action", color = Color(0xFF1E88E5), style = MaterialTheme.typography.labelLarge)
                    
                    val actions = listOf("PENDING" to "Review Later", "NEW_PRODUCT" to "Create New Product", "ADD_BATCH" to "Add as New Batch", "UPDATE_BATCH" to "Add to Existing Batch")
                    actions.forEach { (action, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = item.inventoryAction == action,
                                onClick = { onActionChange(action, action == "NEW_PRODUCT") },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1E88E5), unselectedColor = Color.Gray)
                            )
                            Text(label, color = Color.White)
                        }
                    }

                    if (item.inventoryAction == "NEW_PRODUCT") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = item.categoryId,
                                onValueChange = { onCategoryBrandChange(it, item.brand) },
                                label = { Text("Category") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            OutlinedTextField(
                                value = item.brand,
                                onValueChange = { onCategoryBrandChange(item.categoryId, it) },
                                label = { Text("Brand") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = item.sellingPrice.takeIf { it > 0 }?.toString() ?: "",
                            onValueChange = { onSellingPriceChange(it.toDoubleOrNull() ?: 0.0) },
                            label = { Text("Selling Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    if (item.inventoryAction == "UPDATE_BATCH") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Select Existing Batch", color = Color(0xFF1E88E5), style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (batches.isEmpty()) {
                            Text(
                                "No active batches found for this product. Select 'Add as New Batch' instead.",
                                color = Color(0xFFFFA726),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            var expanded by remember { mutableStateOf(false) }
                            val selectedBatch = batches.find { it.batchId == item.batchId }
                            Box {
                                OutlinedButton(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Text(
                                        text = selectedBatch?.let { "${it.batchNumber} (Stock: ${it.stockQty})" }
                                            ?: "Select a batch...",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    batches.forEach { batch ->
                                        DropdownMenuItem(
                                            text = {
                                                Text("${batch.batchNumber} — Stock: ${batch.stockQty} | Exp: ${batch.expiryDate}")
                                            },
                                            onClick = {
                                                onBatchSelected(batch.batchId, batch.batchNumber, batch.manufacturingDate, batch.expiryDate)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (item.inventoryAction in listOf("NEW_PRODUCT", "ADD_BATCH")) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Batch Information", color = Color(0xFF1E88E5), style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = item.batchNumber,
                            onValueChange = { onBatchInfoChange(it, item.manufacturingDate, item.expiryDate) },
                            label = { Text("Batch / Lot Number") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = item.manufacturingDate,
                                onValueChange = { onBatchInfoChange(item.batchNumber, it, item.expiryDate) },
                                label = { Text("Mfg Date") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            OutlinedTextField(
                                value = item.expiryDate,
                                onValueChange = { onBatchInfoChange(item.batchNumber, item.manufacturingDate, it) },
                                label = { Text("Exp Date") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                        }
                    }
                }
            }
        }
    }
}
