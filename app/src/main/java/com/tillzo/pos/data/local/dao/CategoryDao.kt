package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tillzo.pos.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("SELECT * FROM Categories WHERE is_deleted = 0 ORDER BY category_name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    // Soft-delete: marks as deleted + sets timestamp + pending sync
    @Query("UPDATE Categories SET is_deleted = 1, deleted_at = :timestamp, sync_status = 'pending' WHERE system_row_id = :categoryId")
    suspend fun deleteCategory(categoryId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM Categories WHERE sync_status = 'pending' AND is_deleted = 0")
    suspend fun getPendingSyncCategories(): List<CategoryEntity>

    @Query("SELECT * FROM Categories WHERE is_deleted = 1 AND sync_status = 'pending'")
    suspend fun getPendingSyncDeleted(): List<CategoryEntity>

    @Query("UPDATE Categories SET sync_status = 'synced' WHERE system_row_id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM Categories WHERE system_row_id = :id")
    suspend fun hardDeleteCategory(id: String)
}
