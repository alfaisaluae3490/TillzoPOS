package com.tillzo.pos.ui.inventory.options.wastage;

import android.content.Context;
import com.tillzo.pos.data.local.dao.InventoryDao;
import com.tillzo.pos.data.local.dao.ProductBatchDao;
import com.tillzo.pos.data.local.dao.WastageDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class WastageViewModel_Factory implements Factory<WastageViewModel> {
  private final Provider<WastageDao> wastageDaoProvider;

  private final Provider<InventoryDao> inventoryDaoProvider;

  private final Provider<ProductBatchDao> productBatchDaoProvider;

  private final Provider<Context> contextProvider;

  public WastageViewModel_Factory(Provider<WastageDao> wastageDaoProvider,
      Provider<InventoryDao> inventoryDaoProvider,
      Provider<ProductBatchDao> productBatchDaoProvider, Provider<Context> contextProvider) {
    this.wastageDaoProvider = wastageDaoProvider;
    this.inventoryDaoProvider = inventoryDaoProvider;
    this.productBatchDaoProvider = productBatchDaoProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public WastageViewModel get() {
    return newInstance(wastageDaoProvider.get(), inventoryDaoProvider.get(), productBatchDaoProvider.get(), contextProvider.get());
  }

  public static WastageViewModel_Factory create(Provider<WastageDao> wastageDaoProvider,
      Provider<InventoryDao> inventoryDaoProvider,
      Provider<ProductBatchDao> productBatchDaoProvider, Provider<Context> contextProvider) {
    return new WastageViewModel_Factory(wastageDaoProvider, inventoryDaoProvider, productBatchDaoProvider, contextProvider);
  }

  public static WastageViewModel newInstance(WastageDao wastageDao, InventoryDao inventoryDao,
      ProductBatchDao productBatchDao, Context context) {
    return new WastageViewModel(wastageDao, inventoryDao, productBatchDao, context);
  }
}
