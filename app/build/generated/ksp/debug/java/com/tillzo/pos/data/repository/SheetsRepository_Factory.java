package com.tillzo.pos.data.repository;

import com.tillzo.pos.data.local.prefs.AppSetupPrefs;
import com.tillzo.pos.data.remote.SheetsRemoteDataSource;
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
public final class SheetsRepository_Factory implements Factory<SheetsRepository> {
  private final Provider<SheetsRemoteDataSource> dataSourceProvider;

  private final Provider<AppSetupPrefs> appSetupPrefsProvider;

  public SheetsRepository_Factory(Provider<SheetsRemoteDataSource> dataSourceProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    this.dataSourceProvider = dataSourceProvider;
    this.appSetupPrefsProvider = appSetupPrefsProvider;
  }

  @Override
  public SheetsRepository get() {
    return newInstance(dataSourceProvider.get(), appSetupPrefsProvider.get());
  }

  public static SheetsRepository_Factory create(Provider<SheetsRemoteDataSource> dataSourceProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    return new SheetsRepository_Factory(dataSourceProvider, appSetupPrefsProvider);
  }

  public static SheetsRepository newInstance(SheetsRemoteDataSource dataSource,
      AppSetupPrefs appSetupPrefs) {
    return new SheetsRepository(dataSource, appSetupPrefs);
  }
}
