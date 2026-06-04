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
public final class UpdatePrefs_Factory implements Factory<UpdatePrefs> {
  private final Provider<Context> contextProvider;

  public UpdatePrefs_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public UpdatePrefs get() {
    return newInstance(contextProvider.get());
  }

  public static UpdatePrefs_Factory create(Provider<Context> contextProvider) {
    return new UpdatePrefs_Factory(contextProvider);
  }

  public static UpdatePrefs newInstance(Context context) {
    return new UpdatePrefs(context);
  }
}
