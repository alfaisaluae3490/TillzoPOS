package com.tillzo.pos.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tillzo.pos.data.local.dao.*
import com.tillzo.pos.data.local.entity.*
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.domain.model.CartItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max

enum class AnalyticsTimeFilter(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    CUSTOM("Custom")
}

data class CategorySalesMetric(
    val categoryName: String,
    val revenue: Double,
    val profit: Double,
    val unitsSold: Double,
    val percentageOfTotal: Double
)

data class ProductSalesMetric(
    val itemId: String,
    val itemName: String,
    val category: String,
    val unitsSold: Double,
    val revenue: Double,
    val profit: Double,
    val profitMarginPercent: Double
)

data class TrendDataPoint(
    val label: String,
    val sales: Double,
    val profit: Double,
    val orderCount: Int
)

data class ExpenseCategoryMetric(
    val category: String,
    val totalAmount: Double,
    val percentage: Double
)

data class CustomerLoyaltyMetric(
    val totalCustomersServed: Int,
    val repeatCustomersCount: Int,
    val newCustomersCount: Int,
    val repeatRatePercent: Double,
    val topCustomers: List<CustomerEntity>
)

data class AnalyticsDashboardState(
    val isLoading: Boolean = true,
    val timeFilter: AnalyticsTimeFilter = AnalyticsTimeFilter.TODAY,
    val startDateMillis: Long = 0L,
    val endDateMillis: Long = 0L,
    val formattedDateRange: String = "",

    // ── 1. Core Financial & P&L ─────────────────────────────────────────────
    val grossSales: Double = 0.0,
    val totalOrders: Int = 0,
    val costOfGoodsSold: Double = 0.0,
    val grossProfit: Double = 0.0,
    val grossMarginPercent: Double = 0.0,
    val operatingExpenses: Double = 0.0,
    val wastageLosses: Double = 0.0,
    val netProfit: Double = 0.0,
    val netMarginPercent: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val isProfitable: Boolean = true,

    // ── 2. Tax Analytics ───────────────────────────────────────────────────
    val totalTaxCollected: Double = 0.0,
    val totalDiscountsGiven: Double = 0.0,
    val effectiveTaxRate: Double = 0.0,

    // ── 3. Tender & Cashflow ───────────────────────────────────────────────
    val cashSales: Double = 0.0,
    val cardSales: Double = 0.0,
    val walletSales: Double = 0.0,
    val udhaarSales: Double = 0.0,

    // ── 4. Inventory Health & Valuation ────────────────────────────────────
    val totalInventoryCostValue: Double = 0.0,
    val totalInventoryRetailValue: Double = 0.0,
    val potentialStoreProfit: Double = 0.0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val totalPurchasesAmount: Double = 0.0, // GRN Purchases in period

    // ── 5. Detailed Rankings & Charts ──────────────────────────────────────
    val topCategories: List<CategorySalesMetric> = emptyList(),
    val topSellingProducts: List<ProductSalesMetric> = emptyList(),
    val slowMovingProducts: List<ProductSalesMetric> = emptyList(),
    val expenseBreakdown: List<ExpenseCategoryMetric> = emptyList(),
    val customerLoyalty: CustomerLoyaltyMetric = CustomerLoyaltyMetric(0, 0, 0, 0.0, emptyList()),
    val trendData: List<TrendDataPoint> = emptyList()
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val saleDao: SaleDao,
    private val inventoryDao: InventoryDao,
    private val expenseDao: ExpenseDao,
    private val wastageDao: WastageDao,
    private val grnDao: GrnDao,
    private val customerDao: CustomerDao,
    val appSetupPrefs: AppSetupPrefs
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(AnalyticsTimeFilter.TODAY)
    val selectedFilter: StateFlow<AnalyticsTimeFilter> = _selectedFilter.asStateFlow()

    private val _customStart = MutableStateFlow<Long?>(null)
    private val _customEnd = MutableStateFlow<Long?>(null)

    private val _dashboardState = MutableStateFlow(AnalyticsDashboardState())
    val dashboardState: StateFlow<AnalyticsDashboardState> = _dashboardState.asStateFlow()

    private val gson = Gson()
    private val cartListType = object : TypeToken<List<CartItem>>() {}.type

    init {
        // Observe all data reactively
        combine(
            _selectedFilter,
            _customStart,
            _customEnd,
            inventoryDao.getAllItems(),
            customerDao.getAllCustomers()
        ) { filter, customStart, customEnd, allInventory, allCustomers ->
            computeDateRangeAndAnalyze(filter, customStart, customEnd, allInventory, allCustomers)
        }.launchIn(viewModelScope)
    }

    fun setTimeFilter(filter: AnalyticsTimeFilter) {
        _selectedFilter.value = filter
    }

    fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        _customStart.value = startMillis
        _customEnd.value = endMillis
        _selectedFilter.value = AnalyticsTimeFilter.CUSTOM
    }

    private fun computeDateRangeAndAnalyze(
        filter: AnalyticsTimeFilter,
        customStart: Long?,
        customEnd: Long?,
        allInventory: List<InventoryEntity>,
        allCustomers: List<CustomerEntity>
    ) {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isLoading = true) }

            val (startMillis, endMillis, formattedRange) = getDateBounds(filter, customStart, customEnd)

            // 1. Fetch Sales in range
            saleDao.getSalesInRange(startMillis, endMillis).collectLatest { salesList ->
                // 2. Fetch Expenses in range
                expenseDao.getExpensesBetweenDates(startMillis, endMillis).collectLatest { expensesList ->
                    // 3. Fetch Wastage
                    wastageDao.getAllWastage().collectLatest { allWastageList ->
                        val wastageInRange = allWastageList.filter { it.createdAt in startMillis..endMillis }

                        // 4. Fetch GRNs (Purchases)
                        grnDao.getAllGrns().collectLatest { allGrns ->
                            val grnsInRange = allGrns.filter { it.createdAt in startMillis..endMillis }

                            // Perform complete aggregations
                            val computedState = aggregateAnalytics(
                                filter = filter,
                                startMillis = startMillis,
                                endMillis = endMillis,
                                formattedRange = formattedRange,
                                sales = salesList,
                                expenses = expensesList,
                                wastage = wastageInRange,
                                grns = grnsInRange,
                                inventory = allInventory,
                                customers = allCustomers
                            )
                            _dashboardState.value = computedState
                        }
                    }
                }
            }
        }
    }

    private fun aggregateAnalytics(
        filter: AnalyticsTimeFilter,
        startMillis: Long,
        endMillis: Long,
        formattedRange: String,
        sales: List<SaleEntity>,
        expenses: List<ExpenseEntity>,
        wastage: List<WastageEntity>,
        grns: List<GrnHeaderEntity>,
        inventory: List<InventoryEntity>,
        customers: List<CustomerEntity>
    ): AnalyticsDashboardState {
        val inventoryMap = inventory.associateBy { it.system_row_id }
        val inventoryByName = inventory.associateBy { it.item_name.lowercase().trim() }

        var totalGrossSales = 0.0
        var totalTax = 0.0
        var totalDiscounts = 0.0
        var totalCogs = 0.0
        var cashTotal = 0.0
        var cardTotal = 0.0
        var walletTotal = 0.0
        var udhaarTotal = 0.0

        val categoryRevenueMap = mutableMapOf<String, Double>()
        val categoryProfitMap = mutableMapOf<String, Double>()
        val categoryUnitsMap = mutableMapOf<String, Double>()

        val productSalesMap = mutableMapOf<String, ProductSalesTracker>()
        val customerVisitCounts = mutableMapOf<String, Int>()

        sales.forEach { sale ->
            totalGrossSales += sale.total
            totalTax += sale.tax
            totalDiscounts += sale.discount

            cashTotal += sale.cash_amount
            cardTotal += sale.card_amount
            walletTotal += sale.wallet_amount
            udhaarTotal += sale.udhaar_amount

            if (!sale.customer_id.isNullOrBlank()) {
                customerVisitCounts[sale.customer_id] = (customerVisitCounts[sale.customer_id] ?: 0) + 1
            }

            // Parse items
            val cartItems: List<CartItem> = try {
                if (sale.items_json.isNotBlank()) {
                    gson.fromJson(sale.items_json, cartListType) ?: emptyList()
                } else emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            cartItems.forEach { item ->
                val invItem = inventoryMap[item.itemId] ?: inventoryByName[item.name.lowercase().trim()]
                val costPrice = invItem?.cost_price ?: (item.pricePerUnit * 0.65) // reasonable estimate if cost unrecorded
                val lineCogs = item.quantity * costPrice
                val lineRevenue = item.total
                val lineProfit = lineRevenue - lineCogs
                val cat = invItem?.category?.ifBlank { "General" } ?: "General"

                totalCogs += lineCogs

                // Category tracking
                categoryRevenueMap[cat] = (categoryRevenueMap[cat] ?: 0.0) + lineRevenue
                categoryProfitMap[cat] = (categoryProfitMap[cat] ?: 0.0) + lineProfit
                categoryUnitsMap[cat] = (categoryUnitsMap[cat] ?: 0.0) + item.quantity

                // Product tracking
                val tracker = productSalesMap.getOrPut(item.itemId.ifBlank { item.name }) {
                    ProductSalesTracker(
                        itemId = item.itemId,
                        itemName = item.name,
                        category = cat
                    )
                }
                tracker.unitsSold += item.quantity
                tracker.revenue += lineRevenue
                tracker.cogs += lineCogs
            }
        }

        val grossProfit = totalGrossSales - totalCogs
        val grossMarginPercent = if (totalGrossSales > 0) (grossProfit / totalGrossSales) * 100.0 else 0.0

        val totalExpenses = expenses.sumOf { it.amount }
        val totalWastageLoss = wastage.sumOf { it.totalLoss }
        val netProfit = grossProfit - totalExpenses - totalWastageLoss
        val netMarginPercent = if (totalGrossSales > 0) (netProfit / totalGrossSales) * 100.0 else 0.0
        val aov = if (sales.isNotEmpty()) totalGrossSales / sales.size else 0.0

        val effectiveTaxRate = if (totalGrossSales > 0) (totalTax / totalGrossSales) * 100.0 else 0.0

        // Store Inventory Valuation
        val totalInvCost = inventory.sumOf { max(0.0, it.current_stock) * it.cost_price }
        val totalInvRetail = inventory.sumOf { max(0.0, it.current_stock) * it.price_per_unit }
        val potentialStoreProfit = max(0.0, totalInvRetail - totalInvCost)
        val lowStock = inventory.count { it.current_stock > 0 && it.current_stock <= it.low_stock_threshold }
        val outOfStock = inventory.count { it.current_stock <= 0 }

        // Purchases (GRN)
        val totalPurchases = grns.sumOf { it.totalAmount }

        // Categories metrics
        val topCategories = categoryRevenueMap.map { (cat, rev) ->
            CategorySalesMetric(
                categoryName = cat,
                revenue = rev,
                profit = categoryProfitMap[cat] ?: 0.0,
                unitsSold = categoryUnitsMap[cat] ?: 0.0,
                percentageOfTotal = if (totalGrossSales > 0) (rev / totalGrossSales) * 100.0 else 0.0
            )
        }.sortedByDescending { it.revenue }

        // Top & Slow moving products
        val allProductMetrics = productSalesMap.values.map { tracker ->
            val profit = tracker.revenue - tracker.cogs
            val margin = if (tracker.revenue > 0) (profit / tracker.revenue) * 100.0 else 0.0
            ProductSalesMetric(
                itemId = tracker.itemId,
                itemName = tracker.itemName,
                category = tracker.category,
                unitsSold = tracker.unitsSold,
                revenue = tracker.revenue,
                profit = profit,
                profitMarginPercent = margin
            )
        }
        val topSelling = allProductMetrics.sortedByDescending { it.revenue }.take(10)
        
        // Find items in inventory with low or 0 sales
        val soldItemIds = productSalesMap.keys.toSet()
        val slowMoving = inventory
            .filter { it.system_row_id !in soldItemIds && it.item_name !in soldItemIds }
            .take(6)
            .map { inv ->
                ProductSalesMetric(
                    itemId = inv.system_row_id,
                    itemName = inv.item_name,
                    category = inv.category.ifBlank { "General" },
                    unitsSold = 0.0,
                    revenue = 0.0,
                    profit = 0.0,
                    profitMarginPercent = 0.0
                )
            }

        // Expense categories breakdown
        val expenseCatMap = expenses.groupBy { it.category.ifBlank { "General" } }
        val expenseBreakdown = expenseCatMap.map { (cat, list) ->
            val catTotal = list.sumOf { it.amount }
            ExpenseCategoryMetric(
                category = cat,
                totalAmount = catTotal,
                percentage = if (totalExpenses > 0) (catTotal / totalExpenses) * 100.0 else 0.0
            )
        }.sortedByDescending { it.totalAmount }

        // Customer loyalty
        val totalCustomersServed = customerVisitCounts.size
        val repeatCustomersCount = customerVisitCounts.count { it.value > 1 }
        val newCustomersCount = totalCustomersServed - repeatCustomersCount
        val repeatRate = if (totalCustomersServed > 0) (repeatCustomersCount.toDouble() / totalCustomersServed) * 100.0 else 0.0

        val customerMap = customers.associateBy { it.system_row_id }
        val topLoyalCustomers = customerVisitCounts.entries
            .sortedByDescending { it.value }
            .take(5)
            .mapNotNull { customerMap[it.key] }

        val customerLoyalty = CustomerLoyaltyMetric(
            totalCustomersServed = totalCustomersServed,
            repeatCustomersCount = repeatCustomersCount,
            newCustomersCount = newCustomersCount,
            repeatRatePercent = repeatRate,
            topCustomers = topLoyalCustomers
        )

        // Trend points
        val trendData = buildTrendPoints(filter, startMillis, endMillis, sales, totalCogs)

        return AnalyticsDashboardState(
            isLoading = false,
            timeFilter = filter,
            startDateMillis = startMillis,
            endDateMillis = endMillis,
            formattedDateRange = formattedRange,
            grossSales = totalGrossSales,
            totalOrders = sales.size,
            costOfGoodsSold = totalCogs,
            grossProfit = grossProfit,
            grossMarginPercent = grossMarginPercent,
            operatingExpenses = totalExpenses,
            wastageLosses = totalWastageLoss,
            netProfit = netProfit,
            netMarginPercent = netMarginPercent,
            averageOrderValue = aov,
            isProfitable = netProfit >= 0,
            totalTaxCollected = totalTax,
            totalDiscountsGiven = totalDiscounts,
            effectiveTaxRate = effectiveTaxRate,
            cashSales = cashTotal,
            cardSales = cardTotal,
            walletSales = walletTotal,
            udhaarSales = udhaarTotal,
            totalInventoryCostValue = totalInvCost,
            totalInventoryRetailValue = totalInvRetail,
            potentialStoreProfit = potentialStoreProfit,
            lowStockCount = lowStock,
            outOfStockCount = outOfStock,
            totalPurchasesAmount = totalPurchases,
            topCategories = topCategories,
            topSellingProducts = topSelling,
            slowMovingProducts = slowMoving,
            expenseBreakdown = expenseBreakdown,
            customerLoyalty = customerLoyalty,
            trendData = trendData
        )
    }

    private fun buildTrendPoints(
        filter: AnalyticsTimeFilter,
        start: Long,
        end: Long,
        sales: List<SaleEntity>,
        totalCogs: Double
    ): List<TrendDataPoint> {
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        val hourSdf = SimpleDateFormat("ha", Locale.getDefault())

        return when (filter) {
            AnalyticsTimeFilter.TODAY, AnalyticsTimeFilter.YESTERDAY -> {
                // Group by 3-hour blocks (e.g. 9am, 12pm, 3pm, 6pm, 9pm)
                val hourlyGroups = (0..23 step 3).map { startHour ->
                    val blockStart = start + (startHour * 3600 * 1000L)
                    val blockEnd = blockStart + (3 * 3600 * 1000L)
                    val blockSales = sales.filter { it.timestamp in blockStart until blockEnd }
                    val rev = blockSales.sumOf { it.total }
                    val prof = rev * 0.35 // proportional estimate for curve
                    val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, startHour) }
                    TrendDataPoint(hourSdf.format(cal.time), rev, prof, blockSales.size)
                }
                hourlyGroups
            }
            else -> {
                // Group by day
                val cal = Calendar.getInstance().apply { timeInMillis = start }
                val points = mutableListOf<TrendDataPoint>()
                while (cal.timeInMillis <= end) {
                    val dayStart = cal.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val dayEnd = dayStart + 86400000L - 1
                    val daySales = sales.filter { it.timestamp in dayStart..dayEnd }
                    val rev = daySales.sumOf { it.total }
                    val prof = rev * 0.35
                    points.add(TrendDataPoint(sdf.format(Date(dayStart)), rev, prof, daySales.size))
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                points.takeLast(14) // max 14 bars for clean look
            }
        }
    }

    private fun getDateBounds(
        filter: AnalyticsTimeFilter,
        customStart: Long?,
        customEnd: Long?
    ): Triple<Long, Long, String> {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        return when (filter) {
            AnalyticsTimeFilter.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                val end = System.currentTimeMillis()
                Triple(start, end, "Today (${sdf.format(Date(start))})")
            }
            AnalyticsTimeFilter.YESTERDAY -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Triple(start, end, "Yesterday (${sdf.format(Date(start))})")
            }
            AnalyticsTimeFilter.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                val end = System.currentTimeMillis()
                Triple(start, end, "${sdf.format(Date(start))} - Today")
            }
            AnalyticsTimeFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                val end = System.currentTimeMillis()
                Triple(start, end, "${sdf.format(Date(start))} - Today")
            }
            AnalyticsTimeFilter.LAST_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, lastDay)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis
                Triple(start, end, "${sdf.format(Date(start))} - ${sdf.format(Date(end))}")
            }
            AnalyticsTimeFilter.CUSTOM -> {
                val start = customStart ?: (System.currentTimeMillis() - (7 * 86400000L))
                val end = customEnd ?: System.currentTimeMillis()
                Triple(start, end, "${sdf.format(Date(start))} - ${sdf.format(Date(end))}")
            }
        }
    }

    private data class ProductSalesTracker(
        val itemId: String,
        val itemName: String,
        val category: String,
        var unitsSold: Double = 0.0,
        var revenue: Double = 0.0,
        var cogs: Double = 0.0
    )
}
