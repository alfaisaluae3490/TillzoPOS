package com.tillzo.pos.ui.auth.options.login

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun getAuthIntent(): Intent = authRepository.getAuthIntent()

    fun handleAuthResult(authCode: String?) {
        if (authCode == null) {
            _loginState.value = LoginState.Error("Auth failed or cancelled")
            return
        }

        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = authRepository.loginWithOAuth(authCode)
            if (result.isSuccess) {
                _loginState.value = LoginState.Success
            } else {
                _loginState.value = LoginState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
