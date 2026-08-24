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

    // FIX (2026-08-23, DEF-61): MAX-based sequence instead of COUNT(*)+1.
    // COUNT(*) includes soft-deleted rows → after a delete the next number could
    // collide with an existing one; read-then-insert also raced. MAX of the
    // numeric suffix +1 gives the true next sequence.
    @Query("SELECT COALESCE(MAX(CAST(SUBSTR(grnNumber, -4) AS INTEGER)), 0) + 1 FROM grn_headers")
    suspend fun getNextGrnSequence(): Int

    @Query("SELECT * FROM grn_headers WHERE syncStatus = 'pending' AND isDeleted = 0")
    suspend fun getPendingGrns(): List<GrnHeaderEntity>

    @Query("SELECT * FROM grn_items WHERE syncStatus = 'pending'")
    suspend fun getPendingGrnItems(): List<GrnItemEntity>

    // DEF-115 (2026-08-23): ALL GRN items for backup export
    @Query("SELECT * FROM grn_items ORDER BY createdAt ASC")
    suspend fun getAllGrnItemsForBackup(): List<GrnItemEntity>

    @Query("UPDATE grn_headers SET syncStatus = 'synced', updatedAt = :time WHERE grnId = :id")
    suspend fun markGrnSynced(id: String, time: Long)

    @Query("UPDATE grn_items SET syncStatus = 'synced' WHERE grnItemId = :id")
    suspend fun markGrnItemSynced(id: String)

    @Query("SELECT * FROM grn_headers WHERE poId = :poId AND isDeleted = 0")
    fun getGrnsForPO(poId: String): Flow<List<GrnHeaderEntity>>

    @Query("UPDATE grn_items SET batchId = :batchId WHERE grnItemId = :grnItemId")
    suspend fun updateGrnItemBatchId(grnItemId: String, batchId: String)

    @Query("UPDATE grn_headers SET paidAmount = :paid, dueBalance = :due, paymentStatus = :status, updatedAt = :time, syncStatus = 'pending' WHERE grnId = :grnId")
    suspend fun updateGrnPayment(grnId: String, paid: Double, due: Double, status: String, time: Long = System.currentTimeMillis())

    @Query("SELECT * FROM grn_headers WHERE isDeleted = 0 AND dueBalance > 0 AND paymentDueDate != '' ORDER BY paymentDueDate ASC")
    suspend fun getUnpaidGrnsWithDueDate(): List<GrnHeaderEntity>

    @Query("SELECT * FROM grn_headers WHERE isDeleted = 0 AND vendorId = :vendorId ORDER BY createdAt DESC")
    fun getGrnsForVendor(vendorId: String): Flow<List<GrnHeaderEntity>>
}
