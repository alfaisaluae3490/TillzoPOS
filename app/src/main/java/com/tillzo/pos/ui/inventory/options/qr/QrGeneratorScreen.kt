package com.tillzo.pos.ui.inventory.options.qr

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.ui.theme.AccentBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrGeneratorScreen(
    barcodeId: String,
    onNavigateBack: () -> Unit,
    viewModel: QrGeneratorViewModel = hiltViewModel()
) {
    val qrBitmap by viewModel.qrBitmap.collectAsState()
    val printStatus by viewModel.printStatus.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(barcodeId) {
        viewModel.generateQrCode(barcodeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Print QR Label") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // verticalArrangement = Arrangement.Center
        ) {
            Text("Label for ID: $barcodeId", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))

            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap!!.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(250.dp)
                )
            } else {
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.printQrCode(barcodeId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Icon(Icons.Default.Print, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Print to TSPL Printer")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (printStatus.isNotEmpty()) {
                Text(
                    text = printStatus,
                    color = if (printStatus.contains("Error", ignoreCase = true)) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
