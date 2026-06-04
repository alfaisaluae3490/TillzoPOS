package com.tillzo.pos.data.remote;

import android.content.Context;
import com.tillzo.pos.data.local.prefs.AppSetupPrefs;
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
public final class TokenProvider_Factory implements Factory<TokenProvider> {
  private final Provider<Context> contextProvider;

  private final Provider<AppSetupPrefs> appSetupPrefsProvider;

  public TokenProvider_Factory(Provider<Context> contextProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    this.contextProvider = contextProvider;
    this.appSetupPrefsProvider = appSetupPrefsProvider;
  }

  @Override
  public TokenProvider get() {
    return newInstance(contextProvider.get(), appSetupPrefsProvider.get());
  }

  public static TokenProvider_Factory create(Provider<Context> contextProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    return new TokenProvider_Factory(contextProvider, appSetupPrefsProvider);
  }

  public static TokenProvider newInstance(Context context, AppSetupPrefs appSetupPrefs) {
    return new TokenProvider(context, appSetupPrefs);
  }
}
