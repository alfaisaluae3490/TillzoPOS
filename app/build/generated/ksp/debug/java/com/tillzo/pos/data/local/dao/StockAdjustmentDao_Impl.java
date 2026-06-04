package com.tillzo.pos.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tillzo.pos.data.local.entity.StockAdjustmentEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
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
public final class StockAdjustmentDao_Impl implements StockAdjustmentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StockAdjustmentEntity> __insertionAdapterOfStockAdjustmentEntity;

  public StockAdjustmentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStockAdjustmentEntity = new EntityInsertionAdapter<StockAdjustmentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `StockAdjustments` (`adjustmentId`,`productId`,`adjustmentType`,`quantityChanged`,`reason`,`adjustedBy`,`syncStatus`,`createdAt`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StockAdjustmentEntity entity) {
        statement.bindString(1, entity.getAdjustmentId());
        statement.bindString(2, entity.getProductId());
        statement.bindString(3, entity.getAdjustmentType());
        statement.bindDouble(4, entity.getQuantityChanged());
        statement.bindString(5, entity.getReason());
        statement.bindString(6, entity.getAdjustedBy());
        statement.bindString(7, entity.getSyncStatus());
        statement.bindLong(8, entity.getCreatedAt());
      }
    };
  }

  @Override
  public Object insertStockAdjustment(final StockAdjustmentEntity adjustment,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStockAdjustmentEntity.insert(adjustment);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<StockAdjustmentEntity>> getAdjustmentsForProduct(final String productId) {
    final String _sql = "SELECT * FROM StockAdjustments WHERE productId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, productId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"StockAdjustments"}, new Callable<List<StockAdjustmentEntity>>() {
      @Override
      @NonNull
      public List<StockAdjustmentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAdjustmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "adjustmentId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfAdjustmentType = CursorUtil.getColumnIndexOrThrow(_cursor, "adjustmentType");
          final int _cursorIndexOfQuantityChanged = CursorUtil.getColumnIndexOrThrow(_cursor, "quantityChanged");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfAdjustedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "adjustedBy");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<StockAdjustmentEntity> _result = new ArrayList<StockAdjustmentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StockAdjustmentEntity _item;
            final String _tmpAdjustmentId;
            _tmpAdjustmentId = _cursor.getString(_cursorIndexOfAdjustmentId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpAdjustmentType;
            _tmpAdjustmentType = _cursor.getString(_cursorIndexOfAdjustmentType);
            final double _tmpQuantityChanged;
            _tmpQuantityChanged = _cursor.getDouble(_cursorIndexOfQuantityChanged);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpAdjustedBy;
            _tmpAdjustedBy = _cursor.getString(_cursorIndexOfAdjustedBy);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new StockAdjustmentEntity(_tmpAdjustmentId,_tmpProductId,_tmpAdjustmentType,_tmpQuantityChanged,_tmpReason,_tmpAdjustedBy,_tmpSyncStatus,_tmpCreatedAt);
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
  public Object getPendingAdjustments(
      final Continuation<? super List<StockAdjustmentEntity>> $completion) {
    final String _sql = "SELECT * FROM StockAdjustments WHERE syncStatus = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StockAdjustmentEntity>>() {
      @Override
      @NonNull
      public List<StockAdjustmentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAdjustmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "adjustmentId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfAdjustmentType = CursorUtil.getColumnIndexOrThrow(_cursor, "adjustmentType");
          final int _cursorIndexOfQuantityChanged = CursorUtil.getColumnIndexOrThrow(_cursor, "quantityChanged");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfAdjustedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "adjustedBy");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<StockAdjustmentEntity> _result = new ArrayList<StockAdjustmentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StockAdjustmentEntity _item;
            final String _tmpAdjustmentId;
            _tmpAdjustmentId = _cursor.getString(_cursorIndexOfAdjustmentId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpAdjustmentType;
            _tmpAdjustmentType = _cursor.getString(_cursorIndexOfAdjustmentType);
            final double _tmpQuantityChanged;
            _tmpQuantityChanged = _cursor.getDouble(_cursorIndexOfQuantityChanged);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpAdjustedBy;
            _tmpAdjustedBy = _cursor.getString(_cursorIndexOfAdjustedBy);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new StockAdjustmentEntity(_tmpAdjustmentId,_tmpProductId,_tmpAdjustmentType,_tmpQuantityChanged,_tmpReason,_tmpAdjustedBy,_tmpSyncStatus,_tmpCreatedAt);
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
  public Object markAsSynced(final List<String> ids, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE StockAdjustments SET syncStatus = 'synced' WHERE adjustmentId IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : ids) {
          _stmt.bindString(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
