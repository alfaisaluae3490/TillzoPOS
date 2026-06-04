package com.tillzo.pos.data.sync.options.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.tillzo.pos.data.local.prefs.AppSetupPrefs;
import com.tillzo.pos.domain.sync.DataSyncInterface;
import dagger.internal.DaggerGenerated;
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
public final class DisasterWorker_Factory {
  private final Provider<DataSyncInterface> syncInterfaceProvider;

  private final Provider<AppSetupPrefs> appSetupPrefsProvider;

  public DisasterWorker_Factory(Provider<DataSyncInterface> syncInterfaceProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    this.syncInterfaceProvider = syncInterfaceProvider;
    this.appSetupPrefsProvider = appSetupPrefsProvider;
  }

  public DisasterWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, syncInterfaceProvider.get(), appSetupPrefsProvider.get());
  }

  public static DisasterWorker_Factory create(Provider<DataSyncInterface> syncInterfaceProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    return new DisasterWorker_Factory(syncInterfaceProvider, appSetupPrefsProvider);
  }

  public static DisasterWorker newInstance(Context context, WorkerParameters params,
      DataSyncInterface syncInterface, AppSetupPrefs appSetupPrefs) {
    return new DisasterWorker(context, params, syncInterface, appSetupPrefs);
  }
}
