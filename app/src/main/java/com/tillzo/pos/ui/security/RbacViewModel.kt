package com.tillzo.pos.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.domain.auth.SessionGuardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * RBAC gate bridge (FIX 2026-08-22, DEF-32).
 *
 * SessionGuardUseCase.hasPermission() was fully implemented but had ZERO
 * call sites — role-based access control was dead: any Cashier could open
 * Settings, Expenses, User Management or the Admin Dashboard. This ViewModel
 * is the single wiring point used by AppNavHost menu navigation.
 *
 * Single-owner mode (no Users_Permissions rows yet) allows everything, so
 * wiring this is safe for fresh installs; once a non-Admin user row exists,
 * Admin-only modules are blocked with a Toast.
 */
@HiltViewModel
class RbacViewModel @Inject constructor(
    private val sessionGuard: SessionGuardUseCase
) : ViewModel() {

    private val _denied = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val denied = _denied.asSharedFlow()

    /** Checks permission for [module]; runs [onAllowed] only if permitted,
     *  otherwise emits a denial message (surfaced as Toast by the caller). */
    fun requireAccess(module: String, onAllowed: () -> Unit) {
        viewModelScope.launch {
            val allowed = sessionGuard.hasPermission("", module)
            if (allowed) {
                onAllowed()
            } else {
                _denied.tryEmit("Admin access required — this section is restricted to Admin role")
            }
        }
    }
}
