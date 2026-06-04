package com.tillzo.pos.data.remote;

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
public final class SheetsApiClient_Factory implements Factory<SheetsApiClient> {
  private final Provider<Context> contextProvider;

  public SheetsApiClient_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SheetsApiClient get() {
    return newInstance(contextProvider.get());
  }

  public static SheetsApiClient_Factory create(Provider<Context> contextProvider) {
    return new SheetsApiClient_Factory(contextProvider);
  }

  public static SheetsApiClient newInstance(Context context) {
    return new SheetsApiClient(context);
  }
}
