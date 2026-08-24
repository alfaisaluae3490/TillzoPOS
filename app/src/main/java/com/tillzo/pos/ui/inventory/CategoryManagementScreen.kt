package com.tillzo.pos.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.widget.Toast
import java.util.UUID
import javax.inject.Inject

// ── ViewModel ──────────────────────────────────────────────────────────────────

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val categoryDao: CategoryDao
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errorChannel = Channel<String>(Channel.BUFFERED)
    val errorChannel: Flow<String> = _errorChannel.receiveAsFlow()

    fun save(existing: CategoryEntity?, name: String, parentCategoryId: String?, posTerminalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                if (existing == null) {
                    categoryDao.insertCategory(
                        CategoryEntity(
                            system_row_id = UUID.randomUUID().toString(),
                            category_name = name.trim(),
                            parent_category_id = parentCategoryId?.takeIf { it.isNotBlank() },
                            pos_terminal_id = posTerminalId,
                            sync_status = "pending",
                            created_at = now, updated_at = now
                        )
                    )
                } else {
                    categoryDao.updateCategory(
                        existing.copy(
                            category_name = name.trim(),
                            parent_category_id = parentCategoryId?.takeIf { it.isNotBlank() },
                            sync_status = "pending",
                            updated_at = now
                        )
                    )
                }
            } catch (e: Exception) {
                _errorChannel.send(e.localizedMessage ?: "Failed to save category")
            }
        }
    }

    fun delete(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Cascade soft-delete: first delete child subcategories
                val allCategories = categoryDao.getAllCategories().first()
                val children = allCategories.filter { it.parent_category_id == categoryId }
                val now = System.currentTimeMillis()
                for (child in children) {
                    categoryDao.deleteCategory(child.system_row_id, now)
                }
                // Then delete the main category itself
                categoryDao.deleteCategory(categoryId, now)
            } catch (e: Exception) {
                _errorChannel.send(e.localizedMessage ?: "Failed to delete category")
            }
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
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.errorChannel.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var preselectedParentId by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<CategoryEntity?>(null) }

    // Separate main categories and subcategories
    val mainCategories = remember(categories) { categories.filter { it.parent_category_id == null } }

    // Build a map of parent_id -> children for quick lookup
    val childrenMap = remember(categories) { categories.groupBy { it.parent_category_id } }

    // Delete confirm dialog
    showDeleteConfirm?.let { cat ->
        val hasChildren = childrenMap[cat.system_row_id]?.isNotEmpty() == true
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            containerColor = Color(0xFF2A2A2A),
            title = { Text("Delete Category?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (hasChildren) {
                        "\"${cat.category_name}\" and all its subcategories will be removed."
                    } else {
                        "\"${cat.category_name}\" will be removed."
                    },
                    color = Color.White.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.delete(cat.system_row_id); showDeleteConfirm = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    // Add/edit dialog
    if (showDialog) {
        CategoryFormDialog(
            existing = editingCategory,
            mainCategories = mainCategories,
            preselectedParentId = preselectedParentId,
            onDismiss = { showDialog = false; editingCategory = null; preselectedParentId = null },
            onSave = { name, parentCategoryId ->
                viewModel.save(editingCategory, name, parentCategoryId, posTerminalId)
                showDialog = false; editingCategory = null; preselectedParentId = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingCategory = null; preselectedParentId = null; showDialog = true },
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
                    Text("No categories yet", color = Color.White.copy(alpha = 0.4f), fontSize = 16.sp)
                    Text("Tap + to add your first category",
                        color = Color.White.copy(alpha = 0.25f), fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Render main categories first, each followed by its subcategories
                items(mainCategories, key = { it.system_row_id }) { cat ->
                    CategoryCard(
                        category = cat,
                        isSubcategory = false,
                        onEdit = { editingCategory = cat; showDialog = true },
                        onDelete = { showDeleteConfirm = cat },
                        onAddSubcategory = { 
                            editingCategory = null
                            preselectedParentId = cat.system_row_id
                            showDialog = true 
                        }
                    )
                    // Render subcategories for this parent
                    val subs = childrenMap[cat.system_row_id] ?: emptyList()
                    subs.forEach { sub ->
                        CategoryCard(
                            category = sub,
                            isSubcategory = true,
                            onEdit = { editingCategory = sub; showDialog = true },
                            onDelete = { showDeleteConfirm = sub },
                            onAddSubcategory = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: CategoryEntity,
    isSubcategory: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddSubcategory: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isSubcategory) Modifier.padding(start = 32.dp) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = if (isSubcategory) Color(0xFF242424) else Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSubcategory) {
                    Icon(Icons.Default.SubdirectoryArrowRight, null,
                        tint = Color(0xFF757575), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Icon(
                    if (isSubcategory) Icons.Default.FiberManualRecord else Icons.AutoMirrored.Filled.Label,
                    null,
                    tint = if (isSubcategory) Color(0xFF757575) else Color(0xFF1E88E5),
                    modifier = Modifier.size(if (isSubcategory) 10.dp else 20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    category.category_name,
                    color = if (isSubcategory) Color.White.copy(alpha = 0.85f) else Color.White,
                    fontWeight = if (isSubcategory) FontWeight.Normal else FontWeight.Medium,
                    fontSize = if (isSubcategory) 14.sp else 15.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null,
                        tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null,
                        tint = Color(0xFFF44336).copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
            if (onAddSubcategory != null) {
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = onAddSubcategory,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF1E88E5))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Subcategory", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFormDialog(
    existing: CategoryEntity?,
    mainCategories: List<CategoryEntity>,
    preselectedParentId: String?,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var name by remember(existing) { mutableStateOf(existing?.category_name ?: "") }
    var selectedParentId by remember(existing, preselectedParentId) {
        mutableStateOf(existing?.parent_category_id ?: preselectedParentId ?: "")
    }
    var parentDropdownExpanded by remember { mutableStateOf(false) }

    // Filter out the current category when editing to prevent self-parenting
    val availableParents = remember(existing, mainCategories) {
        mainCategories.filter { it.system_row_id != existing?.system_row_id }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2A2A2A),
        title = {
            Text(
                if (existing == null) {
                    if (preselectedParentId != null) "Add Subcategory" else "Add Main Category"
                } else {
                    "Edit Category"
                },
                color = Color.White, fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

                // Parent Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = parentDropdownExpanded,
                    onExpandedChange = { parentDropdownExpanded = !parentDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = if (selectedParentId.isEmpty()) "None (Main Category)"
                               else availableParents.firstOrNull { it.system_row_id == selectedParentId }?.category_name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Parent Category") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF1E88E5),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFF1E88E5),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                            cursorColor = Color(0xFF1E88E5)
                        ),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = parentDropdownExpanded,
                        onDismissRequest = { parentDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (Main Category)") },
                            onClick = {
                                selectedParentId = ""
                                parentDropdownExpanded = false
                            }
                        )
                        availableParents.forEach { parent ->
                            DropdownMenuItem(
                                text = { Text(parent.category_name) },
                                onClick = {
                                    selectedParentId = parent.system_row_id
                                    parentDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, selectedParentId.takeIf { it.isNotEmpty() })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}
