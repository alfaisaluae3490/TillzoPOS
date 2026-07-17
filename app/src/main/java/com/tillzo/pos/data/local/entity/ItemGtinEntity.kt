package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "ItemGtins",
    foreignKeys = [
        ForeignKey(
            entity = InventoryEntity::class,
            parentColumns = ["system_row_id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("item_id"),
        Index("gtin", unique = true)
    ]
)
data class ItemGtinEntity(
    @PrimaryKey
    val gtin_id: String = UUID.randomUUID().toString(),
    val item_id: String,
    val gtin: String
)
