package com.tillzo.pos.ui.auth.options.usermanagement;

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
public final class UserManagementViewModel_Factory implements Factory<UserManagementViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  public UserManagementViewModel_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public UserManagementViewModel get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static UserManagementViewModel_Factory create(
      Provider<UserRepository> userRepositoryProvider) {
    return new UserManagementViewModel_Factory(userRepositoryProvider);
  }

  public static UserManagementViewModel newInstance(UserRepository userRepository) {
    return new UserManagementViewModel(userRepository);
  }
}
