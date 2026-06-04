package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "wastage_log")
data class WastageEntity(
    @PrimaryKey
    val wastageId: String = UUID.randomUUID().toString(),
    val productId: String,
    val productName: String,
    val batchId: String = "",
    val batchNumber: String = "",
    val quantity: Double,
    val unit: String,
    val costPrice: Double,
    val totalLoss: Double,          // quantity × costPrice
    val reason: String,             // EXPIRED | DAMAGED | THEFT | OTHER
    val notes: String = "",
    val loggedBy: String = "",
    val wastageDate: String,        // YYYY-MM-DD
    val syncStatus: String = "pending",
    val posTerminalId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

fun WastageEntity.toSheetRow(): List<Any> = listOf(
    wastageId, productId, productName, batchId, batchNumber,
    quantity, unit, costPrice, totalLoss, reason, notes, loggedBy,
    wastageDate, syncStatus, posTerminalId, createdAt, updatedAt
)
