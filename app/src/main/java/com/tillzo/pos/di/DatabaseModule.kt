package com.tillzo.pos.di

import android.content.Context
import androidx.room.Room
import com.tillzo.pos.data.local.AppDatabase
import com.tillzo.pos.data.local.dao.SaleDao
import com.tillzo.pos.data.local.dao.SyncLogDao
import com.tillzo.pos.data.local.dao.UserDao
import com.tillzo.pos.data.local.dao.InventoryDao
import com.tillzo.pos.data.local.dao.CustomerDao
import com.tillzo.pos.data.local.dao.KhataEventDao
import com.tillzo.pos.data.local.dao.ExpenseDao
import com.tillzo.pos.data.local.dao.StockAdjustmentDao
import com.tillzo.pos.data.local.dao.CategoryDao
import com.tillzo.pos.data.local.dao.ProductBatchDao
import com.tillzo.pos.data.local.dao.PurchaseOrderDao
import com.tillzo.pos.data.local.dao.VendorDao
import com.tillzo.pos.data.local.dao.GrnDao
import com.tillzo.pos.data.local.dao.ProductUnitDao
import com.tillzo.pos.data.local.dao.TillSessionDao
import com.tillzo.pos.data.local.dao.WastageDao
import com.tillzo.pos.data.local.dao.BarcodeConfigDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module — provides AppDatabase singleton and all DAOs.
 *
 * Architecture Law:
 * - Standard Room SQLite ONLY (no SQLCipher — OPT-3).
 * - Database is a singleton, shared across the entire app lifetime.
 * - Each module's DAO is provided as a separate @Provides function here.
 *
 * Migration history:
 *   v1 → v2: M2 SyncLogDao (no schema change, safe no-op migration)
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10,
                AppDatabase.MIGRATION_10_11,
                AppDatabase.MIGRATION_11_12,
                AppDatabase.MIGRATION_12_13,
                AppDatabase.MIGRATION_13_14,
                AppDatabase.MIGRATION_14_15,
                AppDatabase.MIGRATION_15_16,
                AppDatabase.MIGRATION_16_17,
                AppDatabase.MIGRATION_17_18,
                AppDatabase.MIGRATION_18_19,
                AppDatabase.MIGRATION_19_20,
                AppDatabase.MIGRATION_20_21,
                AppDatabase.MIGRATION_21_22,
                AppDatabase.MIGRATION_22_23
            )
            // fallbackToDestructiveMigration() ← dev-only safety net, commented out in favor of real migration
            .build()
    }

    // ── DAOs ─────────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideSyncLogDao(db: AppDatabase): SyncLogDao = db.syncLogDao()

    // DAOs added here as modules are built:
    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideSaleDao(appDatabase: AppDatabase): SaleDao {
        return appDatabase.saleDao()
    }
    @Provides
    @Singleton
    fun provideInventoryDao(appDatabase: AppDatabase): InventoryDao {
        return appDatabase.inventoryDao()
    }

    @Provides
    @Singleton
    fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()

    @Provides
    @Singleton
    fun provideKhataEventDao(db: AppDatabase): KhataEventDao = db.khataEventDao()

    @Provides
    @Singleton
    fun provideExpenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()

    @Provides
    @Singleton
    fun provideStockAdjustmentDao(db: AppDatabase): StockAdjustmentDao = db.stockAdjustmentDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    @Singleton
    fun provideProductBatchDao(db: AppDatabase): ProductBatchDao = db.productBatchDao()

    @Provides
    @Singleton
    fun providePurchaseOrderDao(db: AppDatabase): PurchaseOrderDao = db.purchaseOrderDao()

    @Provides
    @Singleton
    fun provideVendorDao(db: AppDatabase): VendorDao = db.vendorDao()

    @Provides
    @Singleton
    fun provideGrnDao(db: AppDatabase): GrnDao = db.grnDao()

    @Provides
    @Singleton
    fun provideProductUnitDao(db: AppDatabase): ProductUnitDao = db.productUnitDao()

    @Provides
    @Singleton
    fun provideTillSessionDao(db: AppDatabase): TillSessionDao = db.tillSessionDao()

    @Provides
    @Singleton
    fun provideWastageDao(db: AppDatabase): WastageDao = db.wastageDao()

    @Provides
    @Singleton
    fun provideBarcodeConfigDao(db: AppDatabase): BarcodeConfigDao = db.barcodeConfigDao()
}
