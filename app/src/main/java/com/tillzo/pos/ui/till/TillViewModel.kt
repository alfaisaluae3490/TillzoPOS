package com.tillzo.pos.ui.till

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.TillSessionDao
import com.tillzo.pos.data.local.entity.TillSessionEntity
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TillViewModel @Inject constructor(
    private val tillSessionDao: TillSessionDao,
    private val appSetupPrefs: AppSetupPrefs
) : ViewModel() {

    val currentSession: StateFlow<TillSessionEntity?> =
        tillSessionDao.getOpenSessionFlow()
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private fun posTerminalId() = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }

    fun openTill(openingCash: Double, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val session = TillSessionEntity(
                sessionId = UUID.randomUUID().toString(),
                cashierId = appSetupPrefs.userEmail.ifBlank { "cashier" },
                cashierName = appSetupPrefs.userDisplayName.ifBlank { "Cashier" },
                posTerminalId = posTerminalId(),
                openingCash = openingCash,
                expectedCash = openingCash,
                notes = notes,
                shiftDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                openedAt = now,
                posId = posTerminalId(),
                createdAt = now,
                updatedAt = now
            )
            tillSessionDao.insertSession(session)
        }
    }

    fun closeTill(
        sessionId: String,
        physicalCashCount: Double,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = tillSessionDao.getOpenSession(posTerminalId()) ?: return@launch
            val expected = session.expectedCash
            tillSessionDao.closeSession(
                sessionId = sessionId,
                closingCash = physicalCashCount,
                netCash = physicalCashCount - expected,
                closedAt = System.currentTimeMillis()
            )
            withContext(Dispatchers.Main) { onDone() }
        }
    }
}
