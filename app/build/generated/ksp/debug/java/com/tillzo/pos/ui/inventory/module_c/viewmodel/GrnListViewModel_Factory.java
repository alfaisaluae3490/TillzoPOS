package com.tillzo.pos.ui.inventory.module_c.viewmodel;

import com.tillzo.pos.domain.usecase.grn.GetGrnsUseCase;
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
public final class GrnListViewModel_Factory implements Factory<GrnListViewModel> {
  private final Provider<GetGrnsUseCase> getGrnsUseCaseProvider;

  public GrnListViewModel_Factory(Provider<GetGrnsUseCase> getGrnsUseCaseProvider) {
    this.getGrnsUseCaseProvider = getGrnsUseCaseProvider;
  }

  @Override
  public GrnListViewModel get() {
    return newInstance(getGrnsUseCaseProvider.get());
  }

  public static GrnListViewModel_Factory create(Provider<GetGrnsUseCase> getGrnsUseCaseProvider) {
    return new GrnListViewModel_Factory(getGrnsUseCaseProvider);
  }

  public static GrnListViewModel newInstance(GetGrnsUseCase getGrnsUseCase) {
    return new GrnListViewModel(getGrnsUseCase);
  }
}
