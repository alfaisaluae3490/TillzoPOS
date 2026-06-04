package com.tillzo.pos.domain.usecase.security;

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
public final class CheckRootStatusUseCase_Factory implements Factory<CheckRootStatusUseCase> {
  private final Provider<Context> contextProvider;

  public CheckRootStatusUseCase_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CheckRootStatusUseCase get() {
    return newInstance(contextProvider.get());
  }

  public static CheckRootStatusUseCase_Factory create(Provider<Context> contextProvider) {
    return new CheckRootStatusUseCase_Factory(contextProvider);
  }

  public static CheckRootStatusUseCase newInstance(Context context) {
    return new CheckRootStatusUseCase(context);
  }
}
