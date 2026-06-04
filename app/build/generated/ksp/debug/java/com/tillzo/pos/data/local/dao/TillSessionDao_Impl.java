package com.tillzo.pos.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tillzo.pos.data.local.entity.TillSessionEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TillSessionDao_Impl implements TillSessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TillSessionEntity> __insertionAdapterOfTillSessionEntity;

  private final EntityDeletionOrUpdateAdapter<TillSessionEntity> __updateAdapterOfTillSessionEntity;

  private final SharedSQLiteStatement __preparedStmtOfCloseSession;

  private final SharedSQLiteStatement __preparedStmtOfAddSaleToSession;

  private final SharedSQLiteStatement __preparedStmtOfMarkSynced;

  public TillSessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTillSessionEntity = new EntityInsertionAdapter<TillSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `till_sessions` (`sessionId`,`cashierId`,`cashierName`,`posTerminalId`,`openingCash`,`closingCash`,`expectedCash`,`totalCashSales`,`totalCardSales`,`totalWalletSales`,`totalUdhaarSales`,`totalSplitSales`,`totalSalesCount`,`totalRefunds`,`netCash`,`status`,`notes`,`shiftDate`,`openedAt`,`closedAt`,`syncStatus`,`posId`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TillSessionEntity entity) {
        statement.bindString(1, entity.getSessionId());
        statement.bindString(2, entity.getCashierId());
        statement.bindString(3, entity.getCashierName());
        statement.bindString(4, entity.getPosTerminalId());
        statement.bindDouble(5, entity.getOpeningCash());
        statement.bindDouble(6, entity.getClosingCash());
        statement.bindDouble(7, entity.getExpectedCash());
        statement.bindDouble(8, entity.getTotalCashSales());
        statement.bindDouble(9, entity.getTotalCardSales());
        statement.bindDouble(10, entity.getTotalWalletSales());
        statement.bindDouble(11, entity.getTotalUdhaarSales());
        statement.bindDouble(12, entity.getTotalSplitSales());
        statement.bindLong(13, entity.getTotalSalesCount());
        statement.bindDouble(14, entity.getTotalRefunds());
        statement.bindDouble(15, entity.getNetCash());
        statement.bindString(16, entity.getStatus());
        statement.bindString(17, entity.getNotes());
        statement.bindString(18, entity.getShiftDate());
        statement.bindLong(19, entity.getOpenedAt());
        if (entity.getClosedAt() == null) {
          statement.bindNull(20);
        } else {
          statement.bindLong(20, entity.getClosedAt());
        }
        statement.bindString(21, entity.getSyncStatus());
        statement.bindString(22, entity.getPosId());
        statement.bindLong(23, entity.getCreatedAt());
        statement.bindLong(24, entity.getUpdatedAt());
      }
    };
    this.__updateAdapterOfTillSessionEntity = new EntityDeletionOrUpdateAdapter<TillSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `till_sessions` SET `sessionId` = ?,`cashierId` = ?,`cashierName` = ?,`posTerminalId` = ?,`openingCash` = ?,`closingCash` = ?,`expectedCash` = ?,`totalCashSales` = ?,`totalCardSales` = ?,`totalWalletSales` = ?,`totalUdhaarSales` = ?,`totalSplitSales` = ?,`totalSalesCount` = ?,`totalRefunds` = ?,`netCash` = ?,`status` = ?,`notes` = ?,`shiftDate` = ?,`openedAt` = ?,`closedAt` = ?,`syncStatus` = ?,`posId` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `sessionId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TillSessionEntity entity) {
        statement.bindString(1, entity.getSessionId());
        statement.bindString(2, entity.getCashierId());
        statement.bindString(3, entity.getCashierName());
        statement.bindString(4, entity.getPosTerminalId());
        statement.bindDouble(5, entity.getOpeningCash());
        statement.bindDouble(6, entity.getClosingCash());
        statement.bindDouble(7, entity.getExpectedCash());
        statement.bindDouble(8, entity.getTotalCashSales());
        statement.bindDouble(9, entity.getTotalCardSales());
        statement.bindDouble(10, entity.getTotalWalletSales());
        statement.bindDouble(11, entity.getTotalUdhaarSales());
        statement.bindDouble(12, entity.getTotalSplitSales());
        statement.bindLong(13, entity.getTotalSalesCount());
        statement.bindDouble(14, entity.getTotalRefunds());
        statement.bindDouble(15, entity.getNetCash());
        statement.bindString(16, entity.getStatus());
        statement.bindString(17, entity.getNotes());
        statement.bindString(18, entity.getShiftDate());
        statement.bindLong(19, entity.getOpenedAt());
        if (entity.getClosedAt() == null) {
          statement.bindNull(20);
        } else {
          statement.bindLong(20, entity.getClosedAt());
        }
        statement.bindString(21, entity.getSyncStatus());
        statement.bindString(22, entity.getPosId());
        statement.bindLong(23, entity.getCreatedAt());
        statement.bindLong(24, entity.getUpdatedAt());
        statement.bindString(25, entity.getSessionId());
      }
    };
    this.__preparedStmtOfCloseSession = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE till_sessions\n"
                + "        SET status = 'CLOSED',\n"
                + "            closingCash = ?,\n"
                + "            netCash = ?,\n"
                + "            closedAt = ?,\n"
                + "            syncStatus = 'pending',\n"
                + "            updatedAt = ?\n"
                + "        WHERE sessionId = ?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfAddSaleToSession = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE till_sessions\n"
                + "        SET totalCashSales = totalCashSales + ?,\n"
                + "            totalSalesCount = totalSalesCount + 1,\n"
                + "            expectedCash = expectedCash + ?,\n"
                + "            syncStatus = 'pending',\n"
                + "            updatedAt = ?\n"
                + "        WHERE sessionId = ?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfMarkSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE till_sessions SET syncStatus = 'synced' WHERE sessionId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertSession(final TillSessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTillSessionEntity.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSession(final TillSessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTillSessionEntity.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object closeSession(final String sessionId, final double closingCash, final double netCash,
      final long closedAt, final long now, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfCloseSession.acquire();
        int _argIndex = 1;
        _stmt.bindDouble(_argIndex, closingCash);
        _argIndex = 2;
        _stmt.bindDouble(_argIndex, netCash);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, closedAt);
        _argIndex = 4;
        _stmt.bindLong(_argIndex, now);
        _argIndex = 5;
        _stmt.bindString(_argIndex, sessionId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfCloseSession.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object addSaleToSession(final String sessionId, final double amount, final double cashIn,
      final long now, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfAddSaleToSession.acquire();
        int _argIndex = 1;
        _stmt.bindDouble(_argIndex, amount);
        _argIndex = 2;
        _stmt.bindDouble(_argIndex, cashIn);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, now);
        _argIndex = 4;
        _stmt.bindString(_argIndex, sessionId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfAddSaleToSession.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markSynced(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkSynced.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkSynced.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getOpenSession(final String terminalId,
      final Continuation<? super TillSessionEntity> $completion) {
    final String _sql = "SELECT * FROM till_sessions WHERE status = 'OPEN' AND posTerminalId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, terminalId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TillSessionEntity>() {
      @Override
      @Nullable
      public TillSessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfCashierId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashierId");
          final int _cursorIndexOfCashierName = CursorUtil.getColumnIndexOrThrow(_cursor, "cashierName");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfOpeningCash = CursorUtil.getColumnIndexOrThrow(_cursor, "openingCash");
          final int _cursorIndexOfClosingCash = CursorUtil.getColumnIndexOrThrow(_cursor, "closingCash");
          final int _cursorIndexOfExpectedCash = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedCash");
          final int _cursorIndexOfTotalCashSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCashSales");
          final int _cursorIndexOfTotalCardSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCardSales");
          final int _cursorIndexOfTotalWalletSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalWalletSales");
          final int _cursorIndexOfTotalUdhaarSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalUdhaarSales");
          final int _cursorIndexOfTotalSplitSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSplitSales");
          final int _cursorIndexOfTotalSalesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSalesCount");
          final int _cursorIndexOfTotalRefunds = CursorUtil.getColumnIndexOrThrow(_cursor, "totalRefunds");
          final int _cursorIndexOfNetCash = CursorUtil.getColumnIndexOrThrow(_cursor, "netCash");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfShiftDate = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftDate");
          final int _cursorIndexOfOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "openedAt");
          final int _cursorIndexOfClosedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "closedAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosId = CursorUtil.getColumnIndexOrThrow(_cursor, "posId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final TillSessionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpCashierId;
            _tmpCashierId = _cursor.getString(_cursorIndexOfCashierId);
            final String _tmpCashierName;
            _tmpCashierName = _cursor.getString(_cursorIndexOfCashierName);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final double _tmpOpeningCash;
            _tmpOpeningCash = _cursor.getDouble(_cursorIndexOfOpeningCash);
            final double _tmpClosingCash;
            _tmpClosingCash = _cursor.getDouble(_cursorIndexOfClosingCash);
            final double _tmpExpectedCash;
            _tmpExpectedCash = _cursor.getDouble(_cursorIndexOfExpectedCash);
            final double _tmpTotalCashSales;
            _tmpTotalCashSales = _cursor.getDouble(_cursorIndexOfTotalCashSales);
            final double _tmpTotalCardSales;
            _tmpTotalCardSales = _cursor.getDouble(_cursorIndexOfTotalCardSales);
            final double _tmpTotalWalletSales;
            _tmpTotalWalletSales = _cursor.getDouble(_cursorIndexOfTotalWalletSales);
            final double _tmpTotalUdhaarSales;
            _tmpTotalUdhaarSales = _cursor.getDouble(_cursorIndexOfTotalUdhaarSales);
            final double _tmpTotalSplitSales;
            _tmpTotalSplitSales = _cursor.getDouble(_cursorIndexOfTotalSplitSales);
            final int _tmpTotalSalesCount;
            _tmpTotalSalesCount = _cursor.getInt(_cursorIndexOfTotalSalesCount);
            final double _tmpTotalRefunds;
            _tmpTotalRefunds = _cursor.getDouble(_cursorIndexOfTotalRefunds);
            final double _tmpNetCash;
            _tmpNetCash = _cursor.getDouble(_cursorIndexOfNetCash);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpShiftDate;
            _tmpShiftDate = _cursor.getString(_cursorIndexOfShiftDate);
            final long _tmpOpenedAt;
            _tmpOpenedAt = _cursor.getLong(_cursorIndexOfOpenedAt);
            final Long _tmpClosedAt;
            if (_cursor.isNull(_cursorIndexOfClosedAt)) {
              _tmpClosedAt = null;
            } else {
              _tmpClosedAt = _cursor.getLong(_cursorIndexOfClosedAt);
            }
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosId;
            _tmpPosId = _cursor.getString(_cursorIndexOfPosId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new TillSessionEntity(_tmpSessionId,_tmpCashierId,_tmpCashierName,_tmpPosTerminalId,_tmpOpeningCash,_tmpClosingCash,_tmpExpectedCash,_tmpTotalCashSales,_tmpTotalCardSales,_tmpTotalWalletSales,_tmpTotalUdhaarSales,_tmpTotalSplitSales,_tmpTotalSalesCount,_tmpTotalRefunds,_tmpNetCash,_tmpStatus,_tmpNotes,_tmpShiftDate,_tmpOpenedAt,_tmpClosedAt,_tmpSyncStatus,_tmpPosId,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<TillSessionEntity> getOpenSessionFlow() {
    final String _sql = "SELECT * FROM till_sessions WHERE status = 'OPEN' LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"till_sessions"}, new Callable<TillSessionEntity>() {
      @Override
      @Nullable
      public TillSessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfCashierId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashierId");
          final int _cursorIndexOfCashierName = CursorUtil.getColumnIndexOrThrow(_cursor, "cashierName");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfOpeningCash = CursorUtil.getColumnIndexOrThrow(_cursor, "openingCash");
          final int _cursorIndexOfClosingCash = CursorUtil.getColumnIndexOrThrow(_cursor, "closingCash");
          final int _cursorIndexOfExpectedCash = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedCash");
          final int _cursorIndexOfTotalCashSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCashSales");
          final int _cursorIndexOfTotalCardSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCardSales");
          final int _cursorIndexOfTotalWalletSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalWalletSales");
          final int _cursorIndexOfTotalUdhaarSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalUdhaarSales");
          final int _cursorIndexOfTotalSplitSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSplitSales");
          final int _cursorIndexOfTotalSalesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSalesCount");
          final int _cursorIndexOfTotalRefunds = CursorUtil.getColumnIndexOrThrow(_cursor, "totalRefunds");
          final int _cursorIndexOfNetCash = CursorUtil.getColumnIndexOrThrow(_cursor, "netCash");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfShiftDate = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftDate");
          final int _cursorIndexOfOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "openedAt");
          final int _cursorIndexOfClosedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "closedAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosId = CursorUtil.getColumnIndexOrThrow(_cursor, "posId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final TillSessionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpCashierId;
            _tmpCashierId = _cursor.getString(_cursorIndexOfCashierId);
            final String _tmpCashierName;
            _tmpCashierName = _cursor.getString(_cursorIndexOfCashierName);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final double _tmpOpeningCash;
            _tmpOpeningCash = _cursor.getDouble(_cursorIndexOfOpeningCash);
            final double _tmpClosingCash;
            _tmpClosingCash = _cursor.getDouble(_cursorIndexOfClosingCash);
            final double _tmpExpectedCash;
            _tmpExpectedCash = _cursor.getDouble(_cursorIndexOfExpectedCash);
            final double _tmpTotalCashSales;
            _tmpTotalCashSales = _cursor.getDouble(_cursorIndexOfTotalCashSales);
            final double _tmpTotalCardSales;
            _tmpTotalCardSales = _cursor.getDouble(_cursorIndexOfTotalCardSales);
            final double _tmpTotalWalletSales;
            _tmpTotalWalletSales = _cursor.getDouble(_cursorIndexOfTotalWalletSales);
            final double _tmpTotalUdhaarSales;
            _tmpTotalUdhaarSales = _cursor.getDouble(_cursorIndexOfTotalUdhaarSales);
            final double _tmpTotalSplitSales;
            _tmpTotalSplitSales = _cursor.getDouble(_cursorIndexOfTotalSplitSales);
            final int _tmpTotalSalesCount;
            _tmpTotalSalesCount = _cursor.getInt(_cursorIndexOfTotalSalesCount);
            final double _tmpTotalRefunds;
            _tmpTotalRefunds = _cursor.getDouble(_cursorIndexOfTotalRefunds);
            final double _tmpNetCash;
            _tmpNetCash = _cursor.getDouble(_cursorIndexOfNetCash);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpShiftDate;
            _tmpShiftDate = _cursor.getString(_cursorIndexOfShiftDate);
            final long _tmpOpenedAt;
            _tmpOpenedAt = _cursor.getLong(_cursorIndexOfOpenedAt);
            final Long _tmpClosedAt;
            if (_cursor.isNull(_cursorIndexOfClosedAt)) {
              _tmpClosedAt = null;
            } else {
              _tmpClosedAt = _cursor.getLong(_cursorIndexOfClosedAt);
            }
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosId;
            _tmpPosId = _cursor.getString(_cursorIndexOfPosId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new TillSessionEntity(_tmpSessionId,_tmpCashierId,_tmpCashierName,_tmpPosTerminalId,_tmpOpeningCash,_tmpClosingCash,_tmpExpectedCash,_tmpTotalCashSales,_tmpTotalCardSales,_tmpTotalWalletSales,_tmpTotalUdhaarSales,_tmpTotalSplitSales,_tmpTotalSalesCount,_tmpTotalRefunds,_tmpNetCash,_tmpStatus,_tmpNotes,_tmpShiftDate,_tmpOpenedAt,_tmpClosedAt,_tmpSyncStatus,_tmpPosId,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<TillSessionEntity>> getAllSessions() {
    final String _sql = "SELECT * FROM till_sessions ORDER BY openedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"till_sessions"}, new Callable<List<TillSessionEntity>>() {
      @Override
      @NonNull
      public List<TillSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfCashierId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashierId");
          final int _cursorIndexOfCashierName = CursorUtil.getColumnIndexOrThrow(_cursor, "cashierName");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfOpeningCash = CursorUtil.getColumnIndexOrThrow(_cursor, "openingCash");
          final int _cursorIndexOfClosingCash = CursorUtil.getColumnIndexOrThrow(_cursor, "closingCash");
          final int _cursorIndexOfExpectedCash = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedCash");
          final int _cursorIndexOfTotalCashSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCashSales");
          final int _cursorIndexOfTotalCardSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCardSales");
          final int _cursorIndexOfTotalWalletSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalWalletSales");
          final int _cursorIndexOfTotalUdhaarSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalUdhaarSales");
          final int _cursorIndexOfTotalSplitSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSplitSales");
          final int _cursorIndexOfTotalSalesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSalesCount");
          final int _cursorIndexOfTotalRefunds = CursorUtil.getColumnIndexOrThrow(_cursor, "totalRefunds");
          final int _cursorIndexOfNetCash = CursorUtil.getColumnIndexOrThrow(_cursor, "netCash");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfShiftDate = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftDate");
          final int _cursorIndexOfOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "openedAt");
          final int _cursorIndexOfClosedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "closedAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosId = CursorUtil.getColumnIndexOrThrow(_cursor, "posId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<TillSessionEntity> _result = new ArrayList<TillSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TillSessionEntity _item;
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpCashierId;
            _tmpCashierId = _cursor.getString(_cursorIndexOfCashierId);
            final String _tmpCashierName;
            _tmpCashierName = _cursor.getString(_cursorIndexOfCashierName);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final double _tmpOpeningCash;
            _tmpOpeningCash = _cursor.getDouble(_cursorIndexOfOpeningCash);
            final double _tmpClosingCash;
            _tmpClosingCash = _cursor.getDouble(_cursorIndexOfClosingCash);
            final double _tmpExpectedCash;
            _tmpExpectedCash = _cursor.getDouble(_cursorIndexOfExpectedCash);
            final double _tmpTotalCashSales;
            _tmpTotalCashSales = _cursor.getDouble(_cursorIndexOfTotalCashSales);
            final double _tmpTotalCardSales;
            _tmpTotalCardSales = _cursor.getDouble(_cursorIndexOfTotalCardSales);
            final double _tmpTotalWalletSales;
            _tmpTotalWalletSales = _cursor.getDouble(_cursorIndexOfTotalWalletSales);
            final double _tmpTotalUdhaarSales;
            _tmpTotalUdhaarSales = _cursor.getDouble(_cursorIndexOfTotalUdhaarSales);
            final double _tmpTotalSplitSales;
            _tmpTotalSplitSales = _cursor.getDouble(_cursorIndexOfTotalSplitSales);
            final int _tmpTotalSalesCount;
            _tmpTotalSalesCount = _cursor.getInt(_cursorIndexOfTotalSalesCount);
            final double _tmpTotalRefunds;
            _tmpTotalRefunds = _cursor.getDouble(_cursorIndexOfTotalRefunds);
            final double _tmpNetCash;
            _tmpNetCash = _cursor.getDouble(_cursorIndexOfNetCash);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpShiftDate;
            _tmpShiftDate = _cursor.getString(_cursorIndexOfShiftDate);
            final long _tmpOpenedAt;
            _tmpOpenedAt = _cursor.getLong(_cursorIndexOfOpenedAt);
            final Long _tmpClosedAt;
            if (_cursor.isNull(_cursorIndexOfClosedAt)) {
              _tmpClosedAt = null;
            } else {
              _tmpClosedAt = _cursor.getLong(_cursorIndexOfClosedAt);
            }
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosId;
            _tmpPosId = _cursor.getString(_cursorIndexOfPosId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new TillSessionEntity(_tmpSessionId,_tmpCashierId,_tmpCashierName,_tmpPosTerminalId,_tmpOpeningCash,_tmpClosingCash,_tmpExpectedCash,_tmpTotalCashSales,_tmpTotalCardSales,_tmpTotalWalletSales,_tmpTotalUdhaarSales,_tmpTotalSplitSales,_tmpTotalSalesCount,_tmpTotalRefunds,_tmpNetCash,_tmpStatus,_tmpNotes,_tmpShiftDate,_tmpOpenedAt,_tmpClosedAt,_tmpSyncStatus,_tmpPosId,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<TillSessionEntity>> getSessionsForDate(final String date) {
    final String _sql = "SELECT * FROM till_sessions WHERE shiftDate = ? ORDER BY openedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"till_sessions"}, new Callable<List<TillSessionEntity>>() {
      @Override
      @NonNull
      public List<TillSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfCashierId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashierId");
          final int _cursorIndexOfCashierName = CursorUtil.getColumnIndexOrThrow(_cursor, "cashierName");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfOpeningCash = CursorUtil.getColumnIndexOrThrow(_cursor, "openingCash");
          final int _cursorIndexOfClosingCash = CursorUtil.getColumnIndexOrThrow(_cursor, "closingCash");
          final int _cursorIndexOfExpectedCash = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedCash");
          final int _cursorIndexOfTotalCashSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCashSales");
          final int _cursorIndexOfTotalCardSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCardSales");
          final int _cursorIndexOfTotalWalletSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalWalletSales");
          final int _cursorIndexOfTotalUdhaarSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalUdhaarSales");
          final int _cursorIndexOfTotalSplitSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSplitSales");
          final int _cursorIndexOfTotalSalesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSalesCount");
          final int _cursorIndexOfTotalRefunds = CursorUtil.getColumnIndexOrThrow(_cursor, "totalRefunds");
          final int _cursorIndexOfNetCash = CursorUtil.getColumnIndexOrThrow(_cursor, "netCash");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfShiftDate = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftDate");
          final int _cursorIndexOfOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "openedAt");
          final int _cursorIndexOfClosedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "closedAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosId = CursorUtil.getColumnIndexOrThrow(_cursor, "posId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<TillSessionEntity> _result = new ArrayList<TillSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TillSessionEntity _item;
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpCashierId;
            _tmpCashierId = _cursor.getString(_cursorIndexOfCashierId);
            final String _tmpCashierName;
            _tmpCashierName = _cursor.getString(_cursorIndexOfCashierName);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final double _tmpOpeningCash;
            _tmpOpeningCash = _cursor.getDouble(_cursorIndexOfOpeningCash);
            final double _tmpClosingCash;
            _tmpClosingCash = _cursor.getDouble(_cursorIndexOfClosingCash);
            final double _tmpExpectedCash;
            _tmpExpectedCash = _cursor.getDouble(_cursorIndexOfExpectedCash);
            final double _tmpTotalCashSales;
            _tmpTotalCashSales = _cursor.getDouble(_cursorIndexOfTotalCashSales);
            final double _tmpTotalCardSales;
            _tmpTotalCardSales = _cursor.getDouble(_cursorIndexOfTotalCardSales);
            final double _tmpTotalWalletSales;
            _tmpTotalWalletSales = _cursor.getDouble(_cursorIndexOfTotalWalletSales);
            final double _tmpTotalUdhaarSales;
            _tmpTotalUdhaarSales = _cursor.getDouble(_cursorIndexOfTotalUdhaarSales);
            final double _tmpTotalSplitSales;
            _tmpTotalSplitSales = _cursor.getDouble(_cursorIndexOfTotalSplitSales);
            final int _tmpTotalSalesCount;
            _tmpTotalSalesCount = _cursor.getInt(_cursorIndexOfTotalSalesCount);
            final double _tmpTotalRefunds;
            _tmpTotalRefunds = _cursor.getDouble(_cursorIndexOfTotalRefunds);
            final double _tmpNetCash;
            _tmpNetCash = _cursor.getDouble(_cursorIndexOfNetCash);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpShiftDate;
            _tmpShiftDate = _cursor.getString(_cursorIndexOfShiftDate);
            final long _tmpOpenedAt;
            _tmpOpenedAt = _cursor.getLong(_cursorIndexOfOpenedAt);
            final Long _tmpClosedAt;
            if (_cursor.isNull(_cursorIndexOfClosedAt)) {
              _tmpClosedAt = null;
            } else {
              _tmpClosedAt = _cursor.getLong(_cursorIndexOfClosedAt);
            }
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosId;
            _tmpPosId = _cursor.getString(_cursorIndexOfPosId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new TillSessionEntity(_tmpSessionId,_tmpCashierId,_tmpCashierName,_tmpPosTerminalId,_tmpOpeningCash,_tmpClosingCash,_tmpExpectedCash,_tmpTotalCashSales,_tmpTotalCardSales,_tmpTotalWalletSales,_tmpTotalUdhaarSales,_tmpTotalSplitSales,_tmpTotalSalesCount,_tmpTotalRefunds,_tmpNetCash,_tmpStatus,_tmpNotes,_tmpShiftDate,_tmpOpenedAt,_tmpClosedAt,_tmpSyncStatus,_tmpPosId,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getPendingSessions(
      final Continuation<? super List<TillSessionEntity>> $completion) {
    final String _sql = "SELECT * FROM till_sessions WHERE syncStatus = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TillSessionEntity>>() {
      @Override
      @NonNull
      public List<TillSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfCashierId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashierId");
          final int _cursorIndexOfCashierName = CursorUtil.getColumnIndexOrThrow(_cursor, "cashierName");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfOpeningCash = CursorUtil.getColumnIndexOrThrow(_cursor, "openingCash");
          final int _cursorIndexOfClosingCash = CursorUtil.getColumnIndexOrThrow(_cursor, "closingCash");
          final int _cursorIndexOfExpectedCash = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedCash");
          final int _cursorIndexOfTotalCashSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCashSales");
          final int _cursorIndexOfTotalCardSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCardSales");
          final int _cursorIndexOfTotalWalletSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalWalletSales");
          final int _cursorIndexOfTotalUdhaarSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalUdhaarSales");
          final int _cursorIndexOfTotalSplitSales = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSplitSales");
          final int _cursorIndexOfTotalSalesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSalesCount");
          final int _cursorIndexOfTotalRefunds = CursorUtil.getColumnIndexOrThrow(_cursor, "totalRefunds");
          final int _cursorIndexOfNetCash = CursorUtil.getColumnIndexOrThrow(_cursor, "netCash");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfShiftDate = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftDate");
          final int _cursorIndexOfOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "openedAt");
          final int _cursorIndexOfClosedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "closedAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosId = CursorUtil.getColumnIndexOrThrow(_cursor, "posId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<TillSessionEntity> _result = new ArrayList<TillSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TillSessionEntity _item;
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpCashierId;
            _tmpCashierId = _cursor.getString(_cursorIndexOfCashierId);
            final String _tmpCashierName;
            _tmpCashierName = _cursor.getString(_cursorIndexOfCashierName);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final double _tmpOpeningCash;
            _tmpOpeningCash = _cursor.getDouble(_cursorIndexOfOpeningCash);
            final double _tmpClosingCash;
            _tmpClosingCash = _cursor.getDouble(_cursorIndexOfClosingCash);
            final double _tmpExpectedCash;
            _tmpExpectedCash = _cursor.getDouble(_cursorIndexOfExpectedCash);
            final double _tmpTotalCashSales;
            _tmpTotalCashSales = _cursor.getDouble(_cursorIndexOfTotalCashSales);
            final double _tmpTotalCardSales;
            _tmpTotalCardSales = _cursor.getDouble(_cursorIndexOfTotalCardSales);
            final double _tmpTotalWalletSales;
            _tmpTotalWalletSales = _cursor.getDouble(_cursorIndexOfTotalWalletSales);
            final double _tmpTotalUdhaarSales;
            _tmpTotalUdhaarSales = _cursor.getDouble(_cursorIndexOfTotalUdhaarSales);
            final double _tmpTotalSplitSales;
            _tmpTotalSplitSales = _cursor.getDouble(_cursorIndexOfTotalSplitSales);
            final int _tmpTotalSalesCount;
            _tmpTotalSalesCount = _cursor.getInt(_cursorIndexOfTotalSalesCount);
            final double _tmpTotalRefunds;
            _tmpTotalRefunds = _cursor.getDouble(_cursorIndexOfTotalRefunds);
            final double _tmpNetCash;
            _tmpNetCash = _cursor.getDouble(_cursorIndexOfNetCash);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpShiftDate;
            _tmpShiftDate = _cursor.getString(_cursorIndexOfShiftDate);
            final long _tmpOpenedAt;
            _tmpOpenedAt = _cursor.getLong(_cursorIndexOfOpenedAt);
            final Long _tmpClosedAt;
            if (_cursor.isNull(_cursorIndexOfClosedAt)) {
              _tmpClosedAt = null;
            } else {
              _tmpClosedAt = _cursor.getLong(_cursorIndexOfClosedAt);
            }
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosId;
            _tmpPosId = _cursor.getString(_cursorIndexOfPosId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new TillSessionEntity(_tmpSessionId,_tmpCashierId,_tmpCashierName,_tmpPosTerminalId,_tmpOpeningCash,_tmpClosingCash,_tmpExpectedCash,_tmpTotalCashSales,_tmpTotalCardSales,_tmpTotalWalletSales,_tmpTotalUdhaarSales,_tmpTotalSplitSales,_tmpTotalSalesCount,_tmpTotalRefunds,_tmpNetCash,_tmpStatus,_tmpNotes,_tmpShiftDate,_tmpOpenedAt,_tmpClosedAt,_tmpSyncStatus,_tmpPosId,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
