package com.tillzo.pos.di;

import com.tillzo.pos.data.local.AppDatabase;
import com.tillzo.pos.data.local.dao.ProductUnitDao;
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
public final class DatabaseModule_ProvideProductUnitDaoFactory implements Factory<ProductUnitDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideProductUnitDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProductUnitDao get() {
    return provideProductUnitDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideProductUnitDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideProductUnitDaoFactory(dbProvider);
  }

  public static ProductUnitDao provideProductUnitDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideProductUnitDao(db));
  }
}
