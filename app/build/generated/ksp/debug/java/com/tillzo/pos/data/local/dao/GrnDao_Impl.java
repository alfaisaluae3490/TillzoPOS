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
import com.tillzo.pos.data.local.entity.GrnHeaderEntity;
import com.tillzo.pos.data.local.entity.GrnItemEntity;
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
public final class GrnDao_Impl implements GrnDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<GrnHeaderEntity> __insertionAdapterOfGrnHeaderEntity;

  private final EntityInsertionAdapter<GrnItemEntity> __insertionAdapterOfGrnItemEntity;

  private final EntityDeletionOrUpdateAdapter<GrnHeaderEntity> __updateAdapterOfGrnHeaderEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateGrnStatus;

  private final SharedSQLiteStatement __preparedStmtOfMarkGrnSynced;

  private final SharedSQLiteStatement __preparedStmtOfMarkGrnItemSynced;

  private final SharedSQLiteStatement __preparedStmtOfUpdateGrnItemBatchId;

  public GrnDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfGrnHeaderEntity = new EntityInsertionAdapter<GrnHeaderEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `grn_headers` (`grnId`,`grnNumber`,`poId`,`poNumber`,`vendorId`,`vendorName`,`vendorPhone`,`status`,`notes`,`receivedBy`,`receivedByName`,`totalItems`,`totalReceivedQty`,`totalAmount`,`syncStatus`,`isDeleted`,`deletedAt`,`posTerminalId`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GrnHeaderEntity entity) {
        statement.bindString(1, entity.getGrnId());
        statement.bindString(2, entity.getGrnNumber());
        statement.bindString(3, entity.getPoId());
        statement.bindString(4, entity.getPoNumber());
        statement.bindString(5, entity.getVendorId());
        statement.bindString(6, entity.getVendorName());
        statement.bindString(7, entity.getVendorPhone());
        statement.bindString(8, entity.getStatus());
        statement.bindString(9, entity.getNotes());
        statement.bindString(10, entity.getReceivedBy());
        statement.bindString(11, entity.getReceivedByName());
        statement.bindLong(12, entity.getTotalItems());
        statement.bindDouble(13, entity.getTotalReceivedQty());
        statement.bindDouble(14, entity.getTotalAmount());
        statement.bindString(15, entity.getSyncStatus());
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(16, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, entity.getDeletedAt());
        }
        statement.bindString(18, entity.getPosTerminalId());
        statement.bindLong(19, entity.getCreatedAt());
        statement.bindLong(20, entity.getUpdatedAt());
      }
    };
    this.__insertionAdapterOfGrnItemEntity = new EntityInsertionAdapter<GrnItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `grn_items` (`grnItemId`,`grnId`,`poItemId`,`productId`,`batchId`,`productName`,`barcodeId`,`sku`,`categoryId`,`brand`,`orderedQty`,`receivedQty`,`unitCostPrice`,`sellingPrice`,`totalCost`,`unit`,`batchNumber`,`manufacturingDate`,`expiryDate`,`inventoryAction`,`isNewProduct`,`lowStockThreshold`,`syncStatus`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GrnItemEntity entity) {
        statement.bindString(1, entity.getGrnItemId());
        statement.bindString(2, entity.getGrnId());
        statement.bindString(3, entity.getPoItemId());
        statement.bindString(4, entity.getProductId());
        statement.bindString(5, entity.getBatchId());
        statement.bindString(6, entity.getProductName());
        statement.bindString(7, entity.getBarcodeId());
        statement.bindString(8, entity.getSku());
        statement.bindString(9, entity.getCategoryId());
        statement.bindString(10, entity.getBrand());
        statement.bindDouble(11, entity.getOrderedQty());
        statement.bindDouble(12, entity.getReceivedQty());
        statement.bindDouble(13, entity.getUnitCostPrice());
        statement.bindDouble(14, entity.getSellingPrice());
        statement.bindDouble(15, entity.getTotalCost());
        statement.bindString(16, entity.getUnit());
        statement.bindString(17, entity.getBatchNumber());
        statement.bindString(18, entity.getManufacturingDate());
        statement.bindString(19, entity.getExpiryDate());
        statement.bindString(20, entity.getInventoryAction());
        final int _tmp = entity.isNewProduct() ? 1 : 0;
        statement.bindLong(21, _tmp);
        statement.bindDouble(22, entity.getLowStockThreshold());
        statement.bindString(23, entity.getSyncStatus());
        statement.bindLong(24, entity.getCreatedAt());
        statement.bindLong(25, entity.getUpdatedAt());
      }
    };
    this.__updateAdapterOfGrnHeaderEntity = new EntityDeletionOrUpdateAdapter<GrnHeaderEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `grn_headers` SET `grnId` = ?,`grnNumber` = ?,`poId` = ?,`poNumber` = ?,`vendorId` = ?,`vendorName` = ?,`vendorPhone` = ?,`status` = ?,`notes` = ?,`receivedBy` = ?,`receivedByName` = ?,`totalItems` = ?,`totalReceivedQty` = ?,`totalAmount` = ?,`syncStatus` = ?,`isDeleted` = ?,`deletedAt` = ?,`posTerminalId` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `grnId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GrnHeaderEntity entity) {
        statement.bindString(1, entity.getGrnId());
        statement.bindString(2, entity.getGrnNumber());
        statement.bindString(3, entity.getPoId());
        statement.bindString(4, entity.getPoNumber());
        statement.bindString(5, entity.getVendorId());
        statement.bindString(6, entity.getVendorName());
        statement.bindString(7, entity.getVendorPhone());
        statement.bindString(8, entity.getStatus());
        statement.bindString(9, entity.getNotes());
        statement.bindString(10, entity.getReceivedBy());
        statement.bindString(11, entity.getReceivedByName());
        statement.bindLong(12, entity.getTotalItems());
        statement.bindDouble(13, entity.getTotalReceivedQty());
        statement.bindDouble(14, entity.getTotalAmount());
        statement.bindString(15, entity.getSyncStatus());
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(16, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, entity.getDeletedAt());
        }
        statement.bindString(18, entity.getPosTerminalId());
        statement.bindLong(19, entity.getCreatedAt());
        statement.bindLong(20, entity.getUpdatedAt());
        statement.bindString(21, entity.getGrnId());
      }
    };
    this.__preparedStmtOfUpdateGrnStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE grn_headers SET status = ?, updatedAt = ?, syncStatus = 'pending' WHERE grnId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkGrnSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE grn_headers SET syncStatus = 'synced', updatedAt = ? WHERE grnId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkGrnItemSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE grn_items SET syncStatus = 'synced' WHERE grnItemId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateGrnItemBatchId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE grn_items SET batchId = ? WHERE grnItemId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertGrnHeader(final GrnHeaderEntity grn,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfGrnHeaderEntity.insert(grn);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertGrnItems(final List<GrnItemEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfGrnItemEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateGrnHeader(final GrnHeaderEntity grn,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfGrnHeaderEntity.handle(grn);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateGrnStatus(final String grnId, final String status, final long time,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateGrnStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, time);
        _argIndex = 3;
        _stmt.bindString(_argIndex, grnId);
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
          __preparedStmtOfUpdateGrnStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markGrnSynced(final String id, final long time,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkGrnSynced.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, time);
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
          __preparedStmtOfMarkGrnSynced.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markGrnItemSynced(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkGrnItemSynced.acquire();
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
          __preparedStmtOfMarkGrnItemSynced.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateGrnItemBatchId(final String grnItemId, final String batchId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateGrnItemBatchId.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, batchId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, grnItemId);
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
          __preparedStmtOfUpdateGrnItemBatchId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<GrnHeaderEntity>> getAllGrns() {
    final String _sql = "SELECT * FROM grn_headers WHERE isDeleted = 0 ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"grn_headers"}, new Callable<List<GrnHeaderEntity>>() {
      @Override
      @NonNull
      public List<GrnHeaderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGrnId = CursorUtil.getColumnIndexOrThrow(_cursor, "grnId");
          final int _cursorIndexOfGrnNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "grnNumber");
          final int _cursorIndexOfPoId = CursorUtil.getColumnIndexOrThrow(_cursor, "poId");
          final int _cursorIndexOfPoNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "poNumber");
          final int _cursorIndexOfVendorId = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorId");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorName");
          final int _cursorIndexOfVendorPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorPhone");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfReceivedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedBy");
          final int _cursorIndexOfReceivedByName = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedByName");
          final int _cursorIndexOfTotalItems = CursorUtil.getColumnIndexOrThrow(_cursor, "totalItems");
          final int _cursorIndexOfTotalReceivedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "totalReceivedQty");
          final int _cursorIndexOfTotalAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAmount");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<GrnHeaderEntity> _result = new ArrayList<GrnHeaderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GrnHeaderEntity _item;
            final String _tmpGrnId;
            _tmpGrnId = _cursor.getString(_cursorIndexOfGrnId);
            final String _tmpGrnNumber;
            _tmpGrnNumber = _cursor.getString(_cursorIndexOfGrnNumber);
            final String _tmpPoId;
            _tmpPoId = _cursor.getString(_cursorIndexOfPoId);
            final String _tmpPoNumber;
            _tmpPoNumber = _cursor.getString(_cursorIndexOfPoNumber);
            final String _tmpVendorId;
            _tmpVendorId = _cursor.getString(_cursorIndexOfVendorId);
            final String _tmpVendorName;
            _tmpVendorName = _cursor.getString(_cursorIndexOfVendorName);
            final String _tmpVendorPhone;
            _tmpVendorPhone = _cursor.getString(_cursorIndexOfVendorPhone);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpReceivedBy;
            _tmpReceivedBy = _cursor.getString(_cursorIndexOfReceivedBy);
            final String _tmpReceivedByName;
            _tmpReceivedByName = _cursor.getString(_cursorIndexOfReceivedByName);
            final int _tmpTotalItems;
            _tmpTotalItems = _cursor.getInt(_cursorIndexOfTotalItems);
            final double _tmpTotalReceivedQty;
            _tmpTotalReceivedQty = _cursor.getDouble(_cursorIndexOfTotalReceivedQty);
            final double _tmpTotalAmount;
            _tmpTotalAmount = _cursor.getDouble(_cursorIndexOfTotalAmount);
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
            _item = new GrnHeaderEntity(_tmpGrnId,_tmpGrnNumber,_tmpPoId,_tmpPoNumber,_tmpVendorId,_tmpVendorName,_tmpVendorPhone,_tmpStatus,_tmpNotes,_tmpReceivedBy,_tmpReceivedByName,_tmpTotalItems,_tmpTotalReceivedQty,_tmpTotalAmount,_tmpSyncStatus,_tmpIsDeleted,_tmpDeletedAt,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getGrnById(final String grnId,
      final Continuation<? super GrnHeaderEntity> $completion) {
    final String _sql = "SELECT * FROM grn_headers WHERE grnId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, grnId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<GrnHeaderEntity>() {
      @Override
      @Nullable
      public GrnHeaderEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGrnId = CursorUtil.getColumnIndexOrThrow(_cursor, "grnId");
          final int _cursorIndexOfGrnNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "grnNumber");
          final int _cursorIndexOfPoId = CursorUtil.getColumnIndexOrThrow(_cursor, "poId");
          final int _cursorIndexOfPoNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "poNumber");
          final int _cursorIndexOfVendorId = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorId");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorName");
          final int _cursorIndexOfVendorPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorPhone");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfReceivedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedBy");
          final int _cursorIndexOfReceivedByName = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedByName");
          final int _cursorIndexOfTotalItems = CursorUtil.getColumnIndexOrThrow(_cursor, "totalItems");
          final int _cursorIndexOfTotalReceivedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "totalReceivedQty");
          final int _cursorIndexOfTotalAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAmount");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final GrnHeaderEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpGrnId;
            _tmpGrnId = _cursor.getString(_cursorIndexOfGrnId);
            final String _tmpGrnNumber;
            _tmpGrnNumber = _cursor.getString(_cursorIndexOfGrnNumber);
            final String _tmpPoId;
            _tmpPoId = _cursor.getString(_cursorIndexOfPoId);
            final String _tmpPoNumber;
            _tmpPoNumber = _cursor.getString(_cursorIndexOfPoNumber);
            final String _tmpVendorId;
            _tmpVendorId = _cursor.getString(_cursorIndexOfVendorId);
            final String _tmpVendorName;
            _tmpVendorName = _cursor.getString(_cursorIndexOfVendorName);
            final String _tmpVendorPhone;
            _tmpVendorPhone = _cursor.getString(_cursorIndexOfVendorPhone);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpReceivedBy;
            _tmpReceivedBy = _cursor.getString(_cursorIndexOfReceivedBy);
            final String _tmpReceivedByName;
            _tmpReceivedByName = _cursor.getString(_cursorIndexOfReceivedByName);
            final int _tmpTotalItems;
            _tmpTotalItems = _cursor.getInt(_cursorIndexOfTotalItems);
            final double _tmpTotalReceivedQty;
            _tmpTotalReceivedQty = _cursor.getDouble(_cursorIndexOfTotalReceivedQty);
            final double _tmpTotalAmount;
            _tmpTotalAmount = _cursor.getDouble(_cursorIndexOfTotalAmount);
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
            _result = new GrnHeaderEntity(_tmpGrnId,_tmpGrnNumber,_tmpPoId,_tmpPoNumber,_tmpVendorId,_tmpVendorName,_tmpVendorPhone,_tmpStatus,_tmpNotes,_tmpReceivedBy,_tmpReceivedByName,_tmpTotalItems,_tmpTotalReceivedQty,_tmpTotalAmount,_tmpSyncStatus,_tmpIsDeleted,_tmpDeletedAt,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getGrnItems(final String grnId,
      final Continuation<? super List<GrnItemEntity>> $completion) {
    final String _sql = "SELECT * FROM grn_items WHERE grnId = ? ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, grnId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<GrnItemEntity>>() {
      @Override
      @NonNull
      public List<GrnItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGrnItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "grnItemId");
          final int _cursorIndexOfGrnId = CursorUtil.getColumnIndexOrThrow(_cursor, "grnId");
          final int _cursorIndexOfPoItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "poItemId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcodeId");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfOrderedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "orderedQty");
          final int _cursorIndexOfReceivedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedQty");
          final int _cursorIndexOfUnitCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "unitCostPrice");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellingPrice");
          final int _cursorIndexOfTotalCost = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCost");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturingDate");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
          final int _cursorIndexOfInventoryAction = CursorUtil.getColumnIndexOrThrow(_cursor, "inventoryAction");
          final int _cursorIndexOfIsNewProduct = CursorUtil.getColumnIndexOrThrow(_cursor, "isNewProduct");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "lowStockThreshold");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<GrnItemEntity> _result = new ArrayList<GrnItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GrnItemEntity _item;
            final String _tmpGrnItemId;
            _tmpGrnItemId = _cursor.getString(_cursorIndexOfGrnItemId);
            final String _tmpGrnId;
            _tmpGrnId = _cursor.getString(_cursorIndexOfGrnId);
            final String _tmpPoItemId;
            _tmpPoItemId = _cursor.getString(_cursorIndexOfPoItemId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpBarcodeId;
            _tmpBarcodeId = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpCategoryId;
            _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final double _tmpOrderedQty;
            _tmpOrderedQty = _cursor.getDouble(_cursorIndexOfOrderedQty);
            final double _tmpReceivedQty;
            _tmpReceivedQty = _cursor.getDouble(_cursorIndexOfReceivedQty);
            final double _tmpUnitCostPrice;
            _tmpUnitCostPrice = _cursor.getDouble(_cursorIndexOfUnitCostPrice);
            final double _tmpSellingPrice;
            _tmpSellingPrice = _cursor.getDouble(_cursorIndexOfSellingPrice);
            final double _tmpTotalCost;
            _tmpTotalCost = _cursor.getDouble(_cursorIndexOfTotalCost);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpManufacturingDate;
            _tmpManufacturingDate = _cursor.getString(_cursorIndexOfManufacturingDate);
            final String _tmpExpiryDate;
            _tmpExpiryDate = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpInventoryAction;
            _tmpInventoryAction = _cursor.getString(_cursorIndexOfInventoryAction);
            final boolean _tmpIsNewProduct;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsNewProduct);
            _tmpIsNewProduct = _tmp != 0;
            final double _tmpLowStockThreshold;
            _tmpLowStockThreshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new GrnItemEntity(_tmpGrnItemId,_tmpGrnId,_tmpPoItemId,_tmpProductId,_tmpBatchId,_tmpProductName,_tmpBarcodeId,_tmpSku,_tmpCategoryId,_tmpBrand,_tmpOrderedQty,_tmpReceivedQty,_tmpUnitCostPrice,_tmpSellingPrice,_tmpTotalCost,_tmpUnit,_tmpBatchNumber,_tmpManufacturingDate,_tmpExpiryDate,_tmpInventoryAction,_tmpIsNewProduct,_tmpLowStockThreshold,_tmpSyncStatus,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<List<GrnItemEntity>> getGrnItemsFlow(final String grnId) {
    final String _sql = "SELECT * FROM grn_items WHERE grnId = ? ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, grnId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"grn_items"}, new Callable<List<GrnItemEntity>>() {
      @Override
      @NonNull
      public List<GrnItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGrnItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "grnItemId");
          final int _cursorIndexOfGrnId = CursorUtil.getColumnIndexOrThrow(_cursor, "grnId");
          final int _cursorIndexOfPoItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "poItemId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcodeId");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfOrderedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "orderedQty");
          final int _cursorIndexOfReceivedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedQty");
          final int _cursorIndexOfUnitCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "unitCostPrice");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellingPrice");
          final int _cursorIndexOfTotalCost = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCost");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturingDate");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
          final int _cursorIndexOfInventoryAction = CursorUtil.getColumnIndexOrThrow(_cursor, "inventoryAction");
          final int _cursorIndexOfIsNewProduct = CursorUtil.getColumnIndexOrThrow(_cursor, "isNewProduct");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "lowStockThreshold");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<GrnItemEntity> _result = new ArrayList<GrnItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GrnItemEntity _item;
            final String _tmpGrnItemId;
            _tmpGrnItemId = _cursor.getString(_cursorIndexOfGrnItemId);
            final String _tmpGrnId;
            _tmpGrnId = _cursor.getString(_cursorIndexOfGrnId);
            final String _tmpPoItemId;
            _tmpPoItemId = _cursor.getString(_cursorIndexOfPoItemId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpBarcodeId;
            _tmpBarcodeId = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpCategoryId;
            _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final double _tmpOrderedQty;
            _tmpOrderedQty = _cursor.getDouble(_cursorIndexOfOrderedQty);
            final double _tmpReceivedQty;
            _tmpReceivedQty = _cursor.getDouble(_cursorIndexOfReceivedQty);
            final double _tmpUnitCostPrice;
            _tmpUnitCostPrice = _cursor.getDouble(_cursorIndexOfUnitCostPrice);
            final double _tmpSellingPrice;
            _tmpSellingPrice = _cursor.getDouble(_cursorIndexOfSellingPrice);
            final double _tmpTotalCost;
            _tmpTotalCost = _cursor.getDouble(_cursorIndexOfTotalCost);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpManufacturingDate;
            _tmpManufacturingDate = _cursor.getString(_cursorIndexOfManufacturingDate);
            final String _tmpExpiryDate;
            _tmpExpiryDate = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpInventoryAction;
            _tmpInventoryAction = _cursor.getString(_cursorIndexOfInventoryAction);
            final boolean _tmpIsNewProduct;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsNewProduct);
            _tmpIsNewProduct = _tmp != 0;
            final double _tmpLowStockThreshold;
            _tmpLowStockThreshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new GrnItemEntity(_tmpGrnItemId,_tmpGrnId,_tmpPoItemId,_tmpProductId,_tmpBatchId,_tmpProductName,_tmpBarcodeId,_tmpSku,_tmpCategoryId,_tmpBrand,_tmpOrderedQty,_tmpReceivedQty,_tmpUnitCostPrice,_tmpSellingPrice,_tmpTotalCost,_tmpUnit,_tmpBatchNumber,_tmpManufacturingDate,_tmpExpiryDate,_tmpInventoryAction,_tmpIsNewProduct,_tmpLowStockThreshold,_tmpSyncStatus,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getTotalGrnCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM grn_headers";
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
  public Object getPendingGrns(final Continuation<? super List<GrnHeaderEntity>> $completion) {
    final String _sql = "SELECT * FROM grn_headers WHERE syncStatus = 'pending' AND isDeleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<GrnHeaderEntity>>() {
      @Override
      @NonNull
      public List<GrnHeaderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGrnId = CursorUtil.getColumnIndexOrThrow(_cursor, "grnId");
          final int _cursorIndexOfGrnNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "grnNumber");
          final int _cursorIndexOfPoId = CursorUtil.getColumnIndexOrThrow(_cursor, "poId");
          final int _cursorIndexOfPoNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "poNumber");
          final int _cursorIndexOfVendorId = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorId");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorName");
          final int _cursorIndexOfVendorPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorPhone");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfReceivedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedBy");
          final int _cursorIndexOfReceivedByName = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedByName");
          final int _cursorIndexOfTotalItems = CursorUtil.getColumnIndexOrThrow(_cursor, "totalItems");
          final int _cursorIndexOfTotalReceivedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "totalReceivedQty");
          final int _cursorIndexOfTotalAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAmount");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<GrnHeaderEntity> _result = new ArrayList<GrnHeaderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GrnHeaderEntity _item;
            final String _tmpGrnId;
            _tmpGrnId = _cursor.getString(_cursorIndexOfGrnId);
            final String _tmpGrnNumber;
            _tmpGrnNumber = _cursor.getString(_cursorIndexOfGrnNumber);
            final String _tmpPoId;
            _tmpPoId = _cursor.getString(_cursorIndexOfPoId);
            final String _tmpPoNumber;
            _tmpPoNumber = _cursor.getString(_cursorIndexOfPoNumber);
            final String _tmpVendorId;
            _tmpVendorId = _cursor.getString(_cursorIndexOfVendorId);
            final String _tmpVendorName;
            _tmpVendorName = _cursor.getString(_cursorIndexOfVendorName);
            final String _tmpVendorPhone;
            _tmpVendorPhone = _cursor.getString(_cursorIndexOfVendorPhone);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpReceivedBy;
            _tmpReceivedBy = _cursor.getString(_cursorIndexOfReceivedBy);
            final String _tmpReceivedByName;
            _tmpReceivedByName = _cursor.getString(_cursorIndexOfReceivedByName);
            final int _tmpTotalItems;
            _tmpTotalItems = _cursor.getInt(_cursorIndexOfTotalItems);
            final double _tmpTotalReceivedQty;
            _tmpTotalReceivedQty = _cursor.getDouble(_cursorIndexOfTotalReceivedQty);
            final double _tmpTotalAmount;
            _tmpTotalAmount = _cursor.getDouble(_cursorIndexOfTotalAmount);
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
            _item = new GrnHeaderEntity(_tmpGrnId,_tmpGrnNumber,_tmpPoId,_tmpPoNumber,_tmpVendorId,_tmpVendorName,_tmpVendorPhone,_tmpStatus,_tmpNotes,_tmpReceivedBy,_tmpReceivedByName,_tmpTotalItems,_tmpTotalReceivedQty,_tmpTotalAmount,_tmpSyncStatus,_tmpIsDeleted,_tmpDeletedAt,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getPendingGrnItems(final Continuation<? super List<GrnItemEntity>> $completion) {
    final String _sql = "SELECT * FROM grn_items WHERE syncStatus = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<GrnItemEntity>>() {
      @Override
      @NonNull
      public List<GrnItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGrnItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "grnItemId");
          final int _cursorIndexOfGrnId = CursorUtil.getColumnIndexOrThrow(_cursor, "grnId");
          final int _cursorIndexOfPoItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "poItemId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcodeId");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfOrderedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "orderedQty");
          final int _cursorIndexOfReceivedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedQty");
          final int _cursorIndexOfUnitCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "unitCostPrice");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellingPrice");
          final int _cursorIndexOfTotalCost = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCost");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturingDate");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
          final int _cursorIndexOfInventoryAction = CursorUtil.getColumnIndexOrThrow(_cursor, "inventoryAction");
          final int _cursorIndexOfIsNewProduct = CursorUtil.getColumnIndexOrThrow(_cursor, "isNewProduct");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "lowStockThreshold");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<GrnItemEntity> _result = new ArrayList<GrnItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GrnItemEntity _item;
            final String _tmpGrnItemId;
            _tmpGrnItemId = _cursor.getString(_cursorIndexOfGrnItemId);
            final String _tmpGrnId;
            _tmpGrnId = _cursor.getString(_cursorIndexOfGrnId);
            final String _tmpPoItemId;
            _tmpPoItemId = _cursor.getString(_cursorIndexOfPoItemId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpBarcodeId;
            _tmpBarcodeId = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpCategoryId;
            _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final double _tmpOrderedQty;
            _tmpOrderedQty = _cursor.getDouble(_cursorIndexOfOrderedQty);
            final double _tmpReceivedQty;
            _tmpReceivedQty = _cursor.getDouble(_cursorIndexOfReceivedQty);
            final double _tmpUnitCostPrice;
            _tmpUnitCostPrice = _cursor.getDouble(_cursorIndexOfUnitCostPrice);
            final double _tmpSellingPrice;
            _tmpSellingPrice = _cursor.getDouble(_cursorIndexOfSellingPrice);
            final double _tmpTotalCost;
            _tmpTotalCost = _cursor.getDouble(_cursorIndexOfTotalCost);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpManufacturingDate;
            _tmpManufacturingDate = _cursor.getString(_cursorIndexOfManufacturingDate);
            final String _tmpExpiryDate;
            _tmpExpiryDate = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpInventoryAction;
            _tmpInventoryAction = _cursor.getString(_cursorIndexOfInventoryAction);
            final boolean _tmpIsNewProduct;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsNewProduct);
            _tmpIsNewProduct = _tmp != 0;
            final double _tmpLowStockThreshold;
            _tmpLowStockThreshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new GrnItemEntity(_tmpGrnItemId,_tmpGrnId,_tmpPoItemId,_tmpProductId,_tmpBatchId,_tmpProductName,_tmpBarcodeId,_tmpSku,_tmpCategoryId,_tmpBrand,_tmpOrderedQty,_tmpReceivedQty,_tmpUnitCostPrice,_tmpSellingPrice,_tmpTotalCost,_tmpUnit,_tmpBatchNumber,_tmpManufacturingDate,_tmpExpiryDate,_tmpInventoryAction,_tmpIsNewProduct,_tmpLowStockThreshold,_tmpSyncStatus,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<List<GrnHeaderEntity>> getGrnsForPO(final String poId) {
    final String _sql = "SELECT * FROM grn_headers WHERE poId = ? AND isDeleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, poId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"grn_headers"}, new Callable<List<GrnHeaderEntity>>() {
      @Override
      @NonNull
      public List<GrnHeaderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGrnId = CursorUtil.getColumnIndexOrThrow(_cursor, "grnId");
          final int _cursorIndexOfGrnNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "grnNumber");
          final int _cursorIndexOfPoId = CursorUtil.getColumnIndexOrThrow(_cursor, "poId");
          final int _cursorIndexOfPoNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "poNumber");
          final int _cursorIndexOfVendorId = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorId");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorName");
          final int _cursorIndexOfVendorPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorPhone");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfReceivedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedBy");
          final int _cursorIndexOfReceivedByName = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedByName");
          final int _cursorIndexOfTotalItems = CursorUtil.getColumnIndexOrThrow(_cursor, "totalItems");
          final int _cursorIndexOfTotalReceivedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "totalReceivedQty");
          final int _cursorIndexOfTotalAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAmount");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<GrnHeaderEntity> _result = new ArrayList<GrnHeaderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GrnHeaderEntity _item;
            final String _tmpGrnId;
            _tmpGrnId = _cursor.getString(_cursorIndexOfGrnId);
            final String _tmpGrnNumber;
            _tmpGrnNumber = _cursor.getString(_cursorIndexOfGrnNumber);
            final String _tmpPoId;
            _tmpPoId = _cursor.getString(_cursorIndexOfPoId);
            final String _tmpPoNumber;
            _tmpPoNumber = _cursor.getString(_cursorIndexOfPoNumber);
            final String _tmpVendorId;
            _tmpVendorId = _cursor.getString(_cursorIndexOfVendorId);
            final String _tmpVendorName;
            _tmpVendorName = _cursor.getString(_cursorIndexOfVendorName);
            final String _tmpVendorPhone;
            _tmpVendorPhone = _cursor.getString(_cursorIndexOfVendorPhone);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpReceivedBy;
            _tmpReceivedBy = _cursor.getString(_cursorIndexOfReceivedBy);
            final String _tmpReceivedByName;
            _tmpReceivedByName = _cursor.getString(_cursorIndexOfReceivedByName);
            final int _tmpTotalItems;
            _tmpTotalItems = _cursor.getInt(_cursorIndexOfTotalItems);
            final double _tmpTotalReceivedQty;
            _tmpTotalReceivedQty = _cursor.getDouble(_cursorIndexOfTotalReceivedQty);
            final double _tmpTotalAmount;
            _tmpTotalAmount = _cursor.getDouble(_cursorIndexOfTotalAmount);
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
            _item = new GrnHeaderEntity(_tmpGrnId,_tmpGrnNumber,_tmpPoId,_tmpPoNumber,_tmpVendorId,_tmpVendorName,_tmpVendorPhone,_tmpStatus,_tmpNotes,_tmpReceivedBy,_tmpReceivedByName,_tmpTotalItems,_tmpTotalReceivedQty,_tmpTotalAmount,_tmpSyncStatus,_tmpIsDeleted,_tmpDeletedAt,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
