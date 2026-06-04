package com.tillzo.pos.domain.usecase.inventory;

import com.tillzo.pos.domain.repository.InventoryRepository;
import com.tillzo.pos.domain.repository.StockAdjustmentRepository;
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
public final class DamagedStockEntryUseCase_Factory implements Factory<DamagedStockEntryUseCase> {
  private final Provider<InventoryRepository> inventoryRepositoryProvider;

  private final Provider<StockAdjustmentRepository> stockAdjustmentRepositoryProvider;

  public DamagedStockEntryUseCase_Factory(Provider<InventoryRepository> inventoryRepositoryProvider,
      Provider<StockAdjustmentRepository> stockAdjustmentRepositoryProvider) {
    this.inventoryRepositoryProvider = inventoryRepositoryProvider;
    this.stockAdjustmentRepositoryProvider = stockAdjustmentRepositoryProvider;
  }

  @Override
  public DamagedStockEntryUseCase get() {
    return newInstance(inventoryRepositoryProvider.get(), stockAdjustmentRepositoryProvider.get());
  }

  public static DamagedStockEntryUseCase_Factory create(
      Provider<InventoryRepository> inventoryRepositoryProvider,
      Provider<StockAdjustmentRepository> stockAdjustmentRepositoryProvider) {
    return new DamagedStockEntryUseCase_Factory(inventoryRepositoryProvider, stockAdjustmentRepositoryProvider);
  }

  public static DamagedStockEntryUseCase newInstance(InventoryRepository inventoryRepository,
      StockAdjustmentRepository stockAdjustmentRepository) {
    return new DamagedStockEntryUseCase(inventoryRepository, stockAdjustmentRepository);
  }
}
