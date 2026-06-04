package com.tillzo.pos.ui.hardware.scanner

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun InlineCameraBox(
    modifier: Modifier = Modifier,
    isCameraActive: Boolean,
    onBarcodeDetected: (String) -> Unit,
    onActivateClick: () -> Unit,
    borderColor: Color = Color(0xFF1E88E5) // animated externally
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // PreviewView — created once, reused
    val previewView = remember { PreviewView(context) }

    // Camera executor — single background thread
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // ML Kit barcode scanner instance — created once
    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_DATA_MATRIX
                )
                .build()
        )
    }

    // Bind/unbind camera when active state changes
    LaunchedEffect(isCameraActive) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        if (isCameraActive) {
            // Build preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Build analyzer — 480p ONLY for performance
            @Suppress("DEPRECATION")
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480)) // 480p — performance sweet spot
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // drop frames
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val inputImage = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )
                    barcodeScanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            barcodes.firstOrNull()?.rawValue?.let { value ->
                                onBarcodeDetected(value)
                            }
                        }
                        .addOnCompleteListener {
                            imageProxy.close() // always close to unblock analyzer
                        }
                } else {
                    imageProxy.close()
                }
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                // Camera bind failed — log silently
            }

        } else {
            // Camera sleeping — unbind to free resources
            try {
                val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                cameraProvider.unbindAll()
            } catch (e: Exception) { }
        }
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
            try {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            } catch (e: Exception) { }
        }
    }

    // Box container — always visible
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp) // compact height — above search bar
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(Color(0xFF0D0D0D))
    ) {
        if (isCameraActive) {
            // Live camera preview
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            // Scan guide reticle overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Center horizontal scan line guide
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(1.5.dp)
                        .background(Color(0xFF1E88E5).copy(alpha = 0.6f))
                )
                // Corner markers
                CornerMarkers(color = Color(0xFF1E88E5))
            }

            // "LIVE" indicator badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "● LIVE",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

        } else {
            // Sleep state — placeholder
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onActivateClick() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color(0xFF1E88E5).copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap to activate scanner",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Text(
                    text = "Camera paused to save battery",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// Corner marker helper
@Composable
fun CornerMarkers(color: Color) {
    val cornerSize = 16.dp
    val strokeWidth = 2.5.dp
    Box(modifier = Modifier.fillMaxSize()) {
        // Top-left
        Canvas(modifier = Modifier.size(cornerSize).align(Alignment.TopStart)) {
            drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth.toPx())
            drawLine(color, Offset(0f, 0f), Offset(0f, size.height), strokeWidth.toPx())
        }
        // Top-right
        Canvas(modifier = Modifier.size(cornerSize).align(Alignment.TopEnd)) {
            drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth.toPx())
            drawLine(color, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidth.toPx())
        }
        // Bottom-left
        Canvas(modifier = Modifier.size(cornerSize).align(Alignment.BottomStart)) {
            drawLine(color, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth.toPx())
            drawLine(color, Offset(0f, 0f), Offset(0f, size.height), strokeWidth.toPx())
        }
        // Bottom-right
        Canvas(modifier = Modifier.size(cornerSize).align(Alignment.BottomEnd)) {
            drawLine(color, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth.toPx())
            drawLine(color, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidth.toPx())
        }
    }
}
