package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tillzo.pos.data.local.entity.TimeClockEntity
import kotlinx.coroutines.flow.Flow

/** Employee time-tracking DAO (FIX 2026-08-06). */
@Dao
interface TimeClockDao {
    @Query("SELECT * FROM Time_Clock WHERE is_deleted = 0 ORDER BY timestamp DESC")
    fun getAllPunches(): Flow<List<TimeClockEntity>>

    @Query("SELECT * FROM Time_Clock WHERE employee_email = :email AND is_deleted = 0 ORDER BY timestamp DESC")
    fun getPunchesForEmployee(email: String): Flow<List<TimeClockEntity>>

    @Query("SELECT * FROM Time_Clock WHERE sync_status = 'pending' ORDER BY timestamp ASC")
    suspend fun getPendingPunches(): List<TimeClockEntity>

    @Query("SELECT * FROM Time_Clock WHERE employee_email = :email AND event_type = 'IN' AND is_deleted = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastClockIn(email: String): TimeClockEntity?

    @Query("UPDATE Time_Clock SET sync_status = 'synced' WHERE system_row_id = :id")
    suspend fun markSynced(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(punch: TimeClockEntity)
}
