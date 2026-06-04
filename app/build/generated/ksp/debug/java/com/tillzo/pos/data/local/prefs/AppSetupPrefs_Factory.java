package com.tillzo.pos.data.local.prefs;

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
public final class AppSetupPrefs_Factory implements Factory<AppSetupPrefs> {
  private final Provider<Context> contextProvider;

  public AppSetupPrefs_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AppSetupPrefs get() {
    return newInstance(contextProvider.get());
  }

  public static AppSetupPrefs_Factory create(Provider<Context> contextProvider) {
    return new AppSetupPrefs_Factory(contextProvider);
  }

  public static AppSetupPrefs newInstance(Context context) {
    return new AppSetupPrefs(context);
  }
}
