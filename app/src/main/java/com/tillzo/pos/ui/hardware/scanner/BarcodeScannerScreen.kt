package com.tillzo.pos.ui.hardware.scanner

import android.Manifest
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.tillzo.pos.data.local.entity.InventoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerScreen(
    onProductScanned: (InventoryEntity) -> Unit,
    onDismiss: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scannerState by viewModel.scannerState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val scannedProduct by viewModel.scannedProduct.collectAsState()

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    var reticleColor by remember { mutableStateOf(Color(0xFF1E88E5)) }
    var torchEnabled by remember { mutableStateOf(false) }

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }

    // Toggle torch
    LaunchedEffect(torchEnabled) {
        camera?.cameraControl?.enableTorch(torchEnabled)
    }

    LaunchedEffect(scannedProduct) {
        scannedProduct?.let { product ->
            // Haptic feedback
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
            // Beep sound
            try { toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150) } catch (e: Exception) {}
            
            // Green flash on reticle
            reticleColor = Color(0xFF4CAF50)
            delay(300)
            reticleColor = Color(0xFF1E88E5)
            
            // Pass product back
            delay(200)
            onProductScanned(product)
            viewModel.clearResult()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startScanning()
    }

    LaunchedEffect(scannerState) {
        if (scannerState is ScannerState.Idle) {
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        val cameraProvider = withContext(Dispatchers.IO) { cameraProviderFuture.get() }

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        // RULE 2: Resolution locked to 720p MAX
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val barcodeScanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_DATA_MATRIX,
                    Barcode.FORMAT_PDF417
                )
                .build()
        )

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null && viewModel.scannerState.value is ScannerState.Scanning) {
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                // ML Kit handles the mediaImage, crop is conceptual to reduce processing area if implemented via Bitmap
                val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                
                barcodeScanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        barcodes.firstOrNull()?.rawValue?.let { barcodeValue ->
                            viewModel.onBarcodeDetected(barcodeValue)
                        }
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val reticleSize = 280.dp.toPx()
            val centerX = size.width / 2
            val centerY = size.height / 2 - 40.dp.toPx()

            drawRect(Color.Black.copy(alpha = 0.65f))

            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(centerX - reticleSize / 2, centerY - reticleSize / 2),
                size = androidx.compose.ui.geometry.Size(reticleSize, reticleSize),
                cornerRadius = CornerRadius(12.dp.toPx()),
                blendMode = BlendMode.Clear
            )
        }

        val reticleSize = 280.dp
        Box(
            modifier = Modifier
                .size(reticleSize)
                .align(Alignment.Center)
                .offset(y = (-40).dp)
                .border(
                    width = 3.dp,
                    color = reticleColor,
                    shape = RoundedCornerShape(12.dp)
                )
        )

        val infiniteTransition = rememberInfiniteTransition()
        val laserY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        if (scannerState is ScannerState.Scanning) {
            Box(
                modifier = Modifier
                    .width(reticleSize)
                    .height(2.dp)
                    .align(Alignment.Center)
                    .offset(y = (-40 + (-140 + 280 * laserY)).dp)
                    .background(Color(0xFF1E88E5).copy(alpha = 0.8f))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.stopScanning()
                onDismiss()
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "Scan Barcode",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(onClick = { torchEnabled = !torchEnabled }) {
                Icon(
                    imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Torch",
                    tint = if (torchEnabled) Color(0xFFFFEB3B) else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (scannerState) {
                is ScannerState.Scanning -> {
                    Text(
                        text = "Position barcode inside the frame",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Auto-detect ON",
                        color = Color(0xFF1E88E5),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                is ScannerState.Processing -> {
                    CircularProgressIndicator(
                        color = Color(0xFF1E88E5),
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Looking up product...",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                is ScannerState.Success -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Product found! Adding to cart...",
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                is ScannerState.NotFound -> {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Product not found in inventory",
                        color = Color(0xFFF44336),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Try scanning again",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
                else -> {}
            }
        }

        errorMessage?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = Color(0xFFF44336),
                contentColor = Color.White
            ) {
                Text(text = msg)
            }
        }
    }
}
