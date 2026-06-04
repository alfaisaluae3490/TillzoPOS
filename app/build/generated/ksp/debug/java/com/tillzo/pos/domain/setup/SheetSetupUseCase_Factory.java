package com.tillzo.pos.domain.setup;

import com.tillzo.pos.data.local.prefs.AppSetupPrefs;
import com.tillzo.pos.data.repository.SheetsRepository;
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
public final class SheetSetupUseCase_Factory implements Factory<SheetSetupUseCase> {
  private final Provider<SheetsRepository> sheetsRepositoryProvider;

  private final Provider<AppSetupPrefs> appSetupPrefsProvider;

  public SheetSetupUseCase_Factory(Provider<SheetsRepository> sheetsRepositoryProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    this.sheetsRepositoryProvider = sheetsRepositoryProvider;
    this.appSetupPrefsProvider = appSetupPrefsProvider;
  }

  @Override
  public SheetSetupUseCase get() {
    return newInstance(sheetsRepositoryProvider.get(), appSetupPrefsProvider.get());
  }

  public static SheetSetupUseCase_Factory create(
      Provider<SheetsRepository> sheetsRepositoryProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    return new SheetSetupUseCase_Factory(sheetsRepositoryProvider, appSetupPrefsProvider);
  }

  public static SheetSetupUseCase newInstance(SheetsRepository sheetsRepository,
      AppSetupPrefs appSetupPrefs) {
    return new SheetSetupUseCase(sheetsRepository, appSetupPrefs);
  }
}
