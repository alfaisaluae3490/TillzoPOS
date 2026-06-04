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
import com.tillzo.pos.data.local.entity.CustomerEntity;
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
public final class CustomerDao_Impl implements CustomerDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CustomerEntity> __insertionAdapterOfCustomerEntity;

  private final EntityDeletionOrUpdateAdapter<CustomerEntity> __deletionAdapterOfCustomerEntity;

  private final EntityDeletionOrUpdateAdapter<CustomerEntity> __updateAdapterOfCustomerEntity;

  private final SharedSQLiteStatement __preparedStmtOfSoftDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfMarkSyncedAndDeleted;

  public CustomerDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCustomerEntity = new EntityInsertionAdapter<CustomerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `Customers` (`system_row_id`,`sync_status`,`created_at`,`updated_at`,`pos_terminal_id`,`name`,`phone`,`whatsapp`,`email`,`address`,`is_deleted`,`deleted_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CustomerEntity entity) {
        statement.bindString(1, entity.getSystem_row_id());
        statement.bindString(2, entity.getSync_status());
        statement.bindLong(3, entity.getCreated_at());
        statement.bindLong(4, entity.getUpdated_at());
        statement.bindString(5, entity.getPos_terminal_id());
        statement.bindString(6, entity.getName());
        statement.bindString(7, entity.getPhone());
        if (entity.getWhatsapp() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getWhatsapp());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getEmail());
        }
        if (entity.getAddress() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getAddress());
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
    this.__deletionAdapterOfCustomerEntity = new EntityDeletionOrUpdateAdapter<CustomerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `Customers` WHERE `system_row_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CustomerEntity entity) {
        statement.bindString(1, entity.getSystem_row_id());
      }
    };
    this.__updateAdapterOfCustomerEntity = new EntityDeletionOrUpdateAdapter<CustomerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `Customers` SET `system_row_id` = ?,`sync_status` = ?,`created_at` = ?,`updated_at` = ?,`pos_terminal_id` = ?,`name` = ?,`phone` = ?,`whatsapp` = ?,`email` = ?,`address` = ?,`is_deleted` = ?,`deleted_at` = ? WHERE `system_row_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CustomerEntity entity) {
        statement.bindString(1, entity.getSystem_row_id());
        statement.bindString(2, entity.getSync_status());
        statement.bindLong(3, entity.getCreated_at());
        statement.bindLong(4, entity.getUpdated_at());
        statement.bindString(5, entity.getPos_terminal_id());
        statement.bindString(6, entity.getName());
        statement.bindString(7, entity.getPhone());
        if (entity.getWhatsapp() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getWhatsapp());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getEmail());
        }
        if (entity.getAddress() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getAddress());
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
        final String _query = "UPDATE Customers SET is_deleted = 1, deleted_at = ?, sync_status = 'pending' WHERE system_row_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkSyncedAndDeleted = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE Customers SET sync_status = 'synced' WHERE system_row_id = ? AND is_deleted = 1";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final CustomerEntity item, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCustomerEntity.insertAndReturnId(item);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<? extends CustomerEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCustomerEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final CustomerEntity item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfCustomerEntity.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final CustomerEntity item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCustomerEntity.handle(item);
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
  public Flow<List<CustomerEntity>> getAllCustomers() {
    final String _sql = "SELECT * FROM Customers WHERE is_deleted = 0 ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Customers"}, new Callable<List<CustomerEntity>>() {
      @Override
      @NonNull
      public List<CustomerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfWhatsapp = CursorUtil.getColumnIndexOrThrow(_cursor, "whatsapp");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<CustomerEntity> _result = new ArrayList<CustomerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CustomerEntity _item;
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
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            final String _tmpWhatsapp;
            if (_cursor.isNull(_cursorIndexOfWhatsapp)) {
              _tmpWhatsapp = null;
            } else {
              _tmpWhatsapp = _cursor.getString(_cursorIndexOfWhatsapp);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpAddress;
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null;
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
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
            _item = new CustomerEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpName,_tmpPhone,_tmpWhatsapp,_tmpEmail,_tmpAddress,_tmpIs_deleted,_tmpDeleted_at);
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
  public Flow<CustomerEntity> getCustomerFlow(final String customerId) {
    final String _sql = "SELECT * FROM Customers WHERE system_row_id = ? AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, customerId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Customers"}, new Callable<CustomerEntity>() {
      @Override
      @Nullable
      public CustomerEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfWhatsapp = CursorUtil.getColumnIndexOrThrow(_cursor, "whatsapp");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final CustomerEntity _result;
          if (_cursor.moveToFirst()) {
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
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            final String _tmpWhatsapp;
            if (_cursor.isNull(_cursorIndexOfWhatsapp)) {
              _tmpWhatsapp = null;
            } else {
              _tmpWhatsapp = _cursor.getString(_cursorIndexOfWhatsapp);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpAddress;
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null;
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
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
            _result = new CustomerEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpName,_tmpPhone,_tmpWhatsapp,_tmpEmail,_tmpAddress,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getCustomerById(final String customerId,
      final Continuation<? super CustomerEntity> $completion) {
    final String _sql = "SELECT * FROM Customers WHERE system_row_id = ? AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, customerId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CustomerEntity>() {
      @Override
      @Nullable
      public CustomerEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfWhatsapp = CursorUtil.getColumnIndexOrThrow(_cursor, "whatsapp");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final CustomerEntity _result;
          if (_cursor.moveToFirst()) {
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
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            final String _tmpWhatsapp;
            if (_cursor.isNull(_cursorIndexOfWhatsapp)) {
              _tmpWhatsapp = null;
            } else {
              _tmpWhatsapp = _cursor.getString(_cursorIndexOfWhatsapp);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpAddress;
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null;
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
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
            _result = new CustomerEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpName,_tmpPhone,_tmpWhatsapp,_tmpEmail,_tmpAddress,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getPendingCustomers(final Continuation<? super List<CustomerEntity>> $completion) {
    final String _sql = "SELECT * FROM Customers WHERE sync_status = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CustomerEntity>>() {
      @Override
      @NonNull
      public List<CustomerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfWhatsapp = CursorUtil.getColumnIndexOrThrow(_cursor, "whatsapp");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<CustomerEntity> _result = new ArrayList<CustomerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CustomerEntity _item;
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
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            final String _tmpWhatsapp;
            if (_cursor.isNull(_cursorIndexOfWhatsapp)) {
              _tmpWhatsapp = null;
            } else {
              _tmpWhatsapp = _cursor.getString(_cursorIndexOfWhatsapp);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpAddress;
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null;
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
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
            _item = new CustomerEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpName,_tmpPhone,_tmpWhatsapp,_tmpEmail,_tmpAddress,_tmpIs_deleted,_tmpDeleted_at);
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
  public Flow<List<CustomerEntity>> searchCustomers(final String query) {
    final String _sql = "SELECT * FROM Customers WHERE (name LIKE '%' || ? || '%' OR phone LIKE '%' || ? || '%') AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Customers"}, new Callable<List<CustomerEntity>>() {
      @Override
      @NonNull
      public List<CustomerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfWhatsapp = CursorUtil.getColumnIndexOrThrow(_cursor, "whatsapp");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<CustomerEntity> _result = new ArrayList<CustomerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CustomerEntity _item;
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
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            final String _tmpWhatsapp;
            if (_cursor.isNull(_cursorIndexOfWhatsapp)) {
              _tmpWhatsapp = null;
            } else {
              _tmpWhatsapp = _cursor.getString(_cursorIndexOfWhatsapp);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpAddress;
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null;
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
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
            _item = new CustomerEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpName,_tmpPhone,_tmpWhatsapp,_tmpEmail,_tmpAddress,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getPendingDeletedRows(
      final Continuation<? super List<CustomerEntity>> $completion) {
    final String _sql = "SELECT * FROM Customers WHERE is_deleted = 1 AND sync_status = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CustomerEntity>>() {
      @Override
      @NonNull
      public List<CustomerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfWhatsapp = CursorUtil.getColumnIndexOrThrow(_cursor, "whatsapp");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<CustomerEntity> _result = new ArrayList<CustomerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CustomerEntity _item;
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
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            final String _tmpWhatsapp;
            if (_cursor.isNull(_cursorIndexOfWhatsapp)) {
              _tmpWhatsapp = null;
            } else {
              _tmpWhatsapp = _cursor.getString(_cursorIndexOfWhatsapp);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpAddress;
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null;
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
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
            _item = new CustomerEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpName,_tmpPhone,_tmpWhatsapp,_tmpEmail,_tmpAddress,_tmpIs_deleted,_tmpDeleted_at);
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
