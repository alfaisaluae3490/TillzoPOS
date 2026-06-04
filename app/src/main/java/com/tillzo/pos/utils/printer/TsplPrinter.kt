package com.tillzo.pos.utils.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

/**
 * M5.4 TSPL/ZPL Protocol for Barcode Stickers (e.g. 1"x1.5").
 * TSPL coordinates system.
 */
class TsplPrinter @Inject constructor() {

    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    suspend fun printBarcodeLabel(macAddress: String, itemName: String, barcode: String): Boolean = withContext(Dispatchers.IO) {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return@withContext false
        @SuppressLint("MissingPermission")
        val device: BluetoothDevice = adapter.getRemoteDevice(macAddress)
        
        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null

        for (attempt in 1..3) {
            try {
                @SuppressLint("MissingPermission")
                socket = device.createRfcommSocketToServiceRecord(sppUuid)
                socket.connect()
                outputStream = socket.outputStream

                // TSPL Commands for 40mm x 30mm label
                val tsplCommand = """
                    SIZE 40 mm, 30 mm
                    GAP 2 mm, 0 mm
                    CLS
                    TEXT 10,10,"3",0,1,1,"$itemName"
                    BARCODE 10,60,"128",80,1,0,2,2,"$barcode"
                    PRINT 1,1
                    
                """.trimIndent()

                outputStream.write(tsplCommand.toByteArray(Charsets.UTF_8))
                outputStream.flush()
                return@withContext true

            } catch (e: Exception) {
                e.printStackTrace()
                socket?.close()
                delay(1000L * attempt)
            }
        }
        
        outputStream?.close()
        socket?.close()
        return@withContext false
    }
}
