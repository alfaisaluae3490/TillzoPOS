package com.tillzo.pos.domain.usecase.batch;

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
public final class GetBatchesForProductUseCase_Factory implements Factory<GetBatchesForProductUseCase> {
  private final Provider<ProductBatchDao> batchDaoProvider;

  public GetBatchesForProductUseCase_Factory(Provider<ProductBatchDao> batchDaoProvider) {
    this.batchDaoProvider = batchDaoProvider;
  }

  @Override
  public GetBatchesForProductUseCase get() {
    return newInstance(batchDaoProvider.get());
  }

  public static GetBatchesForProductUseCase_Factory create(
      Provider<ProductBatchDao> batchDaoProvider) {
    return new GetBatchesForProductUseCase_Factory(batchDaoProvider);
  }

  public static GetBatchesForProductUseCase newInstance(ProductBatchDao batchDao) {
    return new GetBatchesForProductUseCase(batchDao);
  }
}
