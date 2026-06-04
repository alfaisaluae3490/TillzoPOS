package com.tillzo.pos.data.sync;

import com.tillzo.pos.data.repository.SheetsRepository;
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
public final class RestApiSyncImpl_Factory implements Factory<RestApiSyncImpl> {
  private final Provider<SheetsRepository> sheetsRepositoryProvider;

  public RestApiSyncImpl_Factory(Provider<SheetsRepository> sheetsRepositoryProvider) {
    this.sheetsRepositoryProvider = sheetsRepositoryProvider;
  }

  @Override
  public RestApiSyncImpl get() {
    return newInstance(sheetsRepositoryProvider.get());
  }

  public static RestApiSyncImpl_Factory create(
      Provider<SheetsRepository> sheetsRepositoryProvider) {
    return new RestApiSyncImpl_Factory(sheetsRepositoryProvider);
  }

  public static RestApiSyncImpl newInstance(SheetsRepository sheetsRepository) {
    return new RestApiSyncImpl(sheetsRepository);
  }
}
