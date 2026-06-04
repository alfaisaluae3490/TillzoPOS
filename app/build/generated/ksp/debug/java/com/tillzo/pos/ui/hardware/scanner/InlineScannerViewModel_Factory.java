package com.tillzo.pos.ui.hardware.scanner;

import com.tillzo.pos.data.local.dao.InventoryDao;
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
public final class InlineScannerViewModel_Factory implements Factory<InlineScannerViewModel> {
  private final Provider<InventoryDao> inventoryDaoProvider;

  public InlineScannerViewModel_Factory(Provider<InventoryDao> inventoryDaoProvider) {
    this.inventoryDaoProvider = inventoryDaoProvider;
  }

  @Override
  public InlineScannerViewModel get() {
    return newInstance(inventoryDaoProvider.get());
  }

  public static InlineScannerViewModel_Factory create(Provider<InventoryDao> inventoryDaoProvider) {
    return new InlineScannerViewModel_Factory(inventoryDaoProvider);
  }

  public static InlineScannerViewModel newInstance(InventoryDao inventoryDao) {
    return new InlineScannerViewModel(inventoryDao);
  }
}
