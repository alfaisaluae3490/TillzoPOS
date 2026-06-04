package com.tillzo.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks a cashier's open/close shift (till session).
 * Created when cashier enters opening cash. Closed at end of day.
 * Sync to Google Sheets via SyncWorker (Till_Sessions tab).
 */
@Entity(tableName = "till_sessions")
data class TillSessionEntity(
    @PrimaryKey val sessionId: String,          // UUID
    val cashierId: String,                       // from AppSetupPrefs.userEmail
    val cashierName: String,                     // from AppSetupPrefs.userDisplayName
    val posTerminalId: String,                   // from AppSetupPrefs.spreadsheetId.take(20)
    val openingCash: Double,                     // manually entered at start
    val closingCash: Double = 0.0,               // physically counted at end
    val expectedCash: Double = 0.0,              // calculated: openingCash + totalCashSales
    val totalCashSales: Double = 0.0,
    val totalCardSales: Double = 0.0,
    val totalWalletSales: Double = 0.0,
    val totalUdhaarSales: Double = 0.0,
    val totalSplitSales: Double = 0.0,
    val totalSalesCount: Int = 0,
    val totalRefunds: Double = 0.0,
    val netCash: Double = 0.0,                   // closingCash - expectedCash (overage/shortage)
    val status: String = "OPEN",                 // OPEN | CLOSED
    val notes: String = "",
    val shiftDate: String = "",                  // YYYY-MM-DD
    val openedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val syncStatus: String = "pending",
    val posId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
