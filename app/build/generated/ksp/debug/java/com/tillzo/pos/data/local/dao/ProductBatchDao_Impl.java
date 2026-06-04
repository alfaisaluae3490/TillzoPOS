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
import com.tillzo.pos.data.local.entity.ProductBatchEntity;
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
public final class ProductBatchDao_Impl implements ProductBatchDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ProductBatchEntity> __insertionAdapterOfProductBatchEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkSynced;

  private final SharedSQLiteStatement __preparedStmtOfSoftDelete;

  private final SharedSQLiteStatement __preparedStmtOfUpdateBatchStock;

  private final SharedSQLiteStatement __preparedStmtOfDeactivateBatch;

  public ProductBatchDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProductBatchEntity = new EntityInsertionAdapter<ProductBatchEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `product_batches` (`batchId`,`productId`,`barcodeId`,`batchNumber`,`manufacturingDate`,`expiryDate`,`stockQty`,`costPrice`,`sellingPrice`,`isActive`,`isDeleted`,`deletedAt`,`syncStatus`,`posTerminalId`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProductBatchEntity entity) {
        statement.bindString(1, entity.getBatchId());
        statement.bindString(2, entity.getProductId());
        statement.bindString(3, entity.getBarcodeId());
        statement.bindString(4, entity.getBatchNumber());
        statement.bindString(5, entity.getManufacturingDate());
        statement.bindString(6, entity.getExpiryDate());
        statement.bindDouble(7, entity.getStockQty());
        statement.bindDouble(8, entity.getCostPrice());
        statement.bindDouble(9, entity.getSellingPrice());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(10, _tmp);
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(11, _tmp_1);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getDeletedAt());
        }
        statement.bindString(13, entity.getSyncStatus());
        statement.bindString(14, entity.getPosTerminalId());
        statement.bindLong(15, entity.getCreatedAt());
        statement.bindLong(16, entity.getUpdatedAt());
      }
    };
    this.__preparedStmtOfMarkSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE product_batches SET syncStatus = 'synced' WHERE batchId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSoftDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE product_batches SET isDeleted = 1, deletedAt = ?, syncStatus = 'pending' WHERE batchId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateBatchStock = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE product_batches SET stockQty = ?, updatedAt = ?, syncStatus = 'pending' WHERE batchId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeactivateBatch = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE product_batches SET isActive = 0, updatedAt = ?, syncStatus = 'pending' WHERE batchId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertBatch(final ProductBatchEntity batch,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfProductBatchEntity.insert(batch);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markSynced(final String batchId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkSynced.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, batchId);
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
  public Object softDelete(final String id, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDelete.acquire();
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
          __preparedStmtOfSoftDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateBatchStock(final String batchId, final double qty, final long time,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateBatchStock.acquire();
        int _argIndex = 1;
        _stmt.bindDouble(_argIndex, qty);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, time);
        _argIndex = 3;
        _stmt.bindString(_argIndex, batchId);
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
          __preparedStmtOfUpdateBatchStock.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deactivateBatch(final String batchId, final long time,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeactivateBatch.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, time);
        _argIndex = 2;
        _stmt.bindString(_argIndex, batchId);
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
          __preparedStmtOfDeactivateBatch.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ProductBatchEntity>> getBatchesForProduct(final String productId) {
    final String _sql = "SELECT * FROM product_batches WHERE productId = ? AND isDeleted = 0 AND isActive = 1 ORDER BY expiryDate ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, productId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"product_batches"}, new Callable<List<ProductBatchEntity>>() {
      @Override
      @NonNull
      public List<ProductBatchEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcodeId");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturingDate");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
          final int _cursorIndexOfStockQty = CursorUtil.getColumnIndexOrThrow(_cursor, "stockQty");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "costPrice");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellingPrice");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ProductBatchEntity> _result = new ArrayList<ProductBatchEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProductBatchEntity _item;
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpBarcodeId;
            _tmpBarcodeId = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpManufacturingDate;
            _tmpManufacturingDate = _cursor.getString(_cursorIndexOfManufacturingDate);
            final String _tmpExpiryDate;
            _tmpExpiryDate = _cursor.getString(_cursorIndexOfExpiryDate);
            final double _tmpStockQty;
            _tmpStockQty = _cursor.getDouble(_cursorIndexOfStockQty);
            final double _tmpCostPrice;
            _tmpCostPrice = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpSellingPrice;
            _tmpSellingPrice = _cursor.getDouble(_cursorIndexOfSellingPrice);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new ProductBatchEntity(_tmpBatchId,_tmpProductId,_tmpBarcodeId,_tmpBatchNumber,_tmpManufacturingDate,_tmpExpiryDate,_tmpStockQty,_tmpCostPrice,_tmpSellingPrice,_tmpIsActive,_tmpIsDeleted,_tmpDeletedAt,_tmpSyncStatus,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getAllBatchesForProduct(final String productId,
      final Continuation<? super List<ProductBatchEntity>> $completion) {
    final String _sql = "SELECT * FROM product_batches WHERE productId = ? AND isDeleted = 0 ORDER BY expiryDate ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, productId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ProductBatchEntity>>() {
      @Override
      @NonNull
      public List<ProductBatchEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcodeId");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturingDate");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
          final int _cursorIndexOfStockQty = CursorUtil.getColumnIndexOrThrow(_cursor, "stockQty");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "costPrice");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellingPrice");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ProductBatchEntity> _result = new ArrayList<ProductBatchEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProductBatchEntity _item;
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpBarcodeId;
            _tmpBarcodeId = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpManufacturingDate;
            _tmpManufacturingDate = _cursor.getString(_cursorIndexOfManufacturingDate);
            final String _tmpExpiryDate;
            _tmpExpiryDate = _cursor.getString(_cursorIndexOfExpiryDate);
            final double _tmpStockQty;
            _tmpStockQty = _cursor.getDouble(_cursorIndexOfStockQty);
            final double _tmpCostPrice;
            _tmpCostPrice = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpSellingPrice;
            _tmpSellingPrice = _cursor.getDouble(_cursorIndexOfSellingPrice);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new ProductBatchEntity(_tmpBatchId,_tmpProductId,_tmpBarcodeId,_tmpBatchNumber,_tmpManufacturingDate,_tmpExpiryDate,_tmpStockQty,_tmpCostPrice,_tmpSellingPrice,_tmpIsActive,_tmpIsDeleted,_tmpDeletedAt,_tmpSyncStatus,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getBatchByBarcode(final String barcodeId,
      final Continuation<? super ProductBatchEntity> $completion) {
    final String _sql = "SELECT * FROM product_batches WHERE barcodeId = ? AND isDeleted = 0 AND isActive = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, barcodeId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProductBatchEntity>() {
      @Override
      @Nullable
      public ProductBatchEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcodeId");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturingDate");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
          final int _cursorIndexOfStockQty = CursorUtil.getColumnIndexOrThrow(_cursor, "stockQty");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "costPrice");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellingPrice");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final ProductBatchEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpBarcodeId;
            _tmpBarcodeId = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpManufacturingDate;
            _tmpManufacturingDate = _cursor.getString(_cursorIndexOfManufacturingDate);
            final String _tmpExpiryDate;
            _tmpExpiryDate = _cursor.getString(_cursorIndexOfExpiryDate);
            final double _tmpStockQty;
            _tmpStockQty = _cursor.getDouble(_cursorIndexOfStockQty);
            final double _tmpCostPrice;
            _tmpCostPrice = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpSellingPrice;
            _tmpSellingPrice = _cursor.getDouble(_cursorIndexOfSellingPrice);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new ProductBatchEntity(_tmpBatchId,_tmpProductId,_tmpBarcodeId,_tmpBatchNumber,_tmpManufacturingDate,_tmpExpiryDate,_tmpStockQty,_tmpCostPrice,_tmpSellingPrice,_tmpIsActive,_tmpIsDeleted,_tmpDeletedAt,_tmpSyncStatus,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getBatchById(final String batchId,
      final Continuation<? super ProductBatchEntity> $completion) {
    final String _sql = "SELECT * FROM product_batches WHERE batchId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, batchId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProductBatchEntity>() {
      @Override
      @Nullable
      public ProductBatchEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcodeId");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturingDate");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
          final int _cursorIndexOfStockQty = CursorUtil.getColumnIndexOrThrow(_cursor, "stockQty");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "costPrice");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellingPrice");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final ProductBatchEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpBarcodeId;
            _tmpBarcodeId = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpManufacturingDate;
            _tmpManufacturingDate = _cursor.getString(_cursorIndexOfManufacturingDate);
            final String _tmpExpiryDate;
            _tmpExpiryDate = _cursor.getString(_cursorIndexOfExpiryDate);
            final double _tmpStockQty;
            _tmpStockQty = _cursor.getDouble(_cursorIndexOfStockQty);
            final double _tmpCostPrice;
            _tmpCostPrice = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpSellingPrice;
            _tmpSellingPrice = _cursor.getDouble(_cursorIndexOfSellingPrice);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new ProductBatchEntity(_tmpBatchId,_tmpProductId,_tmpBarcodeId,_tmpBatchNumber,_tmpManufacturingDate,_tmpExpiryDate,_tmpStockQty,_tmpCostPrice,_tmpSellingPrice,_tmpIsActive,_tmpIsDeleted,_tmpDeletedAt,_tmpSyncStatus,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getPendingBatches(
      final Continuation<? super List<ProductBatchEntity>> $completion) {
    final String _sql = "SELECT * FROM product_batches WHERE syncStatus = 'pending' AND isDeleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ProductBatchEntity>>() {
      @Override
      @NonNull
      public List<ProductBatchEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcodeId");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturingDate");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
          final int _cursorIndexOfStockQty = CursorUtil.getColumnIndexOrThrow(_cursor, "stockQty");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "costPrice");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellingPrice");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ProductBatchEntity> _result = new ArrayList<ProductBatchEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProductBatchEntity _item;
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpBarcodeId;
            _tmpBarcodeId = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpManufacturingDate;
            _tmpManufacturingDate = _cursor.getString(_cursorIndexOfManufacturingDate);
            final String _tmpExpiryDate;
            _tmpExpiryDate = _cursor.getString(_cursorIndexOfExpiryDate);
            final double _tmpStockQty;
            _tmpStockQty = _cursor.getDouble(_cursorIndexOfStockQty);
            final double _tmpCostPrice;
            _tmpCostPrice = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpSellingPrice;
            _tmpSellingPrice = _cursor.getDouble(_cursorIndexOfSellingPrice);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new ProductBatchEntity(_tmpBatchId,_tmpProductId,_tmpBarcodeId,_tmpBatchNumber,_tmpManufacturingDate,_tmpExpiryDate,_tmpStockQty,_tmpCostPrice,_tmpSellingPrice,_tmpIsActive,_tmpIsDeleted,_tmpDeletedAt,_tmpSyncStatus,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getPendingDeletedBatches(
      final Continuation<? super List<ProductBatchEntity>> $completion) {
    final String _sql = "SELECT * FROM product_batches WHERE isDeleted = 1 AND syncStatus = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ProductBatchEntity>>() {
      @Override
      @NonNull
      public List<ProductBatchEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcodeId");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturingDate");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
          final int _cursorIndexOfStockQty = CursorUtil.getColumnIndexOrThrow(_cursor, "stockQty");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "costPrice");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellingPrice");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ProductBatchEntity> _result = new ArrayList<ProductBatchEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProductBatchEntity _item;
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpBarcodeId;
            _tmpBarcodeId = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpManufacturingDate;
            _tmpManufacturingDate = _cursor.getString(_cursorIndexOfManufacturingDate);
            final String _tmpExpiryDate;
            _tmpExpiryDate = _cursor.getString(_cursorIndexOfExpiryDate);
            final double _tmpStockQty;
            _tmpStockQty = _cursor.getDouble(_cursorIndexOfStockQty);
            final double _tmpCostPrice;
            _tmpCostPrice = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpSellingPrice;
            _tmpSellingPrice = _cursor.getDouble(_cursorIndexOfSellingPrice);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new ProductBatchEntity(_tmpBatchId,_tmpProductId,_tmpBarcodeId,_tmpBatchNumber,_tmpManufacturingDate,_tmpExpiryDate,_tmpStockQty,_tmpCostPrice,_tmpSellingPrice,_tmpIsActive,_tmpIsDeleted,_tmpDeletedAt,_tmpSyncStatus,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<List<ProductBatchEntity>> getNearExpiryBatches(final String thresholdDate,
      final String today) {
    final String _sql = "SELECT * FROM product_batches WHERE isDeleted = 0 AND isActive = 1 AND expiryDate <= ? AND expiryDate >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, thresholdDate);
    _argIndex = 2;
    _statement.bindString(_argIndex, today);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"product_batches"}, new Callable<List<ProductBatchEntity>>() {
      @Override
      @NonNull
      public List<ProductBatchEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcodeId");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturingDate");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
          final int _cursorIndexOfStockQty = CursorUtil.getColumnIndexOrThrow(_cursor, "stockQty");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "costPrice");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellingPrice");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ProductBatchEntity> _result = new ArrayList<ProductBatchEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProductBatchEntity _item;
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpBarcodeId;
            _tmpBarcodeId = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpManufacturingDate;
            _tmpManufacturingDate = _cursor.getString(_cursorIndexOfManufacturingDate);
            final String _tmpExpiryDate;
            _tmpExpiryDate = _cursor.getString(_cursorIndexOfExpiryDate);
            final double _tmpStockQty;
            _tmpStockQty = _cursor.getDouble(_cursorIndexOfStockQty);
            final double _tmpCostPrice;
            _tmpCostPrice = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpSellingPrice;
            _tmpSellingPrice = _cursor.getDouble(_cursorIndexOfSellingPrice);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new ProductBatchEntity(_tmpBatchId,_tmpProductId,_tmpBarcodeId,_tmpBatchNumber,_tmpManufacturingDate,_tmpExpiryDate,_tmpStockQty,_tmpCostPrice,_tmpSellingPrice,_tmpIsActive,_tmpIsDeleted,_tmpDeletedAt,_tmpSyncStatus,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<List<ProductBatchEntity>> getExpiredBatches(final String today) {
    final String _sql = "SELECT * FROM product_batches WHERE isDeleted = 0 AND expiryDate < ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, today);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"product_batches"}, new Callable<List<ProductBatchEntity>>() {
      @Override
      @NonNull
      public List<ProductBatchEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcodeId");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturingDate");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
          final int _cursorIndexOfStockQty = CursorUtil.getColumnIndexOrThrow(_cursor, "stockQty");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "costPrice");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellingPrice");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ProductBatchEntity> _result = new ArrayList<ProductBatchEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProductBatchEntity _item;
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpBarcodeId;
            _tmpBarcodeId = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpManufacturingDate;
            _tmpManufacturingDate = _cursor.getString(_cursorIndexOfManufacturingDate);
            final String _tmpExpiryDate;
            _tmpExpiryDate = _cursor.getString(_cursorIndexOfExpiryDate);
            final double _tmpStockQty;
            _tmpStockQty = _cursor.getDouble(_cursorIndexOfStockQty);
            final double _tmpCostPrice;
            _tmpCostPrice = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpSellingPrice;
            _tmpSellingPrice = _cursor.getDouble(_cursorIndexOfSellingPrice);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new ProductBatchEntity(_tmpBatchId,_tmpProductId,_tmpBarcodeId,_tmpBatchNumber,_tmpManufacturingDate,_tmpExpiryDate,_tmpStockQty,_tmpCostPrice,_tmpSellingPrice,_tmpIsActive,_tmpIsDeleted,_tmpDeletedAt,_tmpSyncStatus,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getOldestActiveBatch(final String productId,
      final Continuation<? super ProductBatchEntity> $completion) {
    final String _sql = "SELECT * FROM product_batches WHERE productId = ? AND isActive = 1 AND isDeleted = 0 ORDER BY createdAt ASC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, productId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProductBatchEntity>() {
      @Override
      @Nullable
      public ProductBatchEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfBatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "batchId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcodeId");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batchNumber");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturingDate");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDate");
          final int _cursorIndexOfStockQty = CursorUtil.getColumnIndexOrThrow(_cursor, "stockQty");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "costPrice");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellingPrice");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "posTerminalId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final ProductBatchEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpBatchId;
            _tmpBatchId = _cursor.getString(_cursorIndexOfBatchId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpBarcodeId;
            _tmpBarcodeId = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpBatchNumber;
            _tmpBatchNumber = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpManufacturingDate;
            _tmpManufacturingDate = _cursor.getString(_cursorIndexOfManufacturingDate);
            final String _tmpExpiryDate;
            _tmpExpiryDate = _cursor.getString(_cursorIndexOfExpiryDate);
            final double _tmpStockQty;
            _tmpStockQty = _cursor.getDouble(_cursorIndexOfStockQty);
            final double _tmpCostPrice;
            _tmpCostPrice = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpSellingPrice;
            _tmpSellingPrice = _cursor.getDouble(_cursorIndexOfSellingPrice);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpPosTerminalId;
            _tmpPosTerminalId = _cursor.getString(_cursorIndexOfPosTerminalId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new ProductBatchEntity(_tmpBatchId,_tmpProductId,_tmpBarcodeId,_tmpBatchNumber,_tmpManufacturingDate,_tmpExpiryDate,_tmpStockQty,_tmpCostPrice,_tmpSellingPrice,_tmpIsActive,_tmpIsDeleted,_tmpDeletedAt,_tmpSyncStatus,_tmpPosTerminalId,_tmpCreatedAt,_tmpUpdatedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
