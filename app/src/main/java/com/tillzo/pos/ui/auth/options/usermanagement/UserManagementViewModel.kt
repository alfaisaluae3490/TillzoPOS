package com.tillzo.pos.ui.auth.options.usermanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.entity.UserEntity
import com.tillzo.pos.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserEntity>>(emptyList())
    val users: StateFlow<List<UserEntity>> = _users

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _users.value = userRepository.getAllUsers()
        }
    }

    fun addUser(name: String, email: String, role: String, passwordRaw: String, permissionsJson: String?) {
        val hash = hashPassword(passwordRaw)
        val newUser = UserEntity(
            system_row_id = UUID.randomUUID().toString(),
            pos_terminal_id = "CURRENT_TERMINAL_ID", // TODO: Get from Device Settings UseCase
            name = name,
            email = email,
            role = role,
            password_hash = hash,
            permissions_json = permissionsJson
        )
        viewModelScope.launch {
            userRepository.insertUser(newUser)
            loadUsers()
        }
    }

    fun deleteUser(systemRowId: String) {
        viewModelScope.launch {
            userRepository.deleteUserById(systemRowId)
            loadUsers()
        }
    }

    private fun hashPassword(password: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password // Fallback, shouldn't happen
        }
    }
}
