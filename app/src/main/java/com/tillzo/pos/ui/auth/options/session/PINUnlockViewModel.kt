package com.tillzo.pos.ui.auth.options.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PINUnlockViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _unlockState = MutableStateFlow<PINUnlockState>(PINUnlockState.Idle)
    val unlockState: StateFlow<PINUnlockState> = _unlockState

    init {
        // Check if user is theoretically logged in at all (has OAuth token)
        if (!authRepository.isLoggedIn()) {
            _unlockState.value = PINUnlockState.NeedsOAuthLogin
        } else {
            // They have a token. Does a PIN exist?
            if (authRepository.hasPIN()) {
                _unlockState.value = PINUnlockState.Idle
            } else {
                _unlockState.value = PINUnlockState.NeedsPINSetup
            }
        }
    }

    fun setPIN(pin: String) {
        viewModelScope.launch {
            authRepository.setPIN(pin)
            _unlockState.value = PINUnlockState.Success
        }
    }

    fun verifyPIN(pin: String) {
        if (authRepository.verifyPIN(pin)) {
            _unlockState.value = PINUnlockState.Success
        } else {
            _unlockState.value = PINUnlockState.Error("Invalid PIN")
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            authRepository.clearPIN()
            _unlockState.value = PINUnlockState.NeedsOAuthLogin
        }
    }
}

sealed class PINUnlockState {
    object Idle : PINUnlockState()
    object NeedsOAuthLogin : PINUnlockState()
    object NeedsPINSetup : PINUnlockState()
    object Success : PINUnlockState()
    data class Error(val message: String) : PINUnlockState()
}
