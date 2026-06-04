package com.tillzo.pos.ui.setup;

import com.tillzo.pos.data.local.prefs.AppSetupPrefs;
import com.tillzo.pos.data.remote.DriveSearchHelper;
import com.tillzo.pos.data.remote.SheetsRemoteDataSource;
import com.tillzo.pos.data.repository.SheetsRepository;
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
public final class SheetPickerViewModel_Factory implements Factory<SheetPickerViewModel> {
  private final Provider<AppSetupPrefs> appSetupPrefsProvider;

  private final Provider<DriveSearchHelper> driveSearchHelperProvider;

  private final Provider<SheetsRepository> sheetsRepositoryProvider;

  private final Provider<SheetsRemoteDataSource> sheetsRemoteDataSourceProvider;

  public SheetPickerViewModel_Factory(Provider<AppSetupPrefs> appSetupPrefsProvider,
      Provider<DriveSearchHelper> driveSearchHelperProvider,
      Provider<SheetsRepository> sheetsRepositoryProvider,
      Provider<SheetsRemoteDataSource> sheetsRemoteDataSourceProvider) {
    this.appSetupPrefsProvider = appSetupPrefsProvider;
    this.driveSearchHelperProvider = driveSearchHelperProvider;
    this.sheetsRepositoryProvider = sheetsRepositoryProvider;
    this.sheetsRemoteDataSourceProvider = sheetsRemoteDataSourceProvider;
  }

  @Override
  public SheetPickerViewModel get() {
    return newInstance(appSetupPrefsProvider.get(), driveSearchHelperProvider.get(), sheetsRepositoryProvider.get(), sheetsRemoteDataSourceProvider.get());
  }

  public static SheetPickerViewModel_Factory create(Provider<AppSetupPrefs> appSetupPrefsProvider,
      Provider<DriveSearchHelper> driveSearchHelperProvider,
      Provider<SheetsRepository> sheetsRepositoryProvider,
      Provider<SheetsRemoteDataSource> sheetsRemoteDataSourceProvider) {
    return new SheetPickerViewModel_Factory(appSetupPrefsProvider, driveSearchHelperProvider, sheetsRepositoryProvider, sheetsRemoteDataSourceProvider);
  }

  public static SheetPickerViewModel newInstance(AppSetupPrefs appSetupPrefs,
      DriveSearchHelper driveSearchHelper, SheetsRepository sheetsRepository,
      SheetsRemoteDataSource sheetsRemoteDataSource) {
    return new SheetPickerViewModel(appSetupPrefs, driveSearchHelper, sheetsRepository, sheetsRemoteDataSource);
  }
}
