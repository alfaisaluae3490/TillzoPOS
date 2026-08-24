package com.tillzo.pos.ui.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tillzo.pos.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.dashboardState.collectAsStateWithLifecycle()
    val currency = viewModel.appSetupPrefs.currencySymbol.ifBlank { "$" }
    val taxLabel = viewModel.appSetupPrefs.taxLabel.ifBlank { "TAX" }

    var showCustomDateDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Business Analytics",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = state.formattedDateRange,
                            color = AccentBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── 1. Time Filter Chips Bar ────────────────────────────────────
            TimeFilterBar(
                selectedFilter = state.timeFilter,
                onSelectFilter = { filter ->
                    if (filter == AnalyticsTimeFilter.CUSTOM) {
                        showCustomDateDialog = true
                    } else {
                        viewModel.setTimeFilter(filter)
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            } else {
                // ── 2. Hero Net Profit / Loss Banner ────────────────────────
                HeroProfitCard(state = state, currency = currency)

                Spacer(Modifier.height(16.dp))

                // ── 3. 4-Box Financial Breakdown Grid ───────────────────────
                FinancialMetricGrid(state = state, currency = currency)

                Spacer(Modifier.height(16.dp))

                // ── 4. Revenue & Sales Visual Trend Chart ───────────────────
                if (state.trendData.isNotEmpty()) {
                    VisualTrendChartCard(trendData = state.trendData, currency = currency)
                    Spacer(Modifier.height(16.dp))
                }

                // ── 5. Payment Tender Distribution ──────────────────────────
                PaymentMethodCard(state = state, currency = currency)

                Spacer(Modifier.height(16.dp))

                // ── 6. Category Performance ─────────────────────────────────
                CategoryPerformanceCard(categories = state.topCategories, currency = currency)

                Spacer(Modifier.height(16.dp))

                // ── 7. Top 5 Best Selling Products ──────────────────────────
                TopProductsCard(products = state.topSellingProducts, currency = currency)

                Spacer(Modifier.height(16.dp))

                // ── 8. Slow-Moving / Reorder Alert Products ──────────────────
                if (state.slowMovingProducts.isNotEmpty()) {
                    SlowMovingProductsCard(products = state.slowMovingProducts)
                    Spacer(Modifier.height(16.dp))
                }

                // ── 9. Tax & Legal Compliance Card ──────────────────────────
                TaxSummaryCard(state = state, currency = currency, taxLabel = taxLabel)

                Spacer(Modifier.height(16.dp))

                // ── 10. Store Inventory Valuation & Purchasing ──────────────
                InventoryValuationCard(state = state, currency = currency)

                Spacer(Modifier.height(16.dp))

                // ── 11. Customer Loyalty & Repeat Customers ─────────────────
                CustomerLoyaltyCard(loyalty = state.customerLoyalty)

                Spacer(Modifier.height(16.dp))

                // ── 12. Operating Expense Categories ────────────────────────
                if (state.expenseBreakdown.isNotEmpty()) {
                    ExpenseBreakdownCard(expenses = state.expenseBreakdown, currency = currency)
                    Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showCustomDateDialog) {
        CustomDateRangeDialog(
            onDismiss = { showCustomDateDialog = false },
            onApply = { startMillis, endMillis ->
                viewModel.setCustomDateRange(startMillis, endMillis)
                showCustomDateDialog = false
            }
        )
    }
}

// ── Components ─────────────────────────────────────────────────────────────────

@Composable
private fun TimeFilterBar(
    selectedFilter: AnalyticsTimeFilter,
    onSelectFilter: (AnalyticsTimeFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AnalyticsTimeFilter.values().forEach { filter ->
            val isSelected = selectedFilter == filter
            Surface(
                color = if (isSelected) AccentBlue else SurfaceVariant,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { onSelectFilter(filter) }
            ) {
                Text(
                    text = filter.label,
                    color = if (isSelected) Color.White else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroProfitCard(state: AnalyticsDashboardState, currency: String) {
    val isProfit = state.isProfitable
    val heroColor = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)
    val heroBgGradient = if (isProfit) {
        Brush.verticalGradient(listOf(Color(0xFF064E3B), Color(0xFF0F172A)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF7F1D1D), Color(0xFF0F172A)))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(heroBgGradient)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = heroColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isProfit) "NET PROFIT (SAFI MUNAFA)" else "NET LOSS (NUQSAN)",
                            color = heroColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Surface(
                        color = heroColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "%.1f%% Margin".format(state.netMarginPercent),
                            color = heroColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "$currency %.2f".format(state.netProfit),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.8.dp)
                Spacer(Modifier.height(12.dp))

                // Sub Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HeroSubStat(
                        label = "Total Sales",
                        value = "$currency %.0f".format(state.grossSales),
                        icon = Icons.Default.PointOfSale
                    )
                    HeroSubStat(
                        label = "Bills / Orders",
                        value = "${state.totalOrders}",
                        icon = Icons.Default.ReceiptLong
                    )
                    HeroSubStat(
                        label = "Avg. Bill (AOV)",
                        value = "$currency %.1f".format(state.averageOrderValue),
                        icon = Icons.Default.ShoppingBag
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroSubStat(label: String, value: String, icon: ImageVector) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, color = TextSecondary, fontSize = 11.sp)
        }
        Spacer(Modifier.height(2.dp))
        Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FinancialMetricGrid(state: AnalyticsDashboardState, currency: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Total Gross Sales",
                value = "$currency %.2f".format(state.grossSales),
                subtitle = "Total revenue earned",
                icon = Icons.Default.AttachMoney,
                color = AccentBlue,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Cost of Stock (COGS)",
                value = "$currency %.2f".format(state.costOfGoodsSold),
                subtitle = "Stock buying cost",
                icon = Icons.Default.Inventory2,
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Operating Expenses",
                value = "$currency %.2f".format(state.operatingExpenses),
                subtitle = "Rent, bills, salaries",
                icon = Icons.Default.MoneyOff,
                color = Color(0xFFEC4899),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Wastage / Spoilage",
                value = "$currency %.2f".format(state.wastageLosses),
                subtitle = "Damaged & expired loss",
                icon = Icons.Default.DeleteOutline,
                color = Color(0xFFA855F7),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(value, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun VisualTrendChartCard(trendData: List<TrendDataPoint>, currency: String) {
    val maxSales = trendData.maxOfOrNull { it.sales }?.takeIf { it > 0 } ?: 1.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BarChart, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sales & Revenue Trend", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Text("Volume Chart", color = TextSecondary, fontSize = 11.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Bars Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                trendData.forEach { point ->
                    val barHeightFraction = (point.sales / maxSales).toFloat().coerceIn(0.06f, 1.0f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Value label on top
                        if (point.sales > 0) {
                            Text(
                                text = "%.0f".format(point.sales),
                                color = AccentBlueLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(2.dp))
                        }

                        // Bar
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .fillMaxHeight(barHeightFraction)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(AccentBlue, AccentBlueDark)
                                    )
                                )
                        )

                        Spacer(Modifier.height(6.dp))

                        // Label
                        Text(
                            text = point.label,
                            color = TextSecondary,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(state: AnalyticsDashboardState, currency: String) {
    val total = state.grossSales.takeIf { it > 0 } ?: 1.0
    val cashPct = (state.cashSales / total).toFloat().coerceIn(0f, 1f)
    val cardPct = (state.cardSales / total).toFloat().coerceIn(0f, 1f)
    val walletPct = (state.walletSales / total).toFloat().coerceIn(0f, 1f)
    val udhaarPct = (state.udhaarSales / total).toFloat().coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Payment Methods Breakdown", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(14.dp))

            // Multi-segment progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(SurfaceVariant)
            ) {
                if (cashPct > 0) Box(Modifier.fillMaxHeight().weight(cashPct).background(SuccessGreen))
                if (cardPct > 0) Box(Modifier.fillMaxHeight().weight(cardPct).background(AccentBlue))
                if (walletPct > 0) Box(Modifier.fillMaxHeight().weight(walletPct).background(Color(0xFFA855F7)))
                if (udhaarPct > 0) Box(Modifier.fillMaxHeight().weight(udhaarPct).background(Color(0xFFF97316)))
            }

            Spacer(Modifier.height(14.dp))

            // Legend Rows
            TenderRow(name = "Cash Collection", amount = state.cashSales, pct = cashPct * 100, color = SuccessGreen, currency = currency)
            TenderRow(name = "Card / POS Terminal", amount = state.cardSales, pct = cardPct * 100, color = AccentBlue, currency = currency)
            TenderRow(name = "Online / Digital Wallets", amount = state.walletSales, pct = walletPct * 100, color = Color(0xFFA855F7), currency = currency)
            TenderRow(name = "Credit (Customer Khata / Udhaar)", amount = state.udhaarSales, pct = udhaarPct * 100, color = Color(0xFFF97316), currency = currency)
        }
    }
}

@Composable
private fun TenderRow(name: String, amount: Double, pct: Float, color: Color, currency: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(8.dp))
            Text(name, color = TextSecondary, fontSize = 12.sp)
        }
        Row {
            Text("$currency %.2f".format(amount), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(6.dp))
            Text("(%.0f%%)".format(pct), color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun CategoryPerformanceCard(categories: List<CategorySalesMetric>, currency: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Category, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Category Sales & Profit Breakdown", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(14.dp))

            if (categories.isEmpty()) {
                Text("No category sales recorded in this period.", color = TextSecondary, fontSize = 12.sp)
            } else {
                categories.take(6).forEach { cat ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(cat.categoryName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("$currency %.2f".format(cat.revenue), color = AccentBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("%.1f%% of total sales".format(cat.percentageOfTotal), color = TextSecondary, fontSize = 10.sp)
                            Text("Profit: $currency %.2f".format(cat.profit), color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (cat.percentageOfTotal / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                            color = AccentBlue,
                            trackColor = SurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun TopProductsCard(products: List<ProductSalesMetric>, currency: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Top Performing Products", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(14.dp))

            if (products.isEmpty()) {
                Text("No product sales recorded in this period.", color = TextSecondary, fontSize = 12.sp)
            } else {
                products.take(5).forEachIndexed { index, prod ->
                    val badgeColor = when (index) {
                        0 -> Color(0xFFFBBF24) // Gold
                        1 -> Color(0xFF94A3B8) // Silver
                        2 -> Color(0xFFD97706) // Bronze
                        else -> AccentBlue
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(badgeColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = badgeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prod.itemName,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${prod.unitsSold.toInt()} sold • ${prod.category}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$currency %.2f".format(prod.revenue),
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "+$currency %.2f (%.0f%%)".format(prod.profit, prod.profitMarginPercent),
                                color = SuccessGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (index < 4) {
                        HorizontalDivider(color = SurfaceVariant, thickness = 0.6.dp, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SlowMovingProductsCard(products: List<ProductSalesMetric>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WarningAmber, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Slow-Moving / 0-Sale Products", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text("These items had no sales in this period. Consider discounting or re-positioning.", color = TextSecondary, fontSize = 11.sp)

            Spacer(Modifier.height(10.dp))

            products.take(4).forEach { prod ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(prod.itemName, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1)
                    Surface(
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "0 Sales",
                            color = Color(0xFFF59E0B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaxSummaryCard(state: AnalyticsDashboardState, currency: String, taxLabel: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Receipt, null, tint = Color(0xFF06B6D4), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Tax & Legal Compliance Summary", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total $taxLabel Collected", color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("$currency %.2f".format(state.totalTaxCollected), color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Effective Tax Rate", color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("%.1f%%".format(state.effectiveTaxRate), color = AccentBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Discounts Given", color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("$currency %.2f".format(state.totalDiscountsGiven), color = Color(0xFFEC4899), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InventoryValuationCard(state: AnalyticsDashboardState, currency: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storefront, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Store Inventory Valuation & Purchasing", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Stock Value (Cost)", color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("$currency %.0f".format(state.totalInventoryCostValue), color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Stock Value (Retail)", color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("$currency %.0f".format(state.totalInventoryRetailValue), color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Potential Profit", color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("$currency %.0f".format(state.potentialStoreProfit), color = SuccessGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = SurfaceVariant, thickness = 0.6.dp)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Goods Purchases in period (GRN):", color = TextSecondary, fontSize = 12.sp)
                Text("$currency %.2f".format(state.totalPurchasesAmount), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CustomerLoyaltyCard(loyalty: CustomerLoyaltyMetric) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.People, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Customer Loyalty & Repeat Visits", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Customers", color = TextSecondary, fontSize = 11.sp)
                    Text("${loyalty.totalCustomersServed}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Repeat Customers", color = TextSecondary, fontSize = 11.sp)
                    Text("${loyalty.repeatCustomersCount}", color = SuccessGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Repeat Rate", color = TextSecondary, fontSize = 11.sp)
                    Text("%.1f%%".format(loyalty.repeatRatePercent), color = AccentBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ExpenseBreakdownCard(expenses: List<ExpenseCategoryMetric>, currency: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Paid, null, tint = Color(0xFFEC4899), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Expense Categories Breakdown", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            expenses.forEach { exp ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(exp.category, color = TextSecondary, fontSize = 12.sp)
                    Text("$currency %.2f (%.0f%%)".format(exp.totalAmount, exp.percentage), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDateRangeDialog(
    onDismiss: () -> Unit,
    onApply: (Long, Long) -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis ?: start
                    if (start != null && end != null) {
                        onApply(start, end + 86400000L - 1)
                    }
                }
            ) { Text("Apply Filter", color = AccentBlue, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = { Text("Select Date Range", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold) },
            headline = { Text("Filter business analytics", modifier = Modifier.padding(horizontal = 16.dp), fontSize = 12.sp) }
        )
    }
}
