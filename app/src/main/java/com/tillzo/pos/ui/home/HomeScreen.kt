package com.tillzo.pos.ui.home

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.ui.inventory.options.alerts.LowStockViewModel
import com.tillzo.pos.ui.theme.*
import com.tillzo.pos.ui.hardware.scanner.InlineCameraBox
import com.tillzo.pos.ui.hardware.scanner.InlineScannerViewModel
import com.tillzo.pos.ui.till.TillViewModel
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * M4 POS Main Screen — full selling flow.
 *
 * Layout:
 *   [TopBar] shop name + sync dot + hamburger
 *   [Search Bar] + [Camera Icon]
 *   [Quick Grid] pinned inventory items
 *   [Cart List] with qty controls and swipe-to-dismiss
 *   [Cart Summary] subtotal / tax / discount / total
 *   [PAY NOW] button → opens PaymentBottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onOpenMenu: () -> Unit,
    onNavigateToInventory: () -> Unit = {},
    onNavigateToReceipt: (String) -> Unit = {},
    onNavigateToTill: () -> Unit = {},
    viewModel: PosViewModel = hiltViewModel(),
    lowStockViewModel: LowStockViewModel = hiltViewModel(),
    tillViewModel: TillViewModel = hiltViewModel()
) {
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartSubtotal by viewModel.cartSubtotal.collectAsStateWithLifecycle()
    val cartTax by viewModel.cartTax.collectAsStateWithLifecycle()
    val cartDiscount by viewModel.cartDiscount.collectAsStateWithLifecycle()
    val cartTotal by viewModel.cartTotal.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val currencySymbol = viewModel.currencySymbol
    val quickGridItems by viewModel.quickGridItems.collectAsStateWithLifecycle()
    val lowStockItems by lowStockViewModel.lowStockItems.collectAsStateWithLifecycle()
    val saleResult by viewModel.saleResult.collectAsStateWithLifecycle()
    val currentTillSession by tillViewModel.currentSession.collectAsStateWithLifecycle()
    val hasPendingSync by viewModel.hasPendingSync.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()

    var showPaymentDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showDecimalQtyDialog by remember { mutableStateOf(false) }
    var decimalQtyItem by remember { mutableStateOf<InventoryEntity?>(null) }
    var showCartDecimalQtyDialog by remember { mutableStateOf(false) }
    var cartDecimalQtyTarget by remember { mutableStateOf<com.tillzo.pos.domain.model.CartItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val stockWarning by viewModel.stockWarning.collectAsStateWithLifecycle()
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var showUnpinConfirm by remember { mutableStateOf(false) }
    var pinTarget by remember { mutableStateOf<InventoryEntity?>(null) }
    var unpinTarget by remember { mutableStateOf<InventoryEntity?>(null) }
    var showDiscountDialog by remember { mutableStateOf(false) }
    var showCustomItemDialog by remember { mutableStateOf(false) }
    var showPayInDialog by remember { mutableStateOf(false) }
    var showPayOutDialog by remember { mutableStateOf(false) }
    val inlineScannerViewModel: InlineScannerViewModel = hiltViewModel()
    val isCameraActive by inlineScannerViewModel.isCameraActive.collectAsStateWithLifecycle()
    // FIX (2026-08-06): clear search focus + hide keyboard when a product tile
    // is tapped, so the numeric qty picker never types into the search field
    // ("Milkq" bug — numpad '1' was landing on the system keyboard's 'q').
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    fun dismissSearchInput() {
        focusManager.clearFocus()
        keyboardController?.hide()
        // FIX (2026-08-06): also reset the query so the dropdown closes and the
        // field can never re-steal focus/type into the system keyboard ("Milkq").
        if (searchQuery.isNotEmpty()) {
            viewModel.onSearchQueryChanged("")
        }
    }

    var scannerBorderColor by remember { mutableStateOf(Color(0xFF1E88E5)) }
    
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        else
            @Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    val toneGen = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }

    LaunchedEffect(Unit) {
        inlineScannerViewModel.scanEvent.collect { event ->
            when (event) {
                is InlineScannerViewModel.ScanEvent.ProductFound -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                    scannerBorderColor = Color(0xFF4CAF50)
                    delay(300)
                    scannerBorderColor = Color(0xFF1E88E5)
                    viewModel.addToCart(event.product, qty = 1.0)
                }
                is InlineScannerViewModel.ScanEvent.ProductNotFound -> {
                    scannerBorderColor = Color(0xFFF44336)
                    delay(300)
                    scannerBorderColor = Color(0xFF1E88E5)
                }
            }
        }
    }

    // Navigate to receipt when sale completes
    LaunchedEffect(saleResult) {
        val result = saleResult
        if (result is SaleResult.Success) {
            showPaymentDialog = false
            onNavigateToReceipt(result.sale.sync_uuid)
        }
    }

    // Show snackbar when stock warning is active (negative stock block)
    LaunchedEffect(stockWarning) {
        stockWarning?.let { warning ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Cannot oversell. Stock limit reached! (${warning.itemName}: requested ${warning.requested.toInt()}, available ${warning.available.toInt()})",
                    duration = SnackbarDuration.Short
                )
            }
            viewModel.clearStockWarning()
        }
    }

    // Till session gate — block POS when no active till session or session is closed
    if (currentTillSession == null || currentTillSession?.status != "OPEN") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    if (currentTillSession?.status == "RECONCILED") "Register Session Closed"
                    else "No Active Register Session",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (currentTillSession?.status == "RECONCILED") "This till has been reconciled. Open a new till to continue selling."
                    else "Please open a till before making sales.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onNavigateToTill,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    modifier = Modifier
                        .widthIn(min = 220.dp)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Open Till", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "TillzoPOS",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    // Sync status dot — Green = synced, Red = pending updates
                    val syncColor = if (hasPendingSync) WarningAmber else SuccessGreen
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(syncColor)
                    )
                    if (pendingSyncCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(ErrorRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = minOf(pendingSyncCount, 99).toString(),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    // Till management — PayIn / PayOut
                    Box {
                        var showTillMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showTillMenu = true }) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Till", tint = AccentBlue, modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(
                            expanded = showTillMenu,
                            onDismissRequest = { showTillMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Pay In (Add Cash)") },
                                onClick = { showTillMenu = false; showPayInDialog = true },
                                leadingIcon = { Icon(Icons.Default.AddCircle, null, tint = SuccessGreen) }
                            )
                            DropdownMenuItem(
                                text = { Text("Pay Out (Remove Cash)") },
                                onClick = { showTillMenu = false; showPayOutDialog = true },
                                leadingIcon = { Icon(Icons.Default.RemoveCircle, null, tint = ErrorRed) }
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToInventory) {
                        Icon(Icons.Default.Inventory, contentDescription = "Inventory", tint = AccentBlue)
                    }
                    IconButton(onClick = onOpenMenu) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = AccentBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary
                )
    )
}
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(padding)
        ) {
            // Low Stock Banner
            if (lowStockItems.isNotEmpty()) {
                val alertText = "LOW STOCK: " + lowStockItems.take(3).joinToString { "${it.item_name} (${it.current_stock})" } +
                    if (lowStockItems.size > 3) " +${lowStockItems.size - 3} more" else ""
                Text(
                    text = alertText,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Inline Camera Scanner and Search Bar Section
            Column(modifier = Modifier.fillMaxWidth()) {
                
                // Camera box — always shown
                InlineCameraBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 8.dp),
                    isCameraActive = isCameraActive,
                    onBarcodeDetected = { barcode ->
                        inlineScannerViewModel.onBarcodeDetected(barcode)
                    },
                    onActivateClick = {
                        inlineScannerViewModel.activateCamera()
                    },
                    borderColor = scannerBorderColor
                )

                // Toggle button — compact, below box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            if (isCameraActive) inlineScannerViewModel.deactivateCamera()
                            else inlineScannerViewModel.activateCamera()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isCameraActive) Color(0xFFF44336) else Color(0xFF1E88E5)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isCameraActive) Color(0xFFF44336) else Color(0xFF1E88E5)
                        ),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = if (isCameraActive)
                                Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCameraActive) "Stop Scan" else "Start Scan",
                            fontSize = 12.sp
                        )
                    }
                }

                // Existing Search Bar
                PosSearchBar(
                    query = searchQuery,
                    onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                    onSearchSubmit = {
                        val match = searchResults.firstOrNull()
                        if (match != null) viewModel.addToCart(match, 1.0)
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            // Search Results Dropdown
            AnimatedVisibility(
                visible = searchResults.isNotEmpty(),
                enter = fadeIn(), exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(top = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                ) {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(searchResults) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        dismissSearchInput()
                                        viewModel.addToCart(item, 1.0)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.item_name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text(text = item.sku.ifBlank { item.barcode_id }, color = TextSecondary, fontSize = 12.sp)
                                }
                                Text(text = "$currencySymbol ${item.price_per_unit.toInt()}", color = AccentBlueLight, fontSize = 13.sp)
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceHighlight)
                                        .clickable {
                                            pinTarget = item
                                            showAdminPinDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "📌", fontSize = 14.sp)
                                }
                            }
                            HorizontalDivider(color = SurfaceDark, thickness = 0.5.dp)
                        }
                    }
                }
            }

            // Quick-Access Grid
            if (searchResults.isEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(quickGridItems, key = { it.system_row_id }) { item ->
                        QuickGridTile(
                            item = item,
                            currencySymbol = currencySymbol,
                            onTapped = {
                                dismissSearchInput()
                                if (item.unit in listOf("KG", "GM", "ML")) {
                                    decimalQtyItem = item
                                    showDecimalQtyDialog = true
                                } else {
                                    viewModel.addToCart(item, 1.0)
                                }
                            },
                            onLongClick = {
                                unpinTarget = item
                                showAdminPinDialog = true
                            }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Cart Section
            if (cartItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                    color = SurfaceDark,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Cart (${cartItems.size})",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                            items(cartItems, key = { it.itemId }) { cartItem ->
                                CartRow(
                                    item = cartItem,
                                    currencySymbol = currencySymbol,
                                    onQtyDecrease = {
                                        val minQty = if (cartItem.unit in listOf("KG", "GM", "ML", "L", "Litre", "LITRE", "LTR")) 0.1 else 1.0
                                        val newQty = (cartItem.quantity - (if (cartItem.unit in listOf("KG", "GM", "ML", "L", "Litre", "LITRE", "LTR")) 0.1 else 1.0)).coerceAtLeast(minQty)
                                        viewModel.updateCartItemQty(cartItem.itemId, newQty)
                                    },
                                    onQtyIncrease = {
                                        val step = if (cartItem.unit in listOf("KG", "GM", "ML", "L", "Litre", "LITRE", "LTR")) 0.1 else 1.0
                                        viewModel.updateCartItemQty(cartItem.itemId, cartItem.quantity + step)
                                    },
                                    onRemove = { viewModel.removeFromCart(cartItem.itemId) },
                                    onRowTapped = {
                                        cartDecimalQtyTarget = cartItem
                                        showCartDecimalQtyDialog = true
                                    }
                                )
                                HorizontalDivider(color = BackgroundDark, thickness = 0.5.dp)
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Totals
                        CartSummaryRow("Subtotal", "$currencySymbol %.2f".format(cartSubtotal))
                        if (cartTax > 0) CartSummaryRow("Tax", "$currencySymbol %.2f".format(cartTax))
                        if (cartDiscount > 0) CartSummaryRow("Discount", "- $currencySymbol %.2f".format(cartDiscount), color = SuccessGreen)
                        HorizontalDivider(color = SurfaceVariant, modifier = Modifier.padding(vertical = 6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("$currencySymbol %.2f".format(cartTotal), color = AccentBlue, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        Spacer(Modifier.height(6.dp))

                        // Discount Apply Row
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (cartDiscount > 0) {
                                Text("Discount: - $currencySymbol %.2f".format(cartDiscount), color = SuccessGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                TextButton(onClick = { viewModel.setDiscount(0.0) }) {
                                    Text("Remove", color = ErrorRed, fontSize = 12.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { showDiscountDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Discount", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Action Buttons
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showCustomItemDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlueLight)
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Open Item", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { showClearConfirm = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                            ) {
                                Text("Clear Cart")
                            }
                            Button(
                                onClick = { showPaymentDialog = true },
                                modifier = Modifier.weight(2f),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("PAY NOW", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Payment Dialog
    if (showPaymentDialog) {
        PaymentDialog(
            cartTotal = cartTotal,
            cartItems = cartItems,
            viewModel = viewModel,
            currencySymbol = currencySymbol,
            onDismiss = { showPaymentDialog = false }
        )
    }

    // Clear Cart Confirm
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Cart?", color = TextPrimary) },
            text = { Text("All ${cartItems.size} items will be removed.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearCart(); showClearConfirm = false }) {
                    Text("Clear", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Decimal Qty Dialog (for KG/GM/ML items)
    if (showDecimalQtyDialog && decimalQtyItem != null) {
        DecimalQtyDialog(
            item = decimalQtyItem!!,
            onConfirm = { qty ->
                viewModel.addToCart(decimalQtyItem!!, qty)
                showDecimalQtyDialog = false
                decimalQtyItem = null
            },
            onDismiss = {
                showDecimalQtyDialog = false
                decimalQtyItem = null
            }
        )
    }

    // Cart Decimal Qty Dialog (for tapping cart row)
    if (showCartDecimalQtyDialog && cartDecimalQtyTarget != null) {
        CartDecimalQtyDialog(
            item = cartDecimalQtyTarget!!,
            onConfirm = { qty ->
                viewModel.updateCartItemQty(cartDecimalQtyTarget!!.itemId, qty)
                showCartDecimalQtyDialog = false
                cartDecimalQtyTarget = null
            },
            onDismiss = {
                showCartDecimalQtyDialog = false
                cartDecimalQtyTarget = null
            }
        )
    }

    // Pay In Dialog
    if (showPayInDialog) {
        PayInOutDialog(
            title = "Pay In — Add Cash to Drawer",
            description = "Enter the amount being added to the till (e.g., change from bank).",
            onConfirm = { amount ->
                tillViewModel.addPayIn(amount)
                showPayInDialog = false
            },
            onDismiss = { showPayInDialog = false }
        )
    }

    // Pay Out Dialog
    if (showPayOutDialog) {
        PayInOutDialog(
            title = "Pay Out — Remove Cash from Drawer",
            description = "Enter the amount being taken out (e.g., for expenses, lunch, etc.).",
            onConfirm = { amount ->
                tillViewModel.addPayOut(amount)
                showPayOutDialog = false
            },
            onDismiss = { showPayOutDialog = false }
        )
    }

    // Custom Item Dialog
    if (showCustomItemDialog) {
        CustomItemDialog(
            onConfirm = { name, price ->
                viewModel.addCustomItem(name, price)
                showCustomItemDialog = false
            },
            onDismiss = { showCustomItemDialog = false }
        )
    }

    // Discount Dialog
    if (showDiscountDialog) {
        DiscountDialog(
            currentDiscount = cartDiscount,
            currencySymbol = currencySymbol,
            onConfirm = { amount ->
                viewModel.setDiscount(amount)
                showDiscountDialog = false
            },
            onDismiss = { showDiscountDialog = false }
        )
    }

    // Admin PIN Dialog (gates pin/unpin actions)
    if (showAdminPinDialog) {
        AdminPinDialog(
            onVerified = {
                showAdminPinDialog = false
                if (pinTarget != null) {
                    viewModel.togglePinItem(pinTarget!!.system_row_id, shouldPin = true)
                    scope.launch { snackbarHostState.showSnackbar("Pinned to quick grid") }
                    pinTarget = null
                }
                if (unpinTarget != null) {
                    showUnpinConfirm = true
                }
            },
            onDismiss = {
                showAdminPinDialog = false
                pinTarget = null
                unpinTarget = null
            }
        )
    }

    // Unpin Confirmation Dialog
    if (showUnpinConfirm && unpinTarget != null) {
        AlertDialog(
            onDismissRequest = {
                showUnpinConfirm = false
                unpinTarget = null
            },
            title = { Text("Remove from quick grid?", color = TextPrimary) },
            text = { Text("Remove ${unpinTarget!!.item_name} from quick grid?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.togglePinItem(unpinTarget!!.system_row_id, shouldPin = false)
                    showUnpinConfirm = false
                    unpinTarget = null
                }) {
                    Text("Remove", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showUnpinConfirm = false
                    unpinTarget = null
                }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

}

// ── Search Bar ────────────────────────────────────────────────────────────────

@Composable
fun PosSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier
            .focusRequester(focusRequester)
            .onKeyEvent { keyEvent ->
                // HID scanner sends Enter after barcode — auto-submit
                if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                    onSearchSubmit()
                    keyboardController?.hide()
                    true
                } else false
            },
        placeholder = { Text("Search by name, SKU, or scan barcode…", color = TextSecondary, fontSize = 13.sp) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = SurfaceVariant,
            unfocusedContainerColor = SurfaceVariant,
            focusedBorderColor = AccentBlue,
            unfocusedBorderColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                }
            }
        },
        textStyle = TextStyle(fontSize = 14.sp, color = TextPrimary),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearchSubmit()
                keyboardController?.hide()
            }
        )
    )
}

// ── Quick Grid Tile ───────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickGridTile(item: InventoryEntity, currencySymbol: String = "$", onTapped: () -> Unit, onLongClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceVariant)
            .combinedClickable(onClick = onTapped, onLongClick = onLongClick)
            .padding(vertical = 10.dp, horizontal = 6.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🛒", fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.item_name,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "$currencySymbol ${item.price_per_unit.toInt()}/${item.unit}",
            color = AccentBlueLight,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ── Cart Row ──────────────────────────────────────────────────────────────────

@Composable
private fun CartRow(
    item: com.tillzo.pos.domain.model.CartItem,
    currencySymbol: String = "Rs",
    onQtyDecrease: () -> Unit,
    onQtyIncrease: () -> Unit,
    onRemove: () -> Unit,
    onRowTapped: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onRowTapped() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$currencySymbol ${item.pricePerUnit.toInt()} / ${item.unit}", color = TextSecondary, fontSize = 11.sp)
        }
        Spacer(Modifier.width(8.dp))
        // Qty Controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            SmallIconButton(Icons.Default.Remove, "Decrease") { onQtyDecrease() }
            Text(
                text = if (item.unit in listOf("KG", "GM", "ML", "L", "Litre", "LITRE", "LTR")) "%.3f".format(item.quantity)
                       else item.quantity.toInt().toString(),
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.widthIn(min = 36.dp),
                textAlign = TextAlign.Center
            )
            SmallIconButton(Icons.Default.Add, "Increase") { onQtyIncrease() }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "$currencySymbol %.0f".format(item.total),
            color = AccentBlueLight,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.widthIn(min = 48.dp),
            textAlign = TextAlign.End
        )
        Spacer(Modifier.width(6.dp))
        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Remove", tint = ErrorRed, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SmallIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(SurfaceHighlight)
            .clickable { onClick() }
    ) {
        Icon(icon, contentDescription = desc, tint = TextPrimary, modifier = Modifier.size(14.dp))
    }
}

// ── Cart Summary Row ──────────────────────────────────────────────────────────

@Composable
private fun CartSummaryRow(label: String, value: String, color: Color = TextSecondary) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value, color = color.takeIf { it != TextSecondary } ?: TextPrimary, fontSize = 13.sp)
    }
}

// ── Decimal Qty Dialog ────────────────────────────────────────────────────────

@Composable
private fun DecimalQtyDialog(
    item: InventoryEntity,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf("1.0") }
    // FIX (2026-08-06): give the qty field EXPLICIT focus so the system keyboard
    // opens for the dialog, NOT for the search field. Without this, the dialog's
    // TextField races the search field for focus and taps land on QWERTY keys
    // ("Milkq" bug — the search QWERTY keyboard steals focus back).
    val keyboardController = LocalSoftwareKeyboardController.current
    val qtyFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        qtyFocusRequester.requestFocus()
        keyboardController?.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Enter Quantity", color = TextPrimary) },
        text = {
            Column {
                Text("Item: ${item.item_name}", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Qty (${item.unit})", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.focusRequester(qtyFocusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(input.toDoubleOrNull() ?: 1.0) }) {
                Text("Add to Cart", color = AccentBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// ── Admin PIN Dialog ──────────────────────────────────────────────────────────

@Composable
private fun AdminPinDialog(
    onVerified: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { AppSetupPrefs(context) }
    val storedPin = remember { prefs.adminPasscode }

    var pinInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var isRegistration by remember { mutableStateOf(storedPin.isEmpty()) }
    var confirmPin by remember { mutableStateOf("") }
    var showConfirmField by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                if (isRegistration) "Set Admin PIN" else "Admin PIN Required",
                color = TextPrimary
            )
        },
        text = {
            Column {
                if (isRegistration) {
                    Text("No admin PIN is set. Please create a 4-digit PIN.", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4) { pinInput = it; showError = false }
                        },
                        label = { Text("New 4-digit PIN", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentBlue
                        )
                    )
                    if (showConfirmField) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = {
                                if (it.length <= 4) { confirmPin = it; showError = false }
                            },
                            label = { Text("Confirm PIN", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentBlue
                            )
                        )
                    }
                    if (showError) {
                        Text(if (pinInput != confirmPin) "PINs do not match" else "PIN must be 4 digits", color = ErrorRed, fontSize = 12.sp)
                    }
                } else {
                    if (showError) {
                        Text("Incorrect PIN", color = ErrorRed, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4) { pinInput = it; showError = false }
                        },
                        label = { Text("Enter 4-digit PIN", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentBlue
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (isRegistration) {
                    if (pinInput.length == 4 && !showConfirmField) {
                        showConfirmField = true
                    } else if (pinInput.length == 4 && pinInput == confirmPin) {
                        prefs.adminPasscode = pinInput
                        onVerified()
                    } else {
                        showError = true
                    }
                } else {
                    if (pinInput == storedPin) {
                        showError = false
                        onVerified()
                    } else {
                        showError = true
                    }
                }
            }) {
                Text(
                    if (isRegistration && !showConfirmField) "Next" else "Confirm",
                    color = AccentBlue
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// ── Discount Dialog ─────────────────────────────────────────────────────────

@Composable
private fun DiscountDialog(
    currentDiscount: Double,
    currencySymbol: String = "Rs",
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf(if (currentDiscount > 0) "%.2f".format(currentDiscount) else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text(if (currentDiscount > 0) "Edit Discount" else "Apply Discount", color = TextPrimary) },
        text = {
            Column {
                Text("Enter discount amount", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Amount ($currencySymbol)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(input.toDoubleOrNull() ?: 0.0) }) {
                Text("Apply", color = AccentBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// ── Pay In / Pay Out Dialog ─────────────────────────────────────────────────

@Composable
private fun PayInOutDialog(
    title: String,
    description: String,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amountInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text(title, color = TextPrimary) },
        text = {
            Column {
                Text(description, color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount (Rs)", color = TextSecondary) },
                    placeholder = { Text("e.g. 500", color = TextDisabled) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = SurfaceVariant,
                        unfocusedContainerColor = SurfaceVariant
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountInput.toDoubleOrNull() ?: 0.0
                    if (amount > 0) onConfirm(amount)
                },
                enabled = (amountInput.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Confirm", color = AccentBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// ── Custom Item Dialog ─────────────────────────────────────────────────────

@Composable
private fun CustomItemDialog(
    onConfirm: (String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Open Item / Custom Amount", color = TextPrimary) },
        text = {
            Column {
                Text("Enter a custom item to add to the cart.", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name / Note", color = TextSecondary) },
                    placeholder = { Text("e.g. Custom Shirt", color = TextDisabled) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = SurfaceVariant,
                        unfocusedContainerColor = SurfaceVariant
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = itemPrice,
                    onValueChange = { itemPrice = it },
                    label = { Text("Selling Price (Rs)", color = TextSecondary) },
                    placeholder = { Text("e.g. 15.50", color = TextDisabled) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = SurfaceVariant,
                        unfocusedContainerColor = SurfaceVariant
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = itemPrice.toDoubleOrNull() ?: 0.0
                    if (price > 0) {
                        val name = itemName.ifBlank { "Custom Item" }
                        onConfirm(name, price)
                    }
                },
                enabled = (itemPrice.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Add to Cart", color = AccentBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// ── Cart Decimal Qty Dialog ────────────────────────────────────────────────

@Composable
private fun CartDecimalQtyDialog(
    item: com.tillzo.pos.domain.model.CartItem,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf("%.3f".format(item.quantity)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Enter Quantity", color = TextPrimary) },
        text = {
            Column {
                Text("Item: ${item.name}", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Qty (${item.unit})", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(input.toDoubleOrNull() ?: item.quantity) }) {
                Text("Update", color = AccentBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
