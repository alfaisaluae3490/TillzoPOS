package com.tillzo.pos.domain.usecase.grn;

import com.tillzo.pos.data.local.dao.PurchaseOrderDao;
import com.tillzo.pos.domain.repository.GrnRepository;
import com.tillzo.pos.domain.repository.InventoryRepository;
import com.tillzo.pos.domain.repository.ProductBatchRepository;
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
public final class ConfirmGrnUseCase_Factory implements Factory<ConfirmGrnUseCase> {
  private final Provider<GrnRepository> grnRepositoryProvider;

  private final Provider<InventoryRepository> inventoryRepositoryProvider;

  private final Provider<ProductBatchRepository> productBatchRepositoryProvider;

  private final Provider<PurchaseOrderDao> purchaseOrderDaoProvider;

  public ConfirmGrnUseCase_Factory(Provider<GrnRepository> grnRepositoryProvider,
      Provider<InventoryRepository> inventoryRepositoryProvider,
      Provider<ProductBatchRepository> productBatchRepositoryProvider,
      Provider<PurchaseOrderDao> purchaseOrderDaoProvider) {
    this.grnRepositoryProvider = grnRepositoryProvider;
    this.inventoryRepositoryProvider = inventoryRepositoryProvider;
    this.productBatchRepositoryProvider = productBatchRepositoryProvider;
    this.purchaseOrderDaoProvider = purchaseOrderDaoProvider;
  }

  @Override
  public ConfirmGrnUseCase get() {
    return newInstance(grnRepositoryProvider.get(), inventoryRepositoryProvider.get(), productBatchRepositoryProvider.get(), purchaseOrderDaoProvider.get());
  }

  public static ConfirmGrnUseCase_Factory create(Provider<GrnRepository> grnRepositoryProvider,
      Provider<InventoryRepository> inventoryRepositoryProvider,
      Provider<ProductBatchRepository> productBatchRepositoryProvider,
      Provider<PurchaseOrderDao> purchaseOrderDaoProvider) {
    return new ConfirmGrnUseCase_Factory(grnRepositoryProvider, inventoryRepositoryProvider, productBatchRepositoryProvider, purchaseOrderDaoProvider);
  }

  public static ConfirmGrnUseCase newInstance(GrnRepository grnRepository,
      InventoryRepository inventoryRepository, ProductBatchRepository productBatchRepository,
      PurchaseOrderDao purchaseOrderDao) {
    return new ConfirmGrnUseCase(grnRepository, inventoryRepository, productBatchRepository, purchaseOrderDao);
  }
}
