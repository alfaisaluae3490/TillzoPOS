package com.tillzo.pos.ui.inventory;

import com.tillzo.pos.data.local.dao.ProductUnitDao;
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
public final class ProductUnitsViewModel_Factory implements Factory<ProductUnitsViewModel> {
  private final Provider<ProductUnitDao> productUnitDaoProvider;

  public ProductUnitsViewModel_Factory(Provider<ProductUnitDao> productUnitDaoProvider) {
    this.productUnitDaoProvider = productUnitDaoProvider;
  }

  @Override
  public ProductUnitsViewModel get() {
    return newInstance(productUnitDaoProvider.get());
  }

  public static ProductUnitsViewModel_Factory create(
      Provider<ProductUnitDao> productUnitDaoProvider) {
    return new ProductUnitsViewModel_Factory(productUnitDaoProvider);
  }

  public static ProductUnitsViewModel newInstance(ProductUnitDao productUnitDao) {
    return new ProductUnitsViewModel(productUnitDao);
  }
}
