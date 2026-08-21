package com.tillzo.pos.ui.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.UserDao
import com.tillzo.pos.data.local.entity.UserEntity
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.local.dao.SaleDao
import com.tillzo.pos.data.local.dao.ExpenseDao
import com.tillzo.pos.data.local.dao.InventoryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════
// M3.2 USER MANAGEMENT + ADMIN DASHBOARD (FIX 2026-08-06 — newly developed)
// Previously: menu items existed but "Admin Dashboard" had NO route and there
// was NO user management screen at all. Both are now implemented here.
// ═══════════════════════════════════════════════════════════════════════════

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {
    val users = MutableStateFlow<List<UserEntity>>(emptyList())
    val errorMsg = MutableStateFlow<String?>(null)

    init { refresh() }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            users.value = userDao.getAllUsers()
        }
    }

    fun addUser(email: String, name: String, role: String, onDone: () -> Unit) {
        if (email.isBlank()) { errorMsg.value = "Email required"; return }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existing = userDao.getUserByEmail(email.trim())
                if (existing != null && !existing.is_deleted) {
                    errorMsg.value = "User with this email already exists"
                    return@launch
                }
                userDao.insertUser(
                    UserEntity(
                        system_row_id = java.util.UUID.randomUUID().toString(),
                        email = email.trim(),
                        name = name.ifBlank { email.substringBefore("@") },
                        role = role,
                        password_hash = null,
                        permissions_json = null,
                        pos_terminal_id = "ADMIN"
                    )
                )
                refresh()
                withContext(Dispatchers.Main) { onDone() }
            } catch (e: Exception) {
                errorMsg.value = e.message
            }
        }
    }

    fun deleteUser(systemRowId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userDao.deleteUserById(systemRowId)
            refresh()
        }
    }

    fun setRole(systemRowId: String, role: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val u = userDao.getUserById(systemRowId) ?: return@launch
            userDao.insertUser(u.copy(role = role, updated_at = System.currentTimeMillis(), sync_status = "pending"))
            refresh()
        }
    }
}

@Composable
fun UserManagementScreen(
    onBack: () -> Unit,
    viewModel: UserManagementViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("← Back") }
            Text("User Management", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Button(onClick = { showAddDialog = true }) { Text("+ Add User") }
        }
        HorizontalDivider()

        if (users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "No users yet.\nAdd the first user (role = Admin) to enable role-based access.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(users) { user ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name, fontWeight = FontWeight.SemiBold)
                                Text(user.email, style = MaterialTheme.typography.bodySmall)
                            }
                            // Role selector
                            var roleMenu by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(onClick = { roleMenu = true }) {
                                    Text(user.role)
                                }
                                DropdownMenu(expanded = roleMenu, onDismissRequest = { roleMenu = false }) {
                                    listOf("Admin", "Manager", "Cashier").forEach { r ->
                                        DropdownMenuItem(
                                            text = { Text(r) },
                                            onClick = {
                                                roleMenu = false
                                                viewModel.setRole(user.system_row_id, r)
                                            }
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.deleteUser(user.system_row_id) }) {
                                Text("🗑", fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddUserDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { email, name, role ->
                viewModel.addUser(email, name, role) { showAddDialog = false }
            }
        )
    }
}

@Composable
private fun AddUserDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Cashier") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add User") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email *") }, singleLine = true)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Display Name") }, singleLine = true)
                var menuOpen by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { menuOpen = true }) { Text("Role: $role") }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        listOf("Admin", "Manager", "Cashier").forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = { role = r; menuOpen = false })
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onAdd(email, name, role) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// ADMIN DASHBOARD (P&L) — M3 "Admin Dashboard" menu item finally has a screen.
// Shows: today's sales, total sales, expenses, COGS proxy, net + user count.
// ═══════════════════════════════════════════════════════════════════════════

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val saleDao: SaleDao,
    private val expenseDao: ExpenseDao,
    private val inventoryDao: InventoryDao,
    private val userDao: UserDao
) : ViewModel() {
    data class Stats(
        val todaySales: Double = 0.0,
        val totalSales: Double = 0.0,
        val saleCount: Int = 0,
        val todayExpenses: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val inventoryValue: Double = 0.0,
        val userCount: Int = 0
    )
    private val _stats = MutableStateFlow(Stats())
    val stats: StateFlow<Stats> = _stats

    init { load() }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val dayStart = now - (now % 86400000L)
            // DAOs expose Flows — collect the current value once for the summary.
            val allSales = saleDao.getAllSales().firstOrNull() ?: emptyList()
            val allExpenses = expenseDao.getAllExpenses().firstOrNull() ?: emptyList()
            val items = inventoryDao.getAllItems().firstOrNull() ?: emptyList()
            _stats.value = Stats(
                todaySales = allSales.filter { it.timestamp >= dayStart }.sumOf { it.total },
                totalSales = allSales.sumOf { it.total },
                saleCount = allSales.size,
                todayExpenses = allExpenses.filter { it.timestamp >= dayStart }.sumOf { it.amount },
                totalExpenses = allExpenses.sumOf { it.amount },
                inventoryValue = items.sumOf { it.current_stock * it.cost_price },
                userCount = userDao.getAllUsers().size
            )
        }
    }
}

@Composable
fun AdminDashboardScreen(
    onBack: () -> Unit,
    onNavigateToUsers: () -> Unit,
    viewModel: AdminDashboardViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val context = LocalContext.current
    val currencySymbol = remember { AppSetupPrefs(context).currencySymbol.ifBlank { "$" } }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("← Back") }
            Text("Admin Dashboard", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Button(onClick = onNavigateToUsers) { Text("Users") }
        }
        HorizontalDivider()

        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                StatCard("Today's Sales", "$currencySymbol ${"%.2f".format(stats.todaySales)}")
                StatCard("Total Sales (All Time)", "$currencySymbol ${"%.2f".format(stats.totalSales)}", count = "${stats.saleCount} invoices")
                StatCard("Today's Expenses", "$currencySymbol ${"%.2f".format(stats.todayExpenses)}")
                StatCard("Total Expenses", "$currencySymbol ${"%.2f".format(stats.totalExpenses)}")
                StatCard("Net (Sales − Expenses)", "$currencySymbol ${"%.2f".format(stats.totalSales - stats.totalExpenses)}")
                StatCard("Inventory Value (cost)", "$currencySymbol ${"%.2f".format(stats.inventoryValue)}")
                StatCard("Registered Users", "${stats.userCount}")
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, count: String? = null) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (count != null) {
                Text(count, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
