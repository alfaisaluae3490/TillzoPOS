package com.tillzo.pos.data.sync.options.micro;

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
public final class MicroBatchManager_Factory implements Factory<MicroBatchManager> {
  private final Provider<DataSyncInterface> syncInterfaceProvider;

  public MicroBatchManager_Factory(Provider<DataSyncInterface> syncInterfaceProvider) {
    this.syncInterfaceProvider = syncInterfaceProvider;
  }

  @Override
  public MicroBatchManager get() {
    return newInstance(syncInterfaceProvider.get());
  }

  public static MicroBatchManager_Factory create(
      Provider<DataSyncInterface> syncInterfaceProvider) {
    return new MicroBatchManager_Factory(syncInterfaceProvider);
  }

  public static MicroBatchManager newInstance(DataSyncInterface syncInterface) {
    return new MicroBatchManager(syncInterface);
  }
}
