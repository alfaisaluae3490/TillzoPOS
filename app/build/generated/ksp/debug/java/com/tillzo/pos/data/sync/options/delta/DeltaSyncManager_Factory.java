package com.tillzo.pos.data.sync.options.delta;

import com.tillzo.pos.data.local.AppDatabase;
import com.tillzo.pos.domain.sync.DataSyncInterface;
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
public final class DeltaSyncManager_Factory implements Factory<DeltaSyncManager> {
  private final Provider<DataSyncInterface> syncInterfaceProvider;

  private final Provider<AppDatabase> appDatabaseProvider;

  public DeltaSyncManager_Factory(Provider<DataSyncInterface> syncInterfaceProvider,
      Provider<AppDatabase> appDatabaseProvider) {
    this.syncInterfaceProvider = syncInterfaceProvider;
    this.appDatabaseProvider = appDatabaseProvider;
  }

  @Override
  public DeltaSyncManager get() {
    return newInstance(syncInterfaceProvider.get(), appDatabaseProvider.get());
  }

  public static DeltaSyncManager_Factory create(Provider<DataSyncInterface> syncInterfaceProvider,
      Provider<AppDatabase> appDatabaseProvider) {
    return new DeltaSyncManager_Factory(syncInterfaceProvider, appDatabaseProvider);
  }

  public static DeltaSyncManager newInstance(DataSyncInterface syncInterface,
      AppDatabase appDatabase) {
    return new DeltaSyncManager(syncInterface, appDatabase);
  }
}
