package com.tillzo.pos.data.repository;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<Context> contextProvider;

  public AuthRepositoryImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(contextProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<Context> contextProvider) {
    return new AuthRepositoryImpl_Factory(contextProvider);
  }

  public static AuthRepositoryImpl newInstance(Context context) {
    return new AuthRepositoryImpl(context);
  }
}
