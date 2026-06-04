package com.tillzo.pos.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tillzo.pos.data.local.entity.PurchaseOrderEntity;
import com.tillzo.pos.data.local.entity.PurchaseOrderItemEntity;
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
public final class PurchaseOrderDao_Impl implements PurchaseOrderDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PurchaseOrderEntity> __insertionAdapterOfPurchaseOrderEntity;

  private final EntityInsertionAdapter<PurchaseOrderItemEntity> __insertionAdapterOfPurchaseOrderItemEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePOStatus;

  private final SharedSQLiteStatement __preparedStmtOfMarkSynced;

  public PurchaseOrderDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPurchaseOrderEntity = new EntityInsertionAdapter<PurchaseOrderEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `purchase_orders` (`poId`,`poNumber`,`vendorId`,`vendorName`,`status`,`notes`,`totalAmount`,`currency`,`expectedDeliveryDate`,`createdBy`,`syncStatus`,`isDeleted`,`deletedAt`,`posTerminalId`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PurchaseOrderEntity entity) {
        statement.bindString(1, entity.getPoId());
        statement.bindString(2, entity.getPoNumber());
        statement.bindString(3, entity.getVendorId());
        statement.bindString(4, entity.getVendorName());
        statement.bindString(5, entity.getStatus());
        statement.bindString(6, entity.getNotes());
        statement.bindDouble(7, entity.getTotalAmount());
        statement.bindString(8, entity.getCurrency());
        statement.bindString(9, entity.getExpectedDeliveryDate());
        statement.bindString(10, entity.getCreatedBy());
        statement.bindString(11, entity.getSyncStatus());
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(12, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getDeletedAt());
        }
        statement.bindString(14, entity.getPosTerminalId());
        statement.bindLong(15, entity.getCreatedAt());
        statement.bindLong(16, entity.getUpdatedAt());
      }
    };
    this.__insertionAdapterOfPurchaseOrderItemEntity = new EntityInsertionAdapter<PurchaseOrderItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `purchase_order_items` (`poItemId`,`poId`,`productId`,`productName`,`sku`,`barcodeId`,`orderedQty`,`receivedQty`,`unitCostPrice`,`totalCost`,`unit`,`syncStatus`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PurchaseOrderItemEntity entity) {
        statement.bindString(1, entity.getPoItemId());
        statement.bindString(2, entity.getPoId());
        statement.bindString(3, entity.getProductId());
        statement.bindString(4, entity.getProductName());
        statement.bindString(5, entity.getSku());
        statement.bindString(6, entity.getBarcodeId());
        statement.bindDouble(7, entity.getOrderedQty());
        statement.bindDouble(8, entity.getReceivedQty());
        statement.bindDouble(9, entity.getUnitCostPrice());
        statement.bindDouble(10, entity.getTotalCost());
        statement.bindString(11, entity.getUnit());
        statement.bindString(12, entity.getSyncStatus());
        statement.bindLong(13, entity.getCreatedAt());
        statement.bindLong(14, entity.getUpdatedAt());
      }
    };
    this.__preparedStmtOfUpdatePOStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE purchase_orders SET status = ?, updatedAt = ?, syncStatus = 'pending' WHERE poId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE purchase_orders SET syncStatus = 'synced' WHERE poId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertPO(final PurchaseOrderEntity po,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPurchaseOrderEntity.insert(po);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertPOItems(final List<PurchaseOrderItemEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPurchaseOrderItemEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePOStatus(final String poId, final String status, final long time,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePOStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, time);
        _argIndex = 3;
        _stmt.bindString(_argIndex, poId);
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
          __preparedStmtOfUpdatePOStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markSynced(final String poId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkSynced.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, poId);
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
  public Flow<List<PurchaseOrderEntity>> getAllPOs() {
    final String _sql = "SELECT * FROM purchase_orders WHERE isDeleted = 0 ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"purchase_orders"}, new Callable<List<PurchaseOrderEntity>>() {
      @Override
      @NonNull
      public List<PurchaseOrderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPoId = CursorUtil.getColumnIndexOrThrow(_cursor, "poId");
          final int _cursorIndexOfPoNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "poNumber");
          final int _cursorIndexOfVendorId = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorId");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorName");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfTotalAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAmount");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfExpectedDeliveryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedDeliveryDate");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<PurchaseOrderEntity> _result = new ArrayList<PurchaseOrderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PurchaseOrderEntity _item;
            final String _tmpPoId;
            _tmpPoId = _cursor.getString(_cursorIndexOfPoId);
            final String _tmpPoNumber;
            _tmpPoNumber = _cursor.getString(_cursorIndexOfPoNumber);
            final String _tmpVendorId;
            _tmpVendorId = _cursor.getString(_cursorIndexOfVendorId);
            final String _tmpVendorName;
            _tmpVendorName = _cursor.getString(_cursorIndexOfVendorName);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final double _tmpTotalAmount;
            _tmpTotalAmount = _cursor.getDouble(_cursorIndexOfTotalAmount);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final String _tmpExpectedDeliveryDate;
            _tmpExpectedDeliveryDate = _cursor.getString(_cursorIndexOfExpectedDeliveryDate);
            final String _tmpCreatedBy;
            _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new PurchaseOrderEntity(_tmpPoId,_tmpPoNumber,_tmpVendorId,_tmpVendorName,_tmpStatus,_tmpNotes,_tmpTotalAmount,_tmpCurrency,_tmpExpectedDeliveryDate,_tmpCreatedBy,_tmpSyncStatus,_tmpIsDeleted,_tmpDeletedAt,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getPOById(final String poId,
      final Continuation<? super PurchaseOrderEntity> $completion) {
    final String _sql = "SELECT * FROM purchase_orders WHERE poId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, poId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PurchaseOrderEntity>() {
      @Override
      @Nullable
      public PurchaseOrderEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPoId = CursorUtil.getColumnIndexOrThrow(_cursor, "poId");
          final int _cursorIndexOfPoNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "poNumber");
          final int _cursorIndexOfVendorId = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorId");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorName");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfTotalAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAmount");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfExpectedDeliveryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedDeliveryDate");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final PurchaseOrderEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpPoId;
            _tmpPoId = _cursor.getString(_cursorIndexOfPoId);
            final String _tmpPoNumber;
            _tmpPoNumber = _cursor.getString(_cursorIndexOfPoNumber);
            final String _tmpVendorId;
            _tmpVendorId = _cursor.getString(_cursorIndexOfVendorId);
            final String _tmpVendorName;
            _tmpVendorName = _cursor.getString(_cursorIndexOfVendorName);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final double _tmpTotalAmount;
            _tmpTotalAmount = _cursor.getDouble(_cursorIndexOfTotalAmount);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final String _tmpExpectedDeliveryDate;
            _tmpExpectedDeliveryDate = _cursor.getString(_cursorIndexOfExpectedDeliveryDate);
            final String _tmpCreatedBy;
            _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new PurchaseOrderEntity(_tmpPoId,_tmpPoNumber,_tmpVendorId,_tmpVendorName,_tmpStatus,_tmpNotes,_tmpTotalAmount,_tmpCurrency,_tmpExpectedDeliveryDate,_tmpCreatedBy,_tmpSyncStatus,_tmpIsDeleted,_tmpDeletedAt,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getPOItems(final String poId,
      final Continuation<? super List<PurchaseOrderItemEntity>> $completion) {
    final String _sql = "SELECT * FROM purchase_order_items WHERE poId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, poId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PurchaseOrderItemEntity>>() {
      @Override
      @NonNull
      public List<PurchaseOrderItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPoItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "poItemId");
          final int _cursorIndexOfPoId = CursorUtil.getColumnIndexOrThrow(_cursor, "poId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcodeId");
          final int _cursorIndexOfOrderedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "orderedQty");
          final int _cursorIndexOfReceivedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedQty");
          final int _cursorIndexOfUnitCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "unitCostPrice");
          final int _cursorIndexOfTotalCost = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCost");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<PurchaseOrderItemEntity> _result = new ArrayList<PurchaseOrderItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PurchaseOrderItemEntity _item;
            final String _tmpPoItemId;
            _tmpPoItemId = _cursor.getString(_cursorIndexOfPoItemId);
            final String _tmpPoId;
            _tmpPoId = _cursor.getString(_cursorIndexOfPoId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBarcodeId;
            _tmpBarcodeId = _cursor.getString(_cursorIndexOfBarcodeId);
            final double _tmpOrderedQty;
            _tmpOrderedQty = _cursor.getDouble(_cursorIndexOfOrderedQty);
            final double _tmpReceivedQty;
            _tmpReceivedQty = _cursor.getDouble(_cursorIndexOfReceivedQty);
            final double _tmpUnitCostPrice;
            _tmpUnitCostPrice = _cursor.getDouble(_cursorIndexOfUnitCostPrice);
            final double _tmpTotalCost;
            _tmpTotalCost = _cursor.getDouble(_cursorIndexOfTotalCost);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new PurchaseOrderItemEntity(_tmpPoItemId,_tmpPoId,_tmpProductId,_tmpProductName,_tmpSku,_tmpBarcodeId,_tmpOrderedQty,_tmpReceivedQty,_tmpUnitCostPrice,_tmpTotalCost,_tmpUnit,_tmpSyncStatus,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getPendingPOs(final Continuation<? super List<PurchaseOrderEntity>> $completion) {
    final String _sql = "SELECT * FROM purchase_orders WHERE syncStatus = 'pending' AND isDeleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PurchaseOrderEntity>>() {
      @Override
      @NonNull
      public List<PurchaseOrderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPoId = CursorUtil.getColumnIndexOrThrow(_cursor, "poId");
          final int _cursorIndexOfPoNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "poNumber");
          final int _cursorIndexOfVendorId = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorId");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorName");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfTotalAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAmount");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfExpectedDeliveryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedDeliveryDate");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<PurchaseOrderEntity> _result = new ArrayList<PurchaseOrderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PurchaseOrderEntity _item;
            final String _tmpPoId;
            _tmpPoId = _cursor.getString(_cursorIndexOfPoId);
            final String _tmpPoNumber;
            _tmpPoNumber = _cursor.getString(_cursorIndexOfPoNumber);
            final String _tmpVendorId;
            _tmpVendorId = _cursor.getString(_cursorIndexOfVendorId);
            final String _tmpVendorName;
            _tmpVendorName = _cursor.getString(_cursorIndexOfVendorName);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final double _tmpTotalAmount;
            _tmpTotalAmount = _cursor.getDouble(_cursorIndexOfTotalAmount);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final String _tmpExpectedDeliveryDate;
            _tmpExpectedDeliveryDate = _cursor.getString(_cursorIndexOfExpectedDeliveryDate);
            final String _tmpCreatedBy;
            _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new PurchaseOrderEntity(_tmpPoId,_tmpPoNumber,_tmpVendorId,_tmpVendorName,_tmpStatus,_tmpNotes,_tmpTotalAmount,_tmpCurrency,_tmpExpectedDeliveryDate,_tmpCreatedBy,_tmpSyncStatus,_tmpIsDeleted,_tmpDeletedAt,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<List<PurchaseOrderEntity>> getPendingDeliveryPOs() {
    final String _sql = "SELECT * FROM purchase_orders WHERE status IN ('SENT','PARTIALLY_RECEIVED') AND isDeleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"purchase_orders"}, new Callable<List<PurchaseOrderEntity>>() {
      @Override
      @NonNull
      public List<PurchaseOrderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPoId = CursorUtil.getColumnIndexOrThrow(_cursor, "poId");
          final int _cursorIndexOfPoNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "poNumber");
          final int _cursorIndexOfVendorId = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorId");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorName");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfTotalAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAmount");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfExpectedDeliveryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedDeliveryDate");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "createdBy");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<PurchaseOrderEntity> _result = new ArrayList<PurchaseOrderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PurchaseOrderEntity _item;
            final String _tmpPoId;
            _tmpPoId = _cursor.getString(_cursorIndexOfPoId);
            final String _tmpPoNumber;
            _tmpPoNumber = _cursor.getString(_cursorIndexOfPoNumber);
            final String _tmpVendorId;
            _tmpVendorId = _cursor.getString(_cursorIndexOfVendorId);
            final String _tmpVendorName;
            _tmpVendorName = _cursor.getString(_cursorIndexOfVendorName);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final double _tmpTotalAmount;
            _tmpTotalAmount = _cursor.getDouble(_cursorIndexOfTotalAmount);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final String _tmpExpectedDeliveryDate;
            _tmpExpectedDeliveryDate = _cursor.getString(_cursorIndexOfExpectedDeliveryDate);
            final String _tmpCreatedBy;
            _tmpCreatedBy = _cursor.getString(_cursorIndexOfCreatedBy);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new PurchaseOrderEntity(_tmpPoId,_tmpPoNumber,_tmpVendorId,_tmpVendorName,_tmpStatus,_tmpNotes,_tmpTotalAmount,_tmpCurrency,_tmpExpectedDeliveryDate,_tmpCreatedBy,_tmpSyncStatus,_tmpIsDeleted,_tmpDeletedAt,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getTotalPOCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM purchase_orders";
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
