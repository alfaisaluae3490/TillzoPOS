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
 *   [Print Receipt]     — ESC/POS Bluetooth printing (escPosPrinter), MAC Printer Settings se
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
    val cartItemsState by viewModel.cartItems.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var whatsappNumber by remember { mutableStateOf("") }
    var showWhatsappInput by remember { mutableStateOf(false) }

    val appSetupPrefs = remember { AppSetupPrefs(context) }
    val currencySymbol = appSetupPrefs.currencySymbol.ifBlank { "$" }
    val businessName = appSetupPrefs.businessName.ifBlank { "TILLZO POS" }
    val businessAddress = appSetupPrefs.businessAddress
    val businessPhone = appSetupPrefs.businessPhone
    val businessSocial = appSetupPrefs.businessSocial
    val businessWebsite = appSetupPrefs.businessWebsite
    val taxNumber = appSetupPrefs.taxNumber
    val taxLabel = appSetupPrefs.taxLabel.ifBlank { "TAX" }
    val taxInclusive = appSetupPrefs.taxInclusive
    val enableZatcaQr = appSetupPrefs.enableZatcaQr
    val countryPreset = com.tillzo.pos.utils.TaxUtils.getPreset(appSetupPrefs.countryCode)
    val scope = rememberCoroutineScope()
    val escPosPrinter = remember { EscPosPrinter() }

    // FIX (2026-08-26, L6C-RECEIPT-LOOP): system back press par bhi New Sale
    // jaisa behaviour — cart/saleResult reset + home pop. Pehle back sirf pop
    // karta tha, home ka LaunchedEffect(saleResult) wapas Success dekh kar
    // receipt par RE-NAVIGATE kar deta tha (infinite loop).
    androidx.activity.compose.BackHandler {
        viewModel.resetAfterSale()
        onNewSale()
    }

    val sale = (saleResult as? SaleResult.Success)?.sale

    // Parse items from sale.items_json if available, else use current cart
    val items: List<com.tillzo.pos.domain.model.CartItem> = remember(sale, cartItemsState) {
        if (sale != null && sale.items_json.isNotBlank()) {
            try {
                val listType = object : com.google.gson.reflect.TypeToken<List<com.tillzo.pos.domain.model.CartItem>>() {}.type
                com.google.gson.Gson().fromJson<List<com.tillzo.pos.domain.model.CartItem>>(sale.items_json, listType) ?: cartItemsState
            } catch (e: Exception) {
                cartItemsState
            }
        } else {
            cartItemsState
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Receipt & Tax Invoice", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
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
                    // Document Badge
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = if (taxNumber.isNotBlank()) "TAX INVOICE" else "SALES INVOICE",
                            color = AccentBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Shop Header
                    Text(
                        text = businessName.uppercase(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    if (businessAddress.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(businessAddress, color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                    val contacts = listOfNotNull(
                        businessPhone.takeIf { it.isNotBlank() },
                        businessSocial.takeIf { it.isNotBlank() },
                        businessWebsite.takeIf { it.isNotBlank() }
                    ).joinToString(" • ")
                    if (contacts.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(contacts, color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                    if (taxNumber.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text("${countryPreset.taxIdLabel}: $taxNumber", color = AccentBlueLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    ReceiptDivider()

                    // Invoice & Customer Info
                    if (sale != null) {
                        ReceiptInfoRow("Invoice #:", sale.sync_uuid.take(8).uppercase())
                        ReceiptInfoRow("Date & Time:", formatTimestamp(sale.timestamp))
                        ReceiptInfoRow("Cashier:", sale.cashier_id.take(20))
                        ReceiptInfoRow("Terminal:", sale.pos_terminal_id)
                    } else {
                        ReceiptInfoRow("Invoice #:", invoiceId.take(8).uppercase())
                    }

                    if (selectedCustomer != null) {
                        ReceiptDivider()
                        ReceiptInfoRow("Customer:", selectedCustomer!!.name)
                        if (selectedCustomer!!.phone.isNotBlank()) {
                            ReceiptInfoRow("Phone:", selectedCustomer!!.phone)
                        }
                        if (!selectedCustomer!!.address.isNullOrBlank()) {
                            ReceiptInfoRow("Address:", selectedCustomer!!.address!!)
                        }
                    }

                    ReceiptDivider()

                    // Items Header
                    Row(Modifier.fillMaxWidth()) {
                        Text("ITEM", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
                        Text("QTY", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                        Text("TAX", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f), textAlign = TextAlign.Center)
                        Text("TOTAL", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                    }
                    Spacer(Modifier.height(4.dp))

                    items.forEach { item ->
                        val lineBase = item.quantity * item.pricePerUnit
                        val lineTax = if (taxInclusive) {
                            lineBase - (lineBase / (1.0 + (item.taxPercent / 100.0)))
                        } else {
                            lineBase * (item.taxPercent / 100.0)
                        }
                        val lineTotal = if (taxInclusive) lineBase else (lineBase + lineTax)

                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1.3f)) {
                                Text(item.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                Text("@ $currencySymbol%.2f".format(item.pricePerUnit), color = TextSecondary, fontSize = 10.sp)
                            }
                            val qtyStr = if (item.unit in listOf("KG", "GM", "ML")) "%.3f %s".format(item.quantity, item.unit) else "${item.quantity.toInt()} %s".format(item.unit)
                            Text(qtyStr, color = TextPrimary, fontSize = 11.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                            Text("%.0f%%".format(item.taxPercent), color = TextSecondary, fontSize = 10.sp, modifier = Modifier.weight(0.6f), textAlign = TextAlign.Center)
                            Text("$currencySymbol %.2f".format(lineTotal), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                        }
                    }

                    ReceiptDivider()

                    // Totals
                    if (sale != null) {
                        ReceiptInfoRow("Subtotal (Gross):", "$currencySymbol %.2f".format(sale.subtotal))
                        if (sale.tax > 0) {
                            val taxTitle = if (taxInclusive) "$taxLabel (Included):" else "$taxLabel Total:"
                            ReceiptInfoRow(taxTitle, "$currencySymbol %.2f".format(sale.tax))
                        }
                        if (sale.discount > 0) ReceiptInfoRow("Discount:", "- $currencySymbol %.2f".format(sale.discount))
                        ReceiptDivider()
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("GRAND TOTAL:", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Text("$currencySymbol %.2f".format(sale.total), color = AccentBlue, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                        Spacer(Modifier.height(6.dp))

                        // Payment breakdown
                        if (sale.cash_amount > 0) ReceiptInfoRow("Cash Paid:", "$currencySymbol %.2f".format(sale.cash_amount))
                        if (sale.card_amount > 0) ReceiptInfoRow("Card Paid:", "$currencySymbol %.2f".format(sale.card_amount))
                        if (sale.wallet_amount > 0) ReceiptInfoRow("Wallet / Online:", "$currencySymbol %.2f".format(sale.wallet_amount))
                        if (sale.udhaar_amount > 0) ReceiptInfoRow("Credit (Udhaar):", "$currencySymbol %.2f".format(sale.udhaar_amount))

                        val cashChange = sale.cash_amount - sale.total
                        if (cashChange > 0) {
                            ReceiptInfoRow("Change Returned:", "$currencySymbol %.2f".format(cashChange), valueColor = SuccessGreen)
                        }
                    }

                    ReceiptDivider()

                    // QR Code (ZATCA compliant TLV Base64 or standard verification)
                    val qrContent = remember(sale, invoiceId) {
                        if (enableZatcaQr && sale != null) {
                            val isoDate = try {
                                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                                }.format(Date(sale.timestamp))
                            } catch (_: Exception) {
                                sale.timestamp.toString()
                            }
                            com.tillzo.pos.utils.TaxUtils.generateZatcaTlvBase64(
                                sellerName = businessName,
                                vatNumber = taxNumber.ifBlank { "N/A" },
                                isoTimestamp = isoDate,
                                totalWithVat = "%.2f".format(sale.total),
                                vatTotal = "%.2f".format(sale.tax)
                            )
                        } else {
                            invoiceId
                        }
                    }
                    val qrBitmap = remember(qrContent) { generateQrCode(qrContent, 400) }
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Invoice QR Code",
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(if (enableZatcaQr) "ZATCA / Tax Verified QR" else "Scan to verify invoice", color = TextSecondary, fontSize = 11.sp)
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Thank you for your business! 🙏", color = AccentBlueLight, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // PDF Action Row: Open PDF & Print PDF
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Open / View PDF
                    Button(
                        onClick = {
                            viewModel.logClick("UI_CLICK", "Open PDF Invoice: $invoiceId")
                            if (sale != null) {
                                val pdfFile = com.tillzo.pos.utils.pdf.InvoicePdfGenerator.generateInvoicePdf(
                                    context, sale, items, selectedCustomer, appSetupPrefs
                                )
                                if (pdfFile != null) {
                                    com.tillzo.pos.utils.pdf.InvoicePdfGenerator.openPdf(context, pdfFile)
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Failed to generate PDF") }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null, tint = Color(0xFFF87171))
                        Spacer(Modifier.width(6.dp))
                        Text("View PDF", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    // Print PDF (System PrintManager)
                    Button(
                        onClick = {
                            viewModel.logClick("UI_CLICK", "Print PDF Invoice: $invoiceId")
                            if (sale != null) {
                                val pdfFile = com.tillzo.pos.utils.pdf.InvoicePdfGenerator.generateInvoicePdf(
                                    context, sale, items, selectedCustomer, appSetupPrefs
                                )
                                if (pdfFile != null) {
                                    com.tillzo.pos.utils.pdf.InvoicePdfGenerator.printPdf(context, pdfFile, invoiceId.take(8).uppercase())
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Failed to prepare PDF for printing") }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Print, null, tint = Color(0xFF60A5FA))
                        Spacer(Modifier.width(6.dp))
                        Text("Print PDF", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                // WhatsApp Share PDF
                Button(
                    onClick = {
                        val number = selectedCustomer?.whatsapp
                            ?: selectedCustomer?.phone
                            ?: whatsappNumber
                        if (number.isBlank() && !showWhatsappInput) {
                            showWhatsappInput = true
                        } else {
                            viewModel.logClick("UI_CLICK", "WhatsApp share PDF receipt: $invoiceId")
                            if (sale != null) {
                                val pdfFile = com.tillzo.pos.utils.pdf.InvoicePdfGenerator.generateInvoicePdf(
                                    context, sale, items, selectedCustomer, appSetupPrefs
                                )
                                val receiptText = buildReceiptText(sale, invoiceId, currencySymbol, businessName, taxLabel, taxNumber, businessWebsite, businessPhone)
                                if (pdfFile != null) {
                                    com.tillzo.pos.utils.pdf.InvoicePdfGenerator.sharePdfOnWhatsApp(
                                        context, pdfFile, number, receiptText
                                    )
                                } else {
                                    sendWhatsApp(context, number, receiptText)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Share PDF on WhatsApp", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                // Send if number was entered
                if (showWhatsappInput && whatsappNumber.isNotBlank()) {
                    Button(
                        onClick = {
                            viewModel.logClick("UI_CLICK", "WhatsApp send receipt: $invoiceId to $whatsappNumber")
                            if (sale != null) {
                                val pdfFile = com.tillzo.pos.utils.pdf.InvoicePdfGenerator.generateInvoicePdf(
                                    context, sale, items, selectedCustomer, appSetupPrefs
                                )
                                val receiptText = buildReceiptText(sale, invoiceId, currencySymbol, businessName, taxLabel, taxNumber, businessWebsite, businessPhone)
                                if (pdfFile != null) {
                                    com.tillzo.pos.utils.pdf.InvoicePdfGenerator.sharePdfOnWhatsApp(
                                        context, pdfFile, whatsappNumber, receiptText
                                    )
                                } else {
                                    sendWhatsApp(context, whatsappNumber, receiptText)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Send PDF to $whatsappNumber") }
                }

                // Bluetooth Thermal Receipt Print
                OutlinedButton(
                    onClick = {
                        viewModel.logClick("UI_CLICK", "Print thermal receipt: $invoiceId")
                        val mac = appSetupPrefs.printerMac
                        if (mac.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "No Bluetooth printer configured. Set MAC in Printer Settings."
                                )
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Printing to Bluetooth Thermal Printer...")
                                val receiptText = buildReceiptText(sale, invoiceId, currencySymbol, businessName, taxLabel, taxNumber, businessWebsite, businessPhone)
                                val success = escPosPrinter.printViaBluetooth(mac, receiptText)
                                snackbarHostState.showSnackbar(
                                    if (success) "Receipt sent to printer"
                                    else "Print failed. Check printer connection."
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Receipt, null, tint = AccentBlue)
                    Spacer(Modifier.width(6.dp))
                    Text("Print Thermal Roll Receipt (ESC/POS)", color = AccentBlue, fontSize = 13.sp)
                }

                // NEW SALE — primary action
                Button(
                    onClick = {
                        viewModel.resetAfterSale()
                        onNewSale()
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
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

private fun buildReceiptText(
    sale: SaleEntity?,
    invoiceId: String,
    currencySymbol: String = "$",
    businessName: String = "TILLZO POS",
    taxLabel: String = "TAX",
    taxNumber: String = "",
    businessWebsite: String = "",
    businessPhone: String = ""
): String {
    return buildString {
        appendLine("================================")
        appendLine(businessName.uppercase())
        if (businessPhone.isNotBlank()) appendLine("Tel: $businessPhone")
        if (businessWebsite.isNotBlank()) appendLine("Web: $businessWebsite")
        if (taxNumber.isNotBlank()) {
            appendLine("Tax ID / TRN: $taxNumber")
        }
        appendLine("================================")
        appendLine("Invoice #: ${invoiceId.take(8).uppercase()}")
        if (sale != null) {
            appendLine("Date: ${formatTimestamp(sale.timestamp)}")
            appendLine("Cashier: ${sale.cashier_id.take(20)}")
            appendLine("Terminal: ${sale.pos_terminal_id}")
            appendLine("--------------------------------")
            appendLine("Subtotal:  $currencySymbol %.2f".format(sale.subtotal))
            if (sale.tax > 0) appendLine("$taxLabel:       $currencySymbol %.2f".format(sale.tax))
            if (sale.discount > 0) appendLine("Discount:  $currencySymbol %.2f".format(sale.discount))
            appendLine("TOTAL:     $currencySymbol %.2f".format(sale.total))
            appendLine("--------------------------------")
            if (sale.cash_amount > 0) appendLine("Cash:      $currencySymbol %.2f".format(sale.cash_amount))
            if (sale.card_amount > 0) appendLine("Card:      $currencySymbol %.2f".format(sale.card_amount))
            if (sale.wallet_amount > 0) appendLine("Wallet:    $currencySymbol %.2f".format(sale.wallet_amount))
            if (sale.udhaar_amount > 0) appendLine("Credit:    $currencySymbol %.2f".format(sale.udhaar_amount))
            val change = sale.cash_amount - sale.total
            if (change > 0) appendLine("Change:    $currencySymbol %.2f".format(change))
        }
        appendLine("================================")
        appendLine("Thank you for your business!")
        appendLine("Scan QR on invoice to verify.")
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
