package com.tillzo.pos.domain.usecase.po;

import com.tillzo.pos.data.local.dao.PurchaseOrderDao;
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
public final class GetPurchaseOrdersUseCase_Factory implements Factory<GetPurchaseOrdersUseCase> {
  private final Provider<PurchaseOrderDao> poDaoProvider;

  public GetPurchaseOrdersUseCase_Factory(Provider<PurchaseOrderDao> poDaoProvider) {
    this.poDaoProvider = poDaoProvider;
  }

  @Override
  public GetPurchaseOrdersUseCase get() {
    return newInstance(poDaoProvider.get());
  }

  public static GetPurchaseOrdersUseCase_Factory create(Provider<PurchaseOrderDao> poDaoProvider) {
    return new GetPurchaseOrdersUseCase_Factory(poDaoProvider);
  }

  public static GetPurchaseOrdersUseCase newInstance(PurchaseOrderDao poDao) {
    return new GetPurchaseOrdersUseCase(poDao);
  }
}
