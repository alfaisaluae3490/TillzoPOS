package com.tillzo.pos.di;

import com.tillzo.pos.data.sync.RestApiSyncImpl;
import com.tillzo.pos.domain.sync.DataSyncInterface;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class SyncModule_ProvideDataSyncInterfaceFactory implements Factory<DataSyncInterface> {
  private final Provider<RestApiSyncImpl> restApiImplProvider;

  public SyncModule_ProvideDataSyncInterfaceFactory(Provider<RestApiSyncImpl> restApiImplProvider) {
    this.restApiImplProvider = restApiImplProvider;
  }

  @Override
  public DataSyncInterface get() {
    return provideDataSyncInterface(restApiImplProvider.get());
  }

  public static SyncModule_ProvideDataSyncInterfaceFactory create(
      Provider<RestApiSyncImpl> restApiImplProvider) {
    return new SyncModule_ProvideDataSyncInterfaceFactory(restApiImplProvider);
  }

  public static DataSyncInterface provideDataSyncInterface(RestApiSyncImpl restApiImpl) {
    return Preconditions.checkNotNullFromProvides(SyncModule.INSTANCE.provideDataSyncInterface(restApiImpl));
  }
}
