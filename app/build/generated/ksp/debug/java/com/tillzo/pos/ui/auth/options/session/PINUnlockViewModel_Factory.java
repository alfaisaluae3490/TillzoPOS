package com.tillzo.pos.ui.auth.options.session;

import com.tillzo.pos.domain.repository.AuthRepository;
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
public final class PINUnlockViewModel_Factory implements Factory<PINUnlockViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public PINUnlockViewModel_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public PINUnlockViewModel get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static PINUnlockViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider) {
    return new PINUnlockViewModel_Factory(authRepositoryProvider);
  }

  public static PINUnlockViewModel newInstance(AuthRepository authRepository) {
    return new PINUnlockViewModel(authRepository);
  }
}
