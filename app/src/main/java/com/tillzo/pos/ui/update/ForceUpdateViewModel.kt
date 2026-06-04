package com.tillzo.pos.ui.update

import androidx.lifecycle.viewModelScope
import com.tillzo.pos.domain.update.CheckForceUpdateUseCase
import com.tillzo.pos.domain.update.ForceUpdateState
import com.tillzo.pos.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ForceUpdateViewModel — checks update state at app startup.
 * Exposes ForceUpdateState to MainActivity for routing decisions.
 */
@HiltViewModel
class ForceUpdateViewModel @Inject constructor(
    private val checkForceUpdateUseCase: CheckForceUpdateUseCase
) : BaseViewModel<ForceUpdateState>(ForceUpdateState.UpToDate) {

    init {
        checkUpdate()
    }

    fun checkUpdate() {
        viewModelScope.launch {
            updateState(checkForceUpdateUseCase(Unit))
        }
    }
}
