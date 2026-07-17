package com.tillzo.pos.data.local

import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tillzo.pos.data.local.converter.RoomConverters
import com.tillzo.pos.data.local.dao.InventoryDao
import com.tillzo.pos.data.local.dao.SaleDao
import com.tillzo.pos.data.local.dao.SyncLogDao
import com.tillzo.pos.data.local.dao.UserDao
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.data.local.entity.StockAdjustmentEntity
import com.tillzo.pos.data.local.entity.SaleEntity
import com.tillzo.pos.data.local.entity.UserEntity
import com.tillzo.pos.data.local.entity.CustomerEntity
import com.tillzo.pos.data.local.entity.KhataEventEntity
import com.tillzo.pos.data.local.entity.ExpenseEntity
import com.tillzo.pos.data.local.dao.CustomerDao
import com.tillzo.pos.data.local.dao.KhataEventDao
import com.tillzo.pos.data.local.dao.ExpenseDao
import com.tillzo.pos.data.local.dao.StockAdjustmentDao
import com.tillzo.pos.data.local.dao.CategoryDao
import com.tillzo.pos.data.local.entity.CategoryEntity
import com.tillzo.pos.data.local.entity.ProductBatchEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderItemEntity
import com.tillzo.pos.data.local.entity.VendorEntity
import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.data.local.entity.GrnItemEntity
import com.tillzo.pos.data.local.dao.ProductBatchDao
import com.tillzo.pos.data.local.dao.PurchaseOrderDao
import com.tillzo.pos.data.local.dao.VendorDao
import com.tillzo.pos.data.local.dao.GrnDao
import com.tillzo.pos.data.local.dao.ProductUnitDao
import com.tillzo.pos.data.local.entity.ProductUnitEntity
import com.tillzo.pos.data.local.dao.TillSessionDao
import com.tillzo.pos.data.local.entity.TillSessionEntity
import com.tillzo.pos.data.local.dao.WastageDao
import com.tillzo.pos.data.local.entity.WastageEntity
import com.tillzo.pos.data.local.dao.BarcodeConfigDao
import com.tillzo.pos.data.local.entity.BarcodeGeneralConfigEntity
import com.tillzo.pos.data.local.entity.BarcodeFieldConfigEntity

/**
 * Minimal entity for M1 — non-empty entities list required by Room.
 * Tracks last sync timestamp per table. Used by M2 sync module.
 *
 * Full module entities added in M3–M7:
 *   M3 → UserEntity, PermissionEntity
 *   M4 → SaleEntity, CartItemEntity
 *   M6 → InventoryItemEntity
 *   M7 → CustomerEntity, KhataEventEntity
 */
@Entity(tableName = "sync_log")
data class SyncLogEntity(
    @PrimaryKey val table_name: String,
    @ColumnInfo(name = "last_synced_at") val lastSyncedAt: Long = 0L,
    @ColumnInfo(name = "last_sync_status") val lastSyncStatus: String = "never"
)

/**
 * AppDatabase — the single Room database for the entire app.
 *
 * Architecture Laws:
 * - Standard SQLite ONLY (no SQLCipher — per OPT-3 performance requirement).
 * - Every module adds its own entities and DAOs here as they are built.
 * - Migrations required for schema changes. Dev builds use fallbackToDestructiveMigration.
 * - This class is a singleton — provided via Hilt in DatabaseModule.kt.
 *
 * Version history:
 *   v1 → M1 foundation (SyncLogEntity table)
 *   v2 → M2 sync module (SyncLogDao registered; no schema change — same table)
 */
@Database(
    entities = [
        SyncLogEntity::class,               // M1 — foundation placeholder
        UserEntity::class,                  // M3 — Auth & Users
        SaleEntity::class,                  // M4 — Sales
        InventoryEntity::class,             // M6 — Inventory
        CustomerEntity::class,              // M7 — Store Operations
        KhataEventEntity::class,
        ExpenseEntity::class,
        StockAdjustmentEntity::class,       // M6.1 — Inventory Adjustments
        CategoryEntity::class,              // M7 — Categories
        ProductBatchEntity::class,
        PurchaseOrderEntity::class,
        PurchaseOrderItemEntity::class,
        VendorEntity::class,
        GrnHeaderEntity::class,
        GrnItemEntity::class,
        ProductUnitEntity::class,         // F1 — Units of Measure
        TillSessionEntity::class,          // M-Till — Shift / Cash Drawer
        WastageEntity::class,              // E1 — Wastage / Damage Logging
        com.tillzo.pos.data.local.entity.ItemGtinEntity::class, // GTIN Support
        BarcodeGeneralConfigEntity::class,
        BarcodeFieldConfigEntity::class
    ],
    version = 23,
    exportSchema = false  // Set to true + provide schemaLocation when releasing to production
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {

    // ── DAOs ────────────────────────────────────────────────────────────────

    abstract fun syncLogDao(): SyncLogDao    // M2 — sync tracking
    abstract fun userDao(): UserDao          // M3 — Auth & Users
    abstract fun saleDao(): SaleDao          // M4 — Sales
    abstract fun inventoryDao(): InventoryDao // M6 — Inventory

    abstract fun customerDao(): CustomerDao       // M7
    abstract fun khataEventDao(): KhataEventDao   // M7
    abstract fun expenseDao(): ExpenseDao         // M7
    abstract fun stockAdjustmentDao(): StockAdjustmentDao // M6.1
    abstract fun categoryDao(): CategoryDao       // M7
    
    abstract fun productBatchDao(): ProductBatchDao
    abstract fun purchaseOrderDao(): PurchaseOrderDao
    abstract fun vendorDao(): VendorDao
    abstract fun grnDao(): GrnDao
    abstract fun productUnitDao(): ProductUnitDao
    abstract fun tillSessionDao(): TillSessionDao // M-Till — Shift management
    abstract fun wastageDao(): WastageDao          // E1 — Wastage / Damage Logging
    abstract fun barcodeConfigDao(): BarcodeConfigDao

    companion object {
        const val DATABASE_NAME = "tillzo_pos_db"

        /**
         * Migration v1→v2: no schema change — SyncLogDao added as DAO abstraction
         * to the existing sync_log table that was created in v1.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No-op: sync_log table already exists, SyncLogDao is a new DAO
                // abstraction layer only — no structural DB change needed.
            }
        }

        /**
         * Migration v2→v3: M3 User Authentication module
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `Users` (
                        `system_row_id` TEXT NOT NULL,
                        `sync_status` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `pos_terminal_id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `role` TEXT NOT NULL,
                        `password_hash` TEXT NOT NULL,
                        `permissions_json` TEXT,
                        PRIMARY KEY(`system_row_id`)
                    )
                    """.trimIndent()
                )
            }
        }

        // M4 Migration: Added Sales table
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `Sales` (
                        `system_row_id` TEXT NOT NULL,
                        `sync_status` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `pos_terminal_id` TEXT NOT NULL,
                        `sync_uuid` TEXT NOT NULL,
                        `cashier_id` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `items_json` TEXT NOT NULL,
                        `subtotal` REAL NOT NULL,
                        `tax` REAL NOT NULL,
                        `total` REAL NOT NULL,
                        `payment_method` TEXT NOT NULL,
                        `payment_split_json` TEXT,
                        `reference_id` TEXT,
                        PRIMARY KEY(`system_row_id`)
                    )
                    """.trimIndent()
                )
            }
        }

        // M6 Migration: Added Inventory table
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `Inventory` (
                        `system_row_id` TEXT NOT NULL,
                        `sync_status` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `pos_terminal_id` TEXT NOT NULL,
                        `item_name` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `barcode_id` TEXT NOT NULL,
                        `unit` TEXT NOT NULL,
                        `price_per_unit` REAL NOT NULL,
                        `current_stock` REAL NOT NULL,
                        `low_stock_threshold` REAL NOT NULL,
                        PRIMARY KEY(`system_row_id`)
                    )
                    """.trimIndent()
                )
            }
        }

        // M7 Migration: Added CRM, Khata, and Expenses tables
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `Customers` (
                        `system_row_id` TEXT NOT NULL,
                        `sync_status` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `pos_terminal_id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `email` TEXT,
                        `address` TEXT,
                        PRIMARY KEY(`system_row_id`)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `KhataEvents` (
                        `system_row_id` TEXT NOT NULL,
                        `sync_status` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `pos_terminal_id` TEXT NOT NULL,
                        `customer_id` TEXT NOT NULL,
                        `event_type` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `note` TEXT,
                        `reference_sale_id` TEXT,
                        PRIMARY KEY(`system_row_id`)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `Expenses` (
                        `system_row_id` TEXT NOT NULL,
                        `sync_status` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `pos_terminal_id` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `description` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `logged_by_user_id` TEXT NOT NULL,
                        PRIMARY KEY(`system_row_id`)
                    )
                    """.trimIndent()
                )
            }
        }
        
        // M6.1 Migration: Adding SaaS fields to Inventory and adding StockAdjustments table
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to Inventory table
                database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `sku` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `brand` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `description` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `cost_price` REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `tax_percent` REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `batch_number` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `expiry_date` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `manufacturing_date` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `is_damaged_stock` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `damaged_qty` REAL NOT NULL DEFAULT 0.0")

                // Create StockAdjustments table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `StockAdjustments` (
                        `adjustmentId` TEXT NOT NULL,
                        `productId` TEXT NOT NULL,
                        `adjustmentType` TEXT NOT NULL,
                        `quantityChanged` REAL NOT NULL,
                        `reason` TEXT NOT NULL,
                        `adjustedBy` TEXT NOT NULL,
                        `syncStatus` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`adjustmentId`)
                    )
                    """.trimIndent()
                )
            }
        }

        // M7 Migration (v8): Adding Custom Expiry Alerts and Category Management
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new column to Inventory table
                database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `expiry_alert_days` INTEGER NOT NULL DEFAULT 30")

                // Create Categories table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `Categories` (
                        `system_row_id` TEXT NOT NULL,
                        `sync_status` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `pos_terminal_id` TEXT NOT NULL,
                        `category_name` TEXT NOT NULL,
                        PRIMARY KEY(`system_row_id`)
                    )
                    """.trimIndent()
                )
            }
        }

        // M8 Migration (v9): Adding Soft Delete pattern to all syncable tables
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val tables = listOf("Users", "Sales", "Inventory", "Customers", "KhataEvents", "Expenses", "Categories")
                
                tables.forEach { table ->
                    database.execSQL("ALTER TABLE `$table` ADD COLUMN `is_deleted` INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE `$table` ADD COLUMN `deleted_at` INTEGER DEFAULT NULL")
                }
            }
        }
        // M4 Full POS Screen Migration (v10): Adding split payment columns and customer_id to Sales,
        // and whatsapp field to Customers
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `Sales` ADD COLUMN `discount` REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE `Sales` ADD COLUMN `cash_amount` REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE `Sales` ADD COLUMN `card_amount` REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE `Sales` ADD COLUMN `wallet_amount` REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE `Sales` ADD COLUMN `udhaar_amount` REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE `Sales` ADD COLUMN `customer_id` TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE `Customers` ADD COLUMN `whatsapp` TEXT DEFAULT NULL")
            }
        }
        
        // M11 Migration (v11): Multi-Batch Inventory, PO, and GRN Modules
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create product_batches table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `product_batches` (
                        `batchId` TEXT NOT NULL,
                        `productId` TEXT NOT NULL,
                        `barcodeId` TEXT NOT NULL,
                        `batchNumber` TEXT NOT NULL,
                        `manufacturingDate` TEXT NOT NULL,
                        `expiryDate` TEXT NOT NULL,
                        `stockQty` REAL NOT NULL,
                        `costPrice` REAL NOT NULL,
                        `sellingPrice` REAL NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `isDeleted` INTEGER NOT NULL,
                        `deletedAt` INTEGER,
                        `syncStatus` TEXT NOT NULL,
                        `posTerminalId` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`batchId`),
                        FOREIGN KEY (`productId`) REFERENCES `Inventory`(`system_row_id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_product_batches_productId` ON `product_batches`(`productId`)")

                // Update Inventory table
                database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `totalStock` REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `hasBatches` INTEGER NOT NULL DEFAULT 0")

                // Create purchase_orders
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `purchase_orders` (
                        `poId` TEXT NOT NULL,
                        `poNumber` TEXT NOT NULL,
                        `vendorId` TEXT NOT NULL,
                        `vendorName` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `totalAmount` REAL NOT NULL,
                        `currency` TEXT NOT NULL,
                        `expectedDeliveryDate` TEXT NOT NULL,
                        `createdBy` TEXT NOT NULL,
                        `syncStatus` TEXT NOT NULL,
                        `isDeleted` INTEGER NOT NULL,
                        `deletedAt` INTEGER,
                        `posTerminalId` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`poId`)
                    )
                """.trimIndent())

                // Create purchase_order_items
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `purchase_order_items` (
                        `poItemId` TEXT NOT NULL,
                        `poId` TEXT NOT NULL,
                        `productId` TEXT NOT NULL,
                        `productName` TEXT NOT NULL,
                        `sku` TEXT NOT NULL,
                        `barcodeId` TEXT NOT NULL,
                        `orderedQty` REAL NOT NULL,
                        `receivedQty` REAL NOT NULL,
                        `unitCostPrice` REAL NOT NULL,
                        `totalCost` REAL NOT NULL,
                        `unit` TEXT NOT NULL,
                        `syncStatus` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`poItemId`),
                        FOREIGN KEY (`poId`) REFERENCES `purchase_orders`(`poId`) ON DELETE CASCADE
                    )
                """.trimIndent())
                
                // Create vendors
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `vendors` (
                        `vendorId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `whatsapp` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `isDeleted` INTEGER NOT NULL,
                        `syncStatus` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`vendorId`)
                    )
                """.trimIndent())
                
                // Create grn_headers
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `grn_headers` (
                        `grnId` TEXT NOT NULL,
                        `grnNumber` TEXT NOT NULL,
                        `poId` TEXT NOT NULL,
                        `vendorId` TEXT NOT NULL,
                        `vendorName` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `receivedBy` TEXT NOT NULL,
                        `totalAmount` REAL NOT NULL,
                        `syncStatus` TEXT NOT NULL,
                        `isDeleted` INTEGER NOT NULL,
                        `posTerminalId` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`grnId`)
                    )
                """.trimIndent())

                // Create grn_items
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `grn_items` (
                        `grnItemId` TEXT NOT NULL,
                        `grnId` TEXT NOT NULL,
                        `poItemId` TEXT NOT NULL,
                        `productId` TEXT NOT NULL,
                        `productName` TEXT NOT NULL,
                        `barcodeId` TEXT NOT NULL,
                        `sku` TEXT NOT NULL,
                        `receivedQty` REAL NOT NULL,
                        `unitCostPrice` REAL NOT NULL,
                        `totalCost` REAL NOT NULL,
                        `unit` TEXT NOT NULL,
                        `batchNumber` TEXT NOT NULL,
                        `manufacturingDate` TEXT NOT NULL,
                        `expiryDate` TEXT NOT NULL,
                        `inventoryAction` TEXT NOT NULL,
                        `isNewItem` INTEGER NOT NULL,
                        `syncStatus` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`grnItemId`)
                    )
                """.trimIndent())
            }
        }

        // M12 Migration (v12): Adding Index for PO Items FK
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_purchase_order_items_poId` ON `purchase_order_items`(`poId`)"
                )
            }
        }
        
        // M13 Migration (v13): Adding missing fields to GRN tables
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // grn_headers — add missing columns
                try { database.execSQL("ALTER TABLE grn_headers ADD COLUMN poNumber TEXT NOT NULL DEFAULT ''") } catch(e: Exception) {}
                try { database.execSQL("ALTER TABLE grn_headers ADD COLUMN vendorPhone TEXT NOT NULL DEFAULT ''") } catch(e: Exception) {}
                try { database.execSQL("ALTER TABLE grn_headers ADD COLUMN receivedByName TEXT NOT NULL DEFAULT ''") } catch(e: Exception) {}
                try { database.execSQL("ALTER TABLE grn_headers ADD COLUMN totalItems INTEGER NOT NULL DEFAULT 0") } catch(e: Exception) {}
                try { database.execSQL("ALTER TABLE grn_headers ADD COLUMN totalReceivedQty REAL NOT NULL DEFAULT 0.0") } catch(e: Exception) {}
                try { database.execSQL("ALTER TABLE grn_headers ADD COLUMN deletedAt INTEGER DEFAULT NULL") } catch(e: Exception) {}

                // grn_items — add missing columns
                try { database.execSQL("ALTER TABLE grn_items ADD COLUMN batchId TEXT NOT NULL DEFAULT ''") } catch(e: Exception) {}
                try { database.execSQL("ALTER TABLE grn_items ADD COLUMN categoryId TEXT NOT NULL DEFAULT ''") } catch(e: Exception) {}
                try { database.execSQL("ALTER TABLE grn_items ADD COLUMN brand TEXT NOT NULL DEFAULT ''") } catch(e: Exception) {}
                try { database.execSQL("ALTER TABLE grn_items ADD COLUMN sellingPrice REAL NOT NULL DEFAULT 0") } catch(e: Exception) {}
                try { database.execSQL("ALTER TABLE grn_items ADD COLUMN isNewProduct INTEGER NOT NULL DEFAULT 0") } catch(e: Exception) {}
                try { database.execSQL("ALTER TABLE grn_items ADD COLUMN orderedQty REAL NOT NULL DEFAULT 0.0") } catch(e: Exception) {}
                try { database.execSQL("ALTER TABLE grn_items ADD COLUMN lowStockThreshold REAL NOT NULL DEFAULT 5") } catch(e: Exception) {}
                try { database.execSQL("ALTER TABLE grn_items ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0") } catch(e: Exception) {}

                // Rename isNewItem to isNewProduct if needed (Room doesn't support RENAME COLUMN easily before 3.25.0, so we just add the new one)
                // SQLite 3.25.0 supports RENAME COLUMN, Android 11+ has it. We will use a fallback logic in our mapper instead or just add it.
                // It's safer to just add the new column and ignore the old one. We already added it above.

                // Add missing indices
                try { database.execSQL("CREATE INDEX IF NOT EXISTS index_grn_items_grnId ON grn_items(grnId)") } catch(e: Exception) {}
                try { database.execSQL("CREATE INDEX IF NOT EXISTS index_grn_items_productId ON grn_items(productId)") } catch(e: Exception) {}
            }
        }
        // F1 Migration (v14): Adding ProductUnits table for units of measure
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `product_units` (
                        `unitId` TEXT NOT NULL,
                        `unitName` TEXT NOT NULL,
                        `abbreviation` TEXT NOT NULL,
                        `isDeleted` INTEGER NOT NULL DEFAULT 0,
                        `syncStatus` TEXT NOT NULL DEFAULT 'synced',
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`unitId`)
                    )
                """.trimIndent())
            }
        }

        // M-Till Migration (v15): Adding isPinned + pinnedOrder columns to Inventory
        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `isPinned` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `pinnedOrder` INTEGER NOT NULL DEFAULT 0")
            }
        }

        // M-Till Migration (v16): Adding till_sessions table for shift/cash management
        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `till_sessions` (
                        `sessionId` TEXT NOT NULL,
                        `cashierId` TEXT NOT NULL DEFAULT '',
                        `cashierName` TEXT NOT NULL DEFAULT '',
                        `posTerminalId` TEXT NOT NULL DEFAULT '',
                        `openingCash` REAL NOT NULL DEFAULT 0,
                        `closingCash` REAL NOT NULL DEFAULT 0,
                        `expectedCash` REAL NOT NULL DEFAULT 0,
                        `totalCashSales` REAL NOT NULL DEFAULT 0,
                        `totalCardSales` REAL NOT NULL DEFAULT 0,
                        `totalWalletSales` REAL NOT NULL DEFAULT 0,
                        `totalUdhaarSales` REAL NOT NULL DEFAULT 0,
                        `totalSplitSales` REAL NOT NULL DEFAULT 0,
                        `totalSalesCount` INTEGER NOT NULL DEFAULT 0,
                        `totalRefunds` REAL NOT NULL DEFAULT 0,
                        `netCash` REAL NOT NULL DEFAULT 0,
                        `status` TEXT NOT NULL DEFAULT 'OPEN',
                        `notes` TEXT NOT NULL DEFAULT '',
                        `shiftDate` TEXT NOT NULL DEFAULT '',
                        `openedAt` INTEGER NOT NULL DEFAULT 0,
                        `closedAt` INTEGER,
                        `syncStatus` TEXT NOT NULL DEFAULT 'pending',
                        `posId` TEXT NOT NULL DEFAULT '',
                        `createdAt` INTEGER NOT NULL DEFAULT 0,
                        `updatedAt` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`sessionId`)
                    )
                """.trimIndent())
            }
        }

        // E1 Migration (v17): Adding wastage_log table for wastage / damage tracking
        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `wastage_log` (
                        `wastageId` TEXT NOT NULL,
                        `productId` TEXT NOT NULL,
                        `productName` TEXT NOT NULL,
                        `batchId` TEXT NOT NULL DEFAULT '',
                        `batchNumber` TEXT NOT NULL DEFAULT '',
                        `quantity` REAL NOT NULL,
                        `unit` TEXT NOT NULL,
                        `costPrice` REAL NOT NULL,
                        `totalLoss` REAL NOT NULL,
                        `reason` TEXT NOT NULL,
                        `notes` TEXT NOT NULL DEFAULT '',
                        `loggedBy` TEXT NOT NULL DEFAULT '',
                        `wastageDate` TEXT NOT NULL,
                        `syncStatus` TEXT NOT NULL DEFAULT 'pending',
                        `posTerminalId` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`wastageId`)
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wastage_log_productId` ON `wastage_log`(`productId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wastage_log_wastageDate` ON `wastage_log`(`wastageDate`)")
            }
        }

        // Hierarchical Categories Migration (v18): Adding parent_category_id to Categories table
        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `Categories` ADD COLUMN `parent_category_id` TEXT DEFAULT NULL")
            }
        }

        // Vendor Profile Extension Migration (v19): Adding 40+ new fields to vendors table
        // and Google Drive attachment columns (contractFileId, contractFileUrl)
        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // ── Geographic ────────────────────────────────────────────────
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `city` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `province` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `country` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `billingAddress` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `ownerName` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}

                // ── Financial / Tax ───────────────────────────────────────────
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `bankAccountTitle` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `bankName` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `bankAccountNumber` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `bankIban` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `bankSwiftCode` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `bankBranch` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `paymentTerms` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `preferredCurrency` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `creditLimit` REAL NOT NULL DEFAULT 0.0") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `registrationNumber` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `ntnNumber` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `cnicNumber` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `trnNumber` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `tradeLicenseNumber` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `tradeLicenseExpiryDate` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}

                // ── Contacts — Primary Manager ────────────────────────────────
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `primaryManagerName` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `primaryManagerPhone` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `primaryManagerEmail` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}

                // ── Contacts — Tech Support ───────────────────────────────────
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `techSupportName` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `techSupportPhone` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `techSupportEmail` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}

                // ── Contacts — Billing ────────────────────────────────────────
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `billingContactName` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `billingContactPhone` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `billingContactEmail` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}

                // ── Contacts — Escalation L1 ──────────────────────────────────
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `escalationL1Name` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `escalationL1Phone` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `escalationL1Email` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}

                // ── Contacts — Escalation L2 ──────────────────────────────────
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `escalationL2Name` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `escalationL2Phone` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `escalationL2Email` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}

                // ── Contacts — Escalation L3 ──────────────────────────────────
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `escalationL3Name` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `escalationL3Phone` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `escalationL3Email` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}

                // ── SLA & Files ───────────────────────────────────────────────
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `contractStartDate` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `contractExpiryDate` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `slaResponseTimes` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `warrantyTerms` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `complianceCertificates` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}

                // ── Google Drive Attachment Metadata ──────────────────────────
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `contractFileId` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE `vendors` ADD COLUMN `contractFileUrl` TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
            }
        }

        // GTIN Migration (v20): Adding ItemGtins table and item_number to Inventory
        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try { database.execSQL("ALTER TABLE `Inventory` ADD COLUMN `item_number` INTEGER NOT NULL DEFAULT 0") } catch(_: Exception) {}
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ItemGtins` (
                        `gtin_id` TEXT NOT NULL,
                        `item_id` TEXT NOT NULL,
                        `gtin` TEXT NOT NULL,
                        PRIMARY KEY(`gtin_id`),
                        FOREIGN KEY(`item_id`) REFERENCES `Inventory`(`system_row_id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_ItemGtins_item_id` ON `ItemGtins`(`item_id`)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ItemGtins_gtin` ON `ItemGtins`(`gtin`)")
            }
        }

        // M22 Migration (v22): Adding attached file columns to grn_headers
        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try { database.execSQL("ALTER TABLE grn_headers ADD COLUMN attachedFileId TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
                try { database.execSQL("ALTER TABLE grn_headers ADD COLUMN attachedFileUrl TEXT NOT NULL DEFAULT ''") } catch(_: Exception) {}
            }
        }

        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try { database.execSQL("ALTER TABLE vendors ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1") } catch(_: Exception) {}
            }
        }

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `BarcodeGeneralConfigs` (
                        `system_row_id` TEXT NOT NULL,
                        `sync_status` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `pos_terminal_id` TEXT NOT NULL,
                        `is_deleted` INTEGER NOT NULL DEFAULT 0,
                        `deleted_at` INTEGER,
                        `labelWidth` INTEGER NOT NULL,
                        `labelHeight` INTEGER NOT NULL,
                        `titleTextSize` REAL NOT NULL,
                        `isTitleBold` INTEGER NOT NULL,
                        `barcodeSize` REAL NOT NULL,
                        `currencySymbol` TEXT NOT NULL,
                        `companyName` TEXT NOT NULL,
                        `companyLogoPath` TEXT NOT NULL,
                        `showCompanyName` INTEGER NOT NULL,
                        `showCompanyLogo` INTEGER NOT NULL,
                        `titleX` REAL NOT NULL,
                        `titleY` REAL NOT NULL,
                        `priceX` REAL NOT NULL,
                        `priceY` REAL NOT NULL,
                        `skuX` REAL NOT NULL,
                        `skuY` REAL NOT NULL,
                        `gtinX` REAL NOT NULL,
                        `gtinY` REAL NOT NULL,
                        `lotX` REAL NOT NULL,
                        `lotY` REAL NOT NULL,
                        `expX` REAL NOT NULL,
                        `expY` REAL NOT NULL,
                        `snX` REAL NOT NULL,
                        `snY` REAL NOT NULL,
                        `barcodeX` REAL NOT NULL,
                        `barcodeY` REAL NOT NULL,
                        `companyNameSize` REAL NOT NULL,
                        `companyLogoSize` REAL NOT NULL,
                        `companyNameX` REAL NOT NULL,
                        `companyNameY` REAL NOT NULL,
                        `companyLogoX` REAL NOT NULL,
                        `companyLogoY` REAL NOT NULL,
                        `usePrefix` INTEGER NOT NULL,
                        `customPrefix` TEXT NOT NULL,
                        `prefixPosition` INTEGER NOT NULL,
                        `useSuffix` INTEGER NOT NULL,
                        `customSuffix` TEXT NOT NULL,
                        `suffixPosition` INTEGER NOT NULL,
                        `useSeparator` INTEGER NOT NULL,
                        PRIMARY KEY(`system_row_id`)
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `BarcodeFieldConfigs` (
                        `system_row_id` TEXT NOT NULL,
                        `sync_status` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `pos_terminal_id` TEXT NOT NULL,
                        `is_deleted` INTEGER NOT NULL DEFAULT 0,
                        `deleted_at` INTEGER,
                        `fieldId` TEXT NOT NULL,
                        `fieldName` TEXT NOT NULL,
                        `aiCode` TEXT NOT NULL,
                        `isEnabled` INTEGER NOT NULL,
                        `sequenceOrder` INTEGER NOT NULL,
                        `useFnc1Separator` INTEGER NOT NULL,
                        `customValue` TEXT NOT NULL DEFAULT '',
                        PRIMARY KEY(`system_row_id`)
                    )
                """.trimIndent())
            }
        }
    }
}
