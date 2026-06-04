package com.tillzo.pos.domain.sync.usecase;

import com.tillzo.pos.data.remote.SheetsRemoteDataSource;
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
public final class SchemaGuardUseCase_Factory implements Factory<SchemaGuardUseCase> {
  private final Provider<SheetsRemoteDataSource> dataSourceProvider;

  public SchemaGuardUseCase_Factory(Provider<SheetsRemoteDataSource> dataSourceProvider) {
    this.dataSourceProvider = dataSourceProvider;
  }

  @Override
  public SchemaGuardUseCase get() {
    return newInstance(dataSourceProvider.get());
  }

  public static SchemaGuardUseCase_Factory create(
      Provider<SheetsRemoteDataSource> dataSourceProvider) {
    return new SchemaGuardUseCase_Factory(dataSourceProvider);
  }

  public static SchemaGuardUseCase newInstance(SheetsRemoteDataSource dataSource) {
    return new SchemaGuardUseCase(dataSource);
  }
}
