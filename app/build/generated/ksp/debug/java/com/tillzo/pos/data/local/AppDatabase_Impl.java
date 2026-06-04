package com.tillzo.pos.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.tillzo.pos.data.local.dao.CategoryDao;
import com.tillzo.pos.data.local.dao.CategoryDao_Impl;
import com.tillzo.pos.data.local.dao.CustomerDao;
import com.tillzo.pos.data.local.dao.CustomerDao_Impl;
import com.tillzo.pos.data.local.dao.ExpenseDao;
import com.tillzo.pos.data.local.dao.ExpenseDao_Impl;
import com.tillzo.pos.data.local.dao.GrnDao;
import com.tillzo.pos.data.local.dao.GrnDao_Impl;
import com.tillzo.pos.data.local.dao.InventoryDao;
import com.tillzo.pos.data.local.dao.InventoryDao_Impl;
import com.tillzo.pos.data.local.dao.KhataEventDao;
import com.tillzo.pos.data.local.dao.KhataEventDao_Impl;
import com.tillzo.pos.data.local.dao.ProductBatchDao;
import com.tillzo.pos.data.local.dao.ProductBatchDao_Impl;
import com.tillzo.pos.data.local.dao.ProductUnitDao;
import com.tillzo.pos.data.local.dao.ProductUnitDao_Impl;
import com.tillzo.pos.data.local.dao.PurchaseOrderDao;
import com.tillzo.pos.data.local.dao.PurchaseOrderDao_Impl;
import com.tillzo.pos.data.local.dao.SaleDao;
import com.tillzo.pos.data.local.dao.SaleDao_Impl;
import com.tillzo.pos.data.local.dao.StockAdjustmentDao;
import com.tillzo.pos.data.local.dao.StockAdjustmentDao_Impl;
import com.tillzo.pos.data.local.dao.SyncLogDao;
import com.tillzo.pos.data.local.dao.SyncLogDao_Impl;
import com.tillzo.pos.data.local.dao.TillSessionDao;
import com.tillzo.pos.data.local.dao.TillSessionDao_Impl;
import com.tillzo.pos.data.local.dao.UserDao;
import com.tillzo.pos.data.local.dao.UserDao_Impl;
import com.tillzo.pos.data.local.dao.VendorDao;
import com.tillzo.pos.data.local.dao.VendorDao_Impl;
import com.tillzo.pos.data.local.dao.WastageDao;
import com.tillzo.pos.data.local.dao.WastageDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile SyncLogDao _syncLogDao;

  private volatile UserDao _userDao;

  private volatile SaleDao _saleDao;

  private volatile InventoryDao _inventoryDao;

  private volatile CustomerDao _customerDao;

  private volatile KhataEventDao _khataEventDao;

  private volatile ExpenseDao _expenseDao;

  private volatile StockAdjustmentDao _stockAdjustmentDao;

  private volatile CategoryDao _categoryDao;

  private volatile ProductBatchDao _productBatchDao;

  private volatile PurchaseOrderDao _purchaseOrderDao;

  private volatile VendorDao _vendorDao;

  private volatile GrnDao _grnDao;

  private volatile ProductUnitDao _productUnitDao;

  private volatile TillSessionDao _tillSessionDao;

  private volatile WastageDao _wastageDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(17) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `sync_log` (`table_name` TEXT NOT NULL, `last_synced_at` INTEGER NOT NULL, `last_sync_status` TEXT NOT NULL, PRIMARY KEY(`table_name`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `Users_Permissions` (`system_row_id` TEXT NOT NULL, `sync_status` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `pos_terminal_id` TEXT NOT NULL, `email` TEXT NOT NULL, `name` TEXT NOT NULL, `role` TEXT NOT NULL, `password_hash` TEXT, `permissions_json` TEXT, `is_deleted` INTEGER NOT NULL, `deleted_at` INTEGER, PRIMARY KEY(`system_row_id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `Sales` (`system_row_id` TEXT NOT NULL, `sync_status` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `pos_terminal_id` TEXT NOT NULL, `sync_uuid` TEXT NOT NULL, `cashier_id` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `items_json` TEXT NOT NULL, `subtotal` REAL NOT NULL, `tax` REAL NOT NULL, `discount` REAL NOT NULL, `total` REAL NOT NULL, `payment_method` TEXT NOT NULL, `cash_amount` REAL NOT NULL, `card_amount` REAL NOT NULL, `wallet_amount` REAL NOT NULL, `udhaar_amount` REAL NOT NULL, `customer_id` TEXT, `payment_split_json` TEXT, `reference_id` TEXT, `is_deleted` INTEGER NOT NULL, `deleted_at` INTEGER, PRIMARY KEY(`system_row_id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `Inventory` (`system_row_id` TEXT NOT NULL, `sync_status` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `pos_terminal_id` TEXT NOT NULL, `item_name` TEXT NOT NULL, `category` TEXT NOT NULL, `barcode_id` TEXT NOT NULL, `unit` TEXT NOT NULL, `price_per_unit` REAL NOT NULL, `current_stock` REAL NOT NULL, `low_stock_threshold` REAL NOT NULL, `sku` TEXT NOT NULL, `brand` TEXT NOT NULL, `description` TEXT NOT NULL, `cost_price` REAL NOT NULL, `tax_percent` REAL NOT NULL, `batch_number` TEXT NOT NULL, `expiry_date` TEXT NOT NULL, `manufacturing_date` TEXT NOT NULL, `expiry_alert_days` INTEGER NOT NULL, `is_damaged_stock` INTEGER NOT NULL, `damaged_qty` REAL NOT NULL, `totalStock` REAL NOT NULL, `hasBatches` INTEGER NOT NULL, `isPinned` INTEGER NOT NULL, `pinnedOrder` INTEGER NOT NULL, `is_deleted` INTEGER NOT NULL, `deleted_at` INTEGER, PRIMARY KEY(`system_row_id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `Customers` (`system_row_id` TEXT NOT NULL, `sync_status` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `pos_terminal_id` TEXT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT NOT NULL, `whatsapp` TEXT, `email` TEXT, `address` TEXT, `is_deleted` INTEGER NOT NULL, `deleted_at` INTEGER, PRIMARY KEY(`system_row_id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `KhataEvents` (`system_row_id` TEXT NOT NULL, `sync_status` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `pos_terminal_id` TEXT NOT NULL, `customer_id` TEXT NOT NULL, `event_type` TEXT NOT NULL, `amount` REAL NOT NULL, `note` TEXT, `reference_sale_id` TEXT, `is_deleted` INTEGER NOT NULL, `deleted_at` INTEGER, PRIMARY KEY(`system_row_id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `Expenses` (`system_row_id` TEXT NOT NULL, `sync_status` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `pos_terminal_id` TEXT NOT NULL, `category` TEXT NOT NULL, `amount` REAL NOT NULL, `description` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `logged_by_user_id` TEXT NOT NULL, `is_deleted` INTEGER NOT NULL, `deleted_at` INTEGER, PRIMARY KEY(`system_row_id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `StockAdjustments` (`adjustmentId` TEXT NOT NULL, `productId` TEXT NOT NULL, `adjustmentType` TEXT NOT NULL, `quantityChanged` REAL NOT NULL, `reason` TEXT NOT NULL, `adjustedBy` TEXT NOT NULL, `syncStatus` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`adjustmentId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `Categories` (`system_row_id` TEXT NOT NULL, `sync_status` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `pos_terminal_id` TEXT NOT NULL, `category_name` TEXT NOT NULL, `is_deleted` INTEGER NOT NULL, `deleted_at` INTEGER, PRIMARY KEY(`system_row_id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `product_batches` (`batchId` TEXT NOT NULL, `productId` TEXT NOT NULL, `barcodeId` TEXT NOT NULL, `batchNumber` TEXT NOT NULL, `manufacturingDate` TEXT NOT NULL, `expiryDate` TEXT NOT NULL, `stockQty` REAL NOT NULL, `costPrice` REAL NOT NULL, `sellingPrice` REAL NOT NULL, `isActive` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `syncStatus` TEXT NOT NULL, `posTerminalId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`batchId`), FOREIGN KEY(`productId`) REFERENCES `Inventory`(`system_row_id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_product_batches_productId` ON `product_batches` (`productId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `purchase_orders` (`poId` TEXT NOT NULL, `poNumber` TEXT NOT NULL, `vendorId` TEXT NOT NULL, `vendorName` TEXT NOT NULL, `status` TEXT NOT NULL, `notes` TEXT NOT NULL, `totalAmount` REAL NOT NULL, `currency` TEXT NOT NULL, `expectedDeliveryDate` TEXT NOT NULL, `createdBy` TEXT NOT NULL, `syncStatus` TEXT NOT NULL, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `posTerminalId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`poId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `purchase_order_items` (`poItemId` TEXT NOT NULL, `poId` TEXT NOT NULL, `productId` TEXT NOT NULL, `productName` TEXT NOT NULL, `sku` TEXT NOT NULL, `barcodeId` TEXT NOT NULL, `orderedQty` REAL NOT NULL, `receivedQty` REAL NOT NULL, `unitCostPrice` REAL NOT NULL, `totalCost` REAL NOT NULL, `unit` TEXT NOT NULL, `syncStatus` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`poItemId`), FOREIGN KEY(`poId`) REFERENCES `purchase_orders`(`poId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_purchase_order_items_poId` ON `purchase_order_items` (`poId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `vendors` (`vendorId` TEXT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT NOT NULL, `whatsapp` TEXT NOT NULL, `email` TEXT NOT NULL, `address` TEXT NOT NULL, `isDeleted` INTEGER NOT NULL, `syncStatus` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`vendorId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `grn_headers` (`grnId` TEXT NOT NULL, `grnNumber` TEXT NOT NULL, `poId` TEXT NOT NULL, `poNumber` TEXT NOT NULL, `vendorId` TEXT NOT NULL, `vendorName` TEXT NOT NULL, `vendorPhone` TEXT NOT NULL, `status` TEXT NOT NULL, `notes` TEXT NOT NULL, `receivedBy` TEXT NOT NULL, `receivedByName` TEXT NOT NULL, `totalItems` INTEGER NOT NULL, `totalReceivedQty` REAL NOT NULL, `totalAmount` REAL NOT NULL, `syncStatus` TEXT NOT NULL, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `posTerminalId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`grnId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `grn_items` (`grnItemId` TEXT NOT NULL, `grnId` TEXT NOT NULL, `poItemId` TEXT NOT NULL, `productId` TEXT NOT NULL, `batchId` TEXT NOT NULL, `productName` TEXT NOT NULL, `barcodeId` TEXT NOT NULL, `sku` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `brand` TEXT NOT NULL, `orderedQty` REAL NOT NULL, `receivedQty` REAL NOT NULL, `unitCostPrice` REAL NOT NULL, `sellingPrice` REAL NOT NULL, `totalCost` REAL NOT NULL, `unit` TEXT NOT NULL, `batchNumber` TEXT NOT NULL, `manufacturingDate` TEXT NOT NULL, `expiryDate` TEXT NOT NULL, `inventoryAction` TEXT NOT NULL, `isNewProduct` INTEGER NOT NULL, `lowStockThreshold` REAL NOT NULL, `syncStatus` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`grnItemId`), FOREIGN KEY(`grnId`) REFERENCES `grn_headers`(`grnId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_grn_items_grnId` ON `grn_items` (`grnId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_grn_items_productId` ON `grn_items` (`productId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `product_units` (`unitId` TEXT NOT NULL, `unitName` TEXT NOT NULL, `abbreviation` TEXT NOT NULL, `isDeleted` INTEGER NOT NULL, `syncStatus` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`unitId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `till_sessions` (`sessionId` TEXT NOT NULL, `cashierId` TEXT NOT NULL, `cashierName` TEXT NOT NULL, `posTerminalId` TEXT NOT NULL, `openingCash` REAL NOT NULL, `closingCash` REAL NOT NULL, `expectedCash` REAL NOT NULL, `totalCashSales` REAL NOT NULL, `totalCardSales` REAL NOT NULL, `totalWalletSales` REAL NOT NULL, `totalUdhaarSales` REAL NOT NULL, `totalSplitSales` REAL NOT NULL, `totalSalesCount` INTEGER NOT NULL, `totalRefunds` REAL NOT NULL, `netCash` REAL NOT NULL, `status` TEXT NOT NULL, `notes` TEXT NOT NULL, `shiftDate` TEXT NOT NULL, `openedAt` INTEGER NOT NULL, `closedAt` INTEGER, `syncStatus` TEXT NOT NULL, `posId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`sessionId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `wastage_log` (`wastageId` TEXT NOT NULL, `productId` TEXT NOT NULL, `productName` TEXT NOT NULL, `batchId` TEXT NOT NULL, `batchNumber` TEXT NOT NULL, `quantity` REAL NOT NULL, `unit` TEXT NOT NULL, `costPrice` REAL NOT NULL, `totalLoss` REAL NOT NULL, `reason` TEXT NOT NULL, `notes` TEXT NOT NULL, `loggedBy` TEXT NOT NULL, `wastageDate` TEXT NOT NULL, `syncStatus` TEXT NOT NULL, `posTerminalId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`wastageId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '18ff3181230c0f8be76104a39f1c5991')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `sync_log`");
        db.execSQL("DROP TABLE IF EXISTS `Users_Permissions`");
        db.execSQL("DROP TABLE IF EXISTS `Sales`");
        db.execSQL("DROP TABLE IF EXISTS `Inventory`");
        db.execSQL("DROP TABLE IF EXISTS `Customers`");
        db.execSQL("DROP TABLE IF EXISTS `KhataEvents`");
        db.execSQL("DROP TABLE IF EXISTS `Expenses`");
        db.execSQL("DROP TABLE IF EXISTS `StockAdjustments`");
        db.execSQL("DROP TABLE IF EXISTS `Categories`");
        db.execSQL("DROP TABLE IF EXISTS `product_batches`");
        db.execSQL("DROP TABLE IF EXISTS `purchase_orders`");
        db.execSQL("DROP TABLE IF EXISTS `purchase_order_items`");
        db.execSQL("DROP TABLE IF EXISTS `vendors`");
        db.execSQL("DROP TABLE IF EXISTS `grn_headers`");
        db.execSQL("DROP TABLE IF EXISTS `grn_items`");
        db.execSQL("DROP TABLE IF EXISTS `product_units`");
        db.execSQL("DROP TABLE IF EXISTS `till_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `wastage_log`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsSyncLog = new HashMap<String, TableInfo.Column>(3);
        _columnsSyncLog.put("table_name", new TableInfo.Column("table_name", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLog.put("last_synced_at", new TableInfo.Column("last_synced_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncLog.put("last_sync_status", new TableInfo.Column("last_sync_status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSyncLog = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSyncLog = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSyncLog = new TableInfo("sync_log", _columnsSyncLog, _foreignKeysSyncLog, _indicesSyncLog);
        final TableInfo _existingSyncLog = TableInfo.read(db, "sync_log");
        if (!_infoSyncLog.equals(_existingSyncLog)) {
          return new RoomOpenHelper.ValidationResult(false, "sync_log(com.tillzo.pos.data.local.SyncLogEntity).\n"
                  + " Expected:\n" + _infoSyncLog + "\n"
                  + " Found:\n" + _existingSyncLog);
        }
        final HashMap<String, TableInfo.Column> _columnsUsersPermissions = new HashMap<String, TableInfo.Column>(12);
        _columnsUsersPermissions.put("system_row_id", new TableInfo.Column("system_row_id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsersPermissions.put("sync_status", new TableInfo.Column("sync_status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsersPermissions.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsersPermissions.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsersPermissions.put("pos_terminal_id", new TableInfo.Column("pos_terminal_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsersPermissions.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsersPermissions.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsersPermissions.put("role", new TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsersPermissions.put("password_hash", new TableInfo.Column("password_hash", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsersPermissions.put("permissions_json", new TableInfo.Column("permissions_json", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsersPermissions.put("is_deleted", new TableInfo.Column("is_deleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsersPermissions.put("deleted_at", new TableInfo.Column("deleted_at", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsersPermissions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsersPermissions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUsersPermissions = new TableInfo("Users_Permissions", _columnsUsersPermissions, _foreignKeysUsersPermissions, _indicesUsersPermissions);
        final TableInfo _existingUsersPermissions = TableInfo.read(db, "Users_Permissions");
        if (!_infoUsersPermissions.equals(_existingUsersPermissions)) {
          return new RoomOpenHelper.ValidationResult(false, "Users_Permissions(com.tillzo.pos.data.local.entity.UserEntity).\n"
                  + " Expected:\n" + _infoUsersPermissions + "\n"
                  + " Found:\n" + _existingUsersPermissions);
        }
        final HashMap<String, TableInfo.Column> _columnsSales = new HashMap<String, TableInfo.Column>(23);
        _columnsSales.put("system_row_id", new TableInfo.Column("system_row_id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("sync_status", new TableInfo.Column("sync_status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("pos_terminal_id", new TableInfo.Column("pos_terminal_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("sync_uuid", new TableInfo.Column("sync_uuid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("cashier_id", new TableInfo.Column("cashier_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("items_json", new TableInfo.Column("items_json", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("subtotal", new TableInfo.Column("subtotal", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("tax", new TableInfo.Column("tax", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("discount", new TableInfo.Column("discount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("total", new TableInfo.Column("total", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("payment_method", new TableInfo.Column("payment_method", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("cash_amount", new TableInfo.Column("cash_amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("card_amount", new TableInfo.Column("card_amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("wallet_amount", new TableInfo.Column("wallet_amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("udhaar_amount", new TableInfo.Column("udhaar_amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("customer_id", new TableInfo.Column("customer_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("payment_split_json", new TableInfo.Column("payment_split_json", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("reference_id", new TableInfo.Column("reference_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("is_deleted", new TableInfo.Column("is_deleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("deleted_at", new TableInfo.Column("deleted_at", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSales = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSales = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSales = new TableInfo("Sales", _columnsSales, _foreignKeysSales, _indicesSales);
        final TableInfo _existingSales = TableInfo.read(db, "Sales");
        if (!_infoSales.equals(_existingSales)) {
          return new RoomOpenHelper.ValidationResult(false, "Sales(com.tillzo.pos.data.local.entity.SaleEntity).\n"
                  + " Expected:\n" + _infoSales + "\n"
                  + " Found:\n" + _existingSales);
        }
        final HashMap<String, TableInfo.Column> _columnsInventory = new HashMap<String, TableInfo.Column>(29);
        _columnsInventory.put("system_row_id", new TableInfo.Column("system_row_id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("sync_status", new TableInfo.Column("sync_status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("pos_terminal_id", new TableInfo.Column("pos_terminal_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("item_name", new TableInfo.Column("item_name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("barcode_id", new TableInfo.Column("barcode_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("unit", new TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("price_per_unit", new TableInfo.Column("price_per_unit", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("current_stock", new TableInfo.Column("current_stock", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("low_stock_threshold", new TableInfo.Column("low_stock_threshold", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("sku", new TableInfo.Column("sku", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("brand", new TableInfo.Column("brand", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("cost_price", new TableInfo.Column("cost_price", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("tax_percent", new TableInfo.Column("tax_percent", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("batch_number", new TableInfo.Column("batch_number", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("expiry_date", new TableInfo.Column("expiry_date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("manufacturing_date", new TableInfo.Column("manufacturing_date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("expiry_alert_days", new TableInfo.Column("expiry_alert_days", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("is_damaged_stock", new TableInfo.Column("is_damaged_stock", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("damaged_qty", new TableInfo.Column("damaged_qty", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("totalStock", new TableInfo.Column("totalStock", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("hasBatches", new TableInfo.Column("hasBatches", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("isPinned", new TableInfo.Column("isPinned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("pinnedOrder", new TableInfo.Column("pinnedOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("is_deleted", new TableInfo.Column("is_deleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventory.put("deleted_at", new TableInfo.Column("deleted_at", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInventory = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesInventory = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoInventory = new TableInfo("Inventory", _columnsInventory, _foreignKeysInventory, _indicesInventory);
        final TableInfo _existingInventory = TableInfo.read(db, "Inventory");
        if (!_infoInventory.equals(_existingInventory)) {
          return new RoomOpenHelper.ValidationResult(false, "Inventory(com.tillzo.pos.data.local.entity.InventoryEntity).\n"
                  + " Expected:\n" + _infoInventory + "\n"
                  + " Found:\n" + _existingInventory);
        }
        final HashMap<String, TableInfo.Column> _columnsCustomers = new HashMap<String, TableInfo.Column>(12);
        _columnsCustomers.put("system_row_id", new TableInfo.Column("system_row_id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("sync_status", new TableInfo.Column("sync_status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("pos_terminal_id", new TableInfo.Column("pos_terminal_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("phone", new TableInfo.Column("phone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("whatsapp", new TableInfo.Column("whatsapp", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("email", new TableInfo.Column("email", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("address", new TableInfo.Column("address", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("is_deleted", new TableInfo.Column("is_deleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("deleted_at", new TableInfo.Column("deleted_at", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCustomers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCustomers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCustomers = new TableInfo("Customers", _columnsCustomers, _foreignKeysCustomers, _indicesCustomers);
        final TableInfo _existingCustomers = TableInfo.read(db, "Customers");
        if (!_infoCustomers.equals(_existingCustomers)) {
          return new RoomOpenHelper.ValidationResult(false, "Customers(com.tillzo.pos.data.local.entity.CustomerEntity).\n"
                  + " Expected:\n" + _infoCustomers + "\n"
                  + " Found:\n" + _existingCustomers);
        }
        final HashMap<String, TableInfo.Column> _columnsKhataEvents = new HashMap<String, TableInfo.Column>(12);
        _columnsKhataEvents.put("system_row_id", new TableInfo.Column("system_row_id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKhataEvents.put("sync_status", new TableInfo.Column("sync_status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKhataEvents.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKhataEvents.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKhataEvents.put("pos_terminal_id", new TableInfo.Column("pos_terminal_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKhataEvents.put("customer_id", new TableInfo.Column("customer_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKhataEvents.put("event_type", new TableInfo.Column("event_type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKhataEvents.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKhataEvents.put("note", new TableInfo.Column("note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKhataEvents.put("reference_sale_id", new TableInfo.Column("reference_sale_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKhataEvents.put("is_deleted", new TableInfo.Column("is_deleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKhataEvents.put("deleted_at", new TableInfo.Column("deleted_at", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysKhataEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesKhataEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoKhataEvents = new TableInfo("KhataEvents", _columnsKhataEvents, _foreignKeysKhataEvents, _indicesKhataEvents);
        final TableInfo _existingKhataEvents = TableInfo.read(db, "KhataEvents");
        if (!_infoKhataEvents.equals(_existingKhataEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "KhataEvents(com.tillzo.pos.data.local.entity.KhataEventEntity).\n"
                  + " Expected:\n" + _infoKhataEvents + "\n"
                  + " Found:\n" + _existingKhataEvents);
        }
        final HashMap<String, TableInfo.Column> _columnsExpenses = new HashMap<String, TableInfo.Column>(12);
        _columnsExpenses.put("system_row_id", new TableInfo.Column("system_row_id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("sync_status", new TableInfo.Column("sync_status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("pos_terminal_id", new TableInfo.Column("pos_terminal_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("logged_by_user_id", new TableInfo.Column("logged_by_user_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("is_deleted", new TableInfo.Column("is_deleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("deleted_at", new TableInfo.Column("deleted_at", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExpenses = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesExpenses = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoExpenses = new TableInfo("Expenses", _columnsExpenses, _foreignKeysExpenses, _indicesExpenses);
        final TableInfo _existingExpenses = TableInfo.read(db, "Expenses");
        if (!_infoExpenses.equals(_existingExpenses)) {
          return new RoomOpenHelper.ValidationResult(false, "Expenses(com.tillzo.pos.data.local.entity.ExpenseEntity).\n"
                  + " Expected:\n" + _infoExpenses + "\n"
                  + " Found:\n" + _existingExpenses);
        }
        final HashMap<String, TableInfo.Column> _columnsStockAdjustments = new HashMap<String, TableInfo.Column>(8);
        _columnsStockAdjustments.put("adjustmentId", new TableInfo.Column("adjustmentId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockAdjustments.put("productId", new TableInfo.Column("productId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockAdjustments.put("adjustmentType", new TableInfo.Column("adjustmentType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockAdjustments.put("quantityChanged", new TableInfo.Column("quantityChanged", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockAdjustments.put("reason", new TableInfo.Column("reason", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockAdjustments.put("adjustedBy", new TableInfo.Column("adjustedBy", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockAdjustments.put("syncStatus", new TableInfo.Column("syncStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockAdjustments.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStockAdjustments = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStockAdjustments = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStockAdjustments = new TableInfo("StockAdjustments", _columnsStockAdjustments, _foreignKeysStockAdjustments, _indicesStockAdjustments);
        final TableInfo _existingStockAdjustments = TableInfo.read(db, "StockAdjustments");
        if (!_infoStockAdjustments.equals(_existingStockAdjustments)) {
          return new RoomOpenHelper.ValidationResult(false, "StockAdjustments(com.tillzo.pos.data.local.entity.StockAdjustmentEntity).\n"
                  + " Expected:\n" + _infoStockAdjustments + "\n"
                  + " Found:\n" + _existingStockAdjustments);
        }
        final HashMap<String, TableInfo.Column> _columnsCategories = new HashMap<String, TableInfo.Column>(8);
        _columnsCategories.put("system_row_id", new TableInfo.Column("system_row_id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("sync_status", new TableInfo.Column("sync_status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("pos_terminal_id", new TableInfo.Column("pos_terminal_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("category_name", new TableInfo.Column("category_name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("is_deleted", new TableInfo.Column("is_deleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("deleted_at", new TableInfo.Column("deleted_at", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCategories = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCategories = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCategories = new TableInfo("Categories", _columnsCategories, _foreignKeysCategories, _indicesCategories);
        final TableInfo _existingCategories = TableInfo.read(db, "Categories");
        if (!_infoCategories.equals(_existingCategories)) {
          return new RoomOpenHelper.ValidationResult(false, "Categories(com.tillzo.pos.data.local.entity.CategoryEntity).\n"
                  + " Expected:\n" + _infoCategories + "\n"
                  + " Found:\n" + _existingCategories);
        }
        final HashMap<String, TableInfo.Column> _columnsProductBatches = new HashMap<String, TableInfo.Column>(16);
        _columnsProductBatches.put("batchId", new TableInfo.Column("batchId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductBatches.put("productId", new TableInfo.Column("productId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductBatches.put("barcodeId", new TableInfo.Column("barcodeId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductBatches.put("batchNumber", new TableInfo.Column("batchNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductBatches.put("manufacturingDate", new TableInfo.Column("manufacturingDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductBatches.put("expiryDate", new TableInfo.Column("expiryDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductBatches.put("stockQty", new TableInfo.Column("stockQty", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductBatches.put("costPrice", new TableInfo.Column("costPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductBatches.put("sellingPrice", new TableInfo.Column("sellingPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductBatches.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductBatches.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductBatches.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductBatches.put("syncStatus", new TableInfo.Column("syncStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductBatches.put("posTerminalId", new TableInfo.Column("posTerminalId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductBatches.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductBatches.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProductBatches = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysProductBatches.add(new TableInfo.ForeignKey("Inventory", "CASCADE", "NO ACTION", Arrays.asList("productId"), Arrays.asList("system_row_id")));
        final HashSet<TableInfo.Index> _indicesProductBatches = new HashSet<TableInfo.Index>(1);
        _indicesProductBatches.add(new TableInfo.Index("index_product_batches_productId", false, Arrays.asList("productId"), Arrays.asList("ASC")));
        final TableInfo _infoProductBatches = new TableInfo("product_batches", _columnsProductBatches, _foreignKeysProductBatches, _indicesProductBatches);
        final TableInfo _existingProductBatches = TableInfo.read(db, "product_batches");
        if (!_infoProductBatches.equals(_existingProductBatches)) {
          return new RoomOpenHelper.ValidationResult(false, "product_batches(com.tillzo.pos.data.local.entity.ProductBatchEntity).\n"
                  + " Expected:\n" + _infoProductBatches + "\n"
                  + " Found:\n" + _existingProductBatches);
        }
        final HashMap<String, TableInfo.Column> _columnsPurchaseOrders = new HashMap<String, TableInfo.Column>(16);
        _columnsPurchaseOrders.put("poId", new TableInfo.Column("poId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrders.put("poNumber", new TableInfo.Column("poNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrders.put("vendorId", new TableInfo.Column("vendorId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrders.put("vendorName", new TableInfo.Column("vendorName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrders.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrders.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrders.put("totalAmount", new TableInfo.Column("totalAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrders.put("currency", new TableInfo.Column("currency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrders.put("expectedDeliveryDate", new TableInfo.Column("expectedDeliveryDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrders.put("createdBy", new TableInfo.Column("createdBy", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrders.put("syncStatus", new TableInfo.Column("syncStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrders.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrders.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrders.put("posTerminalId", new TableInfo.Column("posTerminalId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrders.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrders.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPurchaseOrders = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPurchaseOrders = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPurchaseOrders = new TableInfo("purchase_orders", _columnsPurchaseOrders, _foreignKeysPurchaseOrders, _indicesPurchaseOrders);
        final TableInfo _existingPurchaseOrders = TableInfo.read(db, "purchase_orders");
        if (!_infoPurchaseOrders.equals(_existingPurchaseOrders)) {
          return new RoomOpenHelper.ValidationResult(false, "purchase_orders(com.tillzo.pos.data.local.entity.PurchaseOrderEntity).\n"
                  + " Expected:\n" + _infoPurchaseOrders + "\n"
                  + " Found:\n" + _existingPurchaseOrders);
        }
        final HashMap<String, TableInfo.Column> _columnsPurchaseOrderItems = new HashMap<String, TableInfo.Column>(14);
        _columnsPurchaseOrderItems.put("poItemId", new TableInfo.Column("poItemId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrderItems.put("poId", new TableInfo.Column("poId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrderItems.put("productId", new TableInfo.Column("productId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrderItems.put("productName", new TableInfo.Column("productName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrderItems.put("sku", new TableInfo.Column("sku", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrderItems.put("barcodeId", new TableInfo.Column("barcodeId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrderItems.put("orderedQty", new TableInfo.Column("orderedQty", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrderItems.put("receivedQty", new TableInfo.Column("receivedQty", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrderItems.put("unitCostPrice", new TableInfo.Column("unitCostPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrderItems.put("totalCost", new TableInfo.Column("totalCost", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrderItems.put("unit", new TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrderItems.put("syncStatus", new TableInfo.Column("syncStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrderItems.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseOrderItems.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPurchaseOrderItems = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysPurchaseOrderItems.add(new TableInfo.ForeignKey("purchase_orders", "CASCADE", "NO ACTION", Arrays.asList("poId"), Arrays.asList("poId")));
        final HashSet<TableInfo.Index> _indicesPurchaseOrderItems = new HashSet<TableInfo.Index>(1);
        _indicesPurchaseOrderItems.add(new TableInfo.Index("index_purchase_order_items_poId", false, Arrays.asList("poId"), Arrays.asList("ASC")));
        final TableInfo _infoPurchaseOrderItems = new TableInfo("purchase_order_items", _columnsPurchaseOrderItems, _foreignKeysPurchaseOrderItems, _indicesPurchaseOrderItems);
        final TableInfo _existingPurchaseOrderItems = TableInfo.read(db, "purchase_order_items");
        if (!_infoPurchaseOrderItems.equals(_existingPurchaseOrderItems)) {
          return new RoomOpenHelper.ValidationResult(false, "purchase_order_items(com.tillzo.pos.data.local.entity.PurchaseOrderItemEntity).\n"
                  + " Expected:\n" + _infoPurchaseOrderItems + "\n"
                  + " Found:\n" + _existingPurchaseOrderItems);
        }
        final HashMap<String, TableInfo.Column> _columnsVendors = new HashMap<String, TableInfo.Column>(10);
        _columnsVendors.put("vendorId", new TableInfo.Column("vendorId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVendors.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVendors.put("phone", new TableInfo.Column("phone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVendors.put("whatsapp", new TableInfo.Column("whatsapp", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVendors.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVendors.put("address", new TableInfo.Column("address", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVendors.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVendors.put("syncStatus", new TableInfo.Column("syncStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVendors.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVendors.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysVendors = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesVendors = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoVendors = new TableInfo("vendors", _columnsVendors, _foreignKeysVendors, _indicesVendors);
        final TableInfo _existingVendors = TableInfo.read(db, "vendors");
        if (!_infoVendors.equals(_existingVendors)) {
          return new RoomOpenHelper.ValidationResult(false, "vendors(com.tillzo.pos.data.local.entity.VendorEntity).\n"
                  + " Expected:\n" + _infoVendors + "\n"
                  + " Found:\n" + _existingVendors);
        }
        final HashMap<String, TableInfo.Column> _columnsGrnHeaders = new HashMap<String, TableInfo.Column>(20);
        _columnsGrnHeaders.put("grnId", new TableInfo.Column("grnId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("grnNumber", new TableInfo.Column("grnNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("poId", new TableInfo.Column("poId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("poNumber", new TableInfo.Column("poNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("vendorId", new TableInfo.Column("vendorId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("vendorName", new TableInfo.Column("vendorName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("vendorPhone", new TableInfo.Column("vendorPhone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("receivedBy", new TableInfo.Column("receivedBy", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("receivedByName", new TableInfo.Column("receivedByName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("totalItems", new TableInfo.Column("totalItems", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("totalReceivedQty", new TableInfo.Column("totalReceivedQty", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("totalAmount", new TableInfo.Column("totalAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("syncStatus", new TableInfo.Column("syncStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("posTerminalId", new TableInfo.Column("posTerminalId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnHeaders.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGrnHeaders = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesGrnHeaders = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGrnHeaders = new TableInfo("grn_headers", _columnsGrnHeaders, _foreignKeysGrnHeaders, _indicesGrnHeaders);
        final TableInfo _existingGrnHeaders = TableInfo.read(db, "grn_headers");
        if (!_infoGrnHeaders.equals(_existingGrnHeaders)) {
          return new RoomOpenHelper.ValidationResult(false, "grn_headers(com.tillzo.pos.data.local.entity.GrnHeaderEntity).\n"
                  + " Expected:\n" + _infoGrnHeaders + "\n"
                  + " Found:\n" + _existingGrnHeaders);
        }
        final HashMap<String, TableInfo.Column> _columnsGrnItems = new HashMap<String, TableInfo.Column>(25);
        _columnsGrnItems.put("grnItemId", new TableInfo.Column("grnItemId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("grnId", new TableInfo.Column("grnId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("poItemId", new TableInfo.Column("poItemId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("productId", new TableInfo.Column("productId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("batchId", new TableInfo.Column("batchId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("productName", new TableInfo.Column("productName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("barcodeId", new TableInfo.Column("barcodeId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("sku", new TableInfo.Column("sku", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("categoryId", new TableInfo.Column("categoryId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("brand", new TableInfo.Column("brand", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("orderedQty", new TableInfo.Column("orderedQty", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("receivedQty", new TableInfo.Column("receivedQty", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("unitCostPrice", new TableInfo.Column("unitCostPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("sellingPrice", new TableInfo.Column("sellingPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("totalCost", new TableInfo.Column("totalCost", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("unit", new TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("batchNumber", new TableInfo.Column("batchNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("manufacturingDate", new TableInfo.Column("manufacturingDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("expiryDate", new TableInfo.Column("expiryDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("inventoryAction", new TableInfo.Column("inventoryAction", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("isNewProduct", new TableInfo.Column("isNewProduct", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("lowStockThreshold", new TableInfo.Column("lowStockThreshold", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("syncStatus", new TableInfo.Column("syncStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrnItems.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGrnItems = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysGrnItems.add(new TableInfo.ForeignKey("grn_headers", "CASCADE", "NO ACTION", Arrays.asList("grnId"), Arrays.asList("grnId")));
        final HashSet<TableInfo.Index> _indicesGrnItems = new HashSet<TableInfo.Index>(2);
        _indicesGrnItems.add(new TableInfo.Index("index_grn_items_grnId", false, Arrays.asList("grnId"), Arrays.asList("ASC")));
        _indicesGrnItems.add(new TableInfo.Index("index_grn_items_productId", false, Arrays.asList("productId"), Arrays.asList("ASC")));
        final TableInfo _infoGrnItems = new TableInfo("grn_items", _columnsGrnItems, _foreignKeysGrnItems, _indicesGrnItems);
        final TableInfo _existingGrnItems = TableInfo.read(db, "grn_items");
        if (!_infoGrnItems.equals(_existingGrnItems)) {
          return new RoomOpenHelper.ValidationResult(false, "grn_items(com.tillzo.pos.data.local.entity.GrnItemEntity).\n"
                  + " Expected:\n" + _infoGrnItems + "\n"
                  + " Found:\n" + _existingGrnItems);
        }
        final HashMap<String, TableInfo.Column> _columnsProductUnits = new HashMap<String, TableInfo.Column>(7);
        _columnsProductUnits.put("unitId", new TableInfo.Column("unitId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductUnits.put("unitName", new TableInfo.Column("unitName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductUnits.put("abbreviation", new TableInfo.Column("abbreviation", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductUnits.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductUnits.put("syncStatus", new TableInfo.Column("syncStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductUnits.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductUnits.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProductUnits = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProductUnits = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoProductUnits = new TableInfo("product_units", _columnsProductUnits, _foreignKeysProductUnits, _indicesProductUnits);
        final TableInfo _existingProductUnits = TableInfo.read(db, "product_units");
        if (!_infoProductUnits.equals(_existingProductUnits)) {
          return new RoomOpenHelper.ValidationResult(false, "product_units(com.tillzo.pos.data.local.entity.ProductUnitEntity).\n"
                  + " Expected:\n" + _infoProductUnits + "\n"
                  + " Found:\n" + _existingProductUnits);
        }
        final HashMap<String, TableInfo.Column> _columnsTillSessions = new HashMap<String, TableInfo.Column>(24);
        _columnsTillSessions.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("cashierId", new TableInfo.Column("cashierId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("cashierName", new TableInfo.Column("cashierName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("posTerminalId", new TableInfo.Column("posTerminalId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("openingCash", new TableInfo.Column("openingCash", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("closingCash", new TableInfo.Column("closingCash", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("expectedCash", new TableInfo.Column("expectedCash", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("totalCashSales", new TableInfo.Column("totalCashSales", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("totalCardSales", new TableInfo.Column("totalCardSales", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("totalWalletSales", new TableInfo.Column("totalWalletSales", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("totalUdhaarSales", new TableInfo.Column("totalUdhaarSales", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("totalSplitSales", new TableInfo.Column("totalSplitSales", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("totalSalesCount", new TableInfo.Column("totalSalesCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("totalRefunds", new TableInfo.Column("totalRefunds", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("netCash", new TableInfo.Column("netCash", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("shiftDate", new TableInfo.Column("shiftDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("openedAt", new TableInfo.Column("openedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("closedAt", new TableInfo.Column("closedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("syncStatus", new TableInfo.Column("syncStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("posId", new TableInfo.Column("posId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTillSessions.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTillSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTillSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTillSessions = new TableInfo("till_sessions", _columnsTillSessions, _foreignKeysTillSessions, _indicesTillSessions);
        final TableInfo _existingTillSessions = TableInfo.read(db, "till_sessions");
        if (!_infoTillSessions.equals(_existingTillSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "till_sessions(com.tillzo.pos.data.local.entity.TillSessionEntity).\n"
                  + " Expected:\n" + _infoTillSessions + "\n"
                  + " Found:\n" + _existingTillSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsWastageLog = new HashMap<String, TableInfo.Column>(17);
        _columnsWastageLog.put("wastageId", new TableInfo.Column("wastageId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("productId", new TableInfo.Column("productId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("productName", new TableInfo.Column("productName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("batchId", new TableInfo.Column("batchId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("batchNumber", new TableInfo.Column("batchNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("quantity", new TableInfo.Column("quantity", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("unit", new TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("costPrice", new TableInfo.Column("costPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("totalLoss", new TableInfo.Column("totalLoss", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("reason", new TableInfo.Column("reason", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("loggedBy", new TableInfo.Column("loggedBy", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("wastageDate", new TableInfo.Column("wastageDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("syncStatus", new TableInfo.Column("syncStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("posTerminalId", new TableInfo.Column("posTerminalId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWastageLog.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWastageLog = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWastageLog = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWastageLog = new TableInfo("wastage_log", _columnsWastageLog, _foreignKeysWastageLog, _indicesWastageLog);
        final TableInfo _existingWastageLog = TableInfo.read(db, "wastage_log");
        if (!_infoWastageLog.equals(_existingWastageLog)) {
          return new RoomOpenHelper.ValidationResult(false, "wastage_log(com.tillzo.pos.data.local.entity.WastageEntity).\n"
                  + " Expected:\n" + _infoWastageLog + "\n"
                  + " Found:\n" + _existingWastageLog);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "18ff3181230c0f8be76104a39f1c5991", "a9c4c61f395292bb06503871ffd239f6");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "sync_log","Users_Permissions","Sales","Inventory","Customers","KhataEvents","Expenses","StockAdjustments","Categories","product_batches","purchase_orders","purchase_order_items","vendors","grn_headers","grn_items","product_units","till_sessions","wastage_log");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `sync_log`");
      _db.execSQL("DELETE FROM `Users_Permissions`");
      _db.execSQL("DELETE FROM `Sales`");
      _db.execSQL("DELETE FROM `Inventory`");
      _db.execSQL("DELETE FROM `Customers`");
      _db.execSQL("DELETE FROM `KhataEvents`");
      _db.execSQL("DELETE FROM `Expenses`");
      _db.execSQL("DELETE FROM `StockAdjustments`");
      _db.execSQL("DELETE FROM `Categories`");
      _db.execSQL("DELETE FROM `product_batches`");
      _db.execSQL("DELETE FROM `purchase_orders`");
      _db.execSQL("DELETE FROM `purchase_order_items`");
      _db.execSQL("DELETE FROM `vendors`");
      _db.execSQL("DELETE FROM `grn_headers`");
      _db.execSQL("DELETE FROM `grn_items`");
      _db.execSQL("DELETE FROM `product_units`");
      _db.execSQL("DELETE FROM `till_sessions`");
      _db.execSQL("DELETE FROM `wastage_log`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(SyncLogDao.class, SyncLogDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SaleDao.class, SaleDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(InventoryDao.class, InventoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CustomerDao.class, CustomerDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(KhataEventDao.class, KhataEventDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ExpenseDao.class, ExpenseDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StockAdjustmentDao.class, StockAdjustmentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CategoryDao.class, CategoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ProductBatchDao.class, ProductBatchDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PurchaseOrderDao.class, PurchaseOrderDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(VendorDao.class, VendorDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(GrnDao.class, GrnDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ProductUnitDao.class, ProductUnitDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TillSessionDao.class, TillSessionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(WastageDao.class, WastageDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public SyncLogDao syncLogDao() {
    if (_syncLogDao != null) {
      return _syncLogDao;
    } else {
      synchronized(this) {
        if(_syncLogDao == null) {
          _syncLogDao = new SyncLogDao_Impl(this);
        }
        return _syncLogDao;
      }
    }
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public SaleDao saleDao() {
    if (_saleDao != null) {
      return _saleDao;
    } else {
      synchronized(this) {
        if(_saleDao == null) {
          _saleDao = new SaleDao_Impl(this);
        }
        return _saleDao;
      }
    }
  }

  @Override
  public InventoryDao inventoryDao() {
    if (_inventoryDao != null) {
      return _inventoryDao;
    } else {
      synchronized(this) {
        if(_inventoryDao == null) {
          _inventoryDao = new InventoryDao_Impl(this);
        }
        return _inventoryDao;
      }
    }
  }

  @Override
  public CustomerDao customerDao() {
    if (_customerDao != null) {
      return _customerDao;
    } else {
      synchronized(this) {
        if(_customerDao == null) {
          _customerDao = new CustomerDao_Impl(this);
        }
        return _customerDao;
      }
    }
  }

  @Override
  public KhataEventDao khataEventDao() {
    if (_khataEventDao != null) {
      return _khataEventDao;
    } else {
      synchronized(this) {
        if(_khataEventDao == null) {
          _khataEventDao = new KhataEventDao_Impl(this);
        }
        return _khataEventDao;
      }
    }
  }

  @Override
  public ExpenseDao expenseDao() {
    if (_expenseDao != null) {
      return _expenseDao;
    } else {
      synchronized(this) {
        if(_expenseDao == null) {
          _expenseDao = new ExpenseDao_Impl(this);
        }
        return _expenseDao;
      }
    }
  }

  @Override
  public StockAdjustmentDao stockAdjustmentDao() {
    if (_stockAdjustmentDao != null) {
      return _stockAdjustmentDao;
    } else {
      synchronized(this) {
        if(_stockAdjustmentDao == null) {
          _stockAdjustmentDao = new StockAdjustmentDao_Impl(this);
        }
        return _stockAdjustmentDao;
      }
    }
  }

  @Override
  public CategoryDao categoryDao() {
    if (_categoryDao != null) {
      return _categoryDao;
    } else {
      synchronized(this) {
        if(_categoryDao == null) {
          _categoryDao = new CategoryDao_Impl(this);
        }
        return _categoryDao;
      }
    }
  }

  @Override
  public ProductBatchDao productBatchDao() {
    if (_productBatchDao != null) {
      return _productBatchDao;
    } else {
      synchronized(this) {
        if(_productBatchDao == null) {
          _productBatchDao = new ProductBatchDao_Impl(this);
        }
        return _productBatchDao;
      }
    }
  }

  @Override
  public PurchaseOrderDao purchaseOrderDao() {
    if (_purchaseOrderDao != null) {
      return _purchaseOrderDao;
    } else {
      synchronized(this) {
        if(_purchaseOrderDao == null) {
          _purchaseOrderDao = new PurchaseOrderDao_Impl(this);
        }
        return _purchaseOrderDao;
      }
    }
  }

  @Override
  public VendorDao vendorDao() {
    if (_vendorDao != null) {
      return _vendorDao;
    } else {
      synchronized(this) {
        if(_vendorDao == null) {
          _vendorDao = new VendorDao_Impl(this);
        }
        return _vendorDao;
      }
    }
  }

  @Override
  public GrnDao grnDao() {
    if (_grnDao != null) {
      return _grnDao;
    } else {
      synchronized(this) {
        if(_grnDao == null) {
          _grnDao = new GrnDao_Impl(this);
        }
        return _grnDao;
      }
    }
  }

  @Override
  public ProductUnitDao productUnitDao() {
    if (_productUnitDao != null) {
      return _productUnitDao;
    } else {
      synchronized(this) {
        if(_productUnitDao == null) {
          _productUnitDao = new ProductUnitDao_Impl(this);
        }
        return _productUnitDao;
      }
    }
  }

  @Override
  public TillSessionDao tillSessionDao() {
    if (_tillSessionDao != null) {
      return _tillSessionDao;
    } else {
      synchronized(this) {
        if(_tillSessionDao == null) {
          _tillSessionDao = new TillSessionDao_Impl(this);
        }
        return _tillSessionDao;
      }
    }
  }

  @Override
  public WastageDao wastageDao() {
    if (_wastageDao != null) {
      return _wastageDao;
    } else {
      synchronized(this) {
        if(_wastageDao == null) {
          _wastageDao = new WastageDao_Impl(this);
        }
        return _wastageDao;
      }
    }
  }
}
