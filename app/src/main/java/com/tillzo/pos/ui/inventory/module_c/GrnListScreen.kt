package com.tillzo.pos.ui.inventory.module_c

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.ui.inventory.module_c.viewmodel.GrnListViewModel
import java.text.SimpleDateFormat
import java.util.*

private fun formatGrnDate(millis: Long): String {
    return try {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
    } catch (e: Exception) { "" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrnListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToGrnDetail: (String) -> Unit = {},
    viewModel: GrnListViewModel = hiltViewModel()
) {
    val grnList by viewModel.grnList.collectAsState()
    // FIX (2026-08-06): multi-currency
    val grnContext = androidx.compose.ui.platform.LocalContext.current
    val currencySymbol = remember(grnContext) { com.tillzo.pos.data.local.prefs.AppSetupPrefs(grnContext).currencySymbol.ifBlank { "$" } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receiving History", color = Color.White, fontWeight = FontWeight.Bold) },
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
        if (grnList.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LocalShipping, null,
                        tint = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No receipts yet", color = Color.White.copy(alpha = 0.5f), fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Create a receipt from a Purchase Order",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                    ) {
                        // FIX (2026-08-06): label said "Go to Purchase Orders" but
                        // called onNavigateBack (goes Home) — renamed to match.
                        Text("Back", color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grnList, key = { it.grnId }) { grn ->
                    // FIX (2026-08-06): cards were not clickable — grn_detail route
                    // existed but was unreachable from the list.
                    GrnListCard(grn = grn, currencySymbol = currencySymbol, onClick = { onNavigateToGrnDetail(grn.grnId) })
                }
            }
        }
    }
}

@Composable
private fun GrnListCard(grn: GrnHeaderEntity, currencySymbol: String = "$", onClick: () -> Unit = {}) {
    val statusColor = if (grn.status == "CONFIRMED") Color(0xFF4CAF50) else Color(0xFFFF9800)

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(grn.grnNumber, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(grn.status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(grn.vendorName, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            if (grn.poNumber.isNotEmpty()) {
                Text("PO: ${grn.poNumber}", color = Color(0xFF1E88E5), fontSize = 12.sp)
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "$currencySymbol ${String.format("%,.0f", grn.totalAmount)}",
                    color = Color(0xFF1E88E5),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    formatGrnDate(grn.createdAt),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
