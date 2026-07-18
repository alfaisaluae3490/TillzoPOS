package com.tillzo.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tillzo.pos.data.local.entity.TillSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TillSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TillSessionEntity)

    @Update
    suspend fun updateSession(session: TillSessionEntity)

    @Query("SELECT * FROM till_sessions WHERE status = 'OPEN' AND posTerminalId = :terminalId LIMIT 1")
    suspend fun getOpenSession(terminalId: String): TillSessionEntity?

    @Query("SELECT * FROM till_sessions WHERE status = 'OPEN' LIMIT 1")
    fun getOpenSessionFlow(): Flow<TillSessionEntity?>

    @Query("SELECT * FROM till_sessions ORDER BY openedAt DESC")
    fun getAllSessions(): Flow<List<TillSessionEntity>>

    @Query("SELECT * FROM till_sessions WHERE shiftDate = :date ORDER BY openedAt DESC")
    fun getSessionsForDate(date: String): Flow<List<TillSessionEntity>>

    @Query("""
        UPDATE till_sessions
        SET status = 'CLOSED',
            closingCash = :closingCash,
            netCash = :netCash,
            closedAt = :closedAt,
            syncStatus = 'pending',
            updatedAt = :now
        WHERE sessionId = :sessionId
    """)
    suspend fun closeSession(
        sessionId: String,
        closingCash: Double,
        netCash: Double,
        closedAt: Long,
        now: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE till_sessions
        SET status = 'RECONCILED',
            closingCash = :physicalCashCount,
            netCash = :physicalCashCount - expectedCash,
            closedAt = :closedAt,
            syncStatus = 'pending',
            updatedAt = :now
        WHERE sessionId = :sessionId AND status = 'OPEN'
    """)
    suspend fun reconcileSession(
        sessionId: String,
        physicalCashCount: Double,
        closedAt: Long,
        now: Long = System.currentTimeMillis()
    )

    @Query("SELECT COUNT(*) FROM till_sessions WHERE syncStatus = 'pending'")
    suspend fun getPendingSyncCount(): Int

    @Query("""
        UPDATE till_sessions
        SET totalCashSales = totalCashSales + :cashIn,
            totalCardSales = totalCardSales + :cardIn,
            totalWalletSales = totalWalletSales + :walletIn,
            totalUdhaarSales = totalUdhaarSales + :udhaarIn,
            totalSplitSales = CASE WHEN :paymentMethod = 'SPLIT' THEN totalSplitSales + :totalAmount ELSE totalSplitSales END,
            totalSalesCount = totalSalesCount + 1,
            expectedCash = expectedCash + :cashIn,
            syncStatus = 'pending',
            updatedAt = :now
        WHERE sessionId = :sessionId
    """)
    suspend fun addSaleToSession(
        sessionId: String,
        totalAmount: Double,
        cashIn: Double,
        cardIn: Double,
        walletIn: Double,
        udhaarIn: Double,
        paymentMethod: String,
        now: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM till_sessions WHERE syncStatus = 'pending'")
    suspend fun getPendingSessions(): List<TillSessionEntity>

    @Query("UPDATE till_sessions SET syncStatus = 'synced' WHERE sessionId = :id")
    suspend fun markSynced(id: String)

    @Query("""
        UPDATE till_sessions
        SET expectedCash = expectedCash - :amount,
            syncStatus = 'pending',
            updatedAt = :now
        WHERE sessionId = :sessionId
    """)
    suspend fun deductExpenseFromSession(
        sessionId: String,
        amount: Double,
        now: Long = System.currentTimeMillis()
    )
}
