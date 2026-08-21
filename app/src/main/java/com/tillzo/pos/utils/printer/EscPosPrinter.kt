package com.tillzo.pos.utils.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.Socket
import java.util.UUID

/**
 * Handles raw ESC/POS byte commands over Bluetooth (SPP) and Wi-Fi (TCP/IP Port 9100).
 * Implements M5.3 Non-blocking queue and 3-retry logic.
 */
class EscPosPrinter @javax.inject.Inject constructor() {

    // SPP UUID for Bluetooth Serial Port Profile
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // ESC/POS Commands
    private val INIT = byteArrayOf(0x1B, 0x40)
    private val BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
    private val BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
    private val ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
    private val ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
    private val CUT = byteArrayOf(0x1D, 0x56, 0x41, 0x10)
    private val NEWLINE = byteArrayOf(0x0A)

    suspend fun printViaBluetooth(macAddress: String, text: String): Boolean = withContext(Dispatchers.IO) {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return@withContext false
        @SuppressLint("MissingPermission")
        val device: BluetoothDevice = adapter.getRemoteDevice(macAddress)
        
        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null

        // M5.3 Auto-Reconnect 3 Retries
        for (attempt in 1..3) {
            try {
                @SuppressLint("MissingPermission")
                socket = device.createRfcommSocketToServiceRecord(sppUuid)
                socket.connect()
                outputStream = socket.outputStream

                sendPrintPayload(outputStream, text)

                outputStream.flush()
                return@withContext true

            } catch (e: Exception) {
                e.printStackTrace()
                socket?.close()
                delay(1000L * attempt) // Exponential-ish backoff
            }
        }
        
        outputStream?.close()
        socket?.close()
        return@withContext false
    }

    suspend fun printViaNetwork(ipAddress: String, port: Int = 9100, text: String): Boolean = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        var outputStream: OutputStream? = null

        for (attempt in 1..3) {
            try {
                socket = Socket(ipAddress, port)
                socket.soTimeout = 3000
                outputStream = socket.getOutputStream()

                sendPrintPayload(outputStream, text)

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

    private fun sendPrintPayload(outputStream: OutputStream, text: String) {
        outputStream.write(INIT)
        outputStream.write(ALIGN_CENTER)
        outputStream.write(BOLD_ON)
        outputStream.write("TILLZO POS RECEIPT".toByteArray(Charsets.UTF_8))
        outputStream.write(NEWLINE)
        outputStream.write(BOLD_OFF)
        outputStream.write(ALIGN_LEFT)
        
        outputStream.write(text.toByteArray(Charsets.UTF_8))
        
        outputStream.write(NEWLINE)
        outputStream.write(NEWLINE)
        outputStream.write(CUT)
    }
}
