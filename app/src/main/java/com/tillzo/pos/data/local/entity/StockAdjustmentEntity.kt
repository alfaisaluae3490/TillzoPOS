package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "StockAdjustments")
data class StockAdjustmentEntity(
    @PrimaryKey
    val adjustmentId: String = UUID.randomUUID().toString(),
    val productId: String,
    val adjustmentType: String, // RECEIVED, RETURNED, CORRECTION, DAMAGED
    val quantityChanged: Double,
    val reason: String,
    val adjustedBy: String,
    val syncStatus: String = "pending",
    val createdAt: Long = System.currentTimeMillis()
)
