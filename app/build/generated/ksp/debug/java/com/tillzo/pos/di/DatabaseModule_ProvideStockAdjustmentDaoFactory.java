package com.tillzo.pos.di;

import com.tillzo.pos.data.local.AppDatabase;
import com.tillzo.pos.data.local.dao.StockAdjustmentDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class DatabaseModule_ProvideStockAdjustmentDaoFactory implements Factory<StockAdjustmentDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideStockAdjustmentDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public StockAdjustmentDao get() {
    return provideStockAdjustmentDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideStockAdjustmentDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideStockAdjustmentDaoFactory(dbProvider);
  }

  public static StockAdjustmentDao provideStockAdjustmentDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideStockAdjustmentDao(db));
  }
}
