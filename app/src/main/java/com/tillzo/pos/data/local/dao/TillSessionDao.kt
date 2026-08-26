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

    @Query("SELECT * FROM till_sessions WHERE status = 'OPEN'")
    fun getAllOpenSessions(): List<TillSessionEntity>

    @Query("SELECT * FROM till_sessions WHERE status = 'OPEN' LIMIT 1")
    fun getOpenSessionFlow(): Flow<TillSessionEntity?>

    // FIX (2026-08-22, DEF-48): terminal-scoped variant — TillViewModel and
    // ZReportViewModel read the open session for THIS terminal; the unfiltered
    // query above returned whatever terminal's session happened to be first
    // (multi-terminal day-close reconciled the WRONG session).
    @Query("SELECT * FROM till_sessions WHERE status = 'OPEN' AND posTerminalId = :terminalId LIMIT 1")
    fun getOpenSessionFlowForTerminal(terminalId: String): Flow<TillSessionEntity?>

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
            cashVariance = :physicalCashCount - expectedCash,
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

    // DEF-127 (2026-08-25): refunds ab till session ko bhi decrement karte
    // hain. Pehle ReturnsViewModel sirf negative SaleEntity insert karta tha
    // — session ke totalCashSales/totalSalesCount/expectedCash kabhi update
    // nahi hote the, isliye Z-Report "Expected Cash" overstated rehta tha
    // aur day-close par jhoota SHORTAGE dikhata tha (drawer mein refund wali
    // cash maujood nahi hoti). CompleteSaleUseCase.addSaleToSession ka
    // negative mirror; totalSalesCount floor-at-0 rakhta hai.
    @Query("""
        UPDATE till_sessions
        SET totalCashSales = CASE WHEN totalCashSales - :cashOut < 0 THEN 0 ELSE totalCashSales - :cashOut END,
            totalCardSales = CASE WHEN totalCardSales - :cardOut < 0 THEN 0 ELSE totalCardSales - :cardOut END,
            totalWalletSales = CASE WHEN totalWalletSales - :walletOut < 0 THEN 0 ELSE totalWalletSales - :walletOut END,
            totalUdhaarSales = CASE WHEN totalUdhaarSales - :udhaarOut < 0 THEN 0 ELSE totalUdhaarSales - :udhaarOut END,
            totalSalesCount = CASE WHEN totalSalesCount > 0 THEN totalSalesCount - 1 ELSE 0 END,
            expectedCash = CASE WHEN expectedCash - :cashOut < 0 THEN 0 ELSE expectedCash - :cashOut END,
            syncStatus = 'pending',
            updatedAt = :now
        WHERE sessionId = :sessionId
    """)
    suspend fun deductRefundFromSession(
        sessionId: String,
        cashOut: Double,
        cardOut: Double,
        walletOut: Double,
        udhaarOut: Double,
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

    @Query("""
        UPDATE till_sessions
        SET totalPayIn = totalPayIn + :amount,
            expectedCash = expectedCash + :amount,
            syncStatus = 'pending',
            updatedAt = :now
        WHERE sessionId = :sessionId
    """)
    suspend fun addPayIn(
        sessionId: String,
        amount: Double,
        now: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE till_sessions
        SET totalPayOut = totalPayOut + :amount,
            expectedCash = expectedCash - :amount,
            syncStatus = 'pending',
            updatedAt = :now
        WHERE sessionId = :sessionId
    """)
    suspend fun addPayOut(
        sessionId: String,
        amount: Double,
        now: Long = System.currentTimeMillis()
    )
}
