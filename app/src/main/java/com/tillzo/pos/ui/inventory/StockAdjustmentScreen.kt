package com.tillzo.pos.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.dao.InventoryDao
import com.tillzo.pos.data.local.dao.StockAdjustmentDao
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.data.local.entity.StockAdjustmentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private val ADJUSTMENT_TYPES = listOf(
    "RECEIVED" to "Received stock",
    "CORRECTION" to "Stock correction",
    "DAMAGED" to "Damaged / wastage",
    "RETURNED" to "Customer return",
    "SALE_RETURN" to "Sale return"
)

// ── ViewModel ──────────────────────────────────────────────────────────────────

@HiltViewModel
class StockAdjustmentViewModel @Inject constructor(
    private val inventoryDao: InventoryDao,
    private val stockAdjustmentDao: StockAdjustmentDao
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<InventoryEntity>>(emptyList())
    val searchResults: StateFlow<List<InventoryEntity>> = _searchResults.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun search(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _searchResults.value = inventoryDao.searchItems(query).let { flow ->
                var result = emptyList<InventoryEntity>()
                // searchItems is a Flow — collect first emission
                val job = viewModelScope.launch(Dispatchers.IO) {
                    flow.collect { result = it; return@collect }
                }
                kotlinx.coroutines.delay(300)
                job.cancel()
                result
            }
        }
    }

    fun searchDirect(query: String) {
        if (query.isBlank()) { _searchResults.value = emptyList(); return }
        viewModelScope.launch(Dispatchers.IO) {
            inventoryDao.searchItems(query).collect { list ->
                _searchResults.value = list
            }
        }
    }

    fun saveAdjustment(
        product: InventoryEntity,
        adjustmentType: String,
        qtyChange: Double,
        reason: String,
        adjustedBy: String = "admin"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Update product stock
            val newStock = (product.current_stock + qtyChange).coerceAtLeast(0.0)
            inventoryDao.updateItem(
                product.copy(
                    current_stock = newStock,
                    sync_status = "pending",
                    updated_at = System.currentTimeMillis()
                )
            )
            // 2. Record adjustment log
            stockAdjustmentDao.insertStockAdjustment(
                StockAdjustmentEntity(
                    adjustmentId = UUID.randomUUID().toString(),
                    productId = product.system_row_id,
                    adjustmentType = adjustmentType,
                    quantityChanged = qtyChange,
                    reason = reason.trim(),
                    adjustedBy = adjustedBy,
                    syncStatus = "pending",
                    createdAt = System.currentTimeMillis()
                )
            )
            _saved.value = true
        }
    }

    fun resetSaved() { _saved.value = false }
}

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAdjustmentScreen(
    onNavigateBack: () -> Unit,
    viewModel: StockAdjustmentViewModel = hiltViewModel()
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val saved by viewModel.saved.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<InventoryEntity?>(null) }
    var selectedType by remember { mutableStateOf(ADJUSTMENT_TYPES[0]) }
    var qtyText by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(saved) {
        if (saved) {
            onNavigateBack()
            viewModel.resetSaved()
        }
    }

    // field colors
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFF1E88E5),
        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
        focusedLabelColor = Color(0xFF1E88E5),
        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
        cursorColor = Color(0xFF1E88E5)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Adjustment", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        },
        containerColor = Color(0xFF1A1A1A)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Product Search ────────────────────────────────────────────────
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                    shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Select Product", color = Color(0xFF1E88E5),
                            fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.searchDirect(it)
                            },
                            label = { Text("Search by name, SKU, or barcode") },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF1E88E5)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors
                        )
                        if (searchResults.isNotEmpty() && selectedProduct == null) {
                            Spacer(Modifier.height(4.dp))
                            searchResults.take(5).forEach { product ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .let { mod ->
                                            mod.then(Modifier.then(Modifier.padding(horizontal = 4.dp)))
                                        },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(product.item_name, color = Color.White, fontSize = 14.sp)
                                        Text("Stock: ${product.current_stock} ${product.unit}",
                                            color = Color.White.copy(alpha=0.5f), fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = {
                                            selectedProduct = product
                                            searchQuery = product.item_name
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) { Text("Select", fontSize = 12.sp) }
                                }
                                Divider(color = Color.White.copy(alpha = 0.08f))
                            }
                        }
                        selectedProduct?.let { product ->
                            Spacer(Modifier.height(8.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("✓ ${product.item_name}", color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.SemiBold)
                                    Text("Current: ${product.current_stock} ${product.unit}",
                                        color = Color.White.copy(alpha=0.5f), fontSize = 12.sp)
                                }
                                TextButton(onClick = { selectedProduct = null; searchQuery = "" }) {
                                    Text("Change", color = Color(0xFF1E88E5), fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ── Adjustment Details ────────────────────────────────────────────
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                    shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Adjustment Details", color = Color(0xFF1E88E5),
                            fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        // Type dropdown
                        ExposedDropdownMenuBox(
                            expanded = typeExpanded,
                            onExpandedChange = { typeExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedType.second,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Adjustment Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = fieldColors
                            )
                            ExposedDropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = { typeExpanded = false }
                            ) {
                                ADJUSTMENT_TYPES.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.second) },
                                        onClick = { selectedType = type; typeExpanded = false }
                                    )
                                }
                            }
                        }

                        // Qty field
                        OutlinedTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it },
                            label = { Text("Quantity Change (use − for negative)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors,
                            supportingText = {
                                val qty = qtyText.toDoubleOrNull()
                                val product = selectedProduct
                                if (qty != null && product != null) {
                                    Text(
                                        "New stock: ${(product.current_stock + qty).coerceAtLeast(0.0)} ${product.unit}",
                                        color = Color(0xFF1E88E5)
                                    )
                                }
                            }
                        )

                        // Reason field
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Reason *") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            colors = fieldColors
                        )
                    }
                }
            }

            // ── Save Button ───────────────────────────────────────────────────
            item {
                val canSave = selectedProduct != null
                    && qtyText.toDoubleOrNull() != null
                    && reason.isNotBlank()
                Button(
                    onClick = {
                        val product = selectedProduct ?: return@Button
                        val qty = qtyText.toDoubleOrNull() ?: return@Button
                        viewModel.saveAdjustment(product, selectedType.first, qty, reason)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save Adjustment", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
