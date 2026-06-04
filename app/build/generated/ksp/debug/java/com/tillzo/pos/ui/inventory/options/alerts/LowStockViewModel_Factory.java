package com.tillzo.pos.ui.inventory.options.alerts;

import com.tillzo.pos.domain.repository.InventoryRepository;
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
public final class LowStockViewModel_Factory implements Factory<LowStockViewModel> {
  private final Provider<InventoryRepository> inventoryRepositoryProvider;

  public LowStockViewModel_Factory(Provider<InventoryRepository> inventoryRepositoryProvider) {
    this.inventoryRepositoryProvider = inventoryRepositoryProvider;
  }

  @Override
  public LowStockViewModel get() {
    return newInstance(inventoryRepositoryProvider.get());
  }

  public static LowStockViewModel_Factory create(
      Provider<InventoryRepository> inventoryRepositoryProvider) {
    return new LowStockViewModel_Factory(inventoryRepositoryProvider);
  }

  public static LowStockViewModel newInstance(InventoryRepository inventoryRepository) {
    return new LowStockViewModel(inventoryRepository);
  }
}
