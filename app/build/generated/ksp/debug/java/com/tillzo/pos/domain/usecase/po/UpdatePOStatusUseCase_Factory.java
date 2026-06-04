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
public final class UpdatePOStatusUseCase_Factory implements Factory<UpdatePOStatusUseCase> {
  private final Provider<PurchaseOrderDao> poDaoProvider;

  public UpdatePOStatusUseCase_Factory(Provider<PurchaseOrderDao> poDaoProvider) {
    this.poDaoProvider = poDaoProvider;
  }

  @Override
  public UpdatePOStatusUseCase get() {
    return newInstance(poDaoProvider.get());
  }

  public static UpdatePOStatusUseCase_Factory create(Provider<PurchaseOrderDao> poDaoProvider) {
    return new UpdatePOStatusUseCase_Factory(poDaoProvider);
  }

  public static UpdatePOStatusUseCase newInstance(PurchaseOrderDao poDao) {
    return new UpdatePOStatusUseCase(poDao);
  }
}
