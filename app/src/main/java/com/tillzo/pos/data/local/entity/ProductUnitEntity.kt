package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * ProductUnitEntity — user-defined units of measure (e.g. KG, Piece, Liter).
 * Pre-seeded with 7 defaults on first launch via ProductUnitsScreen.
 */
@Entity(tableName = "product_units")
data class ProductUnitEntity(
    @PrimaryKey val unitId: String = UUID.randomUUID().toString(),
    val unitName: String,           // e.g. "Kilogram", "Piece", "Liter"
    val abbreviation: String,       // e.g. "KG", "PC", "L"
    val isDeleted: Boolean = false,
    val syncStatus: String = "synced", // units are local config, synced = default
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
