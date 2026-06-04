package com.tillzo.pos.domain.usecase.batch;

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
public final class AddBatchToProductUseCase_Factory implements Factory<AddBatchToProductUseCase> {
  private final Provider<ProductBatchDao> batchDaoProvider;

  private final Provider<InventoryDao> inventoryDaoProvider;

  public AddBatchToProductUseCase_Factory(Provider<ProductBatchDao> batchDaoProvider,
      Provider<InventoryDao> inventoryDaoProvider) {
    this.batchDaoProvider = batchDaoProvider;
    this.inventoryDaoProvider = inventoryDaoProvider;
  }

  @Override
  public AddBatchToProductUseCase get() {
    return newInstance(batchDaoProvider.get(), inventoryDaoProvider.get());
  }

  public static AddBatchToProductUseCase_Factory create(Provider<ProductBatchDao> batchDaoProvider,
      Provider<InventoryDao> inventoryDaoProvider) {
    return new AddBatchToProductUseCase_Factory(batchDaoProvider, inventoryDaoProvider);
  }

  public static AddBatchToProductUseCase newInstance(ProductBatchDao batchDao,
      InventoryDao inventoryDao) {
    return new AddBatchToProductUseCase(batchDao, inventoryDao);
  }
}
