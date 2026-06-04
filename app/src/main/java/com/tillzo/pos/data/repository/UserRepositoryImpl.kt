package com.tillzo.pos.data.repository

import com.tillzo.pos.data.local.dao.UserDao
import com.tillzo.pos.data.local.entity.UserEntity
import com.tillzo.pos.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {
    override suspend fun getAllUsers(): List<UserEntity> = userDao.getAllUsers()

    override suspend fun getUserById(systemRowId: String): UserEntity? = userDao.getUserById(systemRowId)

    override suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)

    override suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)

    override suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    override suspend fun deleteUserById(systemRowId: String) = userDao.deleteUserById(systemRowId)
}
