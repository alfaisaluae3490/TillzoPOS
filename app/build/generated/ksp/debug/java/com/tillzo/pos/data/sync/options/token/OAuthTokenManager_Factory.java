package com.tillzo.pos.data.sync.options.token;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class OAuthTokenManager_Factory implements Factory<OAuthTokenManager> {
  private final Provider<Context> contextProvider;

  public OAuthTokenManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public OAuthTokenManager get() {
    return newInstance(contextProvider.get());
  }

  public static OAuthTokenManager_Factory create(Provider<Context> contextProvider) {
    return new OAuthTokenManager_Factory(contextProvider);
  }

  public static OAuthTokenManager newInstance(Context context) {
    return new OAuthTokenManager(context);
  }
}
