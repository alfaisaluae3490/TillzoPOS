package com.tillzo.pos.ui.settings.options.privacy

import androidx.lifecycle.ViewModel
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSetupPrefs: AppSetupPrefs
) : ViewModel() {

    private val _spreadsheetId = MutableStateFlow(appSetupPrefs.spreadsheetId)
    val spreadsheetId = _spreadsheetId.asStateFlow()

    fun updateSpreadsheetId(newId: String) {
        val extracted = extractSheetId(newId)
        if (extracted.isNotEmpty()) {
            // Because saveProvisioningResult also sets isProvisioned to true, we can safely call it.
            appSetupPrefs.saveProvisioningResult(extracted)
            _spreadsheetId.value = extracted
        }
    }

    private fun extractSheetId(input: String): String {
        return if (input.contains("/d/")) {
            input.substringAfter("/d/").substringBefore("/")
        } else {
            input.trim()
        }
    }
}
