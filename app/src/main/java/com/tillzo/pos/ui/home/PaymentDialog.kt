package com.tillzo.pos.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tillzo.pos.domain.model.CartItem
import com.tillzo.pos.ui.theme.*

/**
 * M4 Payment Bottom Sheet Dialog.
 *
 * Supports: Cash, Card, Digital Wallet, Credit
 * Split payment: user may fill multiple fields — sum must equal grandTotal before confirm enables.
 * Udhaar section: customer search OR new customer form.
 * Cash change: shown when cash > grandTotal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDialog(
    cartTotal: Double,
    cartItems: List<CartItem>,
    viewModel: PosViewModel,
    currencySymbol: String = "Rs",
    onDismiss: () -> Unit
) {
    val paymentBreakdown by viewModel.paymentBreakdown.collectAsStateWithLifecycle()
    val remainingAmount by viewModel.remainingAmount.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val customerResults by viewModel.customerSearchResults.collectAsStateWithLifecycle()
    val customerQuery by viewModel.customerQuery.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()

    // Local UI state
    var cashText by remember { mutableStateOf("") }
    var cardText by remember { mutableStateOf("") }
    var walletText by remember { mutableStateOf("") }
    var udhaarText by remember { mutableStateOf("") }
    var showUdhaarSection by remember { mutableStateOf(false) }
    var showNewCustomerForm by remember { mutableStateOf(false) }
    var newCustName by remember { mutableStateOf("") }
    var newCustPhone by remember { mutableStateOf("") }
    var newCustWhatsapp by remember { mutableStateOf("") }

    // Partial payment / Khata allocation
    var addBalanceToKhata by remember { mutableStateOf(false) }

    val cashAmount = cashText.toDoubleOrNull() ?: 0.0
    val partialCash = cashAmount > 0 && cashAmount < cartTotal
    val balanceForKhata = if (partialCash && addBalanceToKhata) cartTotal - cashAmount else 0.0

    val cashChange = remember(cashText, cartTotal) {
        val cash = cashText.toDoubleOrNull() ?: 0.0
        if (cash > cartTotal) cash - cartTotal else 0.0
    }

    // When Khata balance is active, auto-set the udhaar amount
    LaunchedEffect(balanceForKhata) {
        if (balanceForKhata > 0) {
            viewModel.onPaymentAmountChanged(PaymentMethod.UDHAAR, balanceForKhata)
        } else if (!showUdhaarSection) {
            viewModel.onPaymentAmountChanged(PaymentMethod.UDHAAR, 0.0)
        }
    }

    val isConfirmEnabled = remainingAmount <= 0.01 && !isProcessing &&
        ((paymentBreakdown.udhaarAmount <= 0.0) || selectedCustomer != null)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        windowInsets = WindowInsets(0),
        containerColor = SurfaceDark,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Payment", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Total: $currencySymbol %.2f".format(cartTotal), color = AccentBlue, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))

            // Payment Method Tiles
            Text("Select Payment Method(s)", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))

            // CASH
            PaymentMethodField(
                icon = Icons.Default.Money,
                label = "Cash",
                value = cashText,
                onValueChange = { v ->
                    cashText = v
                    viewModel.onPaymentAmountChanged(PaymentMethod.CASH, v.toDoubleOrNull() ?: 0.0)
                }
            )

            // Quick Tender Bill Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val denominations = listOf(10.0, 20.0, 50.0, 100.0, 500.0, 1000.0)
                denominations.forEach { amount ->
                    OutlinedButton(
                        onClick = {
                            val current = cashText.toDoubleOrNull() ?: 0.0
                            val newAmount = current + amount
                            cashText = if (newAmount == newAmount.toLong().toDouble()) "%.0f".format(newAmount) else "%.2f".format(newAmount)
                            viewModel.onPaymentAmountChanged(PaymentMethod.CASH, newAmount)
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.weight(1f).height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue),
                        border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("+$currencySymbol %.0f".format(amount), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (cashChange > 0) {
                Text(
                    "Change to return: $currencySymbol %.2f".format(cashChange),
                    color = SuccessGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Partial Payment — Khata Allocation
            if (partialCash) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (addBalanceToKhata) AccentBlue.copy(alpha = 0.15f) else SurfaceVariant)
                        .clickable { addBalanceToKhata = !addBalanceToKhata }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = if (addBalanceToKhata) AccentBlue else TextSecondary
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Add $currencySymbol %.2f balance to customer account".format(balanceForKhata),
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text("Remaining amount goes to customer credit", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = addBalanceToKhata,
                        onCheckedChange = { addBalanceToKhata = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentBlue)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // Khata customer selection (when balance will be added to Khata)
            if (addBalanceToKhata && balanceForKhata > 0) {
                Text("Select Customer for Account Balance", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))

                if (selectedCustomer != null) {
                    SelectedCustomerCard(
                        customer = selectedCustomer!!,
                        onClear = {
                            viewModel.clearSelectedCustomer()
                            addBalanceToKhata = false
                        }
                    )
                } else {
                    OutlinedTextField(
                        value = customerQuery,
                        onValueChange = { viewModel.onCustomerQueryChanged(it) },
                        label = { Text("Search customer name or phone", color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedTextFieldColors(),
                        singleLine = true
                    )

                    if (customerResults.isNotEmpty()) {
                        LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
                            items(customerResults) { customer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectCustomer(customer) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(customer.name, color = TextPrimary, fontSize = 14.sp)
                                        Text(customer.phone, color = TextSecondary, fontSize = 12.sp)
                                    }
                                    Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
                                }
                                HorizontalDivider(color = SurfaceVariant)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { showNewCustomerForm = !showNewCustomerForm }) {
                        Icon(Icons.Default.PersonAdd, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add New Customer", color = AccentBlue, fontSize = 13.sp)
                    }

                    if (showNewCustomerForm) {
                        NewCustomerForm(
                            name = newCustName,
                            phone = newCustPhone,
                            whatsapp = newCustWhatsapp,
                            onNameChange = { newCustName = it },
                            onPhoneChange = { newCustPhone = it },
                            onWhatsappChange = { newCustWhatsapp = it },
                            onSave = {
                                if (newCustName.isNotBlank() && newCustPhone.isNotBlank()) {
                                    viewModel.createAndSelectNewCustomer(newCustName, newCustPhone, newCustWhatsapp)
                                    showNewCustomerForm = false
                                }
                            }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // CARD
            PaymentMethodField(
                icon = Icons.Default.CreditCard,
                label = "Card",
                value = cardText,
                onValueChange = { v ->
                    cardText = v
                    viewModel.onPaymentAmountChanged(PaymentMethod.CARD, v.toDoubleOrNull() ?: 0.0)
                }
            )

            // WALLET
            PaymentMethodField(
                icon = Icons.Default.AccountBalanceWallet,
                label = "Digital Wallet",
                value = walletText,
                onValueChange = { v ->
                    walletText = v
                    viewModel.onPaymentAmountChanged(PaymentMethod.WALLET, v.toDoubleOrNull() ?: 0.0)
                }
            )

            // UDHAAR toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (showUdhaarSection) AccentBlue.copy(alpha = 0.15f) else SurfaceVariant)
                    .clickable {
                        showUdhaarSection = !showUdhaarSection
                        if (!showUdhaarSection) {
                            udhaarText = ""
                            viewModel.onPaymentAmountChanged(PaymentMethod.UDHAAR, 0.0)
                        }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = if (showUdhaarSection) AccentBlue else TextSecondary)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Credit", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Customer must be selected", color = TextSecondary, fontSize = 11.sp)
                }
                Switch(
                    checked = showUdhaarSection,
                    onCheckedChange = { checked ->
                        showUdhaarSection = checked
                        if (!checked) {
                            udhaarText = ""
                            viewModel.onPaymentAmountChanged(PaymentMethod.UDHAAR, 0.0)
                            viewModel.clearSelectedCustomer()
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentBlue)
                )
            }
            Spacer(Modifier.height(8.dp))

            // Udhaar section
            if (showUdhaarSection) {
                OutlinedTextField(
                    value = udhaarText,
                    onValueChange = { v ->
                        udhaarText = v
                        viewModel.onPaymentAmountChanged(PaymentMethod.UDHAAR, v.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("Credit Amount", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, null, tint = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedTextFieldColors(),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))

                // Customer section
                Text("Select Customer for Credit", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))

                if (selectedCustomer != null) {
                    SelectedCustomerCard(
                        customer = selectedCustomer!!,
                        onClear = { viewModel.clearSelectedCustomer() }
                    )
                } else {
                    // Search field
                    OutlinedTextField(
                        value = customerQuery,
                        onValueChange = { viewModel.onCustomerQueryChanged(it) },
                        label = { Text("Search customer name or phone", color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedTextFieldColors(),
                        singleLine = true
                    )

                    if (customerResults.isNotEmpty()) {
                        LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
                            items(customerResults) { customer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectCustomer(customer) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(customer.name, color = TextPrimary, fontSize = 14.sp)
                                        Text(customer.phone, color = TextSecondary, fontSize = 12.sp)
                                    }
                                    Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
                                }
                                HorizontalDivider(color = SurfaceVariant)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { showNewCustomerForm = !showNewCustomerForm }) {
                        Icon(Icons.Default.PersonAdd, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add New Customer", color = AccentBlue, fontSize = 13.sp)
                    }

                    if (showNewCustomerForm) {
                        NewCustomerForm(
                            name = newCustName,
                            phone = newCustPhone,
                            whatsapp = newCustWhatsapp,
                            onNameChange = { newCustName = it },
                            onPhoneChange = { newCustPhone = it },
                            onWhatsappChange = { newCustWhatsapp = it },
                            onSave = {
                                if (newCustName.isNotBlank() && newCustPhone.isNotBlank()) {
                                    viewModel.createAndSelectNewCustomer(newCustName, newCustPhone, newCustWhatsapp)
                                    showNewCustomerForm = false
                                }
                            }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Remaining indicator
            if (paymentBreakdown.total > 0) {
                val remaining = (cartTotal - paymentBreakdown.total).coerceAtLeast(0.0)
                val color = if (remaining <= 0.01) SuccessGreen else ErrorRed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.1f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Remaining:", color = color, fontSize = 14.sp)
                    Text("$currencySymbol %.2f".format(remaining), color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
            }

            // Udhaar without customer warning
            if (showUdhaarSection && paymentBreakdown.udhaarAmount > 0 && selectedCustomer == null) {
                Text(
                    "⚠ Please select a customer to proceed with Credit",
                    color = ErrorRed, fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Confirm Button
            Button(
                onClick = { viewModel.completeSale() },
                enabled = isConfirmEnabled,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, disabledContainerColor = SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Confirm Payment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PaymentMethodField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = if (value.isNotEmpty()) AccentBlue else TextSecondary) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = outlinedTextFieldColors(),
        singleLine = true
    )
}

@Composable
private fun SelectedCustomerCard(
    customer: com.tillzo.pos.data.local.entity.CustomerEntity,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AccentBlue.copy(alpha = 0.1f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Person, null, tint = AccentBlue)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(customer.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(customer.phone, color = TextSecondary, fontSize = 12.sp)
        }
        IconButton(onClick = onClear) {
            Icon(Icons.Default.Close, null, tint = ErrorRed)
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun NewCustomerForm(
    name: String, phone: String, whatsapp: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onWhatsappChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceVariant)
            .padding(12.dp)
    ) {
        Text("New Customer", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name *", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = outlinedTextFieldColors(),
            singleLine = true
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Phone *", color = TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            colors = outlinedTextFieldColors(),
            singleLine = true
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = whatsapp,
            onValueChange = onWhatsappChange,
            label = { Text("WhatsApp (optional)", color = TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            colors = outlinedTextFieldColors(),
            singleLine = true
        )
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onSave,
            enabled = name.isNotBlank() && phone.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            Text("Save & Select Customer")
        }
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = SurfaceVariant,
    unfocusedContainerColor = SurfaceVariant,
    focusedBorderColor = AccentBlue,
    unfocusedBorderColor = Color.Transparent
)
