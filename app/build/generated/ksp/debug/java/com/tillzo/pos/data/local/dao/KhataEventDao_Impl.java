package com.tillzo.pos.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tillzo.pos.data.local.entity.KhataEventEntity;
import java.lang.Class;
import java.lang.Double;
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
public final class KhataEventDao_Impl implements KhataEventDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<KhataEventEntity> __insertionAdapterOfKhataEventEntity;

  private final EntityDeletionOrUpdateAdapter<KhataEventEntity> __deletionAdapterOfKhataEventEntity;

  private final EntityDeletionOrUpdateAdapter<KhataEventEntity> __updateAdapterOfKhataEventEntity;

  private final SharedSQLiteStatement __preparedStmtOfSoftDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfMarkSyncedAndDeleted;

  public KhataEventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfKhataEventEntity = new EntityInsertionAdapter<KhataEventEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `KhataEvents` (`system_row_id`,`sync_status`,`created_at`,`updated_at`,`pos_terminal_id`,`customer_id`,`event_type`,`amount`,`note`,`reference_sale_id`,`is_deleted`,`deleted_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final KhataEventEntity entity) {
        statement.bindString(1, entity.getSystem_row_id());
        statement.bindString(2, entity.getSync_status());
        statement.bindLong(3, entity.getCreated_at());
        statement.bindLong(4, entity.getUpdated_at());
        statement.bindString(5, entity.getPos_terminal_id());
        statement.bindString(6, entity.getCustomer_id());
        statement.bindString(7, entity.getEvent_type());
        statement.bindDouble(8, entity.getAmount());
        if (entity.getNote() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getNote());
        }
        if (entity.getReference_sale_id() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getReference_sale_id());
        }
        final int _tmp = entity.is_deleted() ? 1 : 0;
        statement.bindLong(11, _tmp);
        if (entity.getDeleted_at() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getDeleted_at());
        }
      }
    };
    this.__deletionAdapterOfKhataEventEntity = new EntityDeletionOrUpdateAdapter<KhataEventEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `KhataEvents` WHERE `system_row_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final KhataEventEntity entity) {
        statement.bindString(1, entity.getSystem_row_id());
      }
    };
    this.__updateAdapterOfKhataEventEntity = new EntityDeletionOrUpdateAdapter<KhataEventEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `KhataEvents` SET `system_row_id` = ?,`sync_status` = ?,`created_at` = ?,`updated_at` = ?,`pos_terminal_id` = ?,`customer_id` = ?,`event_type` = ?,`amount` = ?,`note` = ?,`reference_sale_id` = ?,`is_deleted` = ?,`deleted_at` = ? WHERE `system_row_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final KhataEventEntity entity) {
        statement.bindString(1, entity.getSystem_row_id());
        statement.bindString(2, entity.getSync_status());
        statement.bindLong(3, entity.getCreated_at());
        statement.bindLong(4, entity.getUpdated_at());
        statement.bindString(5, entity.getPos_terminal_id());
        statement.bindString(6, entity.getCustomer_id());
        statement.bindString(7, entity.getEvent_type());
        statement.bindDouble(8, entity.getAmount());
        if (entity.getNote() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getNote());
        }
        if (entity.getReference_sale_id() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getReference_sale_id());
        }
        final int _tmp = entity.is_deleted() ? 1 : 0;
        statement.bindLong(11, _tmp);
        if (entity.getDeleted_at() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getDeleted_at());
        }
        statement.bindString(13, entity.getSystem_row_id());
      }
    };
    this.__preparedStmtOfSoftDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE KhataEvents SET is_deleted = 1, deleted_at = ?, sync_status = 'pending' WHERE system_row_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkSyncedAndDeleted = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE KhataEvents SET sync_status = 'synced' WHERE system_row_id = ? AND is_deleted = 1";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final KhataEventEntity item, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfKhataEventEntity.insertAndReturnId(item);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<? extends KhataEventEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfKhataEventEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final KhataEventEntity item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfKhataEventEntity.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final KhataEventEntity item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfKhataEventEntity.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object softDeleteById(final String id, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 2;
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
          __preparedStmtOfSoftDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markSyncedAndDeleted(final String id,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkSyncedAndDeleted.acquire();
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
          __preparedStmtOfMarkSyncedAndDeleted.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<KhataEventEntity>> getEventsForCustomer(final String customerId) {
    final String _sql = "SELECT * FROM KhataEvents WHERE customer_id = ? AND is_deleted = 0 ORDER BY created_at DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, customerId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"KhataEvents"}, new Callable<List<KhataEventEntity>>() {
      @Override
      @NonNull
      public List<KhataEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_id");
          final int _cursorIndexOfEventType = CursorUtil.getColumnIndexOrThrow(_cursor, "event_type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfReferenceSaleId = CursorUtil.getColumnIndexOrThrow(_cursor, "reference_sale_id");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<KhataEventEntity> _result = new ArrayList<KhataEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final KhataEventEntity _item;
            final String _tmpSystem_row_id;
            _tmpSystem_row_id = _cursor.getString(_cursorIndexOfSystemRowId);
            final String _tmpSync_status;
            _tmpSync_status = _cursor.getString(_cursorIndexOfSyncStatus);
            final long _tmpCreated_at;
            _tmpCreated_at = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdated_at;
            _tmpUpdated_at = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpPos_terminal_id;
            _tmpPos_terminal_id = _cursor.getString(_cursorIndexOfPosTerminalId);
            final String _tmpCustomer_id;
            _tmpCustomer_id = _cursor.getString(_cursorIndexOfCustomerId);
            final String _tmpEvent_type;
            _tmpEvent_type = _cursor.getString(_cursorIndexOfEventType);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final String _tmpReference_sale_id;
            if (_cursor.isNull(_cursorIndexOfReferenceSaleId)) {
              _tmpReference_sale_id = null;
            } else {
              _tmpReference_sale_id = _cursor.getString(_cursorIndexOfReferenceSaleId);
            }
            final boolean _tmpIs_deleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new KhataEventEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpCustomer_id,_tmpEvent_type,_tmpAmount,_tmpNote,_tmpReference_sale_id,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getPendingKhataEvents(
      final Continuation<? super List<KhataEventEntity>> $completion) {
    final String _sql = "SELECT * FROM KhataEvents WHERE sync_status = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<KhataEventEntity>>() {
      @Override
      @NonNull
      public List<KhataEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_id");
          final int _cursorIndexOfEventType = CursorUtil.getColumnIndexOrThrow(_cursor, "event_type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfReferenceSaleId = CursorUtil.getColumnIndexOrThrow(_cursor, "reference_sale_id");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<KhataEventEntity> _result = new ArrayList<KhataEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final KhataEventEntity _item;
            final String _tmpSystem_row_id;
            _tmpSystem_row_id = _cursor.getString(_cursorIndexOfSystemRowId);
            final String _tmpSync_status;
            _tmpSync_status = _cursor.getString(_cursorIndexOfSyncStatus);
            final long _tmpCreated_at;
            _tmpCreated_at = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdated_at;
            _tmpUpdated_at = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpPos_terminal_id;
            _tmpPos_terminal_id = _cursor.getString(_cursorIndexOfPosTerminalId);
            final String _tmpCustomer_id;
            _tmpCustomer_id = _cursor.getString(_cursorIndexOfCustomerId);
            final String _tmpEvent_type;
            _tmpEvent_type = _cursor.getString(_cursorIndexOfEventType);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final String _tmpReference_sale_id;
            if (_cursor.isNull(_cursorIndexOfReferenceSaleId)) {
              _tmpReference_sale_id = null;
            } else {
              _tmpReference_sale_id = _cursor.getString(_cursorIndexOfReferenceSaleId);
            }
            final boolean _tmpIs_deleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new KhataEventEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpCustomer_id,_tmpEvent_type,_tmpAmount,_tmpNote,_tmpReference_sale_id,_tmpIs_deleted,_tmpDeleted_at);
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
  public Flow<Double> getTotalUdhaarFlow(final String customerId) {
    final String _sql = "SELECT COALESCE(SUM(amount), 0.0) FROM KhataEvents WHERE customer_id = ? AND event_type = 'UDHAAR' AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, customerId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"KhataEvents"}, new Callable<Double>() {
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
  public Flow<Double> getTotalJamaFlow(final String customerId) {
    final String _sql = "SELECT COALESCE(SUM(amount), 0.0) FROM KhataEvents WHERE customer_id = ? AND event_type = 'JAMA' AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, customerId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"KhataEvents"}, new Callable<Double>() {
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
  public Flow<Double> getBaqayaBalanceFlow(final String customerId) {
    final String _sql = "SELECT COALESCE(SUM(amount), 0.0) FROM KhataEvents WHERE customer_id = ? AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, customerId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"KhataEvents"}, new Callable<Double>() {
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
  public Object getPendingDeletedRows(
      final Continuation<? super List<KhataEventEntity>> $completion) {
    final String _sql = "SELECT * FROM KhataEvents WHERE is_deleted = 1 AND sync_status = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<KhataEventEntity>>() {
      @Override
      @NonNull
      public List<KhataEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_id");
          final int _cursorIndexOfEventType = CursorUtil.getColumnIndexOrThrow(_cursor, "event_type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfReferenceSaleId = CursorUtil.getColumnIndexOrThrow(_cursor, "reference_sale_id");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<KhataEventEntity> _result = new ArrayList<KhataEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final KhataEventEntity _item;
            final String _tmpSystem_row_id;
            _tmpSystem_row_id = _cursor.getString(_cursorIndexOfSystemRowId);
            final String _tmpSync_status;
            _tmpSync_status = _cursor.getString(_cursorIndexOfSyncStatus);
            final long _tmpCreated_at;
            _tmpCreated_at = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdated_at;
            _tmpUpdated_at = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpPos_terminal_id;
            _tmpPos_terminal_id = _cursor.getString(_cursorIndexOfPosTerminalId);
            final String _tmpCustomer_id;
            _tmpCustomer_id = _cursor.getString(_cursorIndexOfCustomerId);
            final String _tmpEvent_type;
            _tmpEvent_type = _cursor.getString(_cursorIndexOfEventType);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final String _tmpReference_sale_id;
            if (_cursor.isNull(_cursorIndexOfReferenceSaleId)) {
              _tmpReference_sale_id = null;
            } else {
              _tmpReference_sale_id = _cursor.getString(_cursorIndexOfReferenceSaleId);
            }
            final boolean _tmpIs_deleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new KhataEventEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpCustomer_id,_tmpEvent_type,_tmpAmount,_tmpNote,_tmpReference_sale_id,_tmpIs_deleted,_tmpDeleted_at);
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
