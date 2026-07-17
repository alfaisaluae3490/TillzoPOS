package com.tillzo.pos.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.tillzo.pos.data.local.dao.ProductUnitDao
import com.tillzo.pos.data.local.entity.ProductUnitEntity
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

// Default unit seed list
private val DEFAULT_UNITS = listOf(
    "Piece" to "PC",
    "Kilogram" to "KG",
    "Gram" to "G",
    "Liter" to "L",
    "Milliliter" to "ML",
    "Box" to "BOX",
    "Dozen" to "DZ"
)

// ── ViewModel ──────────────────────────────────────────────────────────────────

@HiltViewModel
class ProductUnitsViewModel @Inject constructor(
    private val productUnitDao: ProductUnitDao
) : ViewModel() {

    val units: StateFlow<List<ProductUnitEntity>> = productUnitDao.getAllUnits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errorChannel = Channel<String>(Channel.BUFFERED)
    val errorChannel: Flow<String> = _errorChannel.receiveAsFlow()

    init {
        // Seed defaults on first launch
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (productUnitDao.getActiveCount() == 0) {
                    val now = System.currentTimeMillis()
                    productUnitDao.insertAll(
                        DEFAULT_UNITS.map { (name, abbr) ->
                            ProductUnitEntity(
                                unitId = UUID.randomUUID().toString(),
                                unitName = name, abbreviation = abbr,
                                syncStatus = "synced", // seed defaults are never pushed to Sheets
                                createdAt = now, updatedAt = now
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _errorChannel.send(e.localizedMessage ?: "Failed to seed default units")
            }
        }
    }

    fun addUnit(name: String, abbreviation: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                productUnitDao.insertUnit(
                    ProductUnitEntity(
                        unitId = UUID.randomUUID().toString(),
                        unitName = name.trim(), abbreviation = abbreviation.trim().uppercase(),
                        syncStatus = "pending", // user-created units picked up by SyncWorker
                        createdAt = now, updatedAt = now
                    )
                )
            } catch (e: Exception) {
                _errorChannel.send(e.localizedMessage ?: "Failed to add unit")
            }
        }
    }

    fun deleteUnit(unitId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                productUnitDao.softDelete(unitId)
            } catch (e: Exception) {
                _errorChannel.send(e.localizedMessage ?: "Failed to delete unit")
            }
        }
    }
}

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductUnitsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProductUnitsViewModel = hiltViewModel()
) {
    val units by viewModel.units.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.errorChannel.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AddUnitDialog(
            onDismiss = { showDialog = false },
            onSave = { name, abbr -> viewModel.addUnit(name, abbr); showDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Units of Measure", color = Color.White, fontWeight = FontWeight.Bold) },
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
                onClick = { showDialog = true },
                containerColor = Color(0xFF1E88E5), contentColor = Color.White
            ) { Icon(Icons.Default.Add, "Add Unit") }
        },
        containerColor = Color(0xFF1A1A1A)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(units, key = { it.unitId }) { unit ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp)
                                .background(Color(0xFF1E88E5).copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(unit.abbreviation, color = Color(0xFF1E88E5),
                                fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(unit.unitName, color = Color.White, fontSize = 15.sp,
                            fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        // Prevent deletion of the 7 default units by checking abbreviation
                        if (!DEFAULT_UNITS.any { it.second == unit.abbreviation }) {
                            IconButton(onClick = { viewModel.deleteUnit(unit.unitId) }) {
                                Icon(Icons.Default.Delete, null,
                                    tint = Color(0xFFF44336).copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp))
                            }
                        } else {
                            Box(Modifier.size(48.dp)) // spacer alignment
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddUnitDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var abbr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2A2A2A),
        title = { Text("Add Custom Unit", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = Color(0xFF1E88E5),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color(0xFF1E88E5)
                )
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Unit Name (e.g. Packet)") },
                    modifier = Modifier.fillMaxWidth(), colors = colors)
                OutlinedTextField(value = abbr, onValueChange = { abbr = it.uppercase() },
                    label = { Text("Abbreviation (e.g. PKT)") },
                    modifier = Modifier.fillMaxWidth(), colors = colors)
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && abbr.isNotBlank()) onSave(name, abbr) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White.copy(alpha=0.6f)) }
        }
    )
}
