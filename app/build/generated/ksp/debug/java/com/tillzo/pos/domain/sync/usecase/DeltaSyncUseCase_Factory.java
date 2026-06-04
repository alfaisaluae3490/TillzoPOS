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
public final class DeltaSyncUseCase_Factory implements Factory<DeltaSyncUseCase> {
  private final Provider<DataSyncInterface> syncInterfaceProvider;

  public DeltaSyncUseCase_Factory(Provider<DataSyncInterface> syncInterfaceProvider) {
    this.syncInterfaceProvider = syncInterfaceProvider;
  }

  @Override
  public DeltaSyncUseCase get() {
    return newInstance(syncInterfaceProvider.get());
  }

  public static DeltaSyncUseCase_Factory create(Provider<DataSyncInterface> syncInterfaceProvider) {
    return new DeltaSyncUseCase_Factory(syncInterfaceProvider);
  }

  public static DeltaSyncUseCase newInstance(DataSyncInterface syncInterface) {
    return new DeltaSyncUseCase(syncInterface);
  }
}
