package com.tillzo.pos.di;

import com.tillzo.pos.data.local.AppDatabase;
import com.tillzo.pos.data.local.dao.VendorDao;
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
public final class DatabaseModule_ProvideVendorDaoFactory implements Factory<VendorDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideVendorDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public VendorDao get() {
    return provideVendorDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideVendorDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideVendorDaoFactory(dbProvider);
  }

  public static VendorDao provideVendorDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideVendorDao(db));
  }
}
