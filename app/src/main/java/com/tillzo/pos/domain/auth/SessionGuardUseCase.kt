package com.tillzo.pos.domain.auth

import com.tillzo.pos.data.local.entity.UserEntity
import com.tillzo.pos.domain.repository.AuthRepository
import com.tillzo.pos.domain.repository.UserRepository
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import javax.inject.Inject

class SessionGuardUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {

    // Currently logged in user derived from AuthRepository token / PIN state
    // For simplicity, we assume we want to query a local active user based on email from token
    // In a real implementation, you'd decode the JWT to get the user email.
    
    // As a Flow so UI can react immediately to DB changes (from SyncWorker remote delta fetch)
    // allowing true live Remote Revocation (M3.4).
    fun observeUserPermissions(systemRowId: String): Flow<Map<String, Boolean>> = flow {
        // Poll or observe room DB. Room DAO needs to return a Flow for real reactivity.
        // For demonstration, taking one-shot, but Room can be modified to return Flow.
        val user = userRepository.getUserById(systemRowId)
        val permissions = parsePermissions(user?.permissions_json)
        emit(permissions)
    }

    suspend fun hasPermission(systemRowId: String, requiredModuleOrOption: String): Boolean {
        val user = userRepository.getUserById(systemRowId) ?: return false
        if (user.role == "Admin") return true // Admins bypass all guards

        val perms = parsePermissions(user.permissions_json)
        // If it's a specific toggle flag like "Inventory Add/Edit"
        return perms[requiredModuleOrOption] == true
    }

    private fun parsePermissions(json: String?): Map<String, Boolean> {
        val map = mutableMapOf<String, Boolean>()
        if (!json.isNullOrEmpty()) {
            try {
                val jsonObj = JSONObject(json)
                val keys = jsonObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = jsonObj.getBoolean(key)
                }
            } catch (e: Exception) {
                // Ignore parse errors, fallback to empty map
            }
        }
        return map
    }
}
