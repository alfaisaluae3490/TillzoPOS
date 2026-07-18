package com.tillzo.pos.ui.inventory.options.qr

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.utils.printer.TsplPrinter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class QrGeneratorViewModel @Inject constructor(
    private val tsplPrinter: TsplPrinter,
    private val appSetupPrefs: AppSetupPrefs
) : ViewModel() {

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap.asStateFlow()

    private val _printStatus = MutableStateFlow("")
    val printStatus = _printStatus.asStateFlow()

    fun generateQrCode(text: String) {
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.Default) {
                val writer = QRCodeWriter()
                try {
                    val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                        }
                    }
                    bmp
                } catch (e: Exception) {
                    null
                }
            }
            _qrBitmap.value = bitmap
        }
    }

    /**
     * M6.5 Connects the generated Barcode ID to the M5 TSPL Printer utility.
     */
    fun printQrCode(barcodeId: String) {
        viewModelScope.launch {
            _printStatus.value = "Printing..."
            try {
                val printerMac = appSetupPrefs.printerMac

                val success = tsplPrinter.printBarcodeLabel(printerMac, "Item ID:", barcodeId)
                if (success) {
                    _printStatus.value = "Print Success!"
                } else {
                    _printStatus.value = "Print Failed. Check Printer connection."
                }
            } catch (e: Exception) {
                _printStatus.value = "Error: ${e.message}"
            }
        }
    }
}
