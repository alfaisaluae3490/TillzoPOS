package com.tillzo.pos.domain.usecase.po

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.tillzo.pos.data.local.entity.PurchaseOrderEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderItemEntity
import com.tillzo.pos.data.local.entity.VendorEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class SharePurchaseOrderUseCase @Inject constructor() {

    fun shareAsPdf(
        context: Context,
        po: PurchaseOrderEntity,
        items: List<PurchaseOrderItemEntity>,
        vendor: VendorEntity?,
        shopName: String,
        shopPhone: String
    ) {
        val pdfFile = generatePdf(context, po, items, vendor, shopName, shopPhone) ?: return
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        showShareChoices(context, uri, po)
    }

    private fun generatePdf(
        context: Context,
        po: PurchaseOrderEntity,
        items: List<PurchaseOrderItemEntity>,
        vendor: VendorEntity?,
        shopName: String,
        shopPhone: String
    ): File? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 20f
                isFakeBoldText = true
            }
            val headerPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 14f
                isFakeBoldText = true
            }
            val bodyPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 11f
            }
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            var y = 40f
            val leftMargin = 40f
            val rightMargin = 572f

            canvas.drawText("PURCHASE ORDER", leftMargin, y, titlePaint)
            y += 30f
            canvas.drawText(shopName, leftMargin, y, headerPaint)
            y += 18f
            if (shopPhone.isNotEmpty()) {
                canvas.drawText("Phone: $shopPhone", leftMargin, y, bodyPaint)
                y += 16f
            }
            y += 8f
            canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
            y += 16f

            canvas.drawText("PO Number: ${po.poNumber}", leftMargin, y, bodyPaint)
            y += 16f
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(po.createdAt))
            canvas.drawText("Date: $dateStr", leftMargin, y, bodyPaint)
            y += 16f
            if (po.expectedDeliveryDate.isNotEmpty()) {
                canvas.drawText("Expected Delivery: ${po.expectedDeliveryDate}", leftMargin, y, bodyPaint)
                y += 16f
            }
            y += 8f
            canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
            y += 16f

            canvas.drawText("VENDOR: ${po.vendorName}", leftMargin, y, headerPaint)
            y += 16f
            vendor?.let {
                if (it.phone.isNotEmpty()) {
                    canvas.drawText("Phone: ${it.phone}", leftMargin, y, bodyPaint)
                    y += 14f
                }
            }
            y += 8f
            canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
            y += 16f

            canvas.drawText("ITEM", leftMargin, y, headerPaint)
            canvas.drawText("QTY", 320f, y, headerPaint)
            canvas.drawText("PRICE", 400f, y, headerPaint)
            canvas.drawText("TOTAL", 480f, y, headerPaint)
            y += 4f
            canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
            y += 14f

            for (item in items) {
                val name = item.productName
                val displayName = if (name.length > 20) name.take(20) else name
                canvas.drawText(displayName, leftMargin, y, bodyPaint)
                canvas.drawText(item.orderedQty.toString(), 320f, y, bodyPaint)
                canvas.drawText(item.unitCostPrice.toString(), 400f, y, bodyPaint)
                canvas.drawText(item.totalCost.toString(), 480f, y, bodyPaint)
                y += 14f
            }

            y += 8f
            canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
            y += 16f
            canvas.drawText("TOTAL AMOUNT: ${po.currency} ${po.totalAmount}", leftMargin, y, headerPaint)
            y += 20f
            if (po.notes.isNotBlank()) {
                canvas.drawText("Notes: ${po.notes}", leftMargin, y, bodyPaint)
            }

            document.finishPage(page)

            val pdfDir = File(context.cacheDir, "pdfs")
            pdfDir.mkdirs()
            val pdfFile = File(pdfDir, "PO_${po.poNumber.replace("/", "_")}.pdf")
            FileOutputStream(pdfFile).use { out ->
                document.writeTo(out)
            }
            document.close()
            pdfFile
        } catch (e: Exception) {
            null
        }
    }

    private fun showShareChoices(context: Context, uri: Uri, po: PurchaseOrderEntity) {
        try {
            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }
            context.startActivity(whatsappIntent)
        } catch (_: Exception) {
            try {
                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Purchase Order: ${po.poNumber}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(emailIntent, "Share PO"))
            } catch (_: Exception) { }
        }
    }
}
