package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.data.local.entity.GrnItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrnDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrnHeader(grn: GrnHeaderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrnItems(items: List<GrnItemEntity>)

    @Update
    suspend fun updateGrnHeader(grn: GrnHeaderEntity)

    @Query("SELECT * FROM grn_headers WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllGrns(): Flow<List<GrnHeaderEntity>>

    @Query("SELECT * FROM grn_headers WHERE grnId = :grnId LIMIT 1")
    suspend fun getGrnById(grnId: String): GrnHeaderEntity?

    @Query("SELECT * FROM grn_items WHERE grnId = :grnId ORDER BY createdAt ASC")
    suspend fun getGrnItems(grnId: String): List<GrnItemEntity>

    @Query("SELECT * FROM grn_items WHERE grnId = :grnId ORDER BY createdAt ASC")
    fun getGrnItemsFlow(grnId: String): Flow<List<GrnItemEntity>>

    @Query("UPDATE grn_headers SET status = :status, updatedAt = :time, syncStatus = 'pending' WHERE grnId = :grnId")
    suspend fun updateGrnStatus(grnId: String, status: String, time: Long)

    @Query("SELECT COUNT(*) FROM grn_headers")
    suspend fun getTotalGrnCount(): Int

    @Query("SELECT * FROM grn_headers WHERE syncStatus = 'pending' AND isDeleted = 0")
    suspend fun getPendingGrns(): List<GrnHeaderEntity>

    @Query("SELECT * FROM grn_items WHERE syncStatus = 'pending'")
    suspend fun getPendingGrnItems(): List<GrnItemEntity>

    @Query("UPDATE grn_headers SET syncStatus = 'synced', updatedAt = :time WHERE grnId = :id")
    suspend fun markGrnSynced(id: String, time: Long)

    @Query("UPDATE grn_items SET syncStatus = 'synced' WHERE grnItemId = :id")
    suspend fun markGrnItemSynced(id: String)

    @Query("SELECT * FROM grn_headers WHERE poId = :poId AND isDeleted = 0")
    fun getGrnsForPO(poId: String): Flow<List<GrnHeaderEntity>>

    @Query("UPDATE grn_items SET batchId = :batchId WHERE grnItemId = :grnItemId")
    suspend fun updateGrnItemBatchId(grnItemId: String, batchId: String)
}
