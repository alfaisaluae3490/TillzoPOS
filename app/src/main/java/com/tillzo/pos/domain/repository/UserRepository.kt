package com.tillzo.pos.domain.repository

import com.tillzo.pos.data.local.entity.UserEntity

interface UserRepository {
    suspend fun getAllUsers(): List<UserEntity>
    suspend fun getUserById(systemRowId: String): UserEntity?
    suspend fun getUserByEmail(email: String): UserEntity?
    suspend fun insertUser(user: UserEntity)
    suspend fun updateUser(user: UserEntity)
    suspend fun deleteUserById(systemRowId: String)
}
