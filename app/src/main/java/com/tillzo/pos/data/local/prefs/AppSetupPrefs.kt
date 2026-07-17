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
