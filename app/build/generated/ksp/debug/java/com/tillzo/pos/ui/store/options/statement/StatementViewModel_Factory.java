package com.tillzo.pos.ui.store.options.statement;

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
public final class StatementViewModel_Factory implements Factory<StatementViewModel> {
  private final Provider<StoreRepository> storeRepositoryProvider;

  public StatementViewModel_Factory(Provider<StoreRepository> storeRepositoryProvider) {
    this.storeRepositoryProvider = storeRepositoryProvider;
  }

  @Override
  public StatementViewModel get() {
    return newInstance(storeRepositoryProvider.get());
  }

  public static StatementViewModel_Factory create(
      Provider<StoreRepository> storeRepositoryProvider) {
    return new StatementViewModel_Factory(storeRepositoryProvider);
  }

  public static StatementViewModel newInstance(StoreRepository storeRepository) {
    return new StatementViewModel(storeRepository);
  }
}
