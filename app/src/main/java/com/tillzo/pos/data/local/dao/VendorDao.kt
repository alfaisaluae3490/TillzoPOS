package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tillzo.pos.data.local.entity.VendorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VendorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendor(vendor: VendorEntity)

    @Update
    suspend fun updateVendor(vendor: VendorEntity)

    @Query("SELECT * FROM vendors WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllVendors(): Flow<List<VendorEntity>>

    // suspend List (not Flow) — used for one-shot search in VM
    @Query("SELECT * FROM vendors WHERE (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') AND isDeleted = 0 ORDER BY name ASC")
    suspend fun searchVendors(query: String): List<VendorEntity>

    @Query("SELECT * FROM vendors WHERE syncStatus = 'pending' AND isDeleted = 0")
    suspend fun getPendingVendors(): List<VendorEntity>

    @Query("SELECT * FROM vendors WHERE vendorId = :vendorId")
    suspend fun getVendorById(vendorId: String): VendorEntity?

    @Query("UPDATE vendors SET syncStatus = 'synced' WHERE vendorId = :vendorId")
    suspend fun markSynced(vendorId: String)

    @Query("UPDATE vendors SET syncStatus = 'synced' WHERE vendorId IN (:vendorIds)")
    suspend fun markMultipleSynced(vendorIds: List<String>)

    @Query("UPDATE vendors SET contractFileId = :fileId, contractFileUrl = :fileUrl, syncStatus = 'pending', updatedAt = :updatedAt WHERE vendorId = :vendorId")
    suspend fun updateContractFile(vendorId: String, fileId: String, fileUrl: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE vendors SET isDeleted = 1, syncStatus = 'pending', updatedAt = :timestamp WHERE vendorId = :vendorId")
    suspend fun softDeleteVendor(vendorId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM vendors WHERE vendorId = :vendorId")
    suspend fun hardDeleteVendor(vendorId: String)

    @Query("SELECT * FROM vendors WHERE isDeleted = 1 AND syncStatus = 'pending'")
    suspend fun getPendingSyncDeleted(): List<VendorEntity>

    @Query("SELECT * FROM vendors WHERE isDeleted = 0 AND isActive = 1 ORDER BY name ASC")
    fun getActiveVendors(): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendors WHERE (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') AND isDeleted = 0 AND isActive = 1 ORDER BY name ASC")
    suspend fun searchActiveVendors(query: String): List<VendorEntity>
}
