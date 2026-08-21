package com.tillzo.pos.ui.inventory.module_a

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.data.local.entity.ProductBatchEntity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit

// A simplified ViewModel or direct UseCase wrapper for the sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchListBottomSheet(
    product: InventoryEntity,
    batches: List<ProductBatchEntity>,
    onAddNewBatch: () -> Unit,
    onDismiss: () -> Unit,
    onEditBatch: (ProductBatchEntity, String, String, String, Double, Double) -> Unit
) {
    var batchToEdit by remember { mutableStateOf<ProductBatchEntity?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Product Batches",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (batches.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No batches found for this product.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(batches) { batch ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Batch: ${batch.batchNumber}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text("Stock: ${batch.stockQty}")
                                    Text("Cost: ${batch.costPrice} | Selling: ${batch.sellingPrice}")
                                    Text("Expiry: ${batch.expiryDate}")
                                }
                                // Add the "Add New Batch" action (FIX 2026-08-06)
                                Button(onClick = onAddNewBatch) { Text("+ Batch") }
                                IconButton(onClick = { batchToEdit = batch }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Batch")
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp)) // Padding for bottom
        }
    }

    batchToEdit?.let { batch ->
        EditBatchDialog(
            batch = batch,
            onSave = { batchNumber, mfgDate, expiryDate, stockQty, sellingPrice ->
                onEditBatch(batch, batchNumber, mfgDate, expiryDate, stockQty, sellingPrice)
                batchToEdit = null
            },
            onDismiss = { batchToEdit = null }
        )
    }
}

@Composable
fun EditBatchDialog(
    batch: ProductBatchEntity,
    onSave: (batchNumber: String, mfgDate: String, expiryDate: String,
             stockQty: Double, sellingPrice: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var batchNumber by remember { mutableStateOf(batch.batchNumber) }
    var mfgDate by remember { mutableStateOf(batch.manufacturingDate) }
    var expiryDate by remember { mutableStateOf(batch.expiryDate) }
    var stockQty by remember { mutableStateOf(batch.stockQty.toString()) }
    var sellingPrice by remember { mutableStateOf(batch.sellingPrice.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2A2A2A),
        title = { Text("Edit Batch", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = batchNumber,
                    onValueChange = { batchNumber = it },
                    label = { Text("Batch Number") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = mfgDate,
                    onValueChange = { mfgDate = it },
                    label = { Text("Mfg Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Expiry Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = stockQty,
                    onValueChange = { stockQty = it },
                    label = { Text("Stock Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Selling Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(batchNumber, mfgDate, expiryDate,
                           stockQty.toDoubleOrNull() ?: batch.stockQty,
                           sellingPrice.toDoubleOrNull() ?: batch.sellingPrice)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5))
            ) { Text("Save", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

// FIX (2026-08-06): AddBatchDialog — new. Powers the "+ Batch" button that
// previously had no dialog (showAddBatchDialog was an empty stub).
@Composable
fun AddBatchDialog(
    onSave: (batchNumber: String, mfgDate: String, expiryDate: String,
             stockQty: Double, costPrice: Double, sellingPrice: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var batchNumber by remember { mutableStateOf("") }
    var mfgDate by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var stockQty by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2A2A2A),
        title = { Text("Add New Batch", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = batchNumber,
                    onValueChange = { batchNumber = it },
                    label = { Text("Batch Number (optional)") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = mfgDate,
                    onValueChange = { mfgDate = it },
                    label = { Text("Mfg Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Expiry Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = stockQty,
                    onValueChange = { stockQty = it },
                    label = { Text("Stock Qty *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = costPrice,
                    onValueChange = { costPrice = it },
                    label = { Text("Cost Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Selling Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(batchNumber, mfgDate, expiryDate,
                           stockQty.toDoubleOrNull() ?: 0.0,
                           costPrice.toDoubleOrNull() ?: 0.0,
                           sellingPrice.toDoubleOrNull() ?: 0.0)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5))
            ) { Text("Save", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}
