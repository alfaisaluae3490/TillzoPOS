package com.tillzo.pos.ui.home

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.tillzo.pos.data.local.entity.SaleEntity
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.ui.theme.*
import com.tillzo.pos.utils.printer.EscPosPrinter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * M4 Receipt Screen — shown after a successful sale.
 *
 * Displays: shop name, invoice number, date, cashier, line items, totals, payment
 * breakdown, QR code (from invoiceId/sync_uuid), and action buttons.
 *
 * Actions:
 *   [Share on WhatsApp] — native Android Intent, zero API cost
 *   [Print Receipt]     — delegates to existing BT printer module (placeholder snackbar for now)
 *   [New Sale]          — clears cart and pops back to POS screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    invoiceId: String,
    onNewSale: () -> Unit,
    viewModel: PosViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val saleResult by viewModel.saleResult.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var whatsappNumber by remember { mutableStateOf("") }
    var showWhatsappInput by remember { mutableStateOf(false) }

    val appSetupPrefs = remember { AppSetupPrefs(context) }
    val currencySymbol = appSetupPrefs.currencySymbol
    val scope = rememberCoroutineScope()
    val escPosPrinter = remember { EscPosPrinter() }

    val sale = (saleResult as? SaleResult.Success)?.sale

    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Receipt", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            // Receipt Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Shop Header
                    Text(
                        text = "TILLZO POS",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(2.dp))
                    Text("Powered by Tillzo", color = TextSecondary, fontSize = 11.sp)

                    ReceiptDivider()

                    // Invoice Info
                    if (sale != null) {
                        ReceiptInfoRow("Invoice #:", sale.sync_uuid.take(8).uppercase())
                        ReceiptInfoRow("Date:", formatTimestamp(sale.timestamp))
                        ReceiptInfoRow("Cashier:", sale.cashier_id.take(20))
                    } else {
                        ReceiptInfoRow("Invoice #:", invoiceId.take(8).uppercase())
                    }

                    ReceiptDivider()

                    // Items Header
                    Row(Modifier.fillMaxWidth()) {
                        Text("ITEM", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("QTY", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                        Text("TOTAL", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(64.dp), textAlign = TextAlign.End)
                    }
                    Spacer(Modifier.height(4.dp))

                    // Items from ViewModel cart (or sale's items_json — use viewModel cart post-sale)
                    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
                    cartItems.forEach { item ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text(item.name, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1)
                            val qtyStr = if (item.unit in listOf("KG", "GM", "ML")) "%.3f ${item.unit}".format(item.quantity) else "${item.quantity.toInt()} ${item.unit}"
                            Text(qtyStr, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                            Text("$currencySymbol %.0f".format(item.total), color = TextPrimary, fontSize = 12.sp, modifier = Modifier.width(64.dp), textAlign = TextAlign.End)
                        }
                    }

                    ReceiptDivider()

                    // Totals
                    if (sale != null) {
                        ReceiptInfoRow("Subtotal:", "$currencySymbol %.2f".format(sale.subtotal))
                        if (sale.tax > 0) ReceiptInfoRow("Tax:", "$currencySymbol %.2f".format(sale.tax))
                        if (sale.discount > 0) ReceiptInfoRow("Discount:", "- Rs %.2f".format(sale.discount))
                        ReceiptDivider()
                        Row(Modifier.fillMaxWidth()) {
                            Text("TOTAL:", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Text("$currencySymbol %.2f".format(sale.total), color = AccentBlue, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                        Spacer(Modifier.height(6.dp))

                        // Payment breakdown
                        if (sale.cash_amount > 0) ReceiptInfoRow("Cash Paid:", "$currencySymbol %.2f".format(sale.cash_amount))
                        if (sale.card_amount > 0) ReceiptInfoRow("Card:", "$currencySymbol %.2f".format(sale.card_amount))
                        if (sale.wallet_amount > 0) ReceiptInfoRow("Wallet:", "$currencySymbol %.2f".format(sale.wallet_amount))
                        if (sale.udhaar_amount > 0) ReceiptInfoRow("Udhaar:", "$currencySymbol %.2f".format(sale.udhaar_amount))

                        val cashChange = sale.cash_amount - sale.total
                        if (cashChange > 0) {
                            ReceiptInfoRow("Change:", "$currencySymbol %.2f".format(cashChange), valueColor = SuccessGreen)
                        }
                    }

                    ReceiptDivider()

                    // QR Code
                    val qrBitmap = remember(invoiceId) { generateQrCode(invoiceId, 400) }
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Invoice QR Code",
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Scan to verify invoice", color = TextSecondary, fontSize = 11.sp)
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Thank you! Come again 🙏", color = AccentBlueLight, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(16.dp))

            // WhatsApp number input if triggered
            if (showWhatsappInput) {
                OutlinedTextField(
                    value = whatsappNumber,
                    onValueChange = { whatsappNumber = it },
                    label = { Text("WhatsApp Number (with country code)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceVariant,
                        unfocusedContainerColor = SurfaceVariant,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
            }

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // WhatsApp Share
                Button(
                    onClick = {
                        val number = selectedCustomer?.whatsapp
                            ?: selectedCustomer?.phone
                            ?: whatsappNumber
                        if (number.isBlank()) {
                            showWhatsappInput = true
                        } else {
                            viewModel.logClick("UI_CLICK", "WhatsApp share receipt: $invoiceId")
                            sendWhatsApp(context, number, buildReceiptText(sale, invoiceId))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Share on WhatsApp", fontWeight = FontWeight.SemiBold)
                }

                // Send if number was entered
                if (showWhatsappInput && whatsappNumber.isNotBlank()) {
                    Button(
                        onClick = {
                            viewModel.logClick("UI_CLICK", "WhatsApp send receipt: $invoiceId to $whatsappNumber")
                            sendWhatsApp(context, whatsappNumber, buildReceiptText(sale, invoiceId))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Send to $whatsappNumber") }
                }

                // Print
                OutlinedButton(
                    onClick = {
                        viewModel.logClick("UI_CLICK", "Print receipt: $invoiceId")
                        val mac = appSetupPrefs.printerMac
                        if (mac.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "No printer configured. Set MAC in Printer Settings."
                                )
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Printing...")
                                val receiptText = buildReceiptText(sale, invoiceId)
                                val success = escPosPrinter.printViaBluetooth(mac, receiptText)
                                snackbarHostState.showSnackbar(
                                    if (success) "Receipt sent to printer"
                                    else "Print failed. Check printer connection."
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Print, null, tint = AccentBlue)
                    Spacer(Modifier.width(6.dp))
                    Text("Print Receipt", color = AccentBlue)
                }

                // NEW SALE — primary action
                Button(
                    onClick = {
                        viewModel.resetAfterSale()
                        onNewSale()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, null)
                    Spacer(Modifier.width(8.dp))
                    Text("New Sale", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

@Composable
private fun ReceiptInfoRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ReceiptDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = SurfaceVariant,
        thickness = 0.8.dp
    )
}

private fun formatTimestamp(ts: Long): String {
    return try {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(ts))
    } catch (e: Exception) {
        ts.toString()
    }
}

private fun generateQrCode(content: String, size: Int): Bitmap? {
    return try {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        null
    }
}

private fun buildReceiptText(sale: SaleEntity?, invoiceId: String): String {
    return buildString {
        appendLine("================================")
        appendLine("TILLZO POS")
        appendLine("================================")
        appendLine("Invoice #: ${invoiceId.take(8).uppercase()}")
        if (sale != null) {
            appendLine("Date: ${formatTimestamp(sale.timestamp)}")
            appendLine("Cashier: ${sale.cashier_id.take(20)}")
            appendLine("--------------------------------")
            appendLine("Subtotal:  Rs %.2f".format(sale.subtotal))
            if (sale.tax > 0) appendLine("Tax:       Rs %.2f".format(sale.tax))
            if (sale.discount > 0) appendLine("Discount:  Rs %.2f".format(sale.discount))
            appendLine("TOTAL:     Rs %.2f".format(sale.total))
            appendLine("--------------------------------")
            if (sale.cash_amount > 0) appendLine("Cash:      Rs %.2f".format(sale.cash_amount))
            if (sale.card_amount > 0) appendLine("Card:      Rs %.2f".format(sale.card_amount))
            if (sale.wallet_amount > 0) appendLine("Wallet:    Rs %.2f".format(sale.wallet_amount))
            if (sale.udhaar_amount > 0) appendLine("Udhaar:    Rs %.2f".format(sale.udhaar_amount))
            val change = sale.cash_amount - sale.total
            if (change > 0) appendLine("Change:    Rs %.2f".format(change))
        }
        appendLine("================================")
        appendLine("Thank you! Come again.")
        appendLine("Scan QR on receipt to verify.")
    }
}

private fun sendWhatsApp(context: android.content.Context, phone: String, message: String) {
    val cleanPhone = phone.replace("[^0-9+]".toRegex(), "")
    val encoded = Uri.encode(message)
    val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encoded"
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        // WhatsApp not installed — fallback to browser
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
