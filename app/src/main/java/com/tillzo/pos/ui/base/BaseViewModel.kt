package com.tillzo.pos.ui.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base ViewModel for all UI screens.
 *
 * Provides:
 *  - [uiState] : observable state using StateFlow (consumed by Compose)
 *  - [errorChannel] : one-shot error events (e.g. snackbar, toast triggers)
 *
 * Architecture Law: ViewModels never import Room, Retrofit, or any local/remote
 * data source directly. Only UseCase calls are permitted.
 *
 * S = UI State type — define a sealed class or data class per screen.
 */
abstract class BaseViewModel<S>(initialState: S) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    // One-shot error events — observed via Channel to avoid re-delivery on recomposition
    val errorChannel = Channel<String>(Channel.BUFFERED)

    /**
     * Update the UI state. Call from within ViewModelScope coroutines.
     */
    protected fun updateState(newState: S) {
        _uiState.value = newState
    }

    /**
     * Send an error message to the UI layer once.
     */
    protected suspend fun sendError(message: String) {
        errorChannel.send(message)
    }

    override fun onCleared() {
        super.onCleared()
        errorChannel.close()
    }
}
