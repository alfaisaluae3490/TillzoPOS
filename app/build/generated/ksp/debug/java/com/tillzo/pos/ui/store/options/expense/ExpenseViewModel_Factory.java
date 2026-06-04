package com.tillzo.pos.ui.store.options.expense;

import com.tillzo.pos.domain.repository.StoreRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class ExpenseViewModel_Factory implements Factory<ExpenseViewModel> {
  private final Provider<StoreRepository> storeRepositoryProvider;

  public ExpenseViewModel_Factory(Provider<StoreRepository> storeRepositoryProvider) {
    this.storeRepositoryProvider = storeRepositoryProvider;
  }

  @Override
  public ExpenseViewModel get() {
    return newInstance(storeRepositoryProvider.get());
  }

  public static ExpenseViewModel_Factory create(Provider<StoreRepository> storeRepositoryProvider) {
    return new ExpenseViewModel_Factory(storeRepositoryProvider);
  }

  public static ExpenseViewModel newInstance(StoreRepository storeRepository) {
    return new ExpenseViewModel(storeRepository);
  }
}
