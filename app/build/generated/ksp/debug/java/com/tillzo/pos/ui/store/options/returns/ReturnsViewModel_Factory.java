package com.tillzo.pos.ui.store.options.returns;

import com.tillzo.pos.data.local.dao.InventoryDao;
import com.tillzo.pos.data.local.dao.ProductBatchDao;
import com.tillzo.pos.data.local.dao.WastageDao;
import com.tillzo.pos.domain.repository.SaleRepository;
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
public final class ReturnsViewModel_Factory implements Factory<ReturnsViewModel> {
  private final Provider<SaleRepository> saleRepositoryProvider;

  private final Provider<InventoryDao> inventoryDaoProvider;

  private final Provider<ProductBatchDao> productBatchDaoProvider;

  private final Provider<WastageDao> wastageDaoProvider;

  public ReturnsViewModel_Factory(Provider<SaleRepository> saleRepositoryProvider,
      Provider<InventoryDao> inventoryDaoProvider,
      Provider<ProductBatchDao> productBatchDaoProvider, Provider<WastageDao> wastageDaoProvider) {
    this.saleRepositoryProvider = saleRepositoryProvider;
    this.inventoryDaoProvider = inventoryDaoProvider;
    this.productBatchDaoProvider = productBatchDaoProvider;
    this.wastageDaoProvider = wastageDaoProvider;
  }

  @Override
  public ReturnsViewModel get() {
    return newInstance(saleRepositoryProvider.get(), inventoryDaoProvider.get(), productBatchDaoProvider.get(), wastageDaoProvider.get());
  }

  public static ReturnsViewModel_Factory create(Provider<SaleRepository> saleRepositoryProvider,
      Provider<InventoryDao> inventoryDaoProvider,
      Provider<ProductBatchDao> productBatchDaoProvider, Provider<WastageDao> wastageDaoProvider) {
    return new ReturnsViewModel_Factory(saleRepositoryProvider, inventoryDaoProvider, productBatchDaoProvider, wastageDaoProvider);
  }

  public static ReturnsViewModel newInstance(SaleRepository saleRepository,
      InventoryDao inventoryDao, ProductBatchDao productBatchDao, WastageDao wastageDao) {
    return new ReturnsViewModel(saleRepository, inventoryDao, productBatchDao, wastageDao);
  }
}
