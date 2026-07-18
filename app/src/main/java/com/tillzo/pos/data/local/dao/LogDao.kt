package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tillzo.pos.data.local.entity.AppLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AppLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLogBlocking(log: AppLogEntity)

    @Query("SELECT * FROM app_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AppLogEntity>>

    @Query("SELECT * FROM app_logs ORDER BY timestamp DESC")
    fun getAllLogsSync(): List<AppLogEntity>

    @Query("SELECT * FROM app_logs WHERE (:tag IS NULL OR tag = :tag) AND (:logLevel IS NULL OR logLevel = :logLevel) ORDER BY timestamp DESC")
    fun getFilteredLogs(tag: String?, logLevel: String?): Flow<List<AppLogEntity>>

    @Query("SELECT DISTINCT tag FROM app_logs ORDER BY tag")
    fun getAllTags(): Flow<List<String>>

    @Query("DELETE FROM app_logs WHERE timestamp < :cutoffTime")
    suspend fun deleteLogsOlderThan(cutoffTime: Long)
}
