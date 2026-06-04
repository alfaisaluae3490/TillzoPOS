package com.tillzo.pos.di;

import com.tillzo.pos.data.local.AppDatabase;
import com.tillzo.pos.data.local.dao.WastageDao;
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
public final class DatabaseModule_ProvideWastageDaoFactory implements Factory<WastageDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideWastageDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public WastageDao get() {
    return provideWastageDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideWastageDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideWastageDaoFactory(dbProvider);
  }

  public static WastageDao provideWastageDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideWastageDao(db));
  }
}
