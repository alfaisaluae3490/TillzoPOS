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

    @Query("UPDATE vendors SET syncStatus = 'synced' WHERE vendorId = :vendorId")
    suspend fun markSynced(vendorId: String)
}
