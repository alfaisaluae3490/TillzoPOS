package com.tillzo.pos.ui.inventory.options.alerts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.ui.theme.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAlertsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToInventory: () -> Unit = {},
    viewModel: LowStockViewModel = hiltViewModel()
) {
    val lowStock   by viewModel.lowStockItems.collectAsState()
    val outOfStock by viewModel.outOfStockItems.collectAsState()
    val nearExpiry by viewModel.nearExpiryItems.collectAsState()
    val expired    by viewModel.expiredItems.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Low Stock (${lowStock.size})", "Out of Stock (${outOfStock.size})", "Expiring (${nearExpiry.size + expired.size})")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Alerts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 13.sp) }
                    )
                }
            }

            when (selectedTab) {
                0 -> AlertItemList(
                    items = lowStock,
                    emptyMessage = "No low stock items 🎉",
                    badgeColor = Color(0xFFFFC107),
                    badgeLabel = "LOW",
                    onTap = onNavigateToInventory
                )
                1 -> AlertItemList(
                    items = outOfStock,
                    emptyMessage = "No out-of-stock items 🎉",
                    badgeColor = Color(0xFFE53935),
                    badgeLabel = "OUT",
                    onTap = onNavigateToInventory
                )
                2 -> ExpiryTabContent(
                    nearExpiry = nearExpiry,
                    expired = expired,
                    onTap = onNavigateToInventory
                )
            }
        }
    }
}

@Composable
private fun AlertItemList(
    items: List<InventoryEntity>,
    emptyMessage: String,
    badgeColor: Color,
    badgeLabel: String,
    onTap: () -> Unit
) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(emptyMessage, color = TextSecondary, fontSize = 16.sp)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.system_row_id }) { item ->
            AlertItemCard(
                item = item,
                badgeColor = badgeColor,
                badgeLabel = badgeLabel,
                onTap = onTap
            )
        }
    }
}

@Composable
private fun AlertItemCard(
    item: InventoryEntity,
    badgeColor: Color,
    badgeLabel: String,
    onTap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Inventory, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.item_name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                Text("${item.category}  •  ${item.sku}", fontSize = 12.sp, color = TextSecondary)
                Text("Stock: ${item.current_stock} ${item.unit}  |  Threshold: ${item.low_stock_threshold}", fontSize = 12.sp, color = TextSecondary)
            }
            Surface(color = badgeColor.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small) {
                Text(badgeLabel, color = badgeColor, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun ExpiryTabContent(
    nearExpiry: List<InventoryEntity>,
    expired: List<InventoryEntity>,
    onTap: () -> Unit
) {
    val combined = (expired.map { it to true }) + (nearExpiry.filter { n -> expired.none { e -> e.system_row_id == n.system_row_id } }.map { it to false })

    if (combined.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No expiry alerts 🎉", color = TextSecondary, fontSize = 16.sp)
        }
        return
    }

    val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(combined, key = { it.first.system_row_id }) { (item, isExpired) ->
            val daysLeft = if (item.expiry_date.isNotBlank()) {
                try { ChronoUnit.DAYS.between(today, LocalDate.parse(item.expiry_date, dtf)).toInt() } catch (e: Exception) { 99 }
            } else 99

            val badgeColor = if (isExpired) Color(0xFFE53935) else Color(0xFFFF6F00)
            val badgeLabel = if (isExpired) "EXPIRED" else "$daysLeft days"

            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onTap),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = badgeColor, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.item_name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                        Text("Expires: ${item.expiry_date}", fontSize = 12.sp, color = TextSecondary)
                        Text("Stock: ${item.current_stock} ${item.unit}", fontSize = 12.sp, color = TextSecondary)
                    }
                    Surface(color = badgeColor.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small) {
                        Text(badgeLabel, color = badgeColor, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}
