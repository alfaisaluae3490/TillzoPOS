package com.tillzo.pos.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BarcodeGeneralConfig(
    val labelWidth: Int = 144,
    val labelHeight: Int = 72,
    val titleTextSize: Float = 6f,
    val isTitleBold: Boolean = true,
    val barcodeSize: Float = 48f,
    val currencySymbol: String = "Rs",
    val companyName: String = "Tillzo POS",
    val companyLogoPath: String = "",
    val showCompanyName: Boolean = true,
    val showCompanyLogo: Boolean = true,
    val titleX: Float = 4f,
    val titleY: Float = 16f,
    val priceX: Float = 4f,
    val priceY: Float = 24f,
    val skuX: Float = 4f,
    val skuY: Float = 32f,
    val gtinX: Float = 4f,
    val gtinY: Float = 40f,
    val lotX: Float = 4f,
    val lotY: Float = 48f,
    val expX: Float = 4f,
    val expY: Float = 56f,
    val snX: Float = 4f,
    val snY: Float = 66f,
    val barcodeX: Float = 92f,
    val barcodeY: Float = 12f,
    val companyNameSize: Float = 5f,
    val companyLogoSize: Float = 8f,
    val companyNameX: Float = 16f,
    val companyNameY: Float = 8f,
    val companyLogoX: Float = 4f,
    val companyLogoY: Float = 4f,
    val usePrefix: Boolean = true,
    val customPrefix: String = "]d2",
    val prefixPosition: Int = 0,
    val useSuffix: Boolean = false,
    val customSuffix: String = "",
    val suffixPosition: Int = 0,
    val useSeparator: Boolean = true
)

data class BarcodeFieldConfig(
    val fieldId: String,
    val fieldName: String,
    val aiCode: String,
    val isEnabled: Boolean = true,
    val sequenceOrder: Int = 0,
    val useFnc1Separator: Boolean = false,
    val customValue: String = ""
)

class BarcodePrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("barcode_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _generalConfig = MutableStateFlow(loadGeneralConfig())
    val generalConfigFlow: Flow<BarcodeGeneralConfig> = _generalConfig.asStateFlow()

    private val _fieldsConfig = MutableStateFlow(loadFieldsConfig())
    val fieldsConfigFlow: Flow<List<BarcodeFieldConfig>> = _fieldsConfig.asStateFlow()

    fun getGeneralConfig(): BarcodeGeneralConfig = _generalConfig.value
    fun getFieldsConfig(): List<BarcodeFieldConfig> = _fieldsConfig.value

    fun saveGeneralConfig(config: BarcodeGeneralConfig) {
        val json = gson.toJson(config)
        prefs.edit().putString("general_config", json).apply()
        _generalConfig.value = config
    }

    fun saveFieldsConfig(fields: List<BarcodeFieldConfig>) {
        val json = gson.toJson(fields)
        prefs.edit().putString("fields_config", json).apply()
        _fieldsConfig.value = fields
    }

    fun addField(field: BarcodeFieldConfig) {
        val fields = _fieldsConfig.value.toMutableList()
        fields.add(field)
        saveFieldsConfig(fields)
    }

    fun updateField(field: BarcodeFieldConfig) {
        val fields = _fieldsConfig.value.toMutableList()
        val index = fields.indexOfFirst { it.fieldId == field.fieldId }
        if (index != -1) {
            fields[index] = field
            saveFieldsConfig(fields)
        }
    }

    fun deleteField(fieldId: String) {
        val fields = _fieldsConfig.value.toMutableList()
        fields.removeAll { it.fieldId == fieldId }
        saveFieldsConfig(fields)
    }

    private fun loadGeneralConfig(): BarcodeGeneralConfig {
        val json = prefs.getString("general_config", null) ?: return BarcodeGeneralConfig()
        return try {
            gson.fromJson(json, BarcodeGeneralConfig::class.java)
        } catch (e: Exception) {
            BarcodeGeneralConfig()
        }
    }

    private fun loadFieldsConfig(): List<BarcodeFieldConfig> {
        val json = prefs.getString("fields_config", null) ?: return defaultFields()
        return try {
            val type = object : TypeToken<List<BarcodeFieldConfig>>() {}.type
            val loaded: List<BarcodeFieldConfig> = gson.fromJson(json, type) ?: emptyList()
            if (loaded.isEmpty()) defaultFields() else loaded
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun defaultFields(): List<BarcodeFieldConfig> = listOf(
        BarcodeFieldConfig(fieldId = "GTIN", fieldName = "GTIN", aiCode = "01", isEnabled = true, sequenceOrder = 0),
        BarcodeFieldConfig(fieldId = "EXPIRY", fieldName = "Expiry Date", aiCode = "17", isEnabled = true, sequenceOrder = 1),
        BarcodeFieldConfig(fieldId = "BATCH", fieldName = "Batch/Lot Number", aiCode = "10", isEnabled = true, sequenceOrder = 2, useFnc1Separator = true),
        BarcodeFieldConfig(fieldId = "SN", fieldName = "Serial Number", aiCode = "21", isEnabled = true, sequenceOrder = 3),
        BarcodeFieldConfig(fieldId = "SKU", fieldName = "SKU Number", aiCode = "240", isEnabled = false, sequenceOrder = 4)
    )

    fun moveFieldUp(fieldId: String) {
        val fields = _fieldsConfig.value.toMutableList()
        val index = fields.indexOfFirst { it.fieldId == fieldId }
        if (index > 0) {
            val current = fields[index]
            val previous = fields[index - 1]
            fields[index] = current.copy(sequenceOrder = previous.sequenceOrder)
            fields[index - 1] = previous.copy(sequenceOrder = current.sequenceOrder)
            saveFieldsConfig(fields)
        }
    }

    fun moveFieldDown(fieldId: String) {
        val fields = _fieldsConfig.value.toMutableList()
        val index = fields.indexOfFirst { it.fieldId == fieldId }
        if (index >= 0 && index < fields.size - 1) {
            val current = fields[index]
            val next = fields[index + 1]
            fields[index] = current.copy(sequenceOrder = next.sequenceOrder)
            fields[index + 1] = next.copy(sequenceOrder = current.sequenceOrder)
            saveFieldsConfig(fields)
        }
    }
}
