package com.tillzo.pos.di;

import com.tillzo.pos.data.local.AppDatabase;
import com.tillzo.pos.data.local.dao.ExpenseDao;
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
public final class DatabaseModule_ProvideExpenseDaoFactory implements Factory<ExpenseDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideExpenseDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ExpenseDao get() {
    return provideExpenseDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideExpenseDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideExpenseDaoFactory(dbProvider);
  }

  public static ExpenseDao provideExpenseDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideExpenseDao(db));
  }
}
