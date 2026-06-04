package com.tillzo.pos.ui.inventory.module_b

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.entity.PurchaseOrderEntity
import com.tillzo.pos.ui.inventory.module_b.viewmodel.PurchaseOrderListViewModel
import java.text.SimpleDateFormat
import java.util.*

private fun formatPoDate(millis: Long): String {
    return try {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
    } catch (e: Exception) { "" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseOrderListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreatePo: () -> Unit,
    onNavigateToPoDetail: (String) -> Unit,
    viewModel: PurchaseOrderListViewModel = hiltViewModel()
) {
    val poList by viewModel.poList.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf("All", "Draft", "Sent", "Received", "Cancelled")
    val statusFilters = listOf(null, "DRAFT", "SENT", "RECEIVED", "CANCELLED")

    val filtered = if (statusFilters[selectedTab] == null) poList
    else poList.filter { it.status == statusFilters[selectedTab] }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purchase Orders", color = Color.White, fontWeight = FontWeight.Bold) },
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
                onClick = onNavigateToCreatePo,
                containerColor = Color(0xFF1E88E5),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Create PO")
            }
        },
        containerColor = Color(0xFF1A1A1A)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF2A2A2A),
                            labelColor = Color.White.copy(alpha = 0.7f),
                            selectedContainerColor = Color(0xFF1E88E5),
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedTab == index,
                            borderColor = Color.Transparent,
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Assignment, null,
                            tint = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No Purchase Orders", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("Tap + to create your first PO", color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.poId }) { po ->
                        POListCard(po = po, onClick = { onNavigateToPoDetail(po.poId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun POListCard(po: PurchaseOrderEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(po.poNumber, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                POStatusChip(status = po.status)
            }
            Spacer(Modifier.height(6.dp))
            Text(po.vendorName, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "PKR ${String.format("%,.0f", po.totalAmount)}",
                    color = Color(0xFF1E88E5),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    formatPoDate(po.createdAt),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
            if (po.expectedDeliveryDate.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    "Expected: ${po.expectedDeliveryDate}",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun POStatusChip(status: String) {
    val (color, label) = when (status) {
        "DRAFT"               -> Pair(Color(0xFF9E9E9E), "Draft")
        "SENT"                -> Pair(Color(0xFF1E88E5), "Sent")
        "PARTIALLY_RECEIVED"  -> Pair(Color(0xFFFF9800), "Partial GRN")
        "RECEIVED"            -> Pair(Color(0xFF4CAF50), "Received")
        "CANCELLED"           -> Pair(Color(0xFFF44336), "Cancelled")
        else                  -> Pair(Color(0xFF9E9E9E), status)
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
