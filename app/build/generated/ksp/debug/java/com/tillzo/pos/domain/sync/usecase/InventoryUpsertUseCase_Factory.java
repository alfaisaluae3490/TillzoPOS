package com.tillzo.pos.domain.sync.usecase;

import com.tillzo.pos.data.local.dao.InventoryDao;
import com.tillzo.pos.data.remote.SheetsRemoteDataSource;
import com.tillzo.pos.data.repository.SheetsRepository;
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
public final class InventoryUpsertUseCase_Factory implements Factory<InventoryUpsertUseCase> {
  private final Provider<InventoryDao> inventoryDaoProvider;

  private final Provider<SheetsRepository> sheetsRepositoryProvider;

  private final Provider<SheetsRemoteDataSource> dataSourceProvider;

  public InventoryUpsertUseCase_Factory(Provider<InventoryDao> inventoryDaoProvider,
      Provider<SheetsRepository> sheetsRepositoryProvider,
      Provider<SheetsRemoteDataSource> dataSourceProvider) {
    this.inventoryDaoProvider = inventoryDaoProvider;
    this.sheetsRepositoryProvider = sheetsRepositoryProvider;
    this.dataSourceProvider = dataSourceProvider;
  }

  @Override
  public InventoryUpsertUseCase get() {
    return newInstance(inventoryDaoProvider.get(), sheetsRepositoryProvider.get(), dataSourceProvider.get());
  }

  public static InventoryUpsertUseCase_Factory create(Provider<InventoryDao> inventoryDaoProvider,
      Provider<SheetsRepository> sheetsRepositoryProvider,
      Provider<SheetsRemoteDataSource> dataSourceProvider) {
    return new InventoryUpsertUseCase_Factory(inventoryDaoProvider, sheetsRepositoryProvider, dataSourceProvider);
  }

  public static InventoryUpsertUseCase newInstance(InventoryDao inventoryDao,
      SheetsRepository sheetsRepository, SheetsRemoteDataSource dataSource) {
    return new InventoryUpsertUseCase(inventoryDao, sheetsRepository, dataSource);
  }
}
