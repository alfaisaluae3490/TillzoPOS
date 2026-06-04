package com.tillzo.pos.ui.inventory.module_b.viewmodel;

import com.tillzo.pos.domain.usecase.po.GetPurchaseOrdersUseCase;
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
public final class PurchaseOrderListViewModel_Factory implements Factory<PurchaseOrderListViewModel> {
  private final Provider<GetPurchaseOrdersUseCase> getPOsUseCaseProvider;

  public PurchaseOrderListViewModel_Factory(
      Provider<GetPurchaseOrdersUseCase> getPOsUseCaseProvider) {
    this.getPOsUseCaseProvider = getPOsUseCaseProvider;
  }

  @Override
  public PurchaseOrderListViewModel get() {
    return newInstance(getPOsUseCaseProvider.get());
  }

  public static PurchaseOrderListViewModel_Factory create(
      Provider<GetPurchaseOrdersUseCase> getPOsUseCaseProvider) {
    return new PurchaseOrderListViewModel_Factory(getPOsUseCaseProvider);
  }

  public static PurchaseOrderListViewModel newInstance(GetPurchaseOrdersUseCase getPOsUseCase) {
    return new PurchaseOrderListViewModel(getPOsUseCase);
  }
}
