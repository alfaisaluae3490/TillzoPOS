package com.tillzo.pos.data.remote;

import com.tillzo.pos.data.local.prefs.AppSetupPrefs;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SheetsRemoteDataSource_Factory implements Factory<SheetsRemoteDataSource> {
  private final Provider<SheetsApiClient> apiClientProvider;

  private final Provider<AppSetupPrefs> appSetupPrefsProvider;

  public SheetsRemoteDataSource_Factory(Provider<SheetsApiClient> apiClientProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    this.apiClientProvider = apiClientProvider;
    this.appSetupPrefsProvider = appSetupPrefsProvider;
  }

  @Override
  public SheetsRemoteDataSource get() {
    return newInstance(apiClientProvider.get(), appSetupPrefsProvider.get());
  }

  public static SheetsRemoteDataSource_Factory create(Provider<SheetsApiClient> apiClientProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    return new SheetsRemoteDataSource_Factory(apiClientProvider, appSetupPrefsProvider);
  }

  public static SheetsRemoteDataSource newInstance(SheetsApiClient apiClient,
      AppSetupPrefs appSetupPrefs) {
    return new SheetsRemoteDataSource(apiClient, appSetupPrefs);
  }
}
