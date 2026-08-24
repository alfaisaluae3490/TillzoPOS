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

    val paymentOption by viewModel.paymentOption.collectAsState()
    val paidAmountInput by viewModel.paidAmountInput.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val paymentDueDate by viewModel.paymentDueDate.collectAsState()
    val reminderEnabled by viewModel.reminderEnabled.collectAsState()
    val reminderIntervalDays by viewModel.reminderIntervalDays.collectAsState()

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

    val totalGrnAmount = remember(items) { items.sumOf { it.totalCost } }
    val paidAmountVal = when (paymentOption) {
        "FULL_PAID" -> totalGrnAmount
        "PARTIAL" -> paidAmountInput.toDoubleOrNull()?.coerceIn(0.0, totalGrnAmount) ?: 0.0
        else -> 0.0
    }
    val dueBalanceVal = (totalGrnAmount - paidAmountVal).coerceAtLeast(0.0)

    fun getFutureDate(days: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, days)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return sdf.format(cal.time)
    }

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
                title = { Text("Receive Goods & AP") },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Info
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF242424)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("PO: ${po?.poNumber ?: "-"}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            Text("Vendor: ${po?.vendorName ?: "Unknown"}", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            
                            Spacer(modifier = Modifier.height(8.dp))
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
                            
                            Spacer(modifier = Modifier.height(8.dp))
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
                                Text("File selected", color = Color(0xFF4CAF50), fontSize = 12.sp)
                            }
                        }
                    }
                }

                // ── PAYMENT TERMS & CREDIT (AP) CARD ─────────────────────────────
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2518)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Payment Terms (AP)", color = Color(0xFFFFB74D), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Total: $currencySymbol%.2f".format(totalGrnAmount),
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // Payment Type Selector (Full / Partial / Credit)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val options = listOf(
                                    "FULL_PAID" to "Paid in Full",
                                    "PARTIAL" to "Partial Pay",
                                    "CREDIT" to "100% Credit"
                                )
                                options.forEach { (opt, label) ->
                                    val isSelected = paymentOption == opt
                                    Button(
                                        onClick = { viewModel.setPaymentOption(opt) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) Color(0xFFE65100) else Color(0xFF3E3E3E),
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(label, fontSize = 11.sp, maxLines = 1)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Partial Payment Amount Input
                            if (paymentOption == "PARTIAL") {
                                OutlinedTextField(
                                    value = paidAmountInput,
                                    onValueChange = { viewModel.setPaidAmountInput(it) },
                                    label = { Text("Amount Paid Now ($currencySymbol)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFFFB74D),
                                        unfocusedBorderColor = Color.Gray
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Payment Method
                            if (paymentOption != "CREDIT") {
                                Text("Payment Method", color = Color.Gray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("CASH", "BANK_TRANSFER", "CHEQUE", "CARD").forEach { method ->
                                        val isSelected = paymentMethod == method
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { viewModel.setPaymentMethod(method) },
                                            label = { Text(method.replace("_", " "), fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF1E88E5),
                                                selectedLabelColor = Color.White,
                                                labelColor = Color.LightGray
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // If there is Due Balance (Partial or Credit)
                            if (dueBalanceVal > 0.0) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1E1E)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Pending Due to Vendor:", color = Color(0xFFFF8A80), fontSize = 13.sp)
                                            Text(
                                                "$currencySymbol%.2f".format(dueBalanceVal),
                                                color = Color(0xFFFF5252),
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Payment Due Date:", color = Color.LightGray, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(4.dp))

                                        OutlinedTextField(
                                            value = paymentDueDate,
                                            onValueChange = { viewModel.setPaymentDueDate(it) },
                                            placeholder = { Text("YYYY-MM-DD (e.g. 2026-09-01)", color = Color.Gray) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = Color(0xFFFF8A80),
                                                unfocusedBorderColor = Color.Gray
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        // Quick Date Pills (+7d, +15d, +30d, +60d)
                                        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf(7 to "+7 Days", 15 to "+15 Days", 30 to "+30 Days", 60 to "+60 Days").forEach { (d, label) ->
                                                val targetDate = getFutureDate(d)
                                                val isSelected = paymentDueDate == targetDate
                                                OutlinedButton(
                                                    onClick = { viewModel.setPaymentDueDate(targetDate) },
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        containerColor = if (isSelected) Color(0xFFFF5252) else Color.Transparent,
                                                        contentColor = if (isSelected) Color.White else Color(0xFFFF8A80)
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(label, fontSize = 10.sp)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(color = Color(0xFF5C2929))
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Reminder Switch & Interval
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Automated Daily Reminders", color = Color.White, fontSize = 13.sp)
                                                Text("Loop alarm until balance is 100% paid", color = Color.Gray, fontSize = 11.sp)
                                            }
                                            Switch(
                                                checked = reminderEnabled,
                                                onCheckedChange = { viewModel.setReminderEnabled(it) },
                                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF5252))
                                            )
                                        }

                                        if (reminderEnabled) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                listOf(1 to "Daily Loop", 3 to "Every 3 Days", 7 to "Weekly").forEach { (days, text) ->
                                                    val isSel = reminderIntervalDays == days
                                                    FilterChip(
                                                        selected = isSel,
                                                        onClick = { viewModel.setReminderIntervalDays(days) },
                                                        label = { Text(text, fontSize = 10.sp) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = Color(0xFFE65100),
                                                            selectedLabelColor = Color.White
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Received Items Header
                item {
                    Text("Received Items (${items.size})", style = MaterialTheme.typography.titleMedium, color = Color(0xFF1E88E5))
                }

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

                // Confirm GRN Button at the bottom
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.saveAndConfirmGRN(notes) },
                        enabled = !isLoading && items.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            if (dueBalanceVal > 0.0) "Confirm GRN & Save AP Bill ($currencySymbol%.2f)".format(totalGrnAmount)
                            else "Confirm & Receive Goods ($currencySymbol%.2f)".format(totalGrnAmount),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
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
