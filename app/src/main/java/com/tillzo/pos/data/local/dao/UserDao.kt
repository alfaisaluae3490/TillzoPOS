package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tillzo.pos.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM Users_Permissions")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM Users_Permissions WHERE system_row_id = :systemRowId")
    suspend fun getUserById(systemRowId: String): UserEntity?

    @Query("SELECT * FROM Users_Permissions WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM Users_Permissions WHERE system_row_id = :systemRowId")
    suspend fun deleteUserById(systemRowId: String)

    @Query("SELECT * FROM Users_Permissions WHERE sync_status = 'pending'")
    suspend fun getPendingSyncUsers(): List<UserEntity>
}
