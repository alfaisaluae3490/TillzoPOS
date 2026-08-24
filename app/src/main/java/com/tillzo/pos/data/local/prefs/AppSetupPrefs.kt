package com.tillzo.pos.data.local.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AppSetupPrefs — Stores all one-time provisioning results.
 *
 * Written ONCE during first sign-in auto-provisioning.
 * Read on every app launch to skip sign-in / provisioning.
 */
@Singleton
class AppSetupPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            "tillzo_setup_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        const val KEY_IS_PROVISIONED     = "is_provisioned"
        const val KEY_SPREADSHEET_ID     = "spreadsheet_id"
        const val KEY_USER_EMAIL         = "user_email"
        const val KEY_USER_DISPLAY_NAME  = "user_display_name"
        const val KEY_IS_PIN_ENABLED     = "is_pin_enabled"
        const val KEY_GRN_FOLDER_ID      = "grn_folder_id"
        const val KEY_GRN_FOLDER_NAME    = "grn_folder_name"
        const val KEY_PRINTER_MAC       = "printer_mac"
        const val KEY_PRINTER_IP        = "printer_ip"
        const val KEY_CURRENCY_SYMBOL   = "currency_symbol"
        const val KEY_ADMIN_PASSCODE    = "admin_passcode"
        const val KEY_BLOCK_NEGATIVE_STOCK = "block_negative_stock"
        const val KEY_BLOCK_SCREEN_CAPTURE = "block_screen_capture"

        // ── Global Tax & Compliance ─────────────────────────────────────────
        const val KEY_COUNTRY_CODE      = "country_code"
        const val KEY_TAX_NUMBER        = "tax_number"
        const val KEY_TAX_LABEL         = "tax_label"
        const val KEY_DEFAULT_TAX_RATE  = "default_tax_rate"
        const val KEY_ENABLE_ZATCA_QR   = "enable_zatca_qr"

        // ── Onboarding / Business Profile (FIX 2026-08-06: Faisal) ──────────
        const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        const val KEY_OWNER_NAME          = "owner_name"
        const val KEY_BUSINESS_NAME       = "business_name"
        const val KEY_BUSINESS_ADDRESS    = "business_address"
        const val KEY_BUSINESS_LOGO_PATH  = "business_logo_path"
        const val KEY_BUSINESS_PHONE      = "business_phone"
        const val KEY_BUSINESS_SOCIAL     = "business_social"
        const val KEY_BUSINESS_WEBSITE    = "business_website"
        const val KEY_BUSINESS_PORTAL     = "business_portal"
        const val KEY_BUSINESS_APP_LINK   = "business_app_link"
        const val KEY_BUSINESS_FOLDER_ID  = "business_folder_id"
    }

    // ── Read ──────────────────────────────────────────────────────────────

    val isProvisioned: Boolean
        get() = prefs.getBoolean(KEY_IS_PROVISIONED, false)

    val spreadsheetId: String
        get() = prefs.getString(KEY_SPREADSHEET_ID, "") ?: ""

    val userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    val userDisplayName: String
        get() = prefs.getString(KEY_USER_DISPLAY_NAME, "") ?: ""

    var isPinEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_PIN_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_PIN_ENABLED, value).apply()

    var grnFolderId: String
        get() = prefs.getString(KEY_GRN_FOLDER_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GRN_FOLDER_ID, value).apply()

    var grnFolderName: String
        get() = prefs.getString(KEY_GRN_FOLDER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GRN_FOLDER_NAME, value).apply()

    // ── Printer Configuration ─────────────────────────────────────────────────

    var printerMac: String
        get() = prefs.getString(KEY_PRINTER_MAC, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PRINTER_MAC, value).apply()

    var printerIp: String
        get() = prefs.getString(KEY_PRINTER_IP, "192.168.1.100") ?: "192.168.1.100"
        set(value) = prefs.edit().putString(KEY_PRINTER_IP, value).apply()

    // ── Currency Configuration ───────────────────────────────────────────────

    var currencySymbol: String
        get() = prefs.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$"
        set(value) = prefs.edit().putString(KEY_CURRENCY_SYMBOL, value).apply()

    // ── Admin Passcode ────────────────────────────────────────────────────────

    var adminPasscode: String
        get() = prefs.getString(KEY_ADMIN_PASSCODE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ADMIN_PASSCODE, value).apply()

    // ── Stock Control ───────────────────────────────────────────────────────────

    var blockNegativeStock: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_NEGATIVE_STOCK, false)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_NEGATIVE_STOCK, value).apply()

    // ── Global Tax & Compliance Configuration ──────────────────────────────────
    var countryCode: String
        get() = prefs.getString(KEY_COUNTRY_CODE, "OTHER") ?: "OTHER"
        set(value) = prefs.edit().putString(KEY_COUNTRY_CODE, value).apply()

    var taxNumber: String
        get() = prefs.getString(KEY_TAX_NUMBER, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TAX_NUMBER, value).apply()

    var taxLabel: String
        get() = prefs.getString(KEY_TAX_LABEL, "VAT") ?: "VAT"
        set(value) = prefs.edit().putString(KEY_TAX_LABEL, value).apply()

    var defaultTaxRate: Double
        get() = prefs.getFloat(KEY_DEFAULT_TAX_RATE, 0.0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_DEFAULT_TAX_RATE, value.toFloat()).apply()

    var enableZatcaQr: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_ZATCA_QR, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_ZATCA_QR, value).apply()

    // true = prices include tax (tax shown separately, total unchanged)
    // false = tax added on top of subtotal
    var taxInclusive: Boolean
        get() = prefs.getBoolean("KEY_TAX_INCLUSIVE", true)
        set(value) = prefs.edit().putBoolean("KEY_TAX_INCLUSIVE", value).apply()

    fun applyCountryPreset(preset: com.tillzo.pos.utils.CountryTaxPreset) {
        countryCode = preset.code
        currencySymbol = preset.currencySymbol
        taxLabel = preset.taxLabel
        defaultTaxRate = preset.defaultTaxRate
        taxInclusive = preset.taxInclusive
        enableZatcaQr = preset.enableZatcaQr
    }

    // ── Security (OVERNIGHT-AUDIT Phase 1c, 2026-08-23) ─────────────────────────
    // true (default) = FLAG_SECURE on all activities: screenshots + screen
    // recording blocked (bank-level). false = capture allowed (demo/sharing).
    var blockScreenCapture: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_SCREEN_CAPTURE, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_SCREEN_CAPTURE, value).apply()

    // ── Loyalty Program (FIX 2026-08-06: industry-standard) ────────────────────
    // Points earned per Rs/currency unit spent; 100 points = 1 currency unit discount
    var loyaltyEnabled: Boolean
        get() = prefs.getBoolean("KEY_LOYALTY_ENABLED", true)
        set(value) = prefs.edit().putBoolean("KEY_LOYALTY_ENABLED", value).apply()

    var loyaltyPointsPerCurrency: Double
        get() = prefs.getFloat("KEY_LOYALTY_RATE", 1f).toDouble()
        set(value) = prefs.edit().putFloat("KEY_LOYALTY_RATE", value.toFloat()).apply()

    fun saveGrnFolder(folderId: String, name: String) {
        prefs.edit()
            .putString(KEY_GRN_FOLDER_ID, folderId)
            .putString(KEY_GRN_FOLDER_NAME, name)
            .apply()
    }

    // ── Write ─────────────────────────────────────────────────────────────

    fun saveUser(email: String, displayName: String) {
        prefs.edit()
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_DISPLAY_NAME, displayName)
            .apply()
    }

    // ── Onboarding / Business Profile (FIX 2026-08-06: Faisal) ─────────────

    val onboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)

    val ownerName: String
        get() = prefs.getString(KEY_OWNER_NAME, "") ?: ""

    val businessName: String
        get() = prefs.getString(KEY_BUSINESS_NAME, "") ?: ""

    val businessAddress: String
        get() = prefs.getString(KEY_BUSINESS_ADDRESS, "") ?: ""

    val businessLogoPath: String
        get() = prefs.getString(KEY_BUSINESS_LOGO_PATH, "") ?: ""

    val businessPhone: String
        get() = prefs.getString(KEY_BUSINESS_PHONE, "") ?: ""

    val businessSocial: String
        get() = prefs.getString(KEY_BUSINESS_SOCIAL, "") ?: ""

    val businessWebsite: String
        get() = prefs.getString(KEY_BUSINESS_WEBSITE, "") ?: ""

    val businessPortal: String
        get() = prefs.getString(KEY_BUSINESS_PORTAL, "") ?: ""

    val businessAppLink: String
        get() = prefs.getString(KEY_BUSINESS_APP_LINK, "") ?: ""

    val businessFolderId: String
        get() = prefs.getString(KEY_BUSINESS_FOLDER_ID, "") ?: ""

    fun saveBusinessProfile(
        ownerName: String,
        businessName: String,
        businessAddress: String,
        businessPhone: String,
        businessSocial: String,
        businessWebsite: String,
        businessPortal: String,
        businessAppLink: String
    ) {
        prefs.edit()
            .putString(KEY_OWNER_NAME, ownerName)
            .putString(KEY_BUSINESS_NAME, businessName)
            .putString(KEY_BUSINESS_ADDRESS, businessAddress)
            .putString(KEY_BUSINESS_PHONE, businessPhone)
            .putString(KEY_BUSINESS_SOCIAL, businessSocial)
            .putString(KEY_BUSINESS_WEBSITE, businessWebsite)
            .putString(KEY_BUSINESS_PORTAL, businessPortal)
            .putString(KEY_BUSINESS_APP_LINK, businessAppLink)
            .putBoolean(KEY_ONBOARDING_COMPLETE, true)
            .apply()
    }

    fun saveBusinessLogoPath(path: String) {
        prefs.edit().putString(KEY_BUSINESS_LOGO_PATH, path).apply()
    }

    fun saveBusinessFolder(folderId: String) {
        prefs.edit().putString(KEY_BUSINESS_FOLDER_ID, folderId).apply()
    }

    fun saveProvisioningResult(
        spreadsheetId: String
    ) {
        prefs.edit()
            .putString(KEY_SPREADSHEET_ID, spreadsheetId)
            .putBoolean(KEY_IS_PROVISIONED, true)
            .apply()
    }

    /** Full reset — used on sign-out. */
    fun clear() {
        prefs.edit().clear().apply()
    }

    /** Clear just the spreadsheet ID, leaving user info intact */
    fun clearSpreadsheetId() {
        prefs.edit()
            .remove(KEY_SPREADSHEET_ID)
            .putBoolean(KEY_IS_PROVISIONED, false)
            .apply()
    }
}
