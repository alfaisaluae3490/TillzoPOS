package com.tillzo.pos.ui.inventory

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.dao.CategoryDao
import com.tillzo.pos.data.local.entity.CategoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// ── ViewModel ──────────────────────────────────────────────────────────────────

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val categoryDao: CategoryDao
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(existing: CategoryEntity?, name: String, posTerminalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            if (existing == null) {
                categoryDao.insertCategory(
                    CategoryEntity(
                        system_row_id = UUID.randomUUID().toString(),
                        category_name = name.trim(),
                        pos_terminal_id = posTerminalId,
                        sync_status = "pending",
                        created_at = now, updated_at = now
                    )
                )
            } else {
                categoryDao.updateCategory(
                    existing.copy(
                        category_name = name.trim(),
                        sync_status = "pending",
                        updated_at = now
                    )
                )
            }
        }
    }

    fun delete(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.deleteCategory(categoryId)  // soft-delete
        }
    }
}

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onNavigateBack: () -> Unit,
    posTerminalId: String = "terminal_1",
    viewModel: CategoryManagementViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<CategoryEntity?>(null) }

    // Delete confirm dialog
    showDeleteConfirm?.let { cat ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            containerColor = Color(0xFF2A2A2A),
            title = { Text("Delete Category?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("\"${cat.category_name}\" will be removed.", color = Color.White.copy(alpha=0.7f)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.delete(cat.system_row_id); showDeleteConfirm = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel", color = Color.White.copy(alpha=0.6f))
                }
            }
        )
    }

    // Add/edit dialog
    if (showDialog) {
        CategoryFormDialog(
            existing = editingCategory,
            onDismiss = { showDialog = false; editingCategory = null },
            onSave = { name ->
                viewModel.save(editingCategory, name, posTerminalId)
                showDialog = false; editingCategory = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", color = Color.White, fontWeight = FontWeight.Bold) },
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
                onClick = { editingCategory = null; showDialog = true },
                containerColor = Color(0xFF1E88E5), contentColor = Color.White
            ) { Icon(Icons.Default.Add, "Add Category") }
        },
        containerColor = Color(0xFF1A1A1A)
    ) { padding ->
        if (categories.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Category, null,
                        tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No categories yet", color = Color.White.copy(alpha=0.4f), fontSize = 16.sp)
                    Text("Tap + to add your first category",
                        color = Color.White.copy(alpha=0.25f), fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key = { it.system_row_id }) { cat ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Label, null,
                                tint = Color(0xFF1E88E5), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(cat.category_name, color = Color.White,
                                fontWeight = FontWeight.Medium, fontSize = 15.sp,
                                modifier = Modifier.weight(1f))
                            IconButton(onClick = { editingCategory = cat; showDialog = true }) {
                                Icon(Icons.Default.Edit, null,
                                    tint = Color.White.copy(alpha=0.5f), modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { showDeleteConfirm = cat }) {
                                Icon(Icons.Default.Delete, null,
                                    tint = Color(0xFFF44336).copy(alpha=0.7f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFormDialog(
    existing: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember(existing) { mutableStateOf(existing?.category_name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2A2A2A),
        title = {
            Text(
                if (existing == null) "Add Category" else "Edit Category",
                color = Color.White, fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category Name *") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = Color(0xFF1E88E5),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color(0xFF1E88E5)
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White.copy(alpha=0.6f)) }
        }
    )
}
