package com.tillzo.pos.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tillzo.pos.data.local.entity.WastageEntity;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
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
public final class WastageDao_Impl implements WastageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WastageEntity> __insertionAdapterOfWastageEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkSynced;

  private final SharedSQLiteStatement __preparedStmtOfSoftDelete;

  public WastageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWastageEntity = new EntityInsertionAdapter<WastageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `wastage_log` (`wastageId`,`productId`,`productName`,`batchId`,`batchNumber`,`quantity`,`unit`,`costPrice`,`totalLoss`,`reason`,`notes`,`loggedBy`,`wastageDate`,`syncStatus`,`posTerminalId`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WastageEntity entity) {
        statement.bindString(1, entity.getWastageId());
        statement.bindString(2, entity.getProductId());
        statement.bindString(3, entity.getProductName());
        statement.bindString(4, entity.getBatchId());
        statement.bindString(5, entity.getBatchNumber());
        statement.bindDouble(6, entity.getQuantity());
        statement.bindString(7, entity.getUnit());
        statement.bindDouble(8, entity.getCostPrice());
        statement.bindDouble(9, entity.getTotalLoss());
        statement.bindString(10, entity.getReason());
        statement.bindString(11, entity.getNotes());
        statement.bindString(12, entity.getLoggedBy());
        statement.bindString(13, entity.getWastageDate());
        statement.bindString(14, entity.getSyncStatus());
        statement.bindString(15, entity.getPosTerminalId());
        statement.bindLong(16, entity.getCreatedAt());
        statement.bindLong(17, entity.getUpdatedAt());
      }
    };
    this.__preparedStmtOfMarkSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE wastage_log SET syncStatus = 'synced' WHERE wastageId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSoftDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE wastage_log SET syncStatus = 'deleted' WHERE wastageId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertWastage(final WastageEntity wastage,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWastageEntity.insert(wastage);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
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
  public Object softDelete(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDelete.acquire();
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
          __preparedStmtOfSoftDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<WastageEntity>> getAllWastage() {
    final String _sql = "SELECT * FROM wastage_log WHERE wastageDate != '' ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"wastage_log"}, new Callable<List<WastageEntity>>() {
      @Override
      @NonNull
      public List<WastageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfWastageId = CursorUtil.getColumnIndexOrThrow(_cursor, "wastageId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "costPrice");
          final int _cursorIndexOfTotalLoss = CursorUtil.getColumnIndexOrThrow(_cursor, "totalLoss");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfLoggedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "loggedBy");
          final int _cursorIndexOfWastageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "wastageDate");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<WastageEntity> _result = new ArrayList<WastageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WastageEntity _item;
            final String _tmpWastageId;
            _tmpWastageId = _cursor.getString(_cursorIndexOfWastageId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final double _tmpQuantity;
            _tmpQuantity = _cursor.getDouble(_cursorIndexOfQuantity);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpCostPrice;
            _tmpCostPrice = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTotalLoss;
            _tmpTotalLoss = _cursor.getDouble(_cursorIndexOfTotalLoss);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpLoggedBy;
            _tmpLoggedBy = _cursor.getString(_cursorIndexOfLoggedBy);
            final String _tmpWastageDate;
            _tmpWastageDate = _cursor.getString(_cursorIndexOfWastageDate);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new WastageEntity(_tmpWastageId,_tmpProductId,_tmpProductName,_tmpBatchId,_tmpBatchNumber,_tmpQuantity,_tmpUnit,_tmpCostPrice,_tmpTotalLoss,_tmpReason,_tmpNotes,_tmpLoggedBy,_tmpWastageDate,_tmpSyncStatus,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<List<WastageEntity>> getWastageByDate(final String date) {
    final String _sql = "SELECT * FROM wastage_log WHERE wastageDate = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"wastage_log"}, new Callable<List<WastageEntity>>() {
      @Override
      @NonNull
      public List<WastageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfWastageId = CursorUtil.getColumnIndexOrThrow(_cursor, "wastageId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "costPrice");
          final int _cursorIndexOfTotalLoss = CursorUtil.getColumnIndexOrThrow(_cursor, "totalLoss");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfLoggedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "loggedBy");
          final int _cursorIndexOfWastageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "wastageDate");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<WastageEntity> _result = new ArrayList<WastageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WastageEntity _item;
            final String _tmpWastageId;
            _tmpWastageId = _cursor.getString(_cursorIndexOfWastageId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final double _tmpQuantity;
            _tmpQuantity = _cursor.getDouble(_cursorIndexOfQuantity);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpCostPrice;
            _tmpCostPrice = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTotalLoss;
            _tmpTotalLoss = _cursor.getDouble(_cursorIndexOfTotalLoss);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpLoggedBy;
            _tmpLoggedBy = _cursor.getString(_cursorIndexOfLoggedBy);
            final String _tmpWastageDate;
            _tmpWastageDate = _cursor.getString(_cursorIndexOfWastageDate);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new WastageEntity(_tmpWastageId,_tmpProductId,_tmpProductName,_tmpBatchId,_tmpBatchNumber,_tmpQuantity,_tmpUnit,_tmpCostPrice,_tmpTotalLoss,_tmpReason,_tmpNotes,_tmpLoggedBy,_tmpWastageDate,_tmpSyncStatus,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<List<WastageEntity>> getWastageByProduct(final String productId) {
    final String _sql = "SELECT * FROM wastage_log WHERE productId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, productId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"wastage_log"}, new Callable<List<WastageEntity>>() {
      @Override
      @NonNull
      public List<WastageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfWastageId = CursorUtil.getColumnIndexOrThrow(_cursor, "wastageId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "costPrice");
          final int _cursorIndexOfTotalLoss = CursorUtil.getColumnIndexOrThrow(_cursor, "totalLoss");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfLoggedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "loggedBy");
          final int _cursorIndexOfWastageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "wastageDate");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<WastageEntity> _result = new ArrayList<WastageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WastageEntity _item;
            final String _tmpWastageId;
            _tmpWastageId = _cursor.getString(_cursorIndexOfWastageId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final double _tmpQuantity;
            _tmpQuantity = _cursor.getDouble(_cursorIndexOfQuantity);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpCostPrice;
            _tmpCostPrice = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTotalLoss;
            _tmpTotalLoss = _cursor.getDouble(_cursorIndexOfTotalLoss);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpLoggedBy;
            _tmpLoggedBy = _cursor.getString(_cursorIndexOfLoggedBy);
            final String _tmpWastageDate;
            _tmpWastageDate = _cursor.getString(_cursorIndexOfWastageDate);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new WastageEntity(_tmpWastageId,_tmpProductId,_tmpProductName,_tmpBatchId,_tmpBatchNumber,_tmpQuantity,_tmpUnit,_tmpCostPrice,_tmpTotalLoss,_tmpReason,_tmpNotes,_tmpLoggedBy,_tmpWastageDate,_tmpSyncStatus,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<Double> getTotalLossToday(final String date) {
    final String _sql = "SELECT COALESCE(SUM(totalLoss), 0) FROM wastage_log WHERE wastageDate = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"wastage_log"}, new Callable<Double>() {
      @Override
      @NonNull
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final double _tmp;
            _tmp = _cursor.getDouble(0);
            _result = _tmp;
          } else {
            _result = 0.0;
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
  public Flow<Double> getTotalLossThisMonth(final String monthPrefix) {
    final String _sql = "SELECT COALESCE(SUM(totalLoss), 0) FROM wastage_log WHERE wastageDate LIKE ? || '%'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, monthPrefix);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"wastage_log"}, new Callable<Double>() {
      @Override
      @NonNull
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final double _tmp;
            _tmp = _cursor.getDouble(0);
            _result = _tmp;
          } else {
            _result = 0.0;
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
  public Object getPendingWastage(final Continuation<? super List<WastageEntity>> $completion) {
    final String _sql = "SELECT * FROM wastage_log WHERE syncStatus = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<WastageEntity>>() {
      @Override
      @NonNull
      public List<WastageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfWastageId = CursorUtil.getColumnIndexOrThrow(_cursor, "wastageId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "costPrice");
          final int _cursorIndexOfTotalLoss = CursorUtil.getColumnIndexOrThrow(_cursor, "totalLoss");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfLoggedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "loggedBy");
          final int _cursorIndexOfWastageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "wastageDate");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<WastageEntity> _result = new ArrayList<WastageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WastageEntity _item;
            final String _tmpWastageId;
            _tmpWastageId = _cursor.getString(_cursorIndexOfWastageId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final double _tmpQuantity;
            _tmpQuantity = _cursor.getDouble(_cursorIndexOfQuantity);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpCostPrice;
            _tmpCostPrice = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTotalLoss;
            _tmpTotalLoss = _cursor.getDouble(_cursorIndexOfTotalLoss);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpLoggedBy;
            _tmpLoggedBy = _cursor.getString(_cursorIndexOfLoggedBy);
            final String _tmpWastageDate;
            _tmpWastageDate = _cursor.getString(_cursorIndexOfWastageDate);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new WastageEntity(_tmpWastageId,_tmpProductId,_tmpProductName,_tmpBatchId,_tmpBatchNumber,_tmpQuantity,_tmpUnit,_tmpCostPrice,_tmpTotalLoss,_tmpReason,_tmpNotes,_tmpLoggedBy,_tmpWastageDate,_tmpSyncStatus,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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

  @Override
  public Flow<List<WastageEntity>> getWastageByReason(final String reason) {
    final String _sql = "SELECT * FROM wastage_log WHERE reason = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, reason);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"wastage_log"}, new Callable<List<WastageEntity>>() {
      @Override
      @NonNull
      public List<WastageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfWastageId = CursorUtil.getColumnIndexOrThrow(_cursor, "wastageId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "costPrice");
          final int _cursorIndexOfTotalLoss = CursorUtil.getColumnIndexOrThrow(_cursor, "totalLoss");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfLoggedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "loggedBy");
          final int _cursorIndexOfWastageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "wastageDate");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<WastageEntity> _result = new ArrayList<WastageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WastageEntity _item;
            final String _tmpWastageId;
            _tmpWastageId = _cursor.getString(_cursorIndexOfWastageId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final double _tmpQuantity;
            _tmpQuantity = _cursor.getDouble(_cursorIndexOfQuantity);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpCostPrice;
            _tmpCostPrice = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTotalLoss;
            _tmpTotalLoss = _cursor.getDouble(_cursorIndexOfTotalLoss);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpLoggedBy;
            _tmpLoggedBy = _cursor.getString(_cursorIndexOfLoggedBy);
            final String _tmpWastageDate;
            _tmpWastageDate = _cursor.getString(_cursorIndexOfWastageDate);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new WastageEntity(_tmpWastageId,_tmpProductId,_tmpProductName,_tmpBatchId,_tmpBatchNumber,_tmpQuantity,_tmpUnit,_tmpCostPrice,_tmpTotalLoss,_tmpReason,_tmpNotes,_tmpLoggedBy,_tmpWastageDate,_tmpSyncStatus,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
