package com.tillzo.pos.di;

import com.tillzo.pos.data.local.AppDatabase;
import com.tillzo.pos.data.local.dao.GrnDao;
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
public final class DatabaseModule_ProvideGrnDaoFactory implements Factory<GrnDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideGrnDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public GrnDao get() {
    return provideGrnDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideGrnDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideGrnDaoFactory(dbProvider);
  }

  public static GrnDao provideGrnDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideGrnDao(db));
  }
}
