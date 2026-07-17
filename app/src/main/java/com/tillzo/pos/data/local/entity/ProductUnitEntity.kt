package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * ProductUnitEntity — user-defined units of measure (e.g. KG, Piece, Liter).
 * Pre-seeded with 7 defaults on first launch via ProductUnitsScreen.
 *
 * syncStatus logic:
 *   - Default units seeded on first launch → "synced" (never pushed to Sheets)
 *   - Custom units added by user → "pending" (picked up by SyncWorker)
 */
@Entity(tableName = "product_units")
data class ProductUnitEntity(
    @PrimaryKey val unitId: String = UUID.randomUUID().toString(),
    val unitName: String,           // e.g. "Kilogram", "Piece", "Liter"
    val abbreviation: String,       // e.g. "KG", "PC", "L"
    val isDeleted: Boolean = false,
    val syncStatus: String = "pending", // "pending" for user-created, "synced" for seed defaults
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toSyncMap(): Map<String, Any> {
        return mapOf(
            "unitId" to unitId,
            "unitName" to unitName,
            "abbreviation" to abbreviation,
            "isDeleted" to (if (isDeleted) 1 else 0),
            "syncStatus" to "synced",
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}
