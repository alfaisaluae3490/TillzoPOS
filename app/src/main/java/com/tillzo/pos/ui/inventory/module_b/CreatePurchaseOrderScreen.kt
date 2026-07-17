package com.tillzo.pos.ui.inventory.module_b

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.data.local.entity.VendorEntity
import com.tillzo.pos.ui.inventory.module_b.viewmodel.CreatePurchaseOrderViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Shared UI helpers ───────────────────────────────────────────────────────

@Composable
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor    = Color.White,
    unfocusedTextColor  = Color.White,
    focusedBorderColor  = Color(0xFF1E88E5),
    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
    focusedLabelColor   = Color(0xFF1E88E5),
    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
    cursorColor         = Color(0xFF1E88E5)
)

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                color      = Color(0xFF1E88E5),
                fontWeight = FontWeight.Bold,
                fontSize   = 13.sp,
                modifier   = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

// ─── Main Screen ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePurchaseOrderScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreatePurchaseOrderViewModel = hiltViewModel()
) {
    val context            = LocalContext.current
    val items              by viewModel.items.collectAsState()
    val selectedVendor     by viewModel.selectedVendor.collectAsState()
    val totalAmount        by viewModel.totalAmount.collectAsState()

    // Local UI state
    var vendorQuery     by remember { mutableStateOf("") }
    var vendorSuggestions by remember { mutableStateOf<List<VendorEntity>>(emptyList()) }
    var inventoryQuery  by remember { mutableStateOf("") }
    var inventorySuggestions by remember { mutableStateOf<List<InventoryEntity>>(emptyList()) }
    var notes           by remember { mutableStateOf("") }
    var deliveryDate    by remember { mutableStateOf("") }
    var showDatePicker  by remember { mutableStateOf(false) }

    // Add-vendor dialog state
    var showAddVendor   by remember { mutableStateOf(false) }
    var newVendorName   by remember { mutableStateOf("") }
    var newVendorPhone  by remember { mutableStateOf("") }
    var newVendorWA     by remember { mutableStateOf("") }

    // Search vendors as query changes
    LaunchedEffect(vendorQuery) {
        viewModel.searchVendors(vendorQuery).collectLatest {
            vendorSuggestions = it
        }
    }

    // Search inventory as query changes
    LaunchedEffect(inventoryQuery) {
        viewModel.searchInventory(inventoryQuery).collectLatest {
            inventorySuggestions = it
        }
    }

    // Add Vendor Dialog
    if (showAddVendor) {
        AlertDialog(
            onDismissRequest = { showAddVendor = false },
            containerColor   = Color(0xFF2A2A2A),
            title = { Text("New Vendor", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newVendorName,
                        onValueChange = { newVendorName = it },
                        label  = { Text("Vendor Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value = newVendorPhone,
                        onValueChange = { newVendorPhone = it },
                        label  = { Text("Phone *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value = newVendorWA,
                        onValueChange = { newVendorWA = it },
                        label = { Text("WhatsApp (optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedTextFieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newVendorName.isNotBlank() && newVendorPhone.isNotBlank()) {
                            viewModel.saveNewVendorAndSelect(
                                name = newVendorName.trim(),
                                phone = newVendorPhone.trim(),
                                whatsapp = newVendorWA.trim()
                            )
                            vendorQuery = newVendorName.trim()
                            showAddVendor = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddVendor = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            deliveryDate = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Purchase Order", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        },
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A))
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.savePO(notes, deliveryDate, onSuccess = onNavigateBack) },
                        modifier = Modifier.weight(1f),
                        border   = BorderStroke(1.dp, Color(0xFF1E88E5)),
                        enabled  = selectedVendor != null && items.isNotEmpty()
                    ) {
                        Text("Save Draft", color = Color(0xFF1E88E5))
                    }
                    Button(
                        onClick = {
                            val vendor = selectedVendor ?: return@Button
                            val wa = vendor.whatsapp.ifEmpty { vendor.phone }
                            val text = buildPOShareText(vendor.name, vendor.phone, notes, deliveryDate, items, totalAmount)
                            val encoded = Uri.encode(text)
                            val uri = if (wa.isNotEmpty())
                                Uri.parse("https://api.whatsapp.com/send?phone=$wa&text=$encoded")
                            else
                                Uri.parse("https://api.whatsapp.com/send?text=$encoded")
                            try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) } catch (_: Exception) {}
                            viewModel.savePO(notes, deliveryDate, markAsSent = true, onNavigateBack)
                        },
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        enabled  = selectedVendor != null && items.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Save & Share", color = Color.White)
                    }
                }
            }
        },
        containerColor = Color(0xFF1A1A1A)
    ) { padding ->
        LazyColumn(
            modifier            = Modifier.padding(padding),
            contentPadding      = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── SECTION 1: Vendor ──────────────────────────────────────────────
            item {
                SectionCard("Vendor") {
                    OutlinedTextField(
                        value         = vendorQuery,
                        onValueChange = { vendorQuery = it },
                        label         = { Text("Search or type vendor name") },
                        leadingIcon   = { Icon(Icons.Default.Person, null, tint = Color(0xFF1E88E5)) },
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = outlinedTextFieldColors()
                    )
                    if (vendorSuggestions.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        vendorSuggestions.take(5).forEach { vendor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setVendor(vendor)
                                        vendorQuery = vendor.name
                                        vendorSuggestions = emptyList()
                                    }
                                    .padding(vertical = 9.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(vendor.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    if (vendor.phone.isNotEmpty())
                                        Text(vendor.phone, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.4f))
                            }
                            Divider(color = Color.White.copy(alpha = 0.08f))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { showAddVendor = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color(0xFF1E88E5), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add New Vendor", color = Color(0xFF1E88E5), fontSize = 13.sp)
                    }
                    if (selectedVendor != null) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E88E5).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(selectedVendor!!.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                                if (selectedVendor!!.phone.isNotEmpty())
                                    Text(selectedVendor!!.phone, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // ── SECTION 2: PO Details ──────────────────────────────────────────
            item {
                SectionCard("Order Details") {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value         = deliveryDate,
                            onValueChange = { },
                            label         = { Text("Expected Delivery Date (YYYY-MM-DD)") },
                            leadingIcon   = { Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF1E88E5)) },
                            modifier      = Modifier.fillMaxWidth(),
                            colors        = outlinedTextFieldColors(),
                            readOnly      = true,
                            enabled       = false
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showDatePicker = true }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = notes,
                        onValueChange = { notes = it },
                        label         = { Text("Notes (optional)") },
                        modifier      = Modifier.fillMaxWidth(),
                        minLines      = 2,
                        colors        = outlinedTextFieldColors()
                    )
                }
            }

            // ── SECTION 3: Add Items ───────────────────────────────────────────
            item {
                SectionCard("Items (${items.size})") {
                    OutlinedTextField(
                        value         = inventoryQuery,
                        onValueChange = { inventoryQuery = it },
                        label         = { Text("Search inventory to add item...") },
                        leadingIcon   = { Icon(Icons.Default.Search, null, tint = Color(0xFF1E88E5)) },
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = outlinedTextFieldColors()
                    )
                    if (inventorySuggestions.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        inventorySuggestions.take(5).forEach { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addItem(product, 1.0, product.cost_price)
                                        inventoryQuery = ""
                                        inventorySuggestions = emptyList()
                                    }
                                    .padding(vertical = 9.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(product.item_name, color = Color.White, fontSize = 14.sp)
                                    if (product.sku.isNotEmpty())
                                        Text(product.sku, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                                }
                                Text(
                                    "PKR ${String.format("%,.0f", product.cost_price)}",
                                    color = Color(0xFF1E88E5),
                                    fontSize = 13.sp
                                )
                            }
                            Divider(color = Color.White.copy(alpha = 0.08f))
                        }
                    }
                }
            }

            // ── PO Item Rows ───────────────────────────────────────────────────
            itemsIndexed(items) { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                    shape    = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                "${index + 1}. ${item.productName}",
                                color      = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                modifier   = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick  = { viewModel.removeItem(item.poItemId) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close, null,
                                    tint     = Color(0xFFF44336),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var qtyText   by remember(item.poItemId) { mutableStateOf(item.orderedQty.toString()) }
                            var priceText by remember(item.poItemId) { mutableStateOf(item.unitCostPrice.toString()) }
                            OutlinedTextField(
                                value         = qtyText,
                                onValueChange = {
                                    qtyText = it
                                    val q = it.toDoubleOrNull() ?: return@OutlinedTextField
                                    viewModel.updateItemQty(item.poItemId, q)
                                },
                                label   = { Text("Qty") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors  = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value         = priceText,
                                onValueChange = {
                                    priceText = it
                                    val p = it.toDoubleOrNull() ?: return@OutlinedTextField
                                    viewModel.updateItemPrice(item.poItemId, p)
                                },
                                label   = { Text("Cost") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors  = outlinedTextFieldColors()
                            )
                            Box(
                                Modifier
                                    .weight(1f)
                                    .align(Alignment.CenterVertically),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "= PKR ${String.format("%,.0f", item.totalCost)}",
                                    color      = Color(0xFF1E88E5),
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 13.sp,
                                    textAlign  = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // ── SECTION 4: Summary ─────────────────────────────────────────────
            if (items.isNotEmpty()) {
                item {
                    SectionCard("Summary") {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Items:", color = Color.White.copy(alpha = 0.7f))
                            Text("${items.size}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount:", color = Color.White.copy(alpha = 0.7f))
                            Text(
                                "PKR ${String.format("%,.0f", totalAmount)}",
                                color      = Color(0xFF1E88E5),
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildPOShareText(
    vendorName: String,
    vendorPhone: String,
    notes: String,
    deliveryDate: String,
    items: List<com.tillzo.pos.data.local.entity.PurchaseOrderItemEntity>,
    total: Double
): String = buildString {
    appendLine("================================")
    appendLine("       PURCHASE ORDER")
    appendLine("================================")
    appendLine("Vendor : $vendorName")
    if (vendorPhone.isNotEmpty()) appendLine("Phone  : $vendorPhone")
    if (deliveryDate.isNotEmpty()) appendLine("Delivery: $deliveryDate")
    appendLine("================================")
    items.forEach { item ->
        appendLine(item.productName)
        appendLine("  ${item.orderedQty} ${item.unit} x PKR ${item.unitCostPrice} = PKR ${item.totalCost}")
    }
    appendLine("================================")
    appendLine("TOTAL: PKR ${String.format("%,.0f", total)}")
    appendLine("================================")
    if (notes.isNotEmpty()) appendLine("Notes: $notes")
    appendLine("Please confirm receipt of this order.")
}
