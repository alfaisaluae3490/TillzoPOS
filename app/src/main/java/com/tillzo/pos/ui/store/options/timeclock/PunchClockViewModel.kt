package com.tillzo.pos.ui.store.options.timeclock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.TimeClockDao
import com.tillzo.pos.data.local.entity.TimeClockEntity
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Employee Time-Tracking (FIX 2026-08-06 — industry-standard punch clock).
 */
@HiltViewModel
class PunchClockViewModel @Inject constructor(
    private val timeClockDao: TimeClockDao,
    private val appSetupPrefs: AppSetupPrefs
) : ViewModel() {

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val punches: StateFlow<List<TimeClockEntity>> = timeClockDao.getAllPunches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isClockedIn = MutableStateFlow(false)
    val isClockedIn: StateFlow<Boolean> = _isClockedIn.asStateFlow()

    init { refreshClockState() }

    fun refreshClockState() {
        val email = appSetupPrefs.userEmail.ifBlank { "user_1" }
        viewModelScope.launch {
            val last = timeClockDao.getLastClockIn(email)
            _isClockedIn.value = last != null
        }
    }

    fun punch(type: String) { // "IN" or "OUT"
        val email = appSetupPrefs.userEmail.ifBlank { "user_1" }
        val terminalId = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }
        viewModelScope.launch {
            timeClockDao.insert(
                TimeClockEntity(
                    pos_terminal_id = terminalId,
                    employee_email = email,
                    employee_name = email.substringBefore("@"),
                    event_type = type,
                    timestamp = System.currentTimeMillis(),
                    note = null
                )
            )
            _statusMessage.value = if (type == "IN") "Clocked IN ✓ (synced on next sync)"
            else "Clocked OUT ✓ (synced on next sync)"
            refreshClockState()
        }
    }

    fun clearStatus() { _statusMessage.value = null }
}
