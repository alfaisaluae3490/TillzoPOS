package com.tillzo.pos.ui.inventory.module_c

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.dao.GrnDao
import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.data.local.entity.GrnItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ── ViewModel ──────────────────────────────────────────────────────────────────

@HiltViewModel
class GrnDetailViewModel @Inject constructor(
    private val grnDao: GrnDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val grnId: String = checkNotNull(savedStateHandle["grnId"])

    private val _header = MutableStateFlow<GrnHeaderEntity?>(null)
    val header: StateFlow<GrnHeaderEntity?> = _header.asStateFlow()

    private val _items = MutableStateFlow<List<GrnItemEntity>>(emptyList())
    val items: StateFlow<List<GrnItemEntity>> = _items.asStateFlow()

    init {
        viewModelScope.launch {
            _header.value = grnDao.getGrnById(grnId)
            _items.value = grnDao.getGrnItems(grnId)
        }
    }
}

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrnDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: GrnDetailViewModel = hiltViewModel()
) {
    val header by viewModel.header.collectAsState()
    val items by viewModel.items.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(header?.grnNumber ?: "GRN Detail",
                        color = Color.White, fontWeight = FontWeight.Bold)
                },
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
        if (header == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1E88E5))
            }
            return@Scaffold
        }

        val grn = header!!
        val statusColor = if (grn.status == "CONFIRMED") Color(0xFF4CAF50) else Color(0xFFFF9800)
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(grn.createdAt))

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── GRN Header Info ───────────────────────────────────────────────
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(grn.grnNumber, color = Color.White,
                                fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Box(
                                Modifier
                                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(grn.status, color = statusColor,
                                    fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        GrnInfoRow(Icons.Default.Store, "Vendor", grn.vendorName)
                        if (grn.vendorPhone.isNotEmpty())
                            GrnInfoRow(Icons.Default.Phone, "Phone", grn.vendorPhone)
                        if (grn.poNumber.isNotEmpty())
                            GrnInfoRow(Icons.Default.ShoppingCart, "PO Reference", grn.poNumber)
                        GrnInfoRow(Icons.Default.CalendarToday, "Received On", dateStr)
                        if (grn.receivedByName.isNotEmpty())
                            GrnInfoRow(Icons.Default.Person, "Received By", grn.receivedByName)
                        if (grn.notes.isNotEmpty())
                            GrnInfoRow(Icons.Default.Notes, "Notes", grn.notes)
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Items: ${grn.totalItems}", color = Color.White.copy(alpha = 0.7f))
                            Text("PKR ${String.format("%,.0f", grn.totalAmount)}",
                                color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        if (grn.attachedFileUrl.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(grn.attachedFileUrl))
                                        context.startActivity(browserIntent)
                                    } catch (_: Exception) { }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.OpenInBrowser, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("View Attached Document")
                            }
                        }
                    }
                }
            }

            // ── Items Header ──────────────────────────────────────────────────
            item {
                Text("Received Items (${items.size})",
                    color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 4.dp))
            }

            // ── Item Cards ────────────────────────────────────────────────────
            items(items, key = { it.grnItemId }) { item ->
                GrnItemDetailCard(item = item)
            }
        }
    }
}

@Composable
private fun GrnInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = Color(0xFF1E88E5).copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Spacer(Modifier.width(8.dp))
        Text("$label: ", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun GrnItemDetailCard(item: GrnItemEntity) {
    val actionColor = when (item.inventoryAction) {
        "NEW_PRODUCT", "NEW_ITEM" -> Color(0xFF4CAF50)
        "ADD_BATCH" -> Color(0xFF1E88E5)
        "UPDATE_BATCH" -> Color(0xFFFF9800)
        else -> Color(0xFF9E9E9E)
    }
    val actionLabel = when (item.inventoryAction) {
        "NEW_PRODUCT", "NEW_ITEM" -> "New Product"
        "ADD_BATCH" -> "New Batch"
        "UPDATE_BATCH" -> "Updated Batch"
        else -> item.inventoryAction
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.productName, color = Color.White, fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp, modifier = Modifier.weight(1f))
                Box(
                    Modifier
                        .background(actionColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(actionLabel, color = actionColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Received: ${item.receivedQty} ${item.unit}",
                    color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                Text("PKR ${String.format("%,.0f", item.totalCost)}",
                    color = Color(0xFF1E88E5), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            if (item.batchNumber.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Batch: ${item.batchNumber}", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    if (item.expiryDate.isNotEmpty())
                        Text("Exp: ${item.expiryDate}", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
            if (item.sku.isNotEmpty())
                Text("SKU: ${item.sku}", color = Color.White.copy(alpha = 0.35f), fontSize = 11.sp)
        }
    }
}
