package com.tillzo.pos.ui.setup.onboarding

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.File

/**
 * OnboardingScreen — First-run setup wizard (FIX 2026-08-06: Faisal).
 *
 * New user enters: owner name → business name → address → logo → phone →
 * social media → website → portal → app link. On finish, the app moves to
 * Google sign-in, then auto-creates the business sheet + Drive folder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(0) }

    val ownerName by viewModel.ownerName.collectAsState()
    val businessName by viewModel.businessName.collectAsState()
    val countryCode by viewModel.countryCode.collectAsState()
    val taxNumber by viewModel.taxNumber.collectAsState()
    val taxLabel by viewModel.taxLabel.collectAsState()
    val defaultTaxRate by viewModel.defaultTaxRate.collectAsState()
    val taxInclusive by viewModel.taxInclusive.collectAsState()
    val businessAddress by viewModel.businessAddress.collectAsState()
    val businessPhone by viewModel.businessPhone.collectAsState()
    val businessSocial by viewModel.businessSocial.collectAsState()
    val businessWebsite by viewModel.businessWebsite.collectAsState()
    val businessPortal by viewModel.businessPortal.collectAsState()
    val businessAppLink by viewModel.businessAppLink.collectAsState()
    val logoPath by viewModel.logoPath.collectAsState()
    val saving by viewModel.saving.collectAsState()

    LaunchedEffect(Unit) { viewModel.prefillFromExisting() }

    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) viewModel.pickLogo(uri) }

    val steps = listOf(
        "Owner Name", "Business Name", "Country & Tax", "Business Address", "Business Logo",
        "Business Phone", "Social Media", "Website / App Link", "Review"
    )

    val canProceed = when (step) {
        0 -> true // owner name optional
        1 -> businessName.isNotBlank()
        2 -> countryCode.isNotBlank()
        3 -> businessAddress.isNotBlank()
        4 -> true // logo optional
        5 -> businessPhone.isNotBlank()
        else -> true
    }

    Scaffold(
        containerColor = Color(0xFF1A1A1A),
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Set Up Your Business", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            "Step ${step + 1} of ${steps.size} — ${steps[step]}",
                            color = Color(0xFFAAAAAA),
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            )
        },
        bottomBar = {
            Surface(color = Color(0xFF1A1A1A)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // FIX (2026-08-06): nav-bar rule — buttons must sit ABOVE the
                        // system navigation bar. Without navigationBarsPadding the
                        // bottomBar Surface renders underneath the 3-button nav bar,
                        // so "Next" is untappable on devices with a nav bar.
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (step > 0) {
                        OutlinedButton(
                            onClick = { step-- },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) { Text("Back") }
                    }
                    Button(
                        onClick = {
                            if (step < steps.size - 1) step++
                            else viewModel.saveProfile(onSaved = onComplete)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = canProceed && !saving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text(if (step == steps.size - 1) (if (saving) "Saving..." else "Finish & Sign In") else "Next")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { (step + 1f) / steps.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFF333333)
            )
            Spacer(Modifier.height(24.dp))

            when (step) {
                0 -> OnboardingField(
                    title = "What's your name?",
                    subtitle = "Optional — used as the owner / manager name.",
                    value = ownerName,
                    placeholder = "e.g. John Smith",
                    onValue = viewModel::setOwnerName
                )
                1 -> OnboardingField(
                    title = "What's your business name? *",
                    subtitle = "Required — shown on receipts, labels and your Google Sheet.",
                    value = businessName,
                    placeholder = "e.g. Smith's Grocery",
                    onValue = viewModel::setBusinessName
                )
                2 -> CountryTaxStep(
                    selectedCountryCode = countryCode,
                    taxNumber = taxNumber,
                    taxLabel = taxLabel,
                    taxRate = defaultTaxRate,
                    taxInclusive = taxInclusive,
                    onSelectCountry = viewModel::selectCountry,
                    onTaxNumberChange = viewModel::setTaxNumber
                )
                3 -> OnboardingField(
                    title = "Business address *",
                    subtitle = "Required — printed on receipts and used for reports.",
                    value = businessAddress,
                    placeholder = "e.g. 123 Main Street, New York, NY",
                    onValue = viewModel::setBusinessAddress,
                    singleLine = false
                )
                4 -> LogoStep(logoPath = logoPath, onPick = { logoPicker.launch("image/*") })
                5 -> OnboardingField(
                    title = "Business phone number *",
                    subtitle = "Required — customers can call this number from receipts.",
                    value = businessPhone,
                    placeholder = "e.g. +1 555 123 4567",
                    onValue = viewModel::setBusinessPhone
                )
                6 -> OnboardingField(
                    title = "Social media username",
                    subtitle = "Optional — just your @username, no full link needed.",
                    value = businessSocial,
                    placeholder = "e.g. @smithsgrocery",
                    onValue = viewModel::setBusinessSocial
                )
                7 -> OnboardingField(
                    title = "Website / App link",
                    subtitle = "Optional — one link for both your website and app.",
                    value = businessWebsite,
                    placeholder = "e.g. www.smithsgrocery.com",
                    onValue = {
                        viewModel.setBusinessWebsite(it)
                        // FIX (2026-08-06): website + app link are one field
                        viewModel.setBusinessAppLink(it)
                    }
                )
                8 -> ReviewStep(
                    ownerName = ownerName,
                    businessName = businessName,
                    countryCode = countryCode,
                    taxNumber = taxNumber,
                    taxLabel = taxLabel,
                    taxRate = defaultTaxRate,
                    businessAddress = businessAddress,
                    businessPhone = businessPhone,
                    businessSocial = businessSocial,
                    businessWebsite = businessWebsite,
                    businessPortal = businessPortal,
                    businessAppLink = businessAppLink
                )
            }
        }
    }
}

@Composable
private fun OnboardingField(
    title: String,
    subtitle: String,
    value: String,
    placeholder: String,
    onValue: (String) -> Unit,
    singleLine: Boolean = true
) {
    Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))
    Text(subtitle, color = Color(0xFFAAAAAA), fontSize = 14.sp)
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color(0xFF777777)) },
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF4CAF50),
            unfocusedBorderColor = Color(0xFF444444),
            cursorColor = Color(0xFF4CAF50)
        )
    )
}

@Composable
private fun LogoStep(logoPath: String, onPick: () -> Unit) {
    Text("Business logo", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))
    Text(
        "Used on receipts and barcode labels. Optional — you can add one later.",
        color = Color(0xFFAAAAAA),
        fontSize = 14.sp
    )
    Spacer(Modifier.height(20.dp))

    val bitmap = remember(logoPath) {
        if (logoPath.isNotBlank() && File(logoPath).exists()) {
            BitmapFactory.decodeFile(logoPath)?.asImageBitmap()
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF262626))
            .clickable(onClick = onPick),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Business logo",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = Color(0xFF777777),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(if (logoPath.isBlank()) "Tap to choose a logo" else "Loading logo...", color = Color(0xFF999999))
            }
        }
    }
    if (logoPath.isNotBlank()) {
        Spacer(Modifier.height(10.dp))
        Text(
            "✓ Logo selected",
            color = Color(0xFF4CAF50),
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CountryTaxStep(
    selectedCountryCode: String,
    taxNumber: String,
    taxLabel: String,
    taxRate: Double,
    taxInclusive: Boolean,
    onSelectCountry: (String) -> Unit,
    onTaxNumberChange: (String) -> Unit
) {
    Text(
        "Country & Tax System",
        color = Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Text(
        "Select your business country. Tillzo will automatically configure official tax rules, currency, and invoice formats.",
        color = Color(0xFFAAAAAA),
        fontSize = 14.sp
    )
    Spacer(Modifier.height(20.dp))

    // Country selection list
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        com.tillzo.pos.utils.TaxUtils.PRESETS.forEach { preset ->
            val isSelected = preset.code.equals(selectedCountryCode, ignoreCase = true)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectCountry(preset.code) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF1E3A5F) else Color(0xFF242424)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) Color(0xFF1E88E5) else Color(0xFF383838)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(preset.flag, fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            preset.name,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            "${preset.currencySymbol} • ${preset.taxLabel} ${preset.defaultTaxRate}% (${if (preset.taxInclusive) "Inclusive" else "Exclusive"})",
                            color = if (isSelected) Color(0xFF90CAF9) else Color(0xFF888888),
                            fontSize = 12.sp
                        )
                    }
                    if (isSelected) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(20.dp))

    val currentPreset = com.tillzo.pos.utils.TaxUtils.getPreset(selectedCountryCode)
    OutlinedTextField(
        value = taxNumber,
        onValueChange = onTaxNumberChange,
        label = { Text("${currentPreset.taxIdLabel} (Optional)") },
        placeholder = { Text("e.g. 100234567890003") },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF1E88E5),
            unfocusedBorderColor = Color(0xFF444444)
        ),
        shape = RoundedCornerShape(10.dp),
        singleLine = true
    )
}

@Composable
private fun ReviewStep(
    ownerName: String,
    businessName: String,
    countryCode: String,
    taxNumber: String,
    taxLabel: String,
    taxRate: Double,
    businessAddress: String,
    businessPhone: String,
    businessSocial: String,
    businessWebsite: String,
    businessPortal: String,
    businessAppLink: String
) {
    Text("Review your details", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))
    Text(
        "Everything looks good? Press Finish to sign in with Google and create your business workspace.",
        color = Color(0xFFAAAAAA),
        fontSize = 14.sp
    )
    Spacer(Modifier.height(20.dp))

    val preset = com.tillzo.pos.utils.TaxUtils.getPreset(countryCode)
    ReviewRow("Owner", ownerName)
    ReviewRow("Business", businessName)
    ReviewRow("Country", "${preset.flag} ${preset.name} (${preset.currencySymbol})")
    ReviewRow("Tax Rule", "$taxLabel ${taxRate}% (${if (preset.taxInclusive) "Inclusive" else "Exclusive"})")
    if (taxNumber.isNotBlank()) {
        ReviewRow(preset.taxIdLabel, taxNumber)
    }
    ReviewRow("Address", businessAddress)
    ReviewRow("Phone", businessPhone)
    ReviewRow("Social", businessSocial)
    ReviewRow("Website / App", businessWebsite)

    Spacer(Modifier.height(12.dp))
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A2B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
            Spacer(Modifier.width(10.dp))
            Text(
                "After this: Google sign-in → your business Sheet + Drive folder are created automatically.",
                color = Color(0xFFCCE8D6),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(
            label,
            color = Color(0xFF888888),
            fontSize = 13.sp,
            modifier = Modifier.width(90.dp)
        )
        Text(
            value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
