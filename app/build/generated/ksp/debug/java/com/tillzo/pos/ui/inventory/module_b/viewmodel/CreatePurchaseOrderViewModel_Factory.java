package com.tillzo.pos.ui.inventory.module_b.viewmodel;

import com.tillzo.pos.data.local.dao.InventoryDao;
import com.tillzo.pos.data.local.dao.PurchaseOrderDao;
import com.tillzo.pos.data.local.dao.VendorDao;
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
public final class CreatePurchaseOrderViewModel_Factory implements Factory<CreatePurchaseOrderViewModel> {
  private final Provider<PurchaseOrderDao> poDaoProvider;

  private final Provider<VendorDao> vendorDaoProvider;

  private final Provider<InventoryDao> inventoryDaoProvider;

  public CreatePurchaseOrderViewModel_Factory(Provider<PurchaseOrderDao> poDaoProvider,
      Provider<VendorDao> vendorDaoProvider, Provider<InventoryDao> inventoryDaoProvider) {
    this.poDaoProvider = poDaoProvider;
    this.vendorDaoProvider = vendorDaoProvider;
    this.inventoryDaoProvider = inventoryDaoProvider;
  }

  @Override
  public CreatePurchaseOrderViewModel get() {
    return newInstance(poDaoProvider.get(), vendorDaoProvider.get(), inventoryDaoProvider.get());
  }

  public static CreatePurchaseOrderViewModel_Factory create(
      Provider<PurchaseOrderDao> poDaoProvider, Provider<VendorDao> vendorDaoProvider,
      Provider<InventoryDao> inventoryDaoProvider) {
    return new CreatePurchaseOrderViewModel_Factory(poDaoProvider, vendorDaoProvider, inventoryDaoProvider);
  }

  public static CreatePurchaseOrderViewModel newInstance(PurchaseOrderDao poDao,
      VendorDao vendorDao, InventoryDao inventoryDao) {
    return new CreatePurchaseOrderViewModel(poDao, vendorDao, inventoryDao);
  }
}
