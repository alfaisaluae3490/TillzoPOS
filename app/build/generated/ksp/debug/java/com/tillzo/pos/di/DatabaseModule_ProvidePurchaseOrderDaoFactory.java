package com.tillzo.pos.di;

import com.tillzo.pos.data.local.AppDatabase;
import com.tillzo.pos.data.local.dao.PurchaseOrderDao;
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
public final class DatabaseModule_ProvidePurchaseOrderDaoFactory implements Factory<PurchaseOrderDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvidePurchaseOrderDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PurchaseOrderDao get() {
    return providePurchaseOrderDao(dbProvider.get());
  }

  public static DatabaseModule_ProvidePurchaseOrderDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvidePurchaseOrderDaoFactory(dbProvider);
  }

  public static PurchaseOrderDao providePurchaseOrderDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePurchaseOrderDao(db));
  }
}
