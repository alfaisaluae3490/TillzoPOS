package com.tillzo.pos.data.repository;

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
public final class ProductBatchRepositoryImpl_Factory implements Factory<ProductBatchRepositoryImpl> {
  private final Provider<ProductBatchDao> productBatchDaoProvider;

  public ProductBatchRepositoryImpl_Factory(Provider<ProductBatchDao> productBatchDaoProvider) {
    this.productBatchDaoProvider = productBatchDaoProvider;
  }

  @Override
  public ProductBatchRepositoryImpl get() {
    return newInstance(productBatchDaoProvider.get());
  }

  public static ProductBatchRepositoryImpl_Factory create(
      Provider<ProductBatchDao> productBatchDaoProvider) {
    return new ProductBatchRepositoryImpl_Factory(productBatchDaoProvider);
  }

  public static ProductBatchRepositoryImpl newInstance(ProductBatchDao productBatchDao) {
    return new ProductBatchRepositoryImpl(productBatchDao);
  }
}
