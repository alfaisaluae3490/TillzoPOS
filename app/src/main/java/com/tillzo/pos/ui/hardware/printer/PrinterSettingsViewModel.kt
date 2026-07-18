package com.tillzo.pos.ui.hardware.printer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.utils.printer.EscPosPrinter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrinterSettingsViewModel @Inject constructor(
    private val appSetupPrefs: AppSetupPrefs
) : ViewModel() {

    private val escPosPrinter = EscPosPrinter()

    private val _ipAddress = MutableStateFlow(appSetupPrefs.printerIp)
    val ipAddress: StateFlow<String> = _ipAddress

    private val _macAddress = MutableStateFlow(appSetupPrefs.printerMac)
    val macAddress: StateFlow<String> = _macAddress

    private val _printStatus = MutableStateFlow("Idle")
    val printStatus: StateFlow<String> = _printStatus

    fun updateIpAddress(ip: String) {
        _ipAddress.value = ip
        appSetupPrefs.printerIp = ip
    }

    fun updateMacAddress(mac: String) {
        _macAddress.value = mac
        appSetupPrefs.printerMac = mac
    }

    fun testNetworkPrint() {
        _printStatus.value = "Testing Network Print..."
        viewModelScope.launch {
            val success = escPosPrinter.printViaNetwork(_ipAddress.value, 9100, "Tillzo Wi-Fi Print Test Successful!")
            _printStatus.value = if (success) "Network Print Success" else "Network Print Failed - Check IP/Port"
        }
    }

    fun testBluetoothPrint() {
        _printStatus.value = "Testing Bluetooth Print..."
        viewModelScope.launch {
            val success = escPosPrinter.printViaBluetooth(_macAddress.value, "Tillzo Bluetooth Print Test Successful!")
            _printStatus.value = if (success) "Bluetooth Print Success" else "Bluetooth Print Failed - Pair Device First"
        }
    }
}
