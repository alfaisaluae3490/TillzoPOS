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
public final class GetGrnDetailUseCase_Factory implements Factory<GetGrnDetailUseCase> {
  private final Provider<GrnRepository> repoProvider;

  public GetGrnDetailUseCase_Factory(Provider<GrnRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public GetGrnDetailUseCase get() {
    return newInstance(repoProvider.get());
  }

  public static GetGrnDetailUseCase_Factory create(Provider<GrnRepository> repoProvider) {
    return new GetGrnDetailUseCase_Factory(repoProvider);
  }

  public static GetGrnDetailUseCase newInstance(GrnRepository repo) {
    return new GetGrnDetailUseCase(repo);
  }
}
