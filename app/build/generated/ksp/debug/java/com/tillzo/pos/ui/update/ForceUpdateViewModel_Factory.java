package com.tillzo.pos.ui.update;

import com.tillzo.pos.domain.update.CheckForceUpdateUseCase;
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
public final class ForceUpdateViewModel_Factory implements Factory<ForceUpdateViewModel> {
  private final Provider<CheckForceUpdateUseCase> checkForceUpdateUseCaseProvider;

  public ForceUpdateViewModel_Factory(
      Provider<CheckForceUpdateUseCase> checkForceUpdateUseCaseProvider) {
    this.checkForceUpdateUseCaseProvider = checkForceUpdateUseCaseProvider;
  }

  @Override
  public ForceUpdateViewModel get() {
    return newInstance(checkForceUpdateUseCaseProvider.get());
  }

  public static ForceUpdateViewModel_Factory create(
      Provider<CheckForceUpdateUseCase> checkForceUpdateUseCaseProvider) {
    return new ForceUpdateViewModel_Factory(checkForceUpdateUseCaseProvider);
  }

  public static ForceUpdateViewModel newInstance(CheckForceUpdateUseCase checkForceUpdateUseCase) {
    return new ForceUpdateViewModel(checkForceUpdateUseCase);
  }
}
