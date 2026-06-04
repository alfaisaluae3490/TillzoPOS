package com.tillzo.pos.ui.signin;

import android.content.Context;
import com.tillzo.pos.data.local.prefs.AppSetupPrefs;
import com.tillzo.pos.domain.setup.SheetSetupUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SignInViewModel_Factory implements Factory<SignInViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<SheetSetupUseCase> sheetSetupUseCaseProvider;

  private final Provider<AppSetupPrefs> appSetupPrefsProvider;

  public SignInViewModel_Factory(Provider<Context> contextProvider,
      Provider<SheetSetupUseCase> sheetSetupUseCaseProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    this.contextProvider = contextProvider;
    this.sheetSetupUseCaseProvider = sheetSetupUseCaseProvider;
    this.appSetupPrefsProvider = appSetupPrefsProvider;
  }

  @Override
  public SignInViewModel get() {
    return newInstance(contextProvider.get(), sheetSetupUseCaseProvider.get(), appSetupPrefsProvider.get());
  }

  public static SignInViewModel_Factory create(Provider<Context> contextProvider,
      Provider<SheetSetupUseCase> sheetSetupUseCaseProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    return new SignInViewModel_Factory(contextProvider, sheetSetupUseCaseProvider, appSetupPrefsProvider);
  }

  public static SignInViewModel newInstance(Context context, SheetSetupUseCase sheetSetupUseCase,
      AppSetupPrefs appSetupPrefs) {
    return new SignInViewModel(context, sheetSetupUseCase, appSetupPrefs);
  }
}
