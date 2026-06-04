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
import com.tillzo.pos.data.local.entity.CategoryEntity;
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
public final class CategoryDao_Impl implements CategoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CategoryEntity> __insertionAdapterOfCategoryEntity;

  private final EntityDeletionOrUpdateAdapter<CategoryEntity> __updateAdapterOfCategoryEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCategory;

  private final SharedSQLiteStatement __preparedStmtOfMarkSynced;

  public CategoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCategoryEntity = new EntityInsertionAdapter<CategoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `Categories` (`system_row_id`,`sync_status`,`created_at`,`updated_at`,`pos_terminal_id`,`category_name`,`is_deleted`,`deleted_at`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CategoryEntity entity) {
        statement.bindString(1, entity.getSystem_row_id());
        statement.bindString(2, entity.getSync_status());
        statement.bindLong(3, entity.getCreated_at());
        statement.bindLong(4, entity.getUpdated_at());
        statement.bindString(5, entity.getPos_terminal_id());
        statement.bindString(6, entity.getCategory_name());
        final int _tmp = entity.is_deleted() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getDeleted_at() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getDeleted_at());
        }
      }
    };
    this.__updateAdapterOfCategoryEntity = new EntityDeletionOrUpdateAdapter<CategoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `Categories` SET `system_row_id` = ?,`sync_status` = ?,`created_at` = ?,`updated_at` = ?,`pos_terminal_id` = ?,`category_name` = ?,`is_deleted` = ?,`deleted_at` = ? WHERE `system_row_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CategoryEntity entity) {
        statement.bindString(1, entity.getSystem_row_id());
        statement.bindString(2, entity.getSync_status());
        statement.bindLong(3, entity.getCreated_at());
        statement.bindLong(4, entity.getUpdated_at());
        statement.bindString(5, entity.getPos_terminal_id());
        statement.bindString(6, entity.getCategory_name());
        final int _tmp = entity.is_deleted() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getDeleted_at() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getDeleted_at());
        }
        statement.bindString(9, entity.getSystem_row_id());
      }
    };
    this.__preparedStmtOfDeleteCategory = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE Categories SET is_deleted = 1, deleted_at = ?, sync_status = 'pending' WHERE system_row_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE Categories SET sync_status = 'synced' WHERE system_row_id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertCategory(final CategoryEntity category,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCategoryEntity.insert(category);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCategory(final CategoryEntity category,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCategoryEntity.handle(category);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCategory(final String categoryId, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCategory.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, categoryId);
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
          __preparedStmtOfDeleteCategory.release(_stmt);
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
  public Flow<List<CategoryEntity>> getAllCategories() {
    final String _sql = "SELECT * FROM Categories WHERE is_deleted = 0 ORDER BY category_name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Categories"}, new Callable<List<CategoryEntity>>() {
      @Override
      @NonNull
      public List<CategoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfCategoryName = CursorUtil.getColumnIndexOrThrow(_cursor, "category_name");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<CategoryEntity> _result = new ArrayList<CategoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryEntity _item;
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
            final String _tmpCategory_name;
            _tmpCategory_name = _cursor.getString(_cursorIndexOfCategoryName);
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
            _item = new CategoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpCategory_name,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getPendingSyncCategories(
      final Continuation<? super List<CategoryEntity>> $completion) {
    final String _sql = "SELECT * FROM Categories WHERE sync_status = 'pending' AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CategoryEntity>>() {
      @Override
      @NonNull
      public List<CategoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfCategoryName = CursorUtil.getColumnIndexOrThrow(_cursor, "category_name");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<CategoryEntity> _result = new ArrayList<CategoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryEntity _item;
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
            final String _tmpCategory_name;
            _tmpCategory_name = _cursor.getString(_cursorIndexOfCategoryName);
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
            _item = new CategoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpCategory_name,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getPendingSyncDeleted(
      final Continuation<? super List<CategoryEntity>> $completion) {
    final String _sql = "SELECT * FROM Categories WHERE is_deleted = 1 AND sync_status = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CategoryEntity>>() {
      @Override
      @NonNull
      public List<CategoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfCategoryName = CursorUtil.getColumnIndexOrThrow(_cursor, "category_name");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<CategoryEntity> _result = new ArrayList<CategoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryEntity _item;
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
            final String _tmpCategory_name;
            _tmpCategory_name = _cursor.getString(_cursorIndexOfCategoryName);
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
            _item = new CategoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpCategory_name,_tmpIs_deleted,_tmpDeleted_at);
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
