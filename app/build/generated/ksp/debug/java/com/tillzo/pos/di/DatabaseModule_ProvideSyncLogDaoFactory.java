package com.tillzo.pos.di;

import com.tillzo.pos.data.local.AppDatabase;
import com.tillzo.pos.data.local.dao.SyncLogDao;
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
public final class DatabaseModule_ProvideSyncLogDaoFactory implements Factory<SyncLogDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideSyncLogDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SyncLogDao get() {
    return provideSyncLogDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideSyncLogDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideSyncLogDaoFactory(dbProvider);
  }

  public static SyncLogDao provideSyncLogDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSyncLogDao(db));
  }
}
