package com.tillzo.pos.ui.inventory.options.crud

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.utils.BarcodeGeneratorUtil
import com.tillzo.pos.utils.LabelPdfPrinter
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import com.tillzo.pos.data.local.entity.BarcodeFieldConfigEntity
import com.tillzo.pos.data.local.entity.BarcodeGeneralConfigEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodePrintSettingsScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    viewModel: InventoryCrudViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var item by remember { mutableStateOf<InventoryEntity?>(null) }
    val gtins by viewModel.getGtinsForItem(itemId).collectAsState(initial = emptyList())

    LaunchedEffect(itemId) {
        item = viewModel.getItemById(itemId)
    }

    if (item == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val customGtinVal = gtins.firstOrNull()?.gtin ?: ""
    val primaryGtin = if (customGtinVal.isNotBlank()) customGtinVal else "0000000"

    // DB config states
    val generalConfigState by viewModel.barcodeGeneralConfig.collectAsState()
    val fieldsState by viewModel.barcodeFieldsConfig.collectAsState()
    var hasInitialized by remember { mutableStateOf(false) }

    // Branding States
    var companyName by remember { mutableStateOf("Tillzo POS") }
    var companyLogoPath by remember { mutableStateOf("") }
    
    // Suffix/Prefix
    var usePrefix by remember { mutableStateOf(true) }
    var customPrefix by remember { mutableStateOf("]d2") }
    var prefixPosition by remember { mutableStateOf("0") }
    var customSuffix by remember { mutableStateOf("") }
    var useSuffix by remember { mutableStateOf(false) }
    var suffixPosition by remember { mutableStateOf("0") }
    var useSeparator by remember { mutableStateOf(true) }
    var quantity by remember { mutableStateOf("1") }
    
    // Layout & Customization States
    var titleTextSize by remember { mutableStateOf(6f) }
    var isTitleBold by remember { mutableStateOf(true) }
    var barcodeSize by remember { mutableStateOf(48f) }
    var currencySymbol by remember { mutableStateOf("Rs") }

    // Label dimensions (points)
    var labelWidth by remember { mutableStateOf("144") }
    var labelHeight by remember { mutableStateOf("72") }

    // Custom Coordinate Offset States
    var titleX by remember { mutableStateOf(4f) }
    var titleY by remember { mutableStateOf(16f) }
    var priceX by remember { mutableStateOf(4f) }
    var priceY by remember { mutableStateOf(24f) }
    var skuX by remember { mutableStateOf(4f) }
    var skuY by remember { mutableStateOf(32f) }
    var gtinX by remember { mutableStateOf(4f) }
    var gtinY by remember { mutableStateOf(40f) }
    var lotX by remember { mutableStateOf(4f) }
    var lotY by remember { mutableStateOf(48f) }
    var expX by remember { mutableStateOf(4f) }
    var expY by remember { mutableStateOf(56f) }
    var snX by remember { mutableStateOf(4f) }
    var snY by remember { mutableStateOf(66f) }
    var barcodeX by remember { mutableStateOf(92f) }
    var barcodeY by remember { mutableStateOf(12f) }

    // Custom Branding Offset States
    var companyNameSize by remember { mutableStateOf(5f) }
    var companyLogoSize by remember { mutableStateOf(8f) }
    var companyNameX by remember { mutableStateOf(16f) }
    var companyNameY by remember { mutableStateOf(8f) }
    var companyLogoX by remember { mutableStateOf(4f) }
    var companyLogoY by remember { mutableStateOf(4f) }

    // Branding Visibility Options
    var showCompanyName by remember { mutableStateOf(true) }
    var showCompanyLogo by remember { mutableStateOf(true) }

    LaunchedEffect(generalConfigState) {
        val config = generalConfigState
        if (config != null && !hasInitialized) {
            companyName = config.companyName
            companyLogoPath = config.companyLogoPath
            usePrefix = config.usePrefix
            customPrefix = config.customPrefix
            prefixPosition = config.prefixPosition.toString()
            customSuffix = config.customSuffix
            useSuffix = config.useSuffix
            suffixPosition = config.suffixPosition.toString()
            useSeparator = config.useSeparator
            titleTextSize = config.titleTextSize
            isTitleBold = config.isTitleBold
            barcodeSize = config.barcodeSize
            currencySymbol = config.currencySymbol
            labelWidth = config.labelWidth.toString()
            labelHeight = config.labelHeight.toString()
            titleX = config.titleX
            titleY = config.titleY
            priceX = config.priceX
            priceY = config.priceY
            skuX = config.skuX
            skuY = config.skuY
            gtinX = config.gtinX
            gtinY = config.gtinY
            lotX = config.lotX
            lotY = config.lotY
            expX = config.expX
            expY = config.expY
            snX = config.snX
            snY = config.snY
            barcodeX = config.barcodeX
            barcodeY = config.barcodeY
            companyNameSize = config.companyNameSize
            companyLogoSize = config.companyLogoSize
            companyNameX = config.companyNameX
            companyNameY = config.companyNameY
            companyLogoX = config.companyLogoX
            companyLogoY = config.companyLogoY
            showCompanyName = config.showCompanyName
            showCompanyLogo = config.showCompanyLogo
            hasInitialized = true
        }
    }

    // Logo bitmap loader
    var logoBitmap by remember(companyLogoPath) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(companyLogoPath) {
        if (companyLogoPath.isNotBlank()) {
            try {
                val file = java.io.File(companyLogoPath)
                if (file.exists()) {
                    logoBitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    val uri = android.net.Uri.parse(companyLogoPath)
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        logoBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                logoBitmap = null
            }
        } else {
            logoBitmap = null
        }
    }

    // Logo picker launcher
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val logoFile = java.io.File(context.filesDir, "company_logo.png")
                    java.io.FileOutputStream(logoFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    companyLogoPath = logoFile.absolutePath
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Real-time Preview Popup Overlay states
    var isAdjusting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // Label PDF generation states
    var isGenerating by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableStateOf(0) }
    var totalQuantity by remember { mutableStateOf(1) }

    fun triggerAdjustmentActive() {
        isAdjusting = true
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            kotlinx.coroutines.delay(1500)
            isAdjusting = false
        }
    }

    // Auto-save settings to Local DB when any layout preference changes
    LaunchedEffect(
        companyName, companyLogoPath, usePrefix, customPrefix, prefixPosition,
        customSuffix, useSuffix, suffixPosition, useSeparator, titleTextSize,
        isTitleBold, barcodeSize, currencySymbol, labelWidth, labelHeight,
        titleX, titleY, priceX, priceY, skuX, skuY, gtinX, gtinY, lotX, lotY, expX, expY, snX, snY,
        barcodeX, barcodeY, companyNameSize, companyLogoSize, companyNameX, companyNameY,
        companyLogoX, companyLogoY, showCompanyName, showCompanyLogo
    ) {
        if (hasInitialized && generalConfigState != null) {
            val updatedConfig = generalConfigState!!.copy(
                companyName = companyName,
                companyLogoPath = companyLogoPath,
                usePrefix = usePrefix,
                customPrefix = customPrefix,
                prefixPosition = prefixPosition.toIntOrNull() ?: 0,
                customSuffix = customSuffix,
                useSuffix = useSuffix,
                suffixPosition = suffixPosition.toIntOrNull() ?: 0,
                useSeparator = useSeparator,
                titleTextSize = titleTextSize,
                isTitleBold = isTitleBold,
                barcodeSize = barcodeSize,
                currencySymbol = currencySymbol,
                labelWidth = labelWidth.toIntOrNull() ?: 144,
                labelHeight = labelHeight.toIntOrNull() ?: 72,
                titleX = titleX,
                titleY = titleY,
                priceX = priceX,
                priceY = priceY,
                skuX = skuX,
                skuY = skuY,
                gtinX = gtinX,
                gtinY = gtinY,
                lotX = lotX,
                lotY = lotY,
                expX = expX,
                expY = expY,
                snX = snX,
                snY = snY,
                barcodeX = barcodeX,
                barcodeY = barcodeY,
                companyNameSize = companyNameSize,
                companyLogoSize = companyLogoSize,
                companyNameX = companyNameX,
                companyNameY = companyNameY,
                companyLogoX = companyLogoX,
                companyLogoY = companyLogoY,
                showCompanyName = showCompanyName,
                showCompanyLogo = showCompanyLogo
            )
            viewModel.saveBarcodeGeneralConfig(updatedConfig)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barcode Print Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 1
                    val w = labelWidth.toIntOrNull() ?: 144
                    val h = labelHeight.toIntOrNull() ?: 72
                    val expiryStr = item!!.expiry_date.replace("-", "").takeIf { it.length == 8 }?.let { it.substring(2) } ?: "000000"

                    totalQuantity = qty
                    currentProgress = 0
                    isGenerating = true

                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        val pdfFile = LabelPdfPrinter.generateLabelPdf(
                            context = context,
                            sku = item!!.sku.ifBlank { item!!.item_number.toString() },
                            price = item!!.price_per_unit.toString(),
                            currencySymbol = currencySymbol,
                            title = item!!.item_name,
                            gtin = primaryGtin,
                            batch = item!!.batch_number.ifBlank { "NONE" },
                            expiry = expiryStr,
                            quantity = qty,
                            labelWidthPoints = w,
                            labelHeightPoints = h,
                            titleTextSize = titleTextSize,
                            isTitleBold = isTitleBold,
                            barcodeSize = barcodeSize,
                            usePrefix = usePrefix,
                            customPrefix = customPrefix,
                            prefixPosition = prefixPosition.toIntOrNull() ?: 0,
                            useSeparator = useSeparator,
                            customSuffix = customSuffix,
                            useSuffix = useSuffix,
                            suffixPosition = suffixPosition.toIntOrNull() ?: 0,
                            companyName = companyName,
                            companyLogoPath = companyLogoPath,
                            titleX = titleX,
                            titleY = titleY,
                            priceX = priceX,
                            priceY = priceY,
                            skuX = skuX,
                            skuY = skuY,
                            gtinX = gtinX,
                            gtinY = gtinY,
                            lotX = lotX,
                            lotY = lotY,
                            expX = expX,
                            expY = expY,
                            snX = snX,
                            snY = snY,
                            barcodeX = barcodeX,
                            barcodeY = barcodeY,
                            companyNameSize = companyNameSize,
                            companyLogoSize = companyLogoSize,
                            companyNameX = companyNameX,
                            companyNameY = companyNameY,
                            companyLogoX = companyLogoX,
                            companyLogoY = companyLogoY,
                            showCompanyName = showCompanyName,
                            showCompanyLogo = showCompanyLogo,
                            fields = fieldsState,
                            onProgress = { current, total ->
                                currentProgress = current
                            }
                        )

                        isGenerating = false

                        if (pdfFile != null) {
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Print GS1 Label"))
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text("Generate PDF (${quantity.toIntOrNull() ?: 1} labels)")
            }
        }
    ) { padding ->
        if (isGenerating) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Generating PDF", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("Generating label $currentProgress of $totalQuantity...", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { currentProgress.toFloat() / totalQuantity.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {}
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Live Preview Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Label Preview", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                    
                    LabelPreviewContent(
                        item = item!!,
                        primaryGtin = primaryGtin,
                        companyName = companyName,
                        logoBitmap = logoBitmap,
                        showCompanyLogo = showCompanyLogo,
                        showCompanyName = showCompanyName,
                        companyLogoX = companyLogoX,
                        companyLogoY = companyLogoY,
                        companyLogoSize = companyLogoSize,
                        companyNameX = companyNameX,
                        companyNameY = companyNameY,
                        companyNameSize = companyNameSize,
                        titleX = titleX,
                        titleY = titleY,
                        titleTextSize = titleTextSize,
                        isTitleBold = isTitleBold,
                        priceX = priceX,
                        priceY = priceY,
                        currencySymbol = currencySymbol,
                        skuX = skuX,
                        skuY = skuY,
                        gtinX = gtinX,
                        gtinY = gtinY,
                        lotX = lotX,
                        lotY = lotY,
                        expX = expX,
                        expY = expY,
                        snX = snX,
                        snY = snY,
                        barcodeX = barcodeX,
                        barcodeY = barcodeY,
                        barcodeSize = barcodeSize,
                        usePrefix = usePrefix,
                        customPrefix = customPrefix,
                        prefixPosition = prefixPosition,
                        useSeparator = useSeparator,
                        customSuffix = customSuffix,
                        useSuffix = useSuffix,
                        suffixPosition = suffixPosition,
                        fields = fieldsState,
                        labelWidth = labelWidth,
                        labelHeight = labelHeight
                    )
                }
            }

            HorizontalDivider()

            // Configuration
            Text("Label Customization", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Print Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = currencySymbol,
                    onValueChange = { currencySymbol = it },
                    label = { Text("Currency") },
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Dimensions (Points, 1 inch = 72 points)", fontSize = 14.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = labelWidth,
                    onValueChange = { labelWidth = it },
                    label = { Text("Width (e.g., 144)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = labelHeight,
                    onValueChange = { labelHeight = it },
                    label = { Text("Height (e.g., 72)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Text & Alignment", fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Item Title Size: ${titleTextSize.toInt()}")
                Slider(
                    value = titleTextSize,
                    onValueChange = { titleTextSize = it; triggerAdjustmentActive() },
                    valueRange = 4f..16f,
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = isTitleBold, onCheckedChange = { isTitleBold = it; triggerAdjustmentActive() })
                Text(" Bold Title", modifier = Modifier.padding(start = 8.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Barcode Size: ${barcodeSize.toInt()}")
                Slider(
                    value = barcodeSize,
                    onValueChange = { barcodeSize = it; triggerAdjustmentActive() },
                    valueRange = 24f..72f,
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
            }

            HorizontalDivider()

            // Branding Section
            Text("Branding & Identification", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Company Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { logoPickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (companyLogoPath.isBlank()) "Upload Logo" else "Change Logo")
                }
                if (companyLogoPath.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            companyLogoPath = ""
                            try {
                                val file = java.io.File(context.filesDir, "company_logo.png")
                                if (file.exists()) {
                                    file.delete()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Clear Logo")
                    }
                }
            }

            HorizontalDivider()

            // Layout Element Offsets Section
            var expandOffsets by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Position & Offsets Configuration", fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { expandOffsets = !expandOffsets }) {
                            Text(if (expandOffsets) "Hide" else "Show")
                        }
                    }

                    if (expandOffsets) {
                        val maxW = (labelWidth.toFloatOrNull() ?: 144f).coerceAtLeast(10f)
                        val maxH = (labelHeight.toFloatOrNull() ?: 72f).coerceAtLeast(10f)

                        var activeTab by remember { mutableStateOf(0) } // 0: Branding, 1: Text, 2: GS1, 3: Barcode

                        ScrollableTabRow(
                            selectedTabIndex = activeTab,
                            edgePadding = 0.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                                Text("Branding", modifier = Modifier.padding(vertical = 8.dp), fontSize = 12.sp)
                            }
                            Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                                Text("Product Details", modifier = Modifier.padding(vertical = 8.dp), fontSize = 12.sp)
                            }
                            Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
                                Text("GS1 Metadata", modifier = Modifier.padding(vertical = 8.dp), fontSize = 12.sp)
                            }
                            Tab(selected = activeTab == 3, onClick = { activeTab = 3 }) {
                                Text("Barcode", modifier = Modifier.padding(vertical = 8.dp), fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        when (activeTab) {
                            0 -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = showCompanyLogo, onCheckedChange = { showCompanyLogo = it; triggerAdjustmentActive() })
                                        Text("Show Logo", fontSize = 12.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = showCompanyName, onCheckedChange = { showCompanyName = it; triggerAdjustmentActive() })
                                        Text("Show Name", fontSize = 12.sp)
                                    }
                                }

                                if (showCompanyLogo) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    Text("Logo Size: ${companyLogoSize.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Slider(value = companyLogoSize, onValueChange = { companyLogoSize = it; triggerAdjustmentActive() }, valueRange = 4f..30f)
                                    
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Logo X (Left): ${companyLogoX.toInt()}", fontSize = 11.sp)
                                            Slider(value = companyLogoX, onValueChange = { companyLogoX = it; triggerAdjustmentActive() }, valueRange = 0f..maxW)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Logo Y (Top): ${companyLogoY.toInt()}", fontSize = 11.sp)
                                            Slider(value = companyLogoY, onValueChange = { companyLogoY = it; triggerAdjustmentActive() }, valueRange = 0f..maxH)
                                        }
                                    }
                                }

                                if (showCompanyName) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    Text("Company Name Text Size: ${companyNameSize.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Slider(value = companyNameSize, onValueChange = { companyNameSize = it; triggerAdjustmentActive() }, valueRange = 3f..15f)

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Name X (Left): ${companyNameX.toInt()}", fontSize = 11.sp)
                                            Slider(value = companyNameX, onValueChange = { companyNameX = it; triggerAdjustmentActive() }, valueRange = 0f..maxW)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Name Y (Top): ${companyNameY.toInt()}", fontSize = 11.sp)
                                            Slider(value = companyNameY, onValueChange = { companyNameY = it; triggerAdjustmentActive() }, valueRange = 0f..maxH)
                                        }
                                    }
                                }
                            }
                            1 -> {
                                Text("Product Title Coordinates", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Title X: ${titleX.toInt()}", fontSize = 11.sp)
                                        Slider(value = titleX, onValueChange = { titleX = it; triggerAdjustmentActive() }, valueRange = 0f..maxW)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Title Y: ${titleY.toInt()}", fontSize = 11.sp)
                                        Slider(value = titleY, onValueChange = { titleY = it; triggerAdjustmentActive() }, valueRange = 0f..maxH)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text("Price Coordinates", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Price X: ${priceX.toInt()}", fontSize = 11.sp)
                                        Slider(value = priceX, onValueChange = { priceX = it; triggerAdjustmentActive() }, valueRange = 0f..maxW)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Price Y: ${priceY.toInt()}", fontSize = 11.sp)
                                        Slider(value = priceY, onValueChange = { priceY = it; triggerAdjustmentActive() }, valueRange = 0f..maxH)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text("SKU Coordinates", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("SKU X: ${skuX.toInt()}", fontSize = 11.sp)
                                        Slider(value = skuX, onValueChange = { skuX = it; triggerAdjustmentActive() }, valueRange = 0f..maxW)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("SKU Y: ${skuY.toInt()}", fontSize = 11.sp)
                                        Slider(value = skuY, onValueChange = { skuY = it; triggerAdjustmentActive() }, valueRange = 0f..maxH)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text("Serial Number (SN) Coordinates", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("SN X: ${snX.toInt()}", fontSize = 11.sp)
                                        Slider(value = snX, onValueChange = { snX = it; triggerAdjustmentActive() }, valueRange = 0f..maxW)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("SN Y: ${snY.toInt()}", fontSize = 11.sp)
                                        Slider(value = snY, onValueChange = { snY = it; triggerAdjustmentActive() }, valueRange = 0f..maxH)
                                    }
                                }
                            }
                            2 -> {
                                Text("GTIN Coordinates", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("GTIN X: ${gtinX.toInt()}", fontSize = 11.sp)
                                        Slider(value = gtinX, onValueChange = { gtinX = it; triggerAdjustmentActive() }, valueRange = 0f..maxW)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("GTIN Y: ${gtinY.toInt()}", fontSize = 11.sp)
                                        Slider(value = gtinY, onValueChange = { gtinY = it; triggerAdjustmentActive() }, valueRange = 0f..maxH)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text("LOT Coordinates", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("LOT X: ${lotX.toInt()}", fontSize = 11.sp)
                                        Slider(value = lotX, onValueChange = { lotX = it; triggerAdjustmentActive() }, valueRange = 0f..maxW)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("LOT Y: ${lotY.toInt()}", fontSize = 11.sp)
                                        Slider(value = lotY, onValueChange = { lotY = it; triggerAdjustmentActive() }, valueRange = 0f..maxH)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text("EXP Coordinates", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("EXP X: ${expX.toInt()}", fontSize = 11.sp)
                                        Slider(value = expX, onValueChange = { expX = it; triggerAdjustmentActive() }, valueRange = 0f..maxW)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("EXP Y: ${expY.toInt()}", fontSize = 11.sp)
                                        Slider(value = expY, onValueChange = { expY = it; triggerAdjustmentActive() }, valueRange = 0f..maxH)
                                    }
                                }
                            }
                            3 -> {
                                Text("Barcode Size: ${barcodeSize.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Slider(value = barcodeSize, onValueChange = { barcodeSize = it; triggerAdjustmentActive() }, valueRange = 24f..72f)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Barcode X: ${barcodeX.toInt()}", fontSize = 11.sp)
                                        Slider(value = barcodeX, onValueChange = { barcodeX = it; triggerAdjustmentActive() }, valueRange = 0f..maxW)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Barcode Y: ${barcodeY.toInt()}", fontSize = 11.sp)
                                        Slider(value = barcodeY, onValueChange = { barcodeY = it; triggerAdjustmentActive() }, valueRange = 0f..maxH)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // Dynamic AI reordering list and adding custom AIs section
            Text("GS1 Barcode Fields Sequence & Ordering", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            // Dynamic fields list
            fieldsState.forEachIndexed { index, field ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "(${field.aiCode}) ${field.fieldName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (field.fieldId !in listOf("GTIN", "EXPIRY", "BATCH", "SN", "SKU")) {
                                    Text(
                                        text = "Value: ${field.customValue.ifBlank { "Static" }}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.moveFieldUp(field) },
                                    enabled = index > 0
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Move Up"
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.moveFieldDown(field) },
                                    enabled = index < fieldsState.size - 1
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Move Down"
                                    )
                                }
                                Checkbox(
                                    checked = field.isEnabled,
                                    onCheckedChange = { isChecked ->
                                        viewModel.updateBarcodeField(field.copy(isEnabled = isChecked))
                                    }
                                )
                                Text("Enabled", fontSize = 12.sp)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = field.useFnc1Separator,
                                    onCheckedChange = { isChecked ->
                                        viewModel.updateBarcodeField(field.copy(useFnc1Separator = isChecked))
                                    }
                                )
                                Text("FNC1 Separator (~1)", fontSize = 12.sp)
                            }
                            if (field.fieldId !in listOf("GTIN", "EXPIRY", "BATCH", "SN", "SKU")) {
                                TextButton(
                                    onClick = { viewModel.deleteBarcodeField(field) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Delete", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            var showAddCustomAiDialog by remember { mutableStateOf(false) }
            var customFieldName by remember { mutableStateOf("") }
            var customAiCode by remember { mutableStateOf("") }
            var customDefaultValue by remember { mutableStateOf("") }
            var customUseFnc1 by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { showAddCustomAiDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Custom Application Identifier (AI)")
            }

            if (showAddCustomAiDialog) {
                AlertDialog(
                    onDismissRequest = { showAddCustomAiDialog = false },
                    title = { Text("Add Custom AI Field", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customFieldName,
                                onValueChange = { customFieldName = it },
                                label = { Text("AI Name (e.g. Additional SKU)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = customAiCode,
                                onValueChange = { customAiCode = it },
                                label = { Text("AI Code (e.g. 240)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = customDefaultValue,
                                onValueChange = { customDefaultValue = it },
                                label = { Text("Value (Static text)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = customUseFnc1, onCheckedChange = { customUseFnc1 = it })
                                Text("Append FNC1 Separator (~1)")
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (customFieldName.isNotBlank() && customAiCode.isNotBlank()) {
                                    viewModel.addCustomBarcodeField(
                                        fieldName = customFieldName,
                                        aiCode = customAiCode,
                                        useFnc1 = customUseFnc1,
                                        defaultValue = customDefaultValue
                                    )
                                    customFieldName = ""
                                    customAiCode = ""
                                    customDefaultValue = ""
                                    customUseFnc1 = false
                                    showAddCustomAiDialog = false
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddCustomAiDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            HorizontalDivider()

            Text("GS1 Encoding Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            // Prefix Settings Group
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = usePrefix, onCheckedChange = { usePrefix = it })
                        Text("Enable Prefix", fontWeight = FontWeight.SemiBold)
                    }
                    if (usePrefix) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customPrefix,
                                onValueChange = { customPrefix = it },
                                label = { Text("Prefix Value") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = prefixPosition,
                                onValueChange = { prefixPosition = it },
                                label = { Text("Position") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Suffix Settings Group
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = useSuffix, onCheckedChange = { useSuffix = it })
                        Text("Enable Suffix", fontWeight = FontWeight.SemiBold)
                    }
                    if (useSuffix) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customSuffix,
                                onValueChange = { customSuffix = it },
                                label = { Text("Suffix Value") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = suffixPosition,
                                onValueChange = { suffixPosition = it },
                                label = { Text("Position") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = useSeparator, onCheckedChange = { useSeparator = it })
                Text("Use FNC1 Separator (~1)")
            }

            OutlinedButton(
                onClick = {
                    usePrefix = true
                    customPrefix = "]d2"
                    prefixPosition = "0"
                    useSuffix = false
                    customSuffix = ""
                    suffixPosition = "0"
                    useSeparator = true
                    quantity = "1"
                    titleTextSize = 6f
                    isTitleBold = true
                    barcodeSize = 48f
                    currencySymbol = "PKR"
                    labelWidth = "144"
                    labelHeight = "72"
                    titleX = 4f
                    titleY = 16f
                    priceX = 4f
                    priceY = 24f
                    skuX = 4f
                    skuY = 32f
                    gtinX = 4f
                    gtinY = 40f
                    lotX = 4f
                    lotY = 48f
                    expX = 4f
                    expY = 56f
                    snX = 4f
                    snY = 66f
                    barcodeX = 92f
                    barcodeY = 12f
                    companyName = "Tillzo POS"
                    companyLogoPath = ""
                    companyNameSize = 5f
                    companyLogoSize = 8f
                    companyNameX = 16f
                    companyNameY = 8f
                    companyLogoX = 4f
                    companyLogoY = 4f
                    showCompanyName = true
                    showCompanyLogo = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Restore Default Settings")
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }

        // Floating Popup Overlay for Live Real-time Preview during slider adjustments
        if (isAdjusting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.padding(16.dp).width(280.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Adjusting Layout...",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LabelPreviewContent(
                            item = item!!,
                            primaryGtin = primaryGtin,
                            companyName = companyName,
                            logoBitmap = logoBitmap,
                            showCompanyLogo = showCompanyLogo,
                            showCompanyName = showCompanyName,
                            companyLogoX = companyLogoX,
                            companyLogoY = companyLogoY,
                            companyLogoSize = companyLogoSize,
                            companyNameX = companyNameX,
                            companyNameY = companyNameY,
                            companyNameSize = companyNameSize,
                            titleX = titleX,
                            titleY = titleY,
                            titleTextSize = titleTextSize,
                            isTitleBold = isTitleBold,
                            priceX = priceX,
                            priceY = priceY,
                            currencySymbol = currencySymbol,
                            skuX = skuX,
                            skuY = skuY,
                            gtinX = gtinX,
                            gtinY = gtinY,
                            lotX = lotX,
                            lotY = lotY,
                            expX = expX,
                            expY = expY,
                            snX = snX,
                            snY = snY,
                            barcodeX = barcodeX,
                            barcodeY = barcodeY,
                            barcodeSize = barcodeSize,
                            usePrefix = usePrefix,
                            customPrefix = customPrefix,
                            prefixPosition = prefixPosition,
                            useSeparator = useSeparator,
                            customSuffix = customSuffix,
                            useSuffix = useSuffix,
                            suffixPosition = suffixPosition,
                            fields = fieldsState,
                            labelWidth = labelWidth,
                            labelHeight = labelHeight
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun LabelPreviewContent(
    item: InventoryEntity,
    primaryGtin: String,
    companyName: String,
    logoBitmap: android.graphics.Bitmap?,
    showCompanyLogo: Boolean,
    showCompanyName: Boolean,
    companyLogoX: Float,
    companyLogoY: Float,
    companyLogoSize: Float,
    companyNameX: Float,
    companyNameY: Float,
    companyNameSize: Float,
    titleX: Float,
    titleY: Float,
    titleTextSize: Float,
    isTitleBold: Boolean,
    priceX: Float,
    priceY: Float,
    currencySymbol: String,
    skuX: Float,
    skuY: Float,
    gtinX: Float,
    gtinY: Float,
    lotX: Float,
    lotY: Float,
    expX: Float,
    expY: Float,
    snX: Float,
    snY: Float,
    barcodeX: Float,
    barcodeY: Float,
    barcodeSize: Float,
    usePrefix: Boolean,
    customPrefix: String,
    prefixPosition: String,
    useSeparator: Boolean,
    customSuffix: String,
    useSuffix: Boolean,
    suffixPosition: String,
    fields: List<BarcodeFieldConfigEntity> = emptyList(),
    labelWidth: String,
    labelHeight: String
) {
    val widthPoints = labelWidth.toFloatOrNull() ?: 144f
    val heightPoints = labelHeight.toFloatOrNull() ?: 72f
    
    // Scale factor: maps 144 points width to 240dp on screen (scale = 5/3)
    val scale = 240f / 144f

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .size(width = (widthPoints * scale).dp, height = (heightPoints * scale).dp)
            .background(Color.White)
            .border(width = 1.dp, color = Color.Gray)
            .clipToBounds()
    ) {
        val nativeCanvas = drawContext.canvas.nativeCanvas
        nativeCanvas.save()

        // Scale coordinates from actual points to DP * density pixels
        val pxScale = scale * density
        nativeCanvas.scale(pxScale, pxScale)

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            isAntiAlias = true
        }

        val resolvedGtin = if (primaryGtin.isNotBlank() && primaryGtin != "0000000") primaryGtin else "0000000"
        val skuValue = item.sku.ifBlank { item.item_number.toString() }
        val priceText = if (currencySymbol.isNotBlank()) "$currencySymbol ${item.price_per_unit}" else "${item.price_per_unit}"
        val previewSn = BarcodeGeneratorUtil.generateDynamicSerialNumber(skuValue)
        val expiryStr = item.expiry_date.replace("-", "").takeIf { it.length == 8 }?.let { it.substring(2) } ?: "000000"

        // 0. Draw branding: company logo and company name if enabled
        if (showCompanyLogo) {
            logoBitmap?.let { bmp ->
                val logoHeight = companyLogoSize
                val logoWidth = logoHeight * (bmp.width.toFloat() / bmp.height.toFloat())
                val scaledLogo = android.graphics.Bitmap.createScaledBitmap(bmp, logoWidth.toInt().coerceAtLeast(1), logoHeight.toInt().coerceAtLeast(1), true)
                nativeCanvas.drawBitmap(scaledLogo, companyLogoX, companyLogoY, null)
            }
        }
        if (showCompanyName && companyName.isNotBlank()) {
            textPaint.textSize = companyNameSize
            textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            nativeCanvas.drawText(companyName, companyNameX, companyNameY, textPaint)
        }

        // 1. Draw Title
        textPaint.textSize = titleTextSize
        textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, if (isTitleBold) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        nativeCanvas.drawText(item.item_name, titleX, titleY, textPaint)

        // 2. Draw Price
        textPaint.textSize = 6f
        textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
        nativeCanvas.drawText(priceText, priceX, priceY, textPaint)

        // 3. Draw SKU/Item Number
        textPaint.textSize = 5f
        textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
        nativeCanvas.drawText("SKU/Item No: $skuValue", skuX, skuY, textPaint)

        // 4. Stacked Data (GTIN, LOT, EXP)
        textPaint.textSize = 5f
        nativeCanvas.drawText("GTIN: $resolvedGtin", gtinX, gtinY, textPaint)
        nativeCanvas.drawText("LOT: ${item.batch_number.ifBlank { "NONE" }}", lotX, lotY, textPaint)
        nativeCanvas.drawText("EXP: $expiryStr", expX, expY, textPaint)
        
        // 5. Serial Number
        textPaint.textSize = 5f
        textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
        nativeCanvas.drawText("SN: $previewSn", snX, snY, textPaint)

        // 6. Draw DataMatrix Bitmap
        val gs1String = BarcodeGeneratorUtil.buildDynamicGs1String(
            fields = fields,
            gtin = resolvedGtin,
            expiryYYMMDD = expiryStr,
            batch = item.batch_number.ifBlank { "NONE" },
            serial = previewSn,
            sku = skuValue,
            usePrefix = usePrefix,
            customPrefix = customPrefix,
            prefixPosition = prefixPosition.toIntOrNull() ?: 0,
            useSuffix = useSuffix,
            customSuffix = customSuffix,
            suffixPosition = suffixPosition.toIntOrNull() ?: 0
        )
        val dataMatrixBitmap = BarcodeGeneratorUtil.generateDataMatrix(gs1String, 120, 120)
        if (dataMatrixBitmap != null) {
            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(dataMatrixBitmap, barcodeSize.toInt().coerceAtLeast(1), barcodeSize.toInt().coerceAtLeast(1), false)
            nativeCanvas.drawBitmap(scaledBitmap, barcodeX, barcodeY, null)
        }

        nativeCanvas.restore()
    }
}
