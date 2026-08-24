package com.tillzo.pos.ui.till

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.TillSessionDao
import com.tillzo.pos.data.local.entity.TillSessionEntity
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // DEF-119 FIX (2026-08-23): currentSession was SharingStarted.Lazily +
    // stateIn(null) — cold start par Room/WAL replay race: flow ka pehla
    // emission null aata tha aur (static OPEN row, koi DB change nahi) dobara
    // kabhi emit nahi hota tha → gate ~10 min tak "No Active Register Session"
    // dikhata tha. Ab: init collect flow ko turant chalu karta hai + manual
    // refreshSession() one-shot re-query se stale-null ko clear kar sakta hai.
    private val _currentSession = MutableStateFlow<TillSessionEntity?>(null)
    val currentSession: StateFlow<TillSessionEntity?> = _currentSession.asStateFlow()

    init {
        viewModelScope.launch {
            tillSessionDao.getOpenSessionFlowForTerminal(posTerminalId())
                .collect { _currentSession.value = it }
        }
    }

    /** DEF-119: cold-start stale-null guard — one-shot re-query; OPEN session
     *  mila to StateFlow update (gate turant clear). */
    fun refreshSession() {
        viewModelScope.launch(Dispatchers.IO) {
            val session = tillSessionDao.getOpenSession(posTerminalId())
            if (session != null) {
                _currentSession.value = session
            }
        }
    }

    private fun posTerminalId() = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }

    fun openTill(openingCash: Double, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // FIX (2026-08-23, DEF-114): negative opening cash pehle accept ho
            // jata tha (UI garbage text par 0.0 default karta tha, lekin "-500"
            // parse ho kar negative session bana deta tha → drawer math broken).
            if (openingCash < 0.0) {
                android.util.Log.w("TillVM", "openTill rejected: negative opening cash $openingCash")
                return@launch
            }
            // FIX (2026-08-22, DEF-49): guard against a second OPEN session —
            // previously openTill inserted unconditionally, so two OPEN
            // sessions could exist and sales went to whichever getOpenSession
            // returned first. Now: if this terminal already has an open
            // session, keep it (idempotent open).
            val existing = tillSessionDao.getOpenSession(posTerminalId())
            if (existing != null) {
                return@launch
            }
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

    fun addPayIn(amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            // DEF-114: zero/negative pay-in reject — expectedCash ghatana
            // (drawer math corrupt) allowed nahi.
            if (amount <= 0.0) {
                android.util.Log.w("TillVM", "addPayIn rejected: amount $amount")
                return@launch
            }
            val session = tillSessionDao.getOpenSession(posTerminalId()) ?: return@launch
            tillSessionDao.addPayIn(sessionId = session.sessionId, amount = amount)
        }
    }

    fun addPayOut(amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            // DEF-114: zero/negative pay-out reject — expectedCash badhana
            // (drawer math corrupt) allowed nahi.
            if (amount <= 0.0) {
                android.util.Log.w("TillVM", "addPayOut rejected: amount $amount")
                return@launch
            }
            val session = tillSessionDao.getOpenSession(posTerminalId()) ?: return@launch
            tillSessionDao.addPayOut(sessionId = session.sessionId, amount = amount)
        }
    }
}
