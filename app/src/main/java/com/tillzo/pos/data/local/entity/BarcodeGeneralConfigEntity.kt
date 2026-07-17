package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "BarcodeGeneralConfigs")
data class BarcodeGeneralConfigEntity(
    @PrimaryKey
    override val system_row_id: String = UUID.randomUUID().toString(),
    override var sync_status: String = "pending",
    override val created_at: Long = System.currentTimeMillis(),
    override var updated_at: Long = System.currentTimeMillis(),
    override val pos_terminal_id: String = "terminal_1",
    override val is_deleted: Boolean = false,
    override val deleted_at: Long? = null,

    // Dimensions
    val labelWidth: Int = 144,
    val labelHeight: Int = 72,

    // Title settings
    val titleTextSize: Float = 6f,
    val isTitleBold: Boolean = true,

    // Barcode settings
    val barcodeSize: Float = 48f,

    // General options
    val currencySymbol: String = "Rs",

    // Branding
    val companyName: String = "Tillzo POS",
    val companyLogoPath: String = "",
    val showCompanyName: Boolean = true,
    val showCompanyLogo: Boolean = true,

    // Custom offsets (coordinates)
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

    // Branding offsets
    val companyNameSize: Float = 5f,
    val companyLogoSize: Float = 8f,
    val companyNameX: Float = 16f,
    val companyNameY: Float = 8f,
    val companyLogoX: Float = 4f,
    val companyLogoY: Float = 4f,

    // Prefix & Suffix
    val usePrefix: Boolean = true,
    val customPrefix: String = "]d2",
    val prefixPosition: Int = 0,
    val useSuffix: Boolean = false,
    val customSuffix: String = "",
    val suffixPosition: Int = 0,
    val useSeparator: Boolean = true
) : BaseEntity()
