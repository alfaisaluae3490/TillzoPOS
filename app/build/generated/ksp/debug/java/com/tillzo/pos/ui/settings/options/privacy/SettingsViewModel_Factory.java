package com.tillzo.pos.ui.settings.options.privacy;

import com.tillzo.pos.data.local.prefs.AppSetupPrefs;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<AppSetupPrefs> appSetupPrefsProvider;

  public SettingsViewModel_Factory(Provider<AppSetupPrefs> appSetupPrefsProvider) {
    this.appSetupPrefsProvider = appSetupPrefsProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(appSetupPrefsProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<AppSetupPrefs> appSetupPrefsProvider) {
    return new SettingsViewModel_Factory(appSetupPrefsProvider);
  }

  public static SettingsViewModel newInstance(AppSetupPrefs appSetupPrefs) {
    return new SettingsViewModel(appSetupPrefs);
  }
}
