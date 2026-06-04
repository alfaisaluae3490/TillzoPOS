package com.tillzo.pos.ui.inventory.module_b

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.dao.VendorDao
import com.tillzo.pos.data.local.entity.VendorEntity
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
class VendorManagementViewModel @Inject constructor(
    private val vendorDao: VendorDao
) : ViewModel() {

    val vendors: StateFlow<List<VendorEntity>> = vendorDao.getAllVendors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchResults = MutableStateFlow<List<VendorEntity>>(emptyList())
    val searchResults: StateFlow<List<VendorEntity>> = _searchResults.asStateFlow()

    fun search(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _searchResults.value = if (query.isBlank()) emptyList()
            else vendorDao.searchVendors(query)
        }
    }

    fun save(
        existing: VendorEntity?,
        name: String, phone: String, whatsapp: String, email: String, address: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            if (existing == null) {
                vendorDao.insertVendor(
                    VendorEntity(
                        vendorId = UUID.randomUUID().toString(),
                        name = name.trim(), phone = phone.trim(),
                        whatsapp = whatsapp.trim(), email = email.trim(),
                        address = address.trim(), syncStatus = "pending",
                        createdAt = now, updatedAt = now
                    )
                )
            } else {
                vendorDao.updateVendor(
                    existing.copy(
                        name = name.trim(), phone = phone.trim(),
                        whatsapp = whatsapp.trim(), email = email.trim(),
                        address = address.trim(), syncStatus = "pending",
                        updatedAt = now
                    )
                )
            }
        }
    }
}

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: VendorManagementViewModel = hiltViewModel()
) {
    val vendors by viewModel.vendors.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var editingVendor by remember { mutableStateOf<VendorEntity?>(null) }

    val displayList = if (searchQuery.isBlank()) vendors else searchResults

    if (showDialog) {
        VendorFormDialog(
            existing = editingVendor,
            onDismiss = { showDialog = false; editingVendor = null },
            onSave = { name, phone, wa, email, addr ->
                viewModel.save(editingVendor, name, phone, wa, email, addr)
                showDialog = false; editingVendor = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendors", color = Color.White, fontWeight = FontWeight.Bold) },
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
                onClick = { editingVendor = null; showDialog = true },
                containerColor = Color(0xFF1E88E5), contentColor = Color.White
            ) { Icon(Icons.Default.Add, "Add Vendor") }
        },
        containerColor = Color(0xFF1A1A1A)
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it; viewModel.search(it) },
                label = { Text("Search vendors...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF1E88E5)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                colors = outlinedTextFieldColors()
            )

            if (displayList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.StoreMallDirectory, null,
                            tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No vendors yet", color = Color.White.copy(alpha=0.4f), fontSize = 16.sp)
                        Text("Tap + to add your first vendor",
                            color = Color.White.copy(alpha=0.25f), fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayList, key = { it.vendorId }) { vendor ->
                        VendorCard(
                            vendor = vendor,
                            onEdit = { editingVendor = vendor; showDialog = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorCard(vendor: VendorEntity, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(44.dp)
                    .background(Color(0xFF1E88E5).copy(alpha = 0.15f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    vendor.name.first().uppercaseChar().toString(),
                    color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold, fontSize = 18.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(vendor.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                if (vendor.phone.isNotEmpty())
                    Text(vendor.phone, color = Color.White.copy(alpha=0.5f), fontSize = 13.sp)
                if (vendor.address.isNotEmpty())
                    Text(vendor.address, color = Color.White.copy(alpha=0.35f), fontSize = 12.sp,
                        maxLines = 1)
            }
            Icon(Icons.Default.Edit, null, tint = Color(0xFF1E88E5).copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun VendorFormDialog(
    existing: VendorEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name    by remember(existing) { mutableStateOf(existing?.name    ?: "") }
    var phone   by remember(existing) { mutableStateOf(existing?.phone   ?: "") }
    var whatsapp by remember(existing) { mutableStateOf(existing?.whatsapp ?: "") }
    var email   by remember(existing) { mutableStateOf(existing?.email   ?: "") }
    var address by remember(existing) { mutableStateOf(existing?.address ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2A2A2A),
        title = {
            Text(
                if (existing == null) "Add Vendor" else "Edit Vendor",
                color = Color.White, fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Name *") }, modifier = Modifier.fillMaxWidth(),
                    colors = outlinedTextFieldColors())
                OutlinedTextField(value = phone, onValueChange = { phone = it },
                    label = { Text("Phone *") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = outlinedTextFieldColors())
                OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = outlinedTextFieldColors())
                OutlinedTextField(value = email, onValueChange = { email = it },
                    label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                    colors = outlinedTextFieldColors())
                OutlinedTextField(value = address, onValueChange = { address = it },
                    label = { Text("Address") }, modifier = Modifier.fillMaxWidth(),
                    minLines = 2, colors = outlinedTextFieldColors())
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && phone.isNotBlank()) onSave(name, phone, whatsapp, email, address) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White.copy(alpha = 0.6f)) }
        }
    )
}
