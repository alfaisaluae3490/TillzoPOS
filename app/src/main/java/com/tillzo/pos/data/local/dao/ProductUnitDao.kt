package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tillzo.pos.data.local.entity.ProductUnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductUnitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: ProductUnitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(units: List<ProductUnitEntity>)

    @Query("SELECT * FROM product_units WHERE isDeleted = 0 ORDER BY unitName ASC")
    fun getAllUnits(): Flow<List<ProductUnitEntity>>

    @Query("SELECT COUNT(*) FROM product_units WHERE isDeleted = 0")
    suspend fun getActiveCount(): Int

    @Query("UPDATE product_units SET isDeleted = 1, updatedAt = :time WHERE unitId = :id")
    suspend fun softDelete(id: String, time: Long = System.currentTimeMillis())
}
