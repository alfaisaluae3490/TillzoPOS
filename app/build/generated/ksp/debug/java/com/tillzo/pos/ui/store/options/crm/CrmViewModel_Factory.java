package com.tillzo.pos.ui.store.options.crm;

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
public final class CrmViewModel_Factory implements Factory<CrmViewModel> {
  private final Provider<StoreRepository> storeRepositoryProvider;

  public CrmViewModel_Factory(Provider<StoreRepository> storeRepositoryProvider) {
    this.storeRepositoryProvider = storeRepositoryProvider;
  }

  @Override
  public CrmViewModel get() {
    return newInstance(storeRepositoryProvider.get());
  }

  public static CrmViewModel_Factory create(Provider<StoreRepository> storeRepositoryProvider) {
    return new CrmViewModel_Factory(storeRepositoryProvider);
  }

  public static CrmViewModel newInstance(StoreRepository storeRepository) {
    return new CrmViewModel(storeRepository);
  }
}
