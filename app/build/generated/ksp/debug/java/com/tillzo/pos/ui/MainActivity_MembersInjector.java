package com.tillzo.pos.ui;

import com.tillzo.pos.data.local.prefs.AppSetupPrefs;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<AppSetupPrefs> appSetupPrefsProvider;

  public MainActivity_MembersInjector(Provider<AppSetupPrefs> appSetupPrefsProvider) {
    this.appSetupPrefsProvider = appSetupPrefsProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    return new MainActivity_MembersInjector(appSetupPrefsProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectAppSetupPrefs(instance, appSetupPrefsProvider.get());
  }

  @InjectedFieldSignature("com.tillzo.pos.ui.MainActivity.appSetupPrefs")
  public static void injectAppSetupPrefs(MainActivity instance, AppSetupPrefs appSetupPrefs) {
    instance.appSetupPrefs = appSetupPrefs;
  }
}
