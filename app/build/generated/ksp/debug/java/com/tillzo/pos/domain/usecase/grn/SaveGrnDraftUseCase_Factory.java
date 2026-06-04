package com.tillzo.pos.domain.usecase.grn;

import com.tillzo.pos.domain.repository.GrnRepository;
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
public final class SaveGrnDraftUseCase_Factory implements Factory<SaveGrnDraftUseCase> {
  private final Provider<GrnRepository> repoProvider;

  public SaveGrnDraftUseCase_Factory(Provider<GrnRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public SaveGrnDraftUseCase get() {
    return newInstance(repoProvider.get());
  }

  public static SaveGrnDraftUseCase_Factory create(Provider<GrnRepository> repoProvider) {
    return new SaveGrnDraftUseCase_Factory(repoProvider);
  }

  public static SaveGrnDraftUseCase newInstance(GrnRepository repo) {
    return new SaveGrnDraftUseCase(repo);
  }
}
