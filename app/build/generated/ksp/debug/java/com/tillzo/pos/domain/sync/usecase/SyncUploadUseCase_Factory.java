package com.tillzo.pos.domain.sync.usecase;

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
public final class SyncUploadUseCase_Factory implements Factory<SyncUploadUseCase> {
  private final Provider<DataSyncInterface> syncInterfaceProvider;

  public SyncUploadUseCase_Factory(Provider<DataSyncInterface> syncInterfaceProvider) {
    this.syncInterfaceProvider = syncInterfaceProvider;
  }

  @Override
  public SyncUploadUseCase get() {
    return newInstance(syncInterfaceProvider.get());
  }

  public static SyncUploadUseCase_Factory create(
      Provider<DataSyncInterface> syncInterfaceProvider) {
    return new SyncUploadUseCase_Factory(syncInterfaceProvider);
  }

  public static SyncUploadUseCase newInstance(DataSyncInterface syncInterface) {
    return new SyncUploadUseCase(syncInterface);
  }
}
