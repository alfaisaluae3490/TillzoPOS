package com.tillzo.pos.data.local.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

/**
 * Generic DAO interface — all DAOs extend this.
 *
 * T = Room Entity type
 *
 * Architecture Law: DAOs are ONLY called from Repositories.
 * No ViewModel or UseCase imports a DAO.
 */
interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: T): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<T>)

    @Update
    suspend fun update(item: T)

    @Delete
    suspend fun delete(item: T)
}
