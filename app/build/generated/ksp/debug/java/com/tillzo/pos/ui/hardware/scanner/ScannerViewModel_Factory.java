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
public final class ScannerViewModel_Factory implements Factory<ScannerViewModel> {
  private final Provider<InventoryDao> inventoryDaoProvider;

  public ScannerViewModel_Factory(Provider<InventoryDao> inventoryDaoProvider) {
    this.inventoryDaoProvider = inventoryDaoProvider;
  }

  @Override
  public ScannerViewModel get() {
    return newInstance(inventoryDaoProvider.get());
  }

  public static ScannerViewModel_Factory create(Provider<InventoryDao> inventoryDaoProvider) {
    return new ScannerViewModel_Factory(inventoryDaoProvider);
  }

  public static ScannerViewModel newInstance(InventoryDao inventoryDao) {
    return new ScannerViewModel(inventoryDao);
  }
}
