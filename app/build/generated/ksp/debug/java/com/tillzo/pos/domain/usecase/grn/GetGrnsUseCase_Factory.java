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
public final class GetGrnsUseCase_Factory implements Factory<GetGrnsUseCase> {
  private final Provider<GrnRepository> repoProvider;

  public GetGrnsUseCase_Factory(Provider<GrnRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public GetGrnsUseCase get() {
    return newInstance(repoProvider.get());
  }

  public static GetGrnsUseCase_Factory create(Provider<GrnRepository> repoProvider) {
    return new GetGrnsUseCase_Factory(repoProvider);
  }

  public static GetGrnsUseCase newInstance(GrnRepository repo) {
    return new GetGrnsUseCase(repo);
  }
}
