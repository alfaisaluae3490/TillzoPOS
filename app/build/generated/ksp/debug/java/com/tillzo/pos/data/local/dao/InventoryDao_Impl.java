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
import com.tillzo.pos.data.local.entity.InventoryEntity;
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
public final class InventoryDao_Impl implements InventoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<InventoryEntity> __insertionAdapterOfInventoryEntity;

  private final EntityDeletionOrUpdateAdapter<InventoryEntity> __updateAdapterOfInventoryEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePinStatus;

  private final SharedSQLiteStatement __preparedStmtOfSoftDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfMarkSyncedAndDeleted;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTotalStock;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTotalStock_1;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStock;

  public InventoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfInventoryEntity = new EntityInsertionAdapter<InventoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `Inventory` (`system_row_id`,`sync_status`,`created_at`,`updated_at`,`pos_terminal_id`,`item_name`,`category`,`barcode_id`,`unit`,`price_per_unit`,`current_stock`,`low_stock_threshold`,`sku`,`brand`,`description`,`cost_price`,`tax_percent`,`batch_number`,`expiry_date`,`manufacturing_date`,`expiry_alert_days`,`is_damaged_stock`,`damaged_qty`,`totalStock`,`hasBatches`,`isPinned`,`pinnedOrder`,`is_deleted`,`deleted_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InventoryEntity entity) {
        statement.bindString(1, entity.getSystem_row_id());
        statement.bindString(2, entity.getSync_status());
        statement.bindLong(3, entity.getCreated_at());
        statement.bindLong(4, entity.getUpdated_at());
        statement.bindString(5, entity.getPos_terminal_id());
        statement.bindString(6, entity.getItem_name());
        statement.bindString(7, entity.getCategory());
        statement.bindString(8, entity.getBarcode_id());
        statement.bindString(9, entity.getUnit());
        statement.bindDouble(10, entity.getPrice_per_unit());
        statement.bindDouble(11, entity.getCurrent_stock());
        statement.bindDouble(12, entity.getLow_stock_threshold());
        statement.bindString(13, entity.getSku());
        statement.bindString(14, entity.getBrand());
        statement.bindString(15, entity.getDescription());
        statement.bindDouble(16, entity.getCost_price());
        statement.bindDouble(17, entity.getTax_percent());
        statement.bindString(18, entity.getBatch_number());
        statement.bindString(19, entity.getExpiry_date());
        statement.bindString(20, entity.getManufacturing_date());
        statement.bindLong(21, entity.getExpiry_alert_days());
        final int _tmp = entity.is_damaged_stock() ? 1 : 0;
        statement.bindLong(22, _tmp);
        statement.bindDouble(23, entity.getDamaged_qty());
        statement.bindDouble(24, entity.getTotalStock());
        final int _tmp_1 = entity.getHasBatches() ? 1 : 0;
        statement.bindLong(25, _tmp_1);
        final int _tmp_2 = entity.isPinned() ? 1 : 0;
        statement.bindLong(26, _tmp_2);
        statement.bindLong(27, entity.getPinnedOrder());
        final int _tmp_3 = entity.is_deleted() ? 1 : 0;
        statement.bindLong(28, _tmp_3);
        if (entity.getDeleted_at() == null) {
          statement.bindNull(29);
        } else {
          statement.bindLong(29, entity.getDeleted_at());
        }
      }
    };
    this.__updateAdapterOfInventoryEntity = new EntityDeletionOrUpdateAdapter<InventoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `Inventory` SET `system_row_id` = ?,`sync_status` = ?,`created_at` = ?,`updated_at` = ?,`pos_terminal_id` = ?,`item_name` = ?,`category` = ?,`barcode_id` = ?,`unit` = ?,`price_per_unit` = ?,`current_stock` = ?,`low_stock_threshold` = ?,`sku` = ?,`brand` = ?,`description` = ?,`cost_price` = ?,`tax_percent` = ?,`batch_number` = ?,`expiry_date` = ?,`manufacturing_date` = ?,`expiry_alert_days` = ?,`is_damaged_stock` = ?,`damaged_qty` = ?,`totalStock` = ?,`hasBatches` = ?,`isPinned` = ?,`pinnedOrder` = ?,`is_deleted` = ?,`deleted_at` = ? WHERE `system_row_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InventoryEntity entity) {
        statement.bindString(1, entity.getSystem_row_id());
        statement.bindString(2, entity.getSync_status());
        statement.bindLong(3, entity.getCreated_at());
        statement.bindLong(4, entity.getUpdated_at());
        statement.bindString(5, entity.getPos_terminal_id());
        statement.bindString(6, entity.getItem_name());
        statement.bindString(7, entity.getCategory());
        statement.bindString(8, entity.getBarcode_id());
        statement.bindString(9, entity.getUnit());
        statement.bindDouble(10, entity.getPrice_per_unit());
        statement.bindDouble(11, entity.getCurrent_stock());
        statement.bindDouble(12, entity.getLow_stock_threshold());
        statement.bindString(13, entity.getSku());
        statement.bindString(14, entity.getBrand());
        statement.bindString(15, entity.getDescription());
        statement.bindDouble(16, entity.getCost_price());
        statement.bindDouble(17, entity.getTax_percent());
        statement.bindString(18, entity.getBatch_number());
        statement.bindString(19, entity.getExpiry_date());
        statement.bindString(20, entity.getManufacturing_date());
        statement.bindLong(21, entity.getExpiry_alert_days());
        final int _tmp = entity.is_damaged_stock() ? 1 : 0;
        statement.bindLong(22, _tmp);
        statement.bindDouble(23, entity.getDamaged_qty());
        statement.bindDouble(24, entity.getTotalStock());
        final int _tmp_1 = entity.getHasBatches() ? 1 : 0;
        statement.bindLong(25, _tmp_1);
        final int _tmp_2 = entity.isPinned() ? 1 : 0;
        statement.bindLong(26, _tmp_2);
        statement.bindLong(27, entity.getPinnedOrder());
        final int _tmp_3 = entity.is_deleted() ? 1 : 0;
        statement.bindLong(28, _tmp_3);
        if (entity.getDeleted_at() == null) {
          statement.bindNull(29);
        } else {
          statement.bindLong(29, entity.getDeleted_at());
        }
        statement.bindString(30, entity.getSystem_row_id());
      }
    };
    this.__preparedStmtOfUpdatePinStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE Inventory SET isPinned = ?, pinnedOrder = ?, sync_status = 'pending' WHERE system_row_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSoftDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE Inventory SET is_deleted = 1, deleted_at = ?, sync_status = 'pending' WHERE system_row_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkSyncedAndDeleted = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE Inventory SET sync_status = 'synced' WHERE system_row_id = ? AND is_deleted = 1";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateTotalStock = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE Inventory SET current_stock = ? WHERE system_row_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateTotalStock_1 = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE Inventory SET totalStock = ?, updated_at = ? WHERE system_row_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateStock = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE Inventory SET current_stock = ? WHERE system_row_id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertItem(final InventoryEntity item,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfInventoryEntity.insert(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateItem(final InventoryEntity item,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfInventoryEntity.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePinStatus(final String id, final boolean pinned, final int order,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePinStatus.acquire();
        int _argIndex = 1;
        final int _tmp = pinned ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, order);
        _argIndex = 3;
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
          __preparedStmtOfUpdatePinStatus.release(_stmt);
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
  public Object deleteItemById(final String id, final long timestamp,
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
  public Object updateTotalStock(final String productId, final double total,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTotalStock.acquire();
        int _argIndex = 1;
        _stmt.bindDouble(_argIndex, total);
        _argIndex = 2;
        _stmt.bindString(_argIndex, productId);
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
          __preparedStmtOfUpdateTotalStock.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTotalStock(final String id, final double total, final long time,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTotalStock_1.acquire();
        int _argIndex = 1;
        _stmt.bindDouble(_argIndex, total);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, time);
        _argIndex = 3;
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
          __preparedStmtOfUpdateTotalStock_1.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStock(final String id, final double newStock,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStock.acquire();
        int _argIndex = 1;
        _stmt.bindDouble(_argIndex, newStock);
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
          __preparedStmtOfUpdateStock.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getItemById(final String id,
      final Continuation<? super InventoryEntity> $completion) {
    final String _sql = "SELECT * FROM Inventory WHERE system_row_id = ? AND is_deleted = 0 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<InventoryEntity>() {
      @Override
      @Nullable
      public InventoryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final InventoryEntity _result;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _result = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getItemByBarcode(final String barcode,
      final Continuation<? super InventoryEntity> $completion) {
    final String _sql = "SELECT * FROM Inventory WHERE barcode_id = ? AND is_deleted = 0 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, barcode);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<InventoryEntity>() {
      @Override
      @Nullable
      public InventoryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final InventoryEntity _result;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _result = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
  public Flow<List<InventoryEntity>> getAllItems() {
    final String _sql = "SELECT * FROM Inventory WHERE is_deleted = 0 ORDER BY item_name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Inventory"}, new Callable<List<InventoryEntity>>() {
      @Override
      @NonNull
      public List<InventoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<InventoryEntity> _result = new ArrayList<InventoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventoryEntity _item;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
  public Flow<List<InventoryEntity>> getPinnedItems() {
    final String _sql = "SELECT * FROM Inventory WHERE isPinned = 1 AND is_deleted = 0 ORDER BY pinnedOrder ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Inventory"}, new Callable<List<InventoryEntity>>() {
      @Override
      @NonNull
      public List<InventoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<InventoryEntity> _result = new ArrayList<InventoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventoryEntity _item;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getPinnedItemsOnce(final Continuation<? super List<InventoryEntity>> $completion) {
    final String _sql = "SELECT * FROM Inventory WHERE isPinned = 1 AND is_deleted = 0 ORDER BY pinnedOrder ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<InventoryEntity>>() {
      @Override
      @NonNull
      public List<InventoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<InventoryEntity> _result = new ArrayList<InventoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventoryEntity _item;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getPendingItems(final Continuation<? super List<InventoryEntity>> $completion) {
    final String _sql = "SELECT * FROM Inventory WHERE sync_status = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<InventoryEntity>>() {
      @Override
      @NonNull
      public List<InventoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<InventoryEntity> _result = new ArrayList<InventoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventoryEntity _item;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
  public Flow<List<InventoryEntity>> getLowStockItems() {
    final String _sql = "SELECT * FROM Inventory WHERE current_stock <= low_stock_threshold AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Inventory"}, new Callable<List<InventoryEntity>>() {
      @Override
      @NonNull
      public List<InventoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<InventoryEntity> _result = new ArrayList<InventoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventoryEntity _item;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getLowStockItemsAsList(
      final Continuation<? super List<InventoryEntity>> $completion) {
    final String _sql = "SELECT * FROM Inventory WHERE current_stock <= low_stock_threshold AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<InventoryEntity>>() {
      @Override
      @NonNull
      public List<InventoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<InventoryEntity> _result = new ArrayList<InventoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventoryEntity _item;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
  public Flow<List<InventoryEntity>> getOutOfStockItems() {
    final String _sql = "SELECT * FROM Inventory WHERE current_stock <= 0 AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Inventory"}, new Callable<List<InventoryEntity>>() {
      @Override
      @NonNull
      public List<InventoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<InventoryEntity> _result = new ArrayList<InventoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventoryEntity _item;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
  public Flow<List<InventoryEntity>> getNearExpiryItems(final String thresholdDate) {
    final String _sql = "SELECT * FROM Inventory WHERE expiry_date != '' AND expiry_date <= ? AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, thresholdDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Inventory"}, new Callable<List<InventoryEntity>>() {
      @Override
      @NonNull
      public List<InventoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<InventoryEntity> _result = new ArrayList<InventoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventoryEntity _item;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
  public Flow<List<InventoryEntity>> getExpiredItems(final String todayDate) {
    final String _sql = "SELECT * FROM Inventory WHERE expiry_date != '' AND expiry_date < ? AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, todayDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Inventory"}, new Callable<List<InventoryEntity>>() {
      @Override
      @NonNull
      public List<InventoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<InventoryEntity> _result = new ArrayList<InventoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventoryEntity _item;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
  public Flow<List<InventoryEntity>> getDamagedItems() {
    final String _sql = "SELECT * FROM Inventory WHERE is_damaged_stock = 1 AND is_deleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Inventory"}, new Callable<List<InventoryEntity>>() {
      @Override
      @NonNull
      public List<InventoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<InventoryEntity> _result = new ArrayList<InventoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventoryEntity _item;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
  public Flow<List<InventoryEntity>> searchItems(final String query) {
    final String _sql = "SELECT * FROM Inventory WHERE (item_name LIKE '%' || ? || '%' OR barcode_id LIKE '%' || ? || '%' OR sku LIKE '%' || ? || '%') AND is_deleted = 0 ORDER BY item_name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    _argIndex = 3;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Inventory"}, new Callable<List<InventoryEntity>>() {
      @Override
      @NonNull
      public List<InventoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<InventoryEntity> _result = new ArrayList<InventoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventoryEntity _item;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
      final Continuation<? super List<InventoryEntity>> $completion) {
    final String _sql = "SELECT * FROM Inventory WHERE is_deleted = 1 AND sync_status = 'pending'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<InventoryEntity>>() {
      @Override
      @NonNull
      public List<InventoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<InventoryEntity> _result = new ArrayList<InventoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventoryEntity _item;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getExpiredItemsList(final String today,
      final Continuation<? super List<InventoryEntity>> $completion) {
    final String _sql = "SELECT * FROM Inventory WHERE is_deleted = 0 AND current_stock > 0 AND expiry_date != '' AND expiry_date < ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, today);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<InventoryEntity>>() {
      @Override
      @NonNull
      public List<InventoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<InventoryEntity> _result = new ArrayList<InventoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventoryEntity _item;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
  public Object getNearExpiryItemsList(final String today, final String thresholdDate,
      final Continuation<? super List<InventoryEntity>> $completion) {
    final String _sql = "SELECT * FROM Inventory WHERE is_deleted = 0 AND expiry_date != '' AND expiry_date >= ? AND expiry_date <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, today);
    _argIndex = 2;
    _statement.bindString(_argIndex, thresholdDate);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<InventoryEntity>>() {
      @Override
      @NonNull
      public List<InventoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSystemRowId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_row_id");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfPosTerminalId = CursorUtil.getColumnIndexOrThrow(_cursor, "pos_terminal_id");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "item_name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBarcodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode_id");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPricePerUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "price_per_unit");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfLowStockThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "low_stock_threshold");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCostPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_price");
          final int _cursorIndexOfTaxPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "tax_percent");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_date");
          final int _cursorIndexOfManufacturingDate = CursorUtil.getColumnIndexOrThrow(_cursor, "manufacturing_date");
          final int _cursorIndexOfExpiryAlertDays = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_alert_days");
          final int _cursorIndexOfIsDamagedStock = CursorUtil.getColumnIndexOrThrow(_cursor, "is_damaged_stock");
          final int _cursorIndexOfDamagedQty = CursorUtil.getColumnIndexOrThrow(_cursor, "damaged_qty");
          final int _cursorIndexOfTotalStock = CursorUtil.getColumnIndexOrThrow(_cursor, "totalStock");
          final int _cursorIndexOfHasBatches = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBatches");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfPinnedOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_deleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<InventoryEntity> _result = new ArrayList<InventoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventoryEntity _item;
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
            final String _tmpItem_name;
            _tmpItem_name = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpBarcode_id;
            _tmpBarcode_id = _cursor.getString(_cursorIndexOfBarcodeId);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPrice_per_unit;
            _tmpPrice_per_unit = _cursor.getDouble(_cursorIndexOfPricePerUnit);
            final double _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getDouble(_cursorIndexOfCurrentStock);
            final double _tmpLow_stock_threshold;
            _tmpLow_stock_threshold = _cursor.getDouble(_cursorIndexOfLowStockThreshold);
            final String _tmpSku;
            _tmpSku = _cursor.getString(_cursorIndexOfSku);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpCost_price;
            _tmpCost_price = _cursor.getDouble(_cursorIndexOfCostPrice);
            final double _tmpTax_percent;
            _tmpTax_percent = _cursor.getDouble(_cursorIndexOfTaxPercent);
            final String _tmpBatch_number;
            _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            final String _tmpExpiry_date;
            _tmpExpiry_date = _cursor.getString(_cursorIndexOfExpiryDate);
            final String _tmpManufacturing_date;
            _tmpManufacturing_date = _cursor.getString(_cursorIndexOfManufacturingDate);
            final int _tmpExpiry_alert_days;
            _tmpExpiry_alert_days = _cursor.getInt(_cursorIndexOfExpiryAlertDays);
            final boolean _tmpIs_damaged_stock;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDamagedStock);
            _tmpIs_damaged_stock = _tmp != 0;
            final double _tmpDamaged_qty;
            _tmpDamaged_qty = _cursor.getDouble(_cursorIndexOfDamagedQty);
            final double _tmpTotalStock;
            _tmpTotalStock = _cursor.getDouble(_cursorIndexOfTotalStock);
            final boolean _tmpHasBatches;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasBatches);
            _tmpHasBatches = _tmp_1 != 0;
            final boolean _tmpIsPinned;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_2 != 0;
            final int _tmpPinnedOrder;
            _tmpPinnedOrder = _cursor.getInt(_cursorIndexOfPinnedOrder);
            final boolean _tmpIs_deleted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIs_deleted = _tmp_3 != 0;
            final Long _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            _item = new InventoryEntity(_tmpSystem_row_id,_tmpSync_status,_tmpCreated_at,_tmpUpdated_at,_tmpPos_terminal_id,_tmpItem_name,_tmpCategory,_tmpBarcode_id,_tmpUnit,_tmpPrice_per_unit,_tmpCurrent_stock,_tmpLow_stock_threshold,_tmpSku,_tmpBrand,_tmpDescription,_tmpCost_price,_tmpTax_percent,_tmpBatch_number,_tmpExpiry_date,_tmpManufacturing_date,_tmpExpiry_alert_days,_tmpIs_damaged_stock,_tmpDamaged_qty,_tmpTotalStock,_tmpHasBatches,_tmpIsPinned,_tmpPinnedOrder,_tmpIs_deleted,_tmpDeleted_at);
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
