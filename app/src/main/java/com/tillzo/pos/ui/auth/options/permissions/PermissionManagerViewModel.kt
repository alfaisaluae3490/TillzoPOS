package com.tillzo.pos.ui.auth.options.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.entity.UserEntity
import com.tillzo.pos.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class PermissionManagerViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user

    private val _permissionsMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val permissionsMap: StateFlow<Map<String, Boolean>> = _permissionsMap

    fun loadUserPermissions(systemRowId: String) {
        viewModelScope.launch {
            val u = userRepository.getUserById(systemRowId)
            _user.value = u
            
            if (u != null) {
                parsePermissions(u.permissions_json)
            }
        }
    }

    private fun parsePermissions(json: String?) {
        val map = mutableMapOf<String, Boolean>()
        if (json != null && json.isNotEmpty()) {
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
        _permissionsMap.value = map
    }

    fun togglePermission(moduleOptionKey: String, granted: Boolean) {
        val currentMap = _permissionsMap.value.toMutableMap()
        currentMap[moduleOptionKey] = granted
        _permissionsMap.value = currentMap
    }

    fun savePermissions() {
        val currentUser = _user.value ?: return
        val map = _permissionsMap.value
        
        val jsonObj = JSONObject()
        for ((k, v) in map) {
            jsonObj.put(k, v)
        }
        
        val updatedUser = currentUser.copy(
            permissions_json = jsonObj.toString(),
            updated_at = System.currentTimeMillis(),
            sync_status = com.tillzo.pos.data.local.entity.SyncStatus.PENDING
        )
        
        viewModelScope.launch {
            userRepository.updateUser(updatedUser)
        }
    }
}
