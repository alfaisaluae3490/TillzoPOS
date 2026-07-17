package com.tillzo.pos.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import com.tillzo.pos.data.local.entity.BarcodeFieldConfigEntity

object LabelPdfPrinter {

    /**
     * Generates a customizable PDF label for a thermal printer.
     * Supports multi-page printing with unique serial numbers.
     */
    fun generateLabelPdf(
        context: Context,
        sku: String,
        price: String,
        currencySymbol: String,
        title: String,
        gtin: String,
        batch: String,
        expiry: String,
        quantity: Int,
        labelWidthPoints: Int = 144, // Default 2 inches
        labelHeightPoints: Int = 72, // Default 1 inch
        titleTextSize: Float = 6f,
        isTitleBold: Boolean = true,
        barcodeSize: Float = 48f,
        usePrefix: Boolean = true,
        customPrefix: String = "]d2",
        prefixPosition: Int = 0,
        useSeparator: Boolean = true,
        customSuffix: String = "",
        useSuffix: Boolean = false,
        suffixPosition: Int = 0,
        companyName: String = "Tillzo POS",
        companyLogoPath: String = "",
        titleX: Float = 4f,
        titleY: Float = 16f,
        priceX: Float = 4f,
        priceY: Float = 24f,
        skuX: Float = 4f,
        skuY: Float = 32f,
        gtinX: Float = 4f,
        gtinY: Float = 40f,
        lotX: Float = 4f,
        lotY: Float = 48f,
        expX: Float = 4f,
        expY: Float = 56f,
        snX: Float = 4f,
        snY: Float = 66f,
        barcodeX: Float = 92f,
        barcodeY: Float = 12f,
        companyNameSize: Float = 5f,
        companyLogoSize: Float = 8f,
        companyNameX: Float = 16f,
        companyNameY: Float = 8f,
        companyLogoX: Float = 4f,
        companyLogoY: Float = 4f,
        showCompanyName: Boolean = true,
        showCompanyLogo: Boolean = true,
        fields: List<BarcodeFieldConfigEntity> = emptyList(),
        onProgress: ((currentPage: Int, totalPages: Int) -> Unit)? = null
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(labelWidthPoints, labelHeightPoints, quantity).create()
        
        val textPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        val priceText = if (currencySymbol.isNotBlank()) "$currencySymbol $price" else price
        val resolvedGtin = if (gtin.isNotBlank() && gtin != "0000000") gtin else "0000000"

        for (i in 0 until quantity) {
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            canvas.drawColor(Color.WHITE)

            // Generate unique serial number for this page using trace format
            val serialNumber = BarcodeGeneratorUtil.generateDynamicSerialNumber(sku, i)

            // 0. Draw branding: company logo and company name if enabled
            if (showCompanyLogo && companyLogoPath.isNotBlank()) {
                val logoBmp = loadLogoBitmap(context, companyLogoPath)
                if (logoBmp != null) {
                    val logoHeight = companyLogoSize
                    val logoWidth = logoHeight * (logoBmp.width.toFloat() / logoBmp.height.toFloat())
                    val scaledLogo = Bitmap.createScaledBitmap(logoBmp, logoWidth.toInt(), logoHeight.toInt(), true)
                    canvas.drawBitmap(scaledLogo, companyLogoX, companyLogoY, null)
                }
            }
            if (showCompanyName && companyName.isNotBlank()) {
                textPaint.textSize = companyNameSize
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(companyName, companyNameX, companyNameY, textPaint)
            }

            // 1. Draw Title
            textPaint.textSize = titleTextSize
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, if (isTitleBold) Typeface.BOLD else Typeface.NORMAL)
            canvas.drawText(title, titleX, titleY, textPaint)

            // 2. Draw Price
            textPaint.textSize = 6f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(priceText, priceX, priceY, textPaint)

            // 3. Draw SKU/Item Number
            textPaint.textSize = 5f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("SKU/Item No: $sku", skuX, skuY, textPaint)

            // 4. Stacked Data (GTIN, LOT, EXP)
            textPaint.textSize = 5f
            canvas.drawText("GTIN: $resolvedGtin", gtinX, gtinY, textPaint)
            canvas.drawText("LOT: $batch", lotX, lotY, textPaint)
            canvas.drawText("EXP: $expiry", expX, expY, textPaint)
            
            // 5. Serial Number
            textPaint.textSize = 5f
            textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            canvas.drawText("SN: $serialNumber", snX, snY, textPaint)

            val expiryStr = if (expiry.length == 6) expiry else (expiry.replace("-", "").takeIf { it.length == 8 }?.let { it.substring(2) } ?: "000000")
            val gs1String = BarcodeGeneratorUtil.buildDynamicGs1String(
                fields = fields,
                gtin = resolvedGtin,
                expiryYYMMDD = expiryStr,
                batch = batch.ifBlank { "NONE" },
                serial = serialNumber,
                sku = sku,
                usePrefix = usePrefix,
                customPrefix = customPrefix,
                prefixPosition = prefixPosition,
                useSuffix = useSuffix,
                customSuffix = customSuffix,
                suffixPosition = suffixPosition
            )
            
            val dataMatrixBitmap = BarcodeGeneratorUtil.generateDataMatrix(gs1String, 120, 120)
            
            if (dataMatrixBitmap != null) {
                // Draw a scaled version of the DataMatrix at its exact left (X) and top (Y) coordinates
                val scaledBitmap = Bitmap.createScaledBitmap(dataMatrixBitmap, barcodeSize.toInt(), barcodeSize.toInt(), false)
                canvas.drawBitmap(scaledBitmap, barcodeX, barcodeY, null)
            }

            pdfDocument.finishPage(page)
            onProgress?.invoke(i + 1, quantity)
        }

        return try {
            val file = File(context.cacheDir, "GS1_Label_$sku.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun loadLogoBitmap(context: Context, logoUriStr: String?): Bitmap? {
        if (logoUriStr.isNullOrBlank()) return null
        return try {
            val file = java.io.File(logoUriStr)
            if (file.exists()) {
                android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            } else {
                val uri = android.net.Uri.parse(logoUriStr)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    android.graphics.BitmapFactory.decodeStream(inputStream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
