package com.tillzo.pos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_logs")
data class AppLogEntity(
    @PrimaryKey(autoGenerate = true)
    val logId: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val logLevel: String,
    val message: String
)
