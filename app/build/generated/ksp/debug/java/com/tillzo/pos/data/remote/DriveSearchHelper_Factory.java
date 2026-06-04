package com.tillzo.pos.data.remote;

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
public final class DriveSearchHelper_Factory implements Factory<DriveSearchHelper> {
  private final Provider<SheetsApiClient> sheetsApiClientProvider;

  public DriveSearchHelper_Factory(Provider<SheetsApiClient> sheetsApiClientProvider) {
    this.sheetsApiClientProvider = sheetsApiClientProvider;
  }

  @Override
  public DriveSearchHelper get() {
    return newInstance(sheetsApiClientProvider.get());
  }

  public static DriveSearchHelper_Factory create(
      Provider<SheetsApiClient> sheetsApiClientProvider) {
    return new DriveSearchHelper_Factory(sheetsApiClientProvider);
  }

  public static DriveSearchHelper newInstance(SheetsApiClient sheetsApiClient) {
    return new DriveSearchHelper(sheetsApiClient);
  }
}
