package com.tillzo.pos.domain.auth;

import com.tillzo.pos.domain.repository.AuthRepository;
import com.tillzo.pos.domain.repository.UserRepository;
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
public final class SessionGuardUseCase_Factory implements Factory<SessionGuardUseCase> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public SessionGuardUseCase_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public SessionGuardUseCase get() {
    return newInstance(authRepositoryProvider.get(), userRepositoryProvider.get());
  }

  public static SessionGuardUseCase_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new SessionGuardUseCase_Factory(authRepositoryProvider, userRepositoryProvider);
  }

  public static SessionGuardUseCase newInstance(AuthRepository authRepository,
      UserRepository userRepository) {
    return new SessionGuardUseCase(authRepository, userRepository);
  }
}
