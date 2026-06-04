package com.tillzo.pos.di;

import com.tillzo.pos.data.local.AppDatabase;
import com.tillzo.pos.data.local.dao.ProductBatchDao;
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
public final class DatabaseModule_ProvideProductBatchDaoFactory implements Factory<ProductBatchDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideProductBatchDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProductBatchDao get() {
    return provideProductBatchDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideProductBatchDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideProductBatchDaoFactory(dbProvider);
  }

  public static ProductBatchDao provideProductBatchDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideProductBatchDao(db));
  }
}
