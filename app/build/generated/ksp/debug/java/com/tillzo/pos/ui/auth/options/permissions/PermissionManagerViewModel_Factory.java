package com.tillzo.pos.ui.auth.options.permissions;

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
public final class PermissionManagerViewModel_Factory implements Factory<PermissionManagerViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  public PermissionManagerViewModel_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public PermissionManagerViewModel get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static PermissionManagerViewModel_Factory create(
      Provider<UserRepository> userRepositoryProvider) {
    return new PermissionManagerViewModel_Factory(userRepositoryProvider);
  }

  public static PermissionManagerViewModel newInstance(UserRepository userRepository) {
    return new PermissionManagerViewModel(userRepository);
  }
}
