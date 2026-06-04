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
import com.tillzo.pos.data.local.entity.SaleEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class SaleDao_Impl implements SaleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SaleEntity> __insertionAdapterOfSaleEntity;

  private final EntityDeletionOrUpdateAdapter<SaleEntity> __updateAdapterOfSaleEntity;

  private final SharedSQLiteStatement __preparedStmtOfSoftDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfMarkSyncedAndDeleted;

  private final SharedSQLiteStatement __preparedStmtOfMarkSyncedByInvoiceId;

  public SaleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSaleEntity = new EntityInsertionAdapter<SaleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `Sales` (`system_row_id`,`sync_status`,`created_at`,`updated_at`,`pos_terminal_id`,`sync_uuid`,`cashier_id`,`timestamp`,`items_json`,`subtotal`,`tax`,`discount`,`total`,`payment_method`,`cash_amount`,`card_amount`,`wallet_amount`,`udhaar_amount`,`customer_id`,`payment_split_json`,`reference_id`,`is_deleted`,`deleted_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SaleEntity entity) {
        statement.bindString(1, entity.getSystem_row_id());
        statement.bindString(2, entity.getSync_status());
        statement.bindLong(3, entity.getCreated_at());
        statement.bindLong(4, entity.getUpdated_at());
        statement.bindString(5, entity.getPos_terminal_id());
        statement.bindString(6, entity.getSync_uuid());
        statement.bindString(7, entity.getCashier_id());
        statement.bindLong(8, entity.getTimestamp());
        statement.bindString(9, entity.getItems_json());
        statement.bindDouble(10, entity.getSubtotal());
        statement.bindDouble(11, entity.getTax());
        statement.bindDouble(12, entity.getDiscount());
        statement.bindDouble(13, entity.getTotal());
        statement.bindString(14, entity.getPayment_method());
        statement.bindDouble(15, entity.getCash_amount());
        statement.bindDouble(16, entity.getCard_amount());
        statement.bindDouble(17, entity.getWallet_amount());
        statement.bindDouble(18, entity.getUdhaar_amount());
        if (entity.getCustomer_id() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getCustomer_id());
        }
        if (entity.getPayment_split_json() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getPayment_split_json());
        }
        if (entity.getReference_id() == null) {
          statement.bindNull(21);
        } else {
          statement.bindString(21, entity.getReference_id());
        }
        final int _tmp = entity.is_deleted() ? 1 : 0;
        statement.bindLong(22, _tmp);
        if (entity.getDeleted_at() == null) {
          statement.bindNull(23);
        } else {
          statement.bindLong(23, entity.getDeleted_at());
        }
      }
    };
    this.__updateAdapterOfSaleEntity = new EntityDeletionOrUpdateAdapter<SaleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `Sales` SET `system_row_id` = ?,`sync_status` = ?,`created_at` = ?,`updated_at` = ?,`pos_terminal_id` = ?,`sync_uuid` = ?,`cashier_id` = ?,`timestamp` = ?,`items_json` = ?,`subtotal` = ?,`tax` = ?,`discount` = ?,`total` = ?,`payment_method` = ?,`cash_amount` = ?,`card_amount` = ?,`wallet_amount` = ?,`udhaar_amount` = ?,`customer_id` = ?,`payment_split_json` = ?,`reference_id` = ?,`is_deleted` = ?,`deleted_at` = ? WHERE `system_row_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SaleEntity entity) {
        statement.bindString(1, entity.getSystem_row_id());
        statement.bindString(2, entity.getSync_status());
        statement.bindLong(3, entity.getCreated_at());
        statement.bindLong(4, entity.getUpdated_at());
        statement.bindString(5, entity.getPos_terminal_id());
        statement.bindString(6, entity.getSync_uuid());
        statement.bindString(7, entity.getCashier_id());
        statement.bindLong(8, entity.getTimestamp());
        statement.bindString(9, entity.getItems_json());
        statement.bindDouble(10, entity.getSubtotal());
        statement.bindDouble(11, entity.getTax());
        statement.bindDouble(12, entity.getDiscount());
        statement.bindDouble(13, entity.getTotal());
        statement.bindString(14, entity.getPayment_method());
        statement.bindDouble(15, entity.getCash_amount());
        statement.bindDouble(16, entity.getCard_amount());
        statement.bindDouble(17, entity.getWallet_amount());
        statement.bindDouble(18, entity.getUdhaar_amount());
        if (entity.getCustomer_id() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getCustomer_id());
        }
        if (entity.getPayment_split_json() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getPayment_split_json());
        }
        if (entity.getReference_id() == null) {
          statement.bindNull(21);
        } else {
          statement.bindString(21, entity.getReference_id());
        }
        final int _tmp = entity.is_deleted() ? 1 : 0;
        statement.bindLong(22, _tmp);
        if (entity.getDeleted_at() == null) {
          statement.bindNull(23);
        } else {
          statement.bindLong(23, entity.getDeleted_at());
        }
        statement.bindString(24, entity.getSystem_row_id());
      }
    };
    this.__preparedStmtOfSoftDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE Sales SET is_deleted = 1, deleted_at = ?, sync_status = 'pending' WHERE system_row_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkSyncedAndDeleted = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE Sales SET sync_status = 'synced' WHERE system_row_id = ? AND is_deleted = 1";
        return _query;
      }
    };
    this.__preparedStmtOfMarkSyncedByInvoiceId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE Sales SET sync_status = 'synced' WHERE sync_uuid = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertSale(final SaleEntity sale, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSaleEntity.insert(sale);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSale(final SaleEntity sale, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSaleEntity.handle(sale);
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
  public Object markSyncedByInvoiceId(final String invoiceUuid,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkSyncedByInvoiceId.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, invoiceUuid);
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
          __preparedStmtOfMarkSyncedByInvoiceId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SaleEntity>> getAllSales() {
    final String _sql = "SELECT * FROM Sales WHERE is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Sales"}, new Callable<List<SaleEntity>>() {
      @Override
      @NonNull
      public List<SaleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfSyncUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_uuid");
          final int _cursorIndexOfCashierId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashier_id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfItemsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "items_json");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfTax = CursorUtil.getColumnIndexOrThrow(_cursor, "tax");
          final int _cursorIndexOfDiscount = CursorUtil.getColumnIndexOrThrow(_cursor, "discount");
          final int _cursorIndexOfTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "total");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "payment_method");
          final int _cursorIndexOfCashAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "cash_amount");
          final int _cursorIndexOfCardAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "card_amount");
          final int _cursorIndexOfWalletAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "wallet_amount");
          final int _cursorIndexOfUdhaarAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "udhaar_amount");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_id");
          final int _cursorIndexOfPaymentSplitJson = CursorUtil.getColumnIndexOrThrow(_cursor, "payment_split_json");
          final int _cursorIndexOfReferenceId = CursorUtil.getColumnIndexOrThrow(_cursor, "reference_id");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<SaleEntity> _result = new ArrayList<SaleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SaleEntity _item;
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
            final String _tmpSync_uuid;
            _tmpSync_uuid = _cursor.getString(_cursorIndexOfSyncUuid);
            final String _tmpCashier_id;
            _tmpCashier_id = _cursor.getString(_cursorIndexOfCashierId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpItems_json;
            _tmpItems_json = _cursor.getString(_cursorIndexOfItemsJson);
            final double _tmpSubtotal;
            _tmpSubtotal = _cursor.getDouble(_cursorIndexOfSubtotal);
            final double _tmpTax;
            _tmpTax = _cursor.getDouble(_cursorIndexOfTax);
            final double _tmpDiscount;
            _tmpDiscount = _cursor.getDouble(_cursorIndexOfDiscount);
            final double _tmpTotal;
            _tmpTotal = _cursor.getDouble(_cursorIndexOfTotal);
            final String _tmpPayment_method;
            _tmpPayment_method = _cursor.getString(_cursorIndexOfPaymentMethod);
            final double _tmpCash_amount;
            _tmpCash_amount = _cursor.getDouble(_cursorIndexOfCashAmount);
            final double _tmpCard_amount;
            _tmpCard_amount = _cursor.getDouble(_cursorIndexOfCardAmount);
            final double _tmpWallet_amount;
            _tmpWallet_amount = _cursor.getDouble(_cursorIndexOfWalletAmount);
            final double _tmpUdhaar_amount;
            _tmpUdhaar_amount = _cursor.getDouble(_cursorIndexOfUdhaarAmount);
            final String _tmpCustomer_id;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomer_id = null;
            } else {
              _tmpCustomer_id = _cursor.getString(_cursorIndexOfCustomerId);
            }
            final String _tmpPayment_split_json;
            if (_cursor.isNull(_cursorIndexOfPaymentSplitJson)) {
              _tmpPayment_split_json = null;
            } else {
              _tmpPayment_split_json = _cursor.getString(_cursorIndexOfPaymentSplitJson);
            }
            final String _tmpReference_id;
            if (_cursor.isNull(_cursorIndexOfReferenceId)) {
              _tmpReference_id = null;
            } else {
              _tmpReference_id = _cursor.getString(_cursorIndexOfReferenceId);
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
            _item = new SaleEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpSync_uuid,_tmpCashier_id,_tmpTimestamp,_tmpItems_json,_tmpSubtotal,_tmpTax,_tmpDiscount,_tmpTotal,_tmpPayment_method,_tmpCash_amount,_tmpCard_amount,_tmpWallet_amount,_tmpUdhaar_amount,_tmpCustomer_id,_tmpPayment_split_json,_tmpReference_id,_tmpIs_deleted,_tmpDeleted_at);
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
  public Flow<List<SaleEntity>> getSalesInRange(final long start, final long end) {
    final String _sql = "SELECT * FROM Sales WHERE timestamp BETWEEN ? AND ? AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Sales"}, new Callable<List<SaleEntity>>() {
      @Override
      @NonNull
      public List<SaleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfSyncUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_uuid");
          final int _cursorIndexOfCashierId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashier_id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfItemsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "items_json");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfTax = CursorUtil.getColumnIndexOrThrow(_cursor, "tax");
          final int _cursorIndexOfDiscount = CursorUtil.getColumnIndexOrThrow(_cursor, "discount");
          final int _cursorIndexOfTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "total");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "payment_method");
          final int _cursorIndexOfCashAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "cash_amount");
          final int _cursorIndexOfCardAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "card_amount");
          final int _cursorIndexOfWalletAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "wallet_amount");
          final int _cursorIndexOfUdhaarAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "udhaar_amount");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_id");
          final int _cursorIndexOfPaymentSplitJson = CursorUtil.getColumnIndexOrThrow(_cursor, "payment_split_json");
          final int _cursorIndexOfReferenceId = CursorUtil.getColumnIndexOrThrow(_cursor, "reference_id");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<SaleEntity> _result = new ArrayList<SaleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SaleEntity _item;
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
            final String _tmpSync_uuid;
            _tmpSync_uuid = _cursor.getString(_cursorIndexOfSyncUuid);
            final String _tmpCashier_id;
            _tmpCashier_id = _cursor.getString(_cursorIndexOfCashierId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpItems_json;
            _tmpItems_json = _cursor.getString(_cursorIndexOfItemsJson);
            final double _tmpSubtotal;
            _tmpSubtotal = _cursor.getDouble(_cursorIndexOfSubtotal);
            final double _tmpTax;
            _tmpTax = _cursor.getDouble(_cursorIndexOfTax);
            final double _tmpDiscount;
            _tmpDiscount = _cursor.getDouble(_cursorIndexOfDiscount);
            final double _tmpTotal;
            _tmpTotal = _cursor.getDouble(_cursorIndexOfTotal);
            final String _tmpPayment_method;
            _tmpPayment_method = _cursor.getString(_cursorIndexOfPaymentMethod);
            final double _tmpCash_amount;
            _tmpCash_amount = _cursor.getDouble(_cursorIndexOfCashAmount);
            final double _tmpCard_amount;
            _tmpCard_amount = _cursor.getDouble(_cursorIndexOfCardAmount);
            final double _tmpWallet_amount;
            _tmpWallet_amount = _cursor.getDouble(_cursorIndexOfWalletAmount);
            final double _tmpUdhaar_amount;
            _tmpUdhaar_amount = _cursor.getDouble(_cursorIndexOfUdhaarAmount);
            final String _tmpCustomer_id;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomer_id = null;
            } else {
              _tmpCustomer_id = _cursor.getString(_cursorIndexOfCustomerId);
            }
            final String _tmpPayment_split_json;
            if (_cursor.isNull(_cursorIndexOfPaymentSplitJson)) {
              _tmpPayment_split_json = null;
            } else {
              _tmpPayment_split_json = _cursor.getString(_cursorIndexOfPaymentSplitJson);
            }
            final String _tmpReference_id;
            if (_cursor.isNull(_cursorIndexOfReferenceId)) {
              _tmpReference_id = null;
            } else {
              _tmpReference_id = _cursor.getString(_cursorIndexOfReferenceId);
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
            _item = new SaleEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpSync_uuid,_tmpCashier_id,_tmpTimestamp,_tmpItems_json,_tmpSubtotal,_tmpTax,_tmpDiscount,_tmpTotal,_tmpPayment_method,_tmpCash_amount,_tmpCard_amount,_tmpWallet_amount,_tmpUdhaar_amount,_tmpCustomer_id,_tmpPayment_split_json,_tmpReference_id,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getSaleById(final String systemRowId,
      final Continuation<? super SaleEntity> $completion) {
    final String _sql = "SELECT * FROM Sales WHERE system_row_id = ? AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, systemRowId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SaleEntity>() {
      @Override
      @Nullable
      public SaleEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfSyncUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_uuid");
          final int _cursorIndexOfCashierId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashier_id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfItemsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "items_json");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfTax = CursorUtil.getColumnIndexOrThrow(_cursor, "tax");
          final int _cursorIndexOfDiscount = CursorUtil.getColumnIndexOrThrow(_cursor, "discount");
          final int _cursorIndexOfTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "total");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "payment_method");
          final int _cursorIndexOfCashAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "cash_amount");
          final int _cursorIndexOfCardAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "card_amount");
          final int _cursorIndexOfWalletAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "wallet_amount");
          final int _cursorIndexOfUdhaarAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "udhaar_amount");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_id");
          final int _cursorIndexOfPaymentSplitJson = CursorUtil.getColumnIndexOrThrow(_cursor, "payment_split_json");
          final int _cursorIndexOfReferenceId = CursorUtil.getColumnIndexOrThrow(_cursor, "reference_id");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final SaleEntity _result;
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
            final String _tmpSync_uuid;
            _tmpSync_uuid = _cursor.getString(_cursorIndexOfSyncUuid);
            final String _tmpCashier_id;
            _tmpCashier_id = _cursor.getString(_cursorIndexOfCashierId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpItems_json;
            _tmpItems_json = _cursor.getString(_cursorIndexOfItemsJson);
            final double _tmpSubtotal;
            _tmpSubtotal = _cursor.getDouble(_cursorIndexOfSubtotal);
            final double _tmpTax;
            _tmpTax = _cursor.getDouble(_cursorIndexOfTax);
            final double _tmpDiscount;
            _tmpDiscount = _cursor.getDouble(_cursorIndexOfDiscount);
            final double _tmpTotal;
            _tmpTotal = _cursor.getDouble(_cursorIndexOfTotal);
            final String _tmpPayment_method;
            _tmpPayment_method = _cursor.getString(_cursorIndexOfPaymentMethod);
            final double _tmpCash_amount;
            _tmpCash_amount = _cursor.getDouble(_cursorIndexOfCashAmount);
            final double _tmpCard_amount;
            _tmpCard_amount = _cursor.getDouble(_cursorIndexOfCardAmount);
            final double _tmpWallet_amount;
            _tmpWallet_amount = _cursor.getDouble(_cursorIndexOfWalletAmount);
            final double _tmpUdhaar_amount;
            _tmpUdhaar_amount = _cursor.getDouble(_cursorIndexOfUdhaarAmount);
            final String _tmpCustomer_id;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomer_id = null;
            } else {
              _tmpCustomer_id = _cursor.getString(_cursorIndexOfCustomerId);
            }
            final String _tmpPayment_split_json;
            if (_cursor.isNull(_cursorIndexOfPaymentSplitJson)) {
              _tmpPayment_split_json = null;
            } else {
              _tmpPayment_split_json = _cursor.getString(_cursorIndexOfPaymentSplitJson);
            }
            final String _tmpReference_id;
            if (_cursor.isNull(_cursorIndexOfReferenceId)) {
              _tmpReference_id = null;
            } else {
              _tmpReference_id = _cursor.getString(_cursorIndexOfReferenceId);
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
            _result = new SaleEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpSync_uuid,_tmpCashier_id,_tmpTimestamp,_tmpItems_json,_tmpSubtotal,_tmpTax,_tmpDiscount,_tmpTotal,_tmpPayment_method,_tmpCash_amount,_tmpCard_amount,_tmpWallet_amount,_tmpUdhaar_amount,_tmpCustomer_id,_tmpPayment_split_json,_tmpReference_id,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getSaleByInvoiceId(final String invoiceId,
      final Continuation<? super SaleEntity> $completion) {
    final String _sql = "SELECT * FROM Sales WHERE sync_uuid = ? AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, invoiceId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SaleEntity>() {
      @Override
      @Nullable
      public SaleEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfSyncUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_uuid");
          final int _cursorIndexOfCashierId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashier_id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfItemsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "items_json");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfTax = CursorUtil.getColumnIndexOrThrow(_cursor, "tax");
          final int _cursorIndexOfDiscount = CursorUtil.getColumnIndexOrThrow(_cursor, "discount");
          final int _cursorIndexOfTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "total");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "payment_method");
          final int _cursorIndexOfCashAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "cash_amount");
          final int _cursorIndexOfCardAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "card_amount");
          final int _cursorIndexOfWalletAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "wallet_amount");
          final int _cursorIndexOfUdhaarAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "udhaar_amount");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_id");
          final int _cursorIndexOfPaymentSplitJson = CursorUtil.getColumnIndexOrThrow(_cursor, "payment_split_json");
          final int _cursorIndexOfReferenceId = CursorUtil.getColumnIndexOrThrow(_cursor, "reference_id");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final SaleEntity _result;
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
            final String _tmpSync_uuid;
            _tmpSync_uuid = _cursor.getString(_cursorIndexOfSyncUuid);
            final String _tmpCashier_id;
            _tmpCashier_id = _cursor.getString(_cursorIndexOfCashierId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpItems_json;
            _tmpItems_json = _cursor.getString(_cursorIndexOfItemsJson);
            final double _tmpSubtotal;
            _tmpSubtotal = _cursor.getDouble(_cursorIndexOfSubtotal);
            final double _tmpTax;
            _tmpTax = _cursor.getDouble(_cursorIndexOfTax);
            final double _tmpDiscount;
            _tmpDiscount = _cursor.getDouble(_cursorIndexOfDiscount);
            final double _tmpTotal;
            _tmpTotal = _cursor.getDouble(_cursorIndexOfTotal);
            final String _tmpPayment_method;
            _tmpPayment_method = _cursor.getString(_cursorIndexOfPaymentMethod);
            final double _tmpCash_amount;
            _tmpCash_amount = _cursor.getDouble(_cursorIndexOfCashAmount);
            final double _tmpCard_amount;
            _tmpCard_amount = _cursor.getDouble(_cursorIndexOfCardAmount);
            final double _tmpWallet_amount;
            _tmpWallet_amount = _cursor.getDouble(_cursorIndexOfWalletAmount);
            final double _tmpUdhaar_amount;
            _tmpUdhaar_amount = _cursor.getDouble(_cursorIndexOfUdhaarAmount);
            final String _tmpCustomer_id;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomer_id = null;
            } else {
              _tmpCustomer_id = _cursor.getString(_cursorIndexOfCustomerId);
            }
            final String _tmpPayment_split_json;
            if (_cursor.isNull(_cursorIndexOfPaymentSplitJson)) {
              _tmpPayment_split_json = null;
            } else {
              _tmpPayment_split_json = _cursor.getString(_cursorIndexOfPaymentSplitJson);
            }
            final String _tmpReference_id;
            if (_cursor.isNull(_cursorIndexOfReferenceId)) {
              _tmpReference_id = null;
            } else {
              _tmpReference_id = _cursor.getString(_cursorIndexOfReferenceId);
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
            _result = new SaleEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpSync_uuid,_tmpCashier_id,_tmpTimestamp,_tmpItems_json,_tmpSubtotal,_tmpTax,_tmpDiscount,_tmpTotal,_tmpPayment_method,_tmpCash_amount,_tmpCard_amount,_tmpWallet_amount,_tmpUdhaar_amount,_tmpCustomer_id,_tmpPayment_split_json,_tmpReference_id,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getPendingSyncSales(final Continuation<? super List<SaleEntity>> $completion) {
    final String _sql = "SELECT * FROM Sales WHERE sync_status = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SaleEntity>>() {
      @Override
      @NonNull
      public List<SaleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfSyncUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_uuid");
          final int _cursorIndexOfCashierId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashier_id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfItemsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "items_json");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfTax = CursorUtil.getColumnIndexOrThrow(_cursor, "tax");
          final int _cursorIndexOfDiscount = CursorUtil.getColumnIndexOrThrow(_cursor, "discount");
          final int _cursorIndexOfTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "total");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "payment_method");
          final int _cursorIndexOfCashAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "cash_amount");
          final int _cursorIndexOfCardAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "card_amount");
          final int _cursorIndexOfWalletAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "wallet_amount");
          final int _cursorIndexOfUdhaarAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "udhaar_amount");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_id");
          final int _cursorIndexOfPaymentSplitJson = CursorUtil.getColumnIndexOrThrow(_cursor, "payment_split_json");
          final int _cursorIndexOfReferenceId = CursorUtil.getColumnIndexOrThrow(_cursor, "reference_id");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<SaleEntity> _result = new ArrayList<SaleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SaleEntity _item;
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
            final String _tmpSync_uuid;
            _tmpSync_uuid = _cursor.getString(_cursorIndexOfSyncUuid);
            final String _tmpCashier_id;
            _tmpCashier_id = _cursor.getString(_cursorIndexOfCashierId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpItems_json;
            _tmpItems_json = _cursor.getString(_cursorIndexOfItemsJson);
            final double _tmpSubtotal;
            _tmpSubtotal = _cursor.getDouble(_cursorIndexOfSubtotal);
            final double _tmpTax;
            _tmpTax = _cursor.getDouble(_cursorIndexOfTax);
            final double _tmpDiscount;
            _tmpDiscount = _cursor.getDouble(_cursorIndexOfDiscount);
            final double _tmpTotal;
            _tmpTotal = _cursor.getDouble(_cursorIndexOfTotal);
            final String _tmpPayment_method;
            _tmpPayment_method = _cursor.getString(_cursorIndexOfPaymentMethod);
            final double _tmpCash_amount;
            _tmpCash_amount = _cursor.getDouble(_cursorIndexOfCashAmount);
            final double _tmpCard_amount;
            _tmpCard_amount = _cursor.getDouble(_cursorIndexOfCardAmount);
            final double _tmpWallet_amount;
            _tmpWallet_amount = _cursor.getDouble(_cursorIndexOfWalletAmount);
            final double _tmpUdhaar_amount;
            _tmpUdhaar_amount = _cursor.getDouble(_cursorIndexOfUdhaarAmount);
            final String _tmpCustomer_id;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomer_id = null;
            } else {
              _tmpCustomer_id = _cursor.getString(_cursorIndexOfCustomerId);
            }
            final String _tmpPayment_split_json;
            if (_cursor.isNull(_cursorIndexOfPaymentSplitJson)) {
              _tmpPayment_split_json = null;
            } else {
              _tmpPayment_split_json = _cursor.getString(_cursorIndexOfPaymentSplitJson);
            }
            final String _tmpReference_id;
            if (_cursor.isNull(_cursorIndexOfReferenceId)) {
              _tmpReference_id = null;
            } else {
              _tmpReference_id = _cursor.getString(_cursorIndexOfReferenceId);
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
            _item = new SaleEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpSync_uuid,_tmpCashier_id,_tmpTimestamp,_tmpItems_json,_tmpSubtotal,_tmpTax,_tmpDiscount,_tmpTotal,_tmpPayment_method,_tmpCash_amount,_tmpCard_amount,_tmpWallet_amount,_tmpUdhaar_amount,_tmpCustomer_id,_tmpPayment_split_json,_tmpReference_id,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getSaleCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM Sales WHERE is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Object getPendingDeletedRows(final Continuation<? super List<SaleEntity>> $completion) {
    final String _sql = "SELECT * FROM Sales WHERE is_deleted = 1 AND sync_status = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SaleEntity>>() {
      @Override
      @NonNull
      public List<SaleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfSyncUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_uuid");
          final int _cursorIndexOfCashierId = CursorUtil.getColumnIndexOrThrow(_cursor, "cashier_id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfItemsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "items_json");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfTax = CursorUtil.getColumnIndexOrThrow(_cursor, "tax");
          final int _cursorIndexOfDiscount = CursorUtil.getColumnIndexOrThrow(_cursor, "discount");
          final int _cursorIndexOfTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "total");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "payment_method");
          final int _cursorIndexOfCashAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "cash_amount");
          final int _cursorIndexOfCardAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "card_amount");
          final int _cursorIndexOfWalletAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "wallet_amount");
          final int _cursorIndexOfUdhaarAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "udhaar_amount");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_id");
          final int _cursorIndexOfPaymentSplitJson = CursorUtil.getColumnIndexOrThrow(_cursor, "payment_split_json");
          final int _cursorIndexOfReferenceId = CursorUtil.getColumnIndexOrThrow(_cursor, "reference_id");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<SaleEntity> _result = new ArrayList<SaleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SaleEntity _item;
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
            final String _tmpSync_uuid;
            _tmpSync_uuid = _cursor.getString(_cursorIndexOfSyncUuid);
            final String _tmpCashier_id;
            _tmpCashier_id = _cursor.getString(_cursorIndexOfCashierId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpItems_json;
            _tmpItems_json = _cursor.getString(_cursorIndexOfItemsJson);
            final double _tmpSubtotal;
            _tmpSubtotal = _cursor.getDouble(_cursorIndexOfSubtotal);
            final double _tmpTax;
            _tmpTax = _cursor.getDouble(_cursorIndexOfTax);
            final double _tmpDiscount;
            _tmpDiscount = _cursor.getDouble(_cursorIndexOfDiscount);
            final double _tmpTotal;
            _tmpTotal = _cursor.getDouble(_cursorIndexOfTotal);
            final String _tmpPayment_method;
            _tmpPayment_method = _cursor.getString(_cursorIndexOfPaymentMethod);
            final double _tmpCash_amount;
            _tmpCash_amount = _cursor.getDouble(_cursorIndexOfCashAmount);
            final double _tmpCard_amount;
            _tmpCard_amount = _cursor.getDouble(_cursorIndexOfCardAmount);
            final double _tmpWallet_amount;
            _tmpWallet_amount = _cursor.getDouble(_cursorIndexOfWalletAmount);
            final double _tmpUdhaar_amount;
            _tmpUdhaar_amount = _cursor.getDouble(_cursorIndexOfUdhaarAmount);
            final String _tmpCustomer_id;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomer_id = null;
            } else {
              _tmpCustomer_id = _cursor.getString(_cursorIndexOfCustomerId);
            }
            final String _tmpPayment_split_json;
            if (_cursor.isNull(_cursorIndexOfPaymentSplitJson)) {
              _tmpPayment_split_json = null;
            } else {
              _tmpPayment_split_json = _cursor.getString(_cursorIndexOfPaymentSplitJson);
            }
            final String _tmpReference_id;
            if (_cursor.isNull(_cursorIndexOfReferenceId)) {
              _tmpReference_id = null;
            } else {
              _tmpReference_id = _cursor.getString(_cursorIndexOfReferenceId);
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
            _item = new SaleEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpSync_uuid,_tmpCashier_id,_tmpTimestamp,_tmpItems_json,_tmpSubtotal,_tmpTax,_tmpDiscount,_tmpTotal,_tmpPayment_method,_tmpCash_amount,_tmpCard_amount,_tmpWallet_amount,_tmpUdhaar_amount,_tmpCustomer_id,_tmpPayment_split_json,_tmpReference_id,_tmpIs_deleted,_tmpDeleted_at);
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
