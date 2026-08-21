package com.tillzo.pos.ui.setup.onboarding

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * OnboardingViewModel — Business profile setup wizard (FIX 2026-08-06: Faisal).
 *
 * New users walk through: owner name → business name → address → logo →
 * phone → social → website → portal → app link. Everything is saved to
 * AppSetupPrefs so it can be used on receipts, barcode labels and the sheet.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSetupPrefs: AppSetupPrefs
) : ViewModel() {

    // ── Form state ─────────────────────────────────────────────────────────
    private val _ownerName = MutableStateFlow("")
    val ownerName: StateFlow<String> get() = _ownerName

    private val _businessName = MutableStateFlow("")
    val businessName: StateFlow<String> get() = _businessName

    private val _businessAddress = MutableStateFlow("")
    val businessAddress: StateFlow<String> get() = _businessAddress

    private val _businessPhone = MutableStateFlow("")
    val businessPhone: StateFlow<String> get() = _businessPhone

    private val _businessSocial = MutableStateFlow("")
    val businessSocial: StateFlow<String> get() = _businessSocial

    private val _businessWebsite = MutableStateFlow("")
    val businessWebsite: StateFlow<String> get() = _businessWebsite

    private val _businessPortal = MutableStateFlow("")
    val businessPortal: StateFlow<String> get() = _businessPortal

    private val _businessAppLink = MutableStateFlow("")
    val businessAppLink: StateFlow<String> get() = _businessAppLink

    private val _logoPath = MutableStateFlow("")
    val logoPath: StateFlow<String> get() = _logoPath

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> get() = _saving

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> get() = _saved

    fun setOwnerName(v: String) { _ownerName.value = v }
    fun setBusinessName(v: String) { _businessName.value = v }
    fun setBusinessAddress(v: String) { _businessAddress.value = v }
    fun setBusinessPhone(v: String) { _businessPhone.value = v }
    fun setBusinessSocial(v: String) { _businessSocial.value = v }
    fun setBusinessWebsite(v: String) { _businessWebsite.value = v }
    fun setBusinessPortal(v: String) { _businessPortal.value = v }
    fun setBusinessAppLink(v: String) { _businessAppLink.value = v }

    /**
     * Copies a picked logo into app-private storage (survives URI expiry)
     * and records the path. Returns true on success.
     */
    fun pickLogo(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val logoFile = java.io.File(context.filesDir, "business_logo.png")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    java.io.FileOutputStream(logoFile).use { out -> input.copyTo(out) }
                } ?: return@launch
                _logoPath.value = logoFile.absolutePath
            } catch (e: Exception) {
                // Non-fatal — logo optional
            }
        }
    }

    /** Persists the whole profile + marks onboarding complete. */
    fun saveProfile(onSaved: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _saving.value = true
            appSetupPrefs.saveBusinessProfile(
                ownerName = _ownerName.value.trim(),
                businessName = _businessName.value.trim(),
                businessAddress = _businessAddress.value.trim(),
                businessPhone = _businessPhone.value.trim(),
                businessSocial = _businessSocial.value.trim(),
                businessWebsite = _businessWebsite.value.trim(),
                businessPortal = _businessPortal.value.trim(),
                businessAppLink = _businessAppLink.value.trim()
            )
            if (_logoPath.value.isNotBlank()) {
                appSetupPrefs.saveBusinessLogoPath(_logoPath.value)
            }
            _saving.value = false
            _saved.value = true
            withContext(Dispatchers.Main) { onSaved() }
        }
    }

    fun prefillFromExisting() {
        _ownerName.value = appSetupPrefs.ownerName
        _businessName.value = appSetupPrefs.businessName
        _businessAddress.value = appSetupPrefs.businessAddress
        _businessPhone.value = appSetupPrefs.businessPhone
        _businessSocial.value = appSetupPrefs.businessSocial
        _businessWebsite.value = appSetupPrefs.businessWebsite
        _businessPortal.value = appSetupPrefs.businessPortal
        _businessAppLink.value = appSetupPrefs.businessAppLink
        _logoPath.value = appSetupPrefs.businessLogoPath
    }
}