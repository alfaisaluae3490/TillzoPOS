package com.tillzo.pos.data.repository;

import com.tillzo.pos.data.local.dao.StockAdjustmentDao;
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
public final class StockAdjustmentRepositoryImpl_Factory implements Factory<StockAdjustmentRepositoryImpl> {
  private final Provider<StockAdjustmentDao> daoProvider;

  public StockAdjustmentRepositoryImpl_Factory(Provider<StockAdjustmentDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public StockAdjustmentRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static StockAdjustmentRepositoryImpl_Factory create(
      Provider<StockAdjustmentDao> daoProvider) {
    return new StockAdjustmentRepositoryImpl_Factory(daoProvider);
  }

  public static StockAdjustmentRepositoryImpl newInstance(StockAdjustmentDao dao) {
    return new StockAdjustmentRepositoryImpl(dao);
  }
}
