package com.tillzo.pos.domain.provisioning;

import com.tillzo.pos.data.local.prefs.AppSetupPrefs;
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
public final class ProvisionWorkspaceUseCase_Factory implements Factory<ProvisionWorkspaceUseCase> {
  private final Provider<SheetsRepository> sheetsRepositoryProvider;

  private final Provider<AppSetupPrefs> appSetupPrefsProvider;

  public ProvisionWorkspaceUseCase_Factory(Provider<SheetsRepository> sheetsRepositoryProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    this.sheetsRepositoryProvider = sheetsRepositoryProvider;
    this.appSetupPrefsProvider = appSetupPrefsProvider;
  }

  @Override
  public ProvisionWorkspaceUseCase get() {
    return newInstance(sheetsRepositoryProvider.get(), appSetupPrefsProvider.get());
  }

  public static ProvisionWorkspaceUseCase_Factory create(
      Provider<SheetsRepository> sheetsRepositoryProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    return new ProvisionWorkspaceUseCase_Factory(sheetsRepositoryProvider, appSetupPrefsProvider);
  }

  public static ProvisionWorkspaceUseCase newInstance(SheetsRepository sheetsRepository,
      AppSetupPrefs appSetupPrefs) {
    return new ProvisionWorkspaceUseCase(sheetsRepository, appSetupPrefs);
  }
}
