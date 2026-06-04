package com.tillzo.pos.domain.update;

import com.tillzo.pos.data.local.prefs.UpdatePrefs;
import com.tillzo.pos.domain.sync.DataSyncInterface;
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
public final class CheckForceUpdateUseCase_Factory implements Factory<CheckForceUpdateUseCase> {
  private final Provider<DataSyncInterface> syncInterfaceProvider;

  private final Provider<UpdatePrefs> updatePrefsProvider;

  public CheckForceUpdateUseCase_Factory(Provider<DataSyncInterface> syncInterfaceProvider,
      Provider<UpdatePrefs> updatePrefsProvider) {
    this.syncInterfaceProvider = syncInterfaceProvider;
    this.updatePrefsProvider = updatePrefsProvider;
  }

  @Override
  public CheckForceUpdateUseCase get() {
    return newInstance(syncInterfaceProvider.get(), updatePrefsProvider.get());
  }

  public static CheckForceUpdateUseCase_Factory create(
      Provider<DataSyncInterface> syncInterfaceProvider,
      Provider<UpdatePrefs> updatePrefsProvider) {
    return new CheckForceUpdateUseCase_Factory(syncInterfaceProvider, updatePrefsProvider);
  }

  public static CheckForceUpdateUseCase newInstance(DataSyncInterface syncInterface,
      UpdatePrefs updatePrefs) {
    return new CheckForceUpdateUseCase(syncInterface, updatePrefs);
  }
}
