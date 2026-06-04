package com.tillzo.pos.ui.till;

import com.tillzo.pos.data.local.dao.TillSessionDao;
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
public final class TillViewModel_Factory implements Factory<TillViewModel> {
  private final Provider<TillSessionDao> tillSessionDaoProvider;

  private final Provider<AppSetupPrefs> appSetupPrefsProvider;

  public TillViewModel_Factory(Provider<TillSessionDao> tillSessionDaoProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    this.tillSessionDaoProvider = tillSessionDaoProvider;
    this.appSetupPrefsProvider = appSetupPrefsProvider;
  }

  @Override
  public TillViewModel get() {
    return newInstance(tillSessionDaoProvider.get(), appSetupPrefsProvider.get());
  }

  public static TillViewModel_Factory create(Provider<TillSessionDao> tillSessionDaoProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    return new TillViewModel_Factory(tillSessionDaoProvider, appSetupPrefsProvider);
  }

  public static TillViewModel newInstance(TillSessionDao tillSessionDao,
      AppSetupPrefs appSetupPrefs) {
    return new TillViewModel(tillSessionDao, appSetupPrefs);
  }
}
