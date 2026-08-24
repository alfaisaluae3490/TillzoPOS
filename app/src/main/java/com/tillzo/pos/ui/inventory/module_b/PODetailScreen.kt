package com.tillzo.pos.ui.inventory.module_b

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderItemEntity
import com.tillzo.pos.ui.inventory.module_b.viewmodel.PODetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PODetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateGrn: (String) -> Unit,
    onNavigateToGrnDetail: (String) -> Unit,
    viewModel: PODetailViewModel = hiltViewModel()
) {
    val po by viewModel.po.collectAsState()
    val items by viewModel.items.collectAsState()
    val linkedGrns by viewModel.linkedGrns.collectAsState()
    val context = LocalContext.current
    val currencySymbol = remember(context) { com.tillzo.pos.data.local.prefs.AppSetupPrefs(context).currencySymbol.ifBlank { "$" } }

    if (po == null) {
        Box(Modifier.fillMaxSize().background(Color(0xFF1A1A1A)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1E88E5))
        }
        return
    }

    val order = po!!
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(order.createdAt))
    val canSend = order.status == "DRAFT"
    // OVERNIGHT-AUDIT FIX (2026-08-24, D7-2): hide Receive when all items fully
    // received — pehle status SENT par hi button zinda reh jata tha → duplicate GRNs.
    val canReceive = order.status in listOf("SENT", "PARTIALLY_RECEIVED") &&
        items.none { it.receivedQty >= it.orderedQty }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(order.poNumber, color = Color.White, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    // Share again via WhatsApp
                    if (order.vendorName.isNotEmpty()) {
                        IconButton(onClick = {
                            val text = buildString {
                                appendLine("*Purchase Order: ${order.poNumber}*")
                                appendLine("Date: $dateStr")
                                appendLine("Dear ${order.vendorName},")
                                appendLine()
                                appendLine("Please process the following order:")
                                items.forEachIndexed { i, item ->
                                    appendLine("${i + 1}. ${item.productName} — Qty: ${item.orderedQty} ${item.unit} @ $currencySymbol ${item.unitCostPrice}")
                                }
                                appendLine()
                                appendLine("*Total: $currencySymbol ${String.format("%,.0f", order.totalAmount)}*")
                                if (order.notes.isNotEmpty()) appendLine("Notes: ${order.notes}")
                            }
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                val encoded = java.net.URLEncoder.encode(text, "UTF-8")
                                data = Uri.parse("https://wa.me/?text=$encoded")
                            }
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Share, null, tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F1F1F))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (canSend) {
                    Button(
                        onClick = { viewModel.updateStatus("SENT") },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Mark as SENT", fontWeight = FontWeight.Bold)
                    }
                }
                if (canReceive) {
                    Button(
                        onClick = { onNavigateToCreateGrn(order.poId) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Inventory, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Receive Goods", fontWeight = FontWeight.Bold)
                    }
                }
                // FIX (2026-08-22, DEF-03): CANCELLED was unreachable — no UI
                // path ever set it, so a mis-created PO stayed in the list
                // forever. Cancel is allowed while the PO is not yet received.
                if (order.status != "RECEIVED" && order.status != "CANCELLED") {
                    OutlinedButton(
                        onClick = {
                            viewModel.updateStatus("CANCELLED")
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF44336)),
                        border = BorderStroke(1.dp, Color(0xFFF44336)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cancel PO", fontWeight = FontWeight.Bold)
                    }
                }
                if (!canSend && !canReceive) {
                    Box(
                        Modifier.fillMaxWidth().height(48.dp)
                            .background(Color(0xFF2A2A2A), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("PO ${order.status}", color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = Color(0xFF1A1A1A)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── PO Header Card ────────────────────────────────────────────────
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
                            Column {
                                Text("Vendor", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                                Text(order.vendorName, color = Color.White,
                                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            POStatusBadge(order.status)
                        }
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            LabelValue("Created", dateStr)
                            if (order.expectedDeliveryDate.isNotEmpty())
                                LabelValue("Expected", order.expectedDeliveryDate)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            LabelValue("Currency", order.currency)
                            LabelValue("Items", "${items.size}")
                        }
                        if (order.notes.isNotEmpty()) {
                            Divider(color = Color.White.copy(alpha = 0.08f))
                            Text(order.notes, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                        }
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Amount", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                            Text("$currencySymbol ${String.format("%,.0f", order.totalAmount)}",
                                color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }

            // ── Items Section ─────────────────────────────────────────────────
            item {
                Text("Order Items (${items.size})",
                    color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 4.dp))
            }
            items(items, key = { it.poItemId }) { item ->
                POItemCard(item, currencySymbol)
            }

            // ── Linked GRNs ───────────────────────────────────────────────────
            if (linkedGrns.isNotEmpty()) {
                item {
                    Text("Linked Receipts (${linkedGrns.size})",
                        color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp))
                }
                items(linkedGrns, key = { it.grnId }) { grn ->
                    LinkedGrnCard(grn, currencySymbol, onClick = { onNavigateToGrnDetail(grn.grnId) })
                }
            }
        }
    }
}

@Composable
private fun POStatusBadge(status: String) {
    val (bg, fg) = when (status) {
        "DRAFT"              -> Color(0xFF9E9E9E).copy(alpha=0.12f) to Color(0xFF9E9E9E)
        "SENT"               -> Color(0xFF1E88E5).copy(alpha=0.12f) to Color(0xFF1E88E5)
        "PARTIALLY_RECEIVED" -> Color(0xFFFF9800).copy(alpha=0.12f) to Color(0xFFFF9800)
        "RECEIVED"           -> Color(0xFF4CAF50).copy(alpha=0.12f) to Color(0xFF4CAF50)
        else                 -> Color(0xFF9E9E9E).copy(alpha=0.12f) to Color(0xFF9E9E9E)
    }
    Box(
        Modifier.background(bg, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(status.replace("_", " "), color = fg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun POItemCard(item: PurchaseOrderItemEntity, currencySymbol: String = "$") {
    val receivedPct = if (item.orderedQty > 0) (item.receivedQty / item.orderedQty).coerceIn(0.0, 1.0) else 0.0
    val progressColor = when {
        receivedPct >= 1.0 -> Color(0xFF4CAF50)
        receivedPct > 0 -> Color(0xFFFF9800)
        else -> Color(0xFF9E9E9E)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF242424)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.productName, color = Color.White, fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text("$currencySymbol ${String.format("%,.0f", item.totalCost)}",
                    color = Color(0xFF1E88E5), fontWeight = FontWeight.Medium, fontSize = 13.sp)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Ordered: ${item.orderedQty} ${item.unit}",
                    color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
                Text("Recv'd: ${item.receivedQty} ${item.unit}",
                    color = progressColor, fontSize = 12.sp)
            }
            // Progress bar
            Box(Modifier.fillMaxWidth().height(3.dp).background(Color.White.copy(alpha=0.08f), RoundedCornerShape(2.dp))) {
                Box(Modifier.fillMaxWidth(receivedPct.toFloat()).height(3.dp)
                    .background(progressColor, RoundedCornerShape(2.dp)))
            }
            if (item.sku.isNotEmpty())
                Text("SKU: ${item.sku}", color = Color.White.copy(alpha=0.3f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun LinkedGrnCard(grn: GrnHeaderEntity, currencySymbol: String = "$", onClick: () -> Unit) {
    val statusColor = if (grn.status == "CONFIRMED") Color(0xFF4CAF50) else Color(0xFFFF9800)
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(grn.createdAt))
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF242424)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = statusColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(grn.grnNumber, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("$currencySymbol ${String.format("%,.0f", grn.totalAmount)} · $dateStr",
                    color = Color.White.copy(alpha=0.5f), fontSize = 12.sp)
            }
            Text(grn.status, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha=0.5f))
        }
    }
}
