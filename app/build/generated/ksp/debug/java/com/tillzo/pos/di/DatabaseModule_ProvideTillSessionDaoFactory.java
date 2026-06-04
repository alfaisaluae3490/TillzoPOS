package com.tillzo.pos.di;

import com.tillzo.pos.data.local.AppDatabase;
import com.tillzo.pos.data.local.dao.TillSessionDao;
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
public final class DatabaseModule_ProvideTillSessionDaoFactory implements Factory<TillSessionDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideTillSessionDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TillSessionDao get() {
    return provideTillSessionDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideTillSessionDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideTillSessionDaoFactory(dbProvider);
  }

  public static TillSessionDao provideTillSessionDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTillSessionDao(db));
  }
}
