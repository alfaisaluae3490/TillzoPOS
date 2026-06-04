package com.tillzo.pos.data.repository;

import com.tillzo.pos.data.local.dao.InventoryDao;
import com.tillzo.pos.data.local.dao.ProductBatchDao;
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
public final class InventoryRepositoryImpl_Factory implements Factory<InventoryRepositoryImpl> {
  private final Provider<InventoryDao> inventoryDaoProvider;

  private final Provider<ProductBatchDao> productBatchDaoProvider;

  public InventoryRepositoryImpl_Factory(Provider<InventoryDao> inventoryDaoProvider,
      Provider<ProductBatchDao> productBatchDaoProvider) {
    this.inventoryDaoProvider = inventoryDaoProvider;
    this.productBatchDaoProvider = productBatchDaoProvider;
  }

  @Override
  public InventoryRepositoryImpl get() {
    return newInstance(inventoryDaoProvider.get(), productBatchDaoProvider.get());
  }

  public static InventoryRepositoryImpl_Factory create(Provider<InventoryDao> inventoryDaoProvider,
      Provider<ProductBatchDao> productBatchDaoProvider) {
    return new InventoryRepositoryImpl_Factory(inventoryDaoProvider, productBatchDaoProvider);
  }

  public static InventoryRepositoryImpl newInstance(InventoryDao inventoryDao,
      ProductBatchDao productBatchDao) {
    return new InventoryRepositoryImpl(inventoryDao, productBatchDao);
  }
}
