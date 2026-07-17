package com.tillzo.pos.data.local.dao

import androidx.room.*
import com.tillzo.pos.data.local.entity.BarcodeFieldConfigEntity
import com.tillzo.pos.data.local.entity.BarcodeGeneralConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BarcodeConfigDao {

    // General Config
    @Query("SELECT * FROM BarcodeGeneralConfigs LIMIT 1")
    fun getGeneralConfigFlow(): Flow<BarcodeGeneralConfigEntity?>

    @Query("SELECT * FROM BarcodeGeneralConfigs LIMIT 1")
    suspend fun getGeneralConfig(): BarcodeGeneralConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneralConfig(config: BarcodeGeneralConfigEntity)

    @Update
    suspend fun updateGeneralConfig(config: BarcodeGeneralConfigEntity)

    // Fields Config
    @Query("SELECT * FROM BarcodeFieldConfigs WHERE is_deleted = 0 ORDER BY sequenceOrder ASC")
    fun getActiveFieldsFlow(): Flow<List<BarcodeFieldConfigEntity>>

    @Query("SELECT * FROM BarcodeFieldConfigs WHERE is_deleted = 0 ORDER BY sequenceOrder ASC")
    suspend fun getActiveFields(): List<BarcodeFieldConfigEntity>

    @Query("SELECT * FROM BarcodeFieldConfigs ORDER BY sequenceOrder ASC")
    suspend fun getAllFields(): List<BarcodeFieldConfigEntity>

    @Query("SELECT * FROM BarcodeFieldConfigs WHERE sync_status = 'pending'")
    suspend fun getPendingFields(): List<BarcodeFieldConfigEntity>

    @Query("SELECT * FROM BarcodeGeneralConfigs WHERE sync_status = 'pending'")
    suspend fun getPendingGeneralConfigs(): List<BarcodeGeneralConfigEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertField(field: BarcodeFieldConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFields(fields: List<BarcodeFieldConfigEntity>)

    @Update
    suspend fun updateField(field: BarcodeFieldConfigEntity)

    @Delete
    suspend fun deleteField(field: BarcodeFieldConfigEntity)

    @Query("UPDATE BarcodeFieldConfigs SET sync_status = 'synced' WHERE system_row_id = :id")
    suspend fun markFieldSynced(id: String)

    @Query("UPDATE BarcodeGeneralConfigs SET sync_status = 'synced' WHERE system_row_id = :id")
    suspend fun markGeneralConfigSynced(id: String)
}
