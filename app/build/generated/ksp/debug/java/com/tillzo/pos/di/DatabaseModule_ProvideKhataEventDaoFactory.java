package com.tillzo.pos.di;

import com.tillzo.pos.data.local.AppDatabase;
import com.tillzo.pos.data.local.dao.KhataEventDao;
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
public final class DatabaseModule_ProvideKhataEventDaoFactory implements Factory<KhataEventDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideKhataEventDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public KhataEventDao get() {
    return provideKhataEventDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideKhataEventDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideKhataEventDaoFactory(dbProvider);
  }

  public static KhataEventDao provideKhataEventDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideKhataEventDao(db));
  }
}
