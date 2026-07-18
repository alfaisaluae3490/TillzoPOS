package com.tillzo.pos.domain.auth

import javax.inject.Inject

class SessionGuardUseCase @Inject constructor() {

    suspend fun hasPermission(systemRowId: String, requiredModuleOrOption: String): Boolean = true
}
