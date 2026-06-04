package com.tillzo.pos.ui.inventory;

import com.tillzo.pos.data.local.dao.InventoryDao;
import com.tillzo.pos.data.local.dao.StockAdjustmentDao;
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
public final class StockAdjustmentViewModel_Factory implements Factory<StockAdjustmentViewModel> {
  private final Provider<InventoryDao> inventoryDaoProvider;

  private final Provider<StockAdjustmentDao> stockAdjustmentDaoProvider;

  public StockAdjustmentViewModel_Factory(Provider<InventoryDao> inventoryDaoProvider,
      Provider<StockAdjustmentDao> stockAdjustmentDaoProvider) {
    this.inventoryDaoProvider = inventoryDaoProvider;
    this.stockAdjustmentDaoProvider = stockAdjustmentDaoProvider;
  }

  @Override
  public StockAdjustmentViewModel get() {
    return newInstance(inventoryDaoProvider.get(), stockAdjustmentDaoProvider.get());
  }

  public static StockAdjustmentViewModel_Factory create(Provider<InventoryDao> inventoryDaoProvider,
      Provider<StockAdjustmentDao> stockAdjustmentDaoProvider) {
    return new StockAdjustmentViewModel_Factory(inventoryDaoProvider, stockAdjustmentDaoProvider);
  }

  public static StockAdjustmentViewModel newInstance(InventoryDao inventoryDao,
      StockAdjustmentDao stockAdjustmentDao) {
    return new StockAdjustmentViewModel(inventoryDao, stockAdjustmentDao);
  }
}
