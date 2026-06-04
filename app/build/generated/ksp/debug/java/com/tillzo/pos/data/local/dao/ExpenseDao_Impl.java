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
import com.tillzo.pos.data.local.entity.ExpenseEntity;
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
public final class ExpenseDao_Impl implements ExpenseDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ExpenseEntity> __insertionAdapterOfExpenseEntity;

  private final EntityDeletionOrUpdateAdapter<ExpenseEntity> __deletionAdapterOfExpenseEntity;

  private final EntityDeletionOrUpdateAdapter<ExpenseEntity> __updateAdapterOfExpenseEntity;

  private final SharedSQLiteStatement __preparedStmtOfSoftDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfMarkSyncedAndDeleted;

  public ExpenseDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfExpenseEntity = new EntityInsertionAdapter<ExpenseEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `Expenses` (`system_row_id`,`sync_status`,`created_at`,`updated_at`,`pos_terminal_id`,`category`,`amount`,`description`,`timestamp`,`logged_by_user_id`,`is_deleted`,`deleted_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExpenseEntity entity) {
        statement.bindString(1, entity.getSystem_row_id());
        statement.bindString(2, entity.getSync_status());
        statement.bindLong(3, entity.getCreated_at());
        statement.bindLong(4, entity.getUpdated_at());
        statement.bindString(5, entity.getPos_terminal_id());
        statement.bindString(6, entity.getCategory());
        statement.bindDouble(7, entity.getAmount());
        statement.bindString(8, entity.getDescription());
        statement.bindLong(9, entity.getTimestamp());
        statement.bindString(10, entity.getLogged_by_user_id());
        final int _tmp = entity.is_deleted() ? 1 : 0;
        statement.bindLong(11, _tmp);
        if (entity.getDeleted_at() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getDeleted_at());
        }
      }
    };
    this.__deletionAdapterOfExpenseEntity = new EntityDeletionOrUpdateAdapter<ExpenseEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `Expenses` WHERE `system_row_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExpenseEntity entity) {
        statement.bindString(1, entity.getSystem_row_id());
      }
    };
    this.__updateAdapterOfExpenseEntity = new EntityDeletionOrUpdateAdapter<ExpenseEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `Expenses` SET `system_row_id` = ?,`sync_status` = ?,`created_at` = ?,`updated_at` = ?,`pos_terminal_id` = ?,`category` = ?,`amount` = ?,`description` = ?,`timestamp` = ?,`logged_by_user_id` = ?,`is_deleted` = ?,`deleted_at` = ? WHERE `system_row_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExpenseEntity entity) {
        statement.bindString(1, entity.getSystem_row_id());
        statement.bindString(2, entity.getSync_status());
        statement.bindLong(3, entity.getCreated_at());
        statement.bindLong(4, entity.getUpdated_at());
        statement.bindString(5, entity.getPos_terminal_id());
        statement.bindString(6, entity.getCategory());
        statement.bindDouble(7, entity.getAmount());
        statement.bindString(8, entity.getDescription());
        statement.bindLong(9, entity.getTimestamp());
        statement.bindString(10, entity.getLogged_by_user_id());
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
        final String _query = "UPDATE Expenses SET is_deleted = 1, deleted_at = ?, sync_status = 'pending' WHERE system_row_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkSyncedAndDeleted = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE Expenses SET sync_status = 'synced' WHERE system_row_id = ? AND is_deleted = 1";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ExpenseEntity item, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfExpenseEntity.insertAndReturnId(item);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<? extends ExpenseEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfExpenseEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ExpenseEntity item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfExpenseEntity.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ExpenseEntity item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfExpenseEntity.handle(item);
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
  public Flow<List<ExpenseEntity>> getAllExpenses() {
    final String _sql = "SELECT * FROM Expenses WHERE is_deleted = 0 ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Expenses"}, new Callable<List<ExpenseEntity>>() {
      @Override
      @NonNull
      public List<ExpenseEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLoggedByUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "logged_by_user_id");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<ExpenseEntity> _result = new ArrayList<ExpenseEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExpenseEntity _item;
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
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpLogged_by_user_id;
            _tmpLogged_by_user_id = _cursor.getString(_cursorIndexOfLoggedByUserId);
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
            _item = new ExpenseEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpCategory,_tmpAmount,_tmpDescription,_tmpTimestamp,_tmpLogged_by_user_id,_tmpIs_deleted,_tmpDeleted_at);
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
  public Flow<List<ExpenseEntity>> getExpensesBetweenDates(final long startTime,
      final long endTime) {
    final String _sql = "SELECT * FROM Expenses WHERE timestamp >= ? AND timestamp <= ? AND is_deleted = 0 ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endTime);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Expenses"}, new Callable<List<ExpenseEntity>>() {
      @Override
      @NonNull
      public List<ExpenseEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLoggedByUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "logged_by_user_id");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<ExpenseEntity> _result = new ArrayList<ExpenseEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExpenseEntity _item;
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
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpLogged_by_user_id;
            _tmpLogged_by_user_id = _cursor.getString(_cursorIndexOfLoggedByUserId);
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
            _item = new ExpenseEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpCategory,_tmpAmount,_tmpDescription,_tmpTimestamp,_tmpLogged_by_user_id,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getPendingExpenses(final Continuation<? super List<ExpenseEntity>> $completion) {
    final String _sql = "SELECT * FROM Expenses WHERE sync_status = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ExpenseEntity>>() {
      @Override
      @NonNull
      public List<ExpenseEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLoggedByUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "logged_by_user_id");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<ExpenseEntity> _result = new ArrayList<ExpenseEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExpenseEntity _item;
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
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpLogged_by_user_id;
            _tmpLogged_by_user_id = _cursor.getString(_cursorIndexOfLoggedByUserId);
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
            _item = new ExpenseEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpCategory,_tmpAmount,_tmpDescription,_tmpTimestamp,_tmpLogged_by_user_id,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getPendingDeletedRows(final Continuation<? super List<ExpenseEntity>> $completion) {
    final String _sql = "SELECT * FROM Expenses WHERE is_deleted = 1 AND sync_status = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ExpenseEntity>>() {
      @Override
      @NonNull
      public List<ExpenseEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLoggedByUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "logged_by_user_id");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<ExpenseEntity> _result = new ArrayList<ExpenseEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExpenseEntity _item;
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
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpLogged_by_user_id;
            _tmpLogged_by_user_id = _cursor.getString(_cursorIndexOfLoggedByUserId);
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
            _item = new ExpenseEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpCategory,_tmpAmount,_tmpDescription,_tmpTimestamp,_tmpLogged_by_user_id,_tmpIs_deleted,_tmpDeleted_at);
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
