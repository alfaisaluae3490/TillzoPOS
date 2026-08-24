package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tillzo.pos.data.local.entity.ReturnsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReturnsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturn(ret: ReturnsEntity)

    @Query("SELECT * FROM returns_log WHERE syncStatus = 'pending' ORDER BY createdAt ASC")
    suspend fun getPendingReturns(): List<ReturnsEntity>

    @Query("SELECT * FROM returns_log WHERE syncStatus = 'pending_deletion' ORDER BY createdAt ASC")
    suspend fun getPendingDeletions(): List<ReturnsEntity>

    @Query("UPDATE returns_log SET syncStatus = 'synced' WHERE returnId = :returnId")
    suspend fun markSynced(returnId: String)

    @Query("SELECT * FROM returns_log ORDER BY createdAt DESC")
    fun getAllReturns(): Flow<List<ReturnsEntity>>

    @Query("SELECT COUNT(*) FROM returns_log WHERE originalInvoiceId = :invoiceId")
    suspend fun countReturnsForInvoice(invoiceId: String): Int
}
