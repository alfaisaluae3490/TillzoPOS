package com.tillzo.pos.domain.usecase.inventory;

import com.tillzo.pos.data.sync.SyncOrchestrator;
import com.tillzo.pos.domain.repository.InventoryRepository;
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
public final class AddProductUseCase_Factory implements Factory<AddProductUseCase> {
  private final Provider<InventoryRepository> inventoryRepositoryProvider;

  private final Provider<SyncOrchestrator> syncOrchestratorProvider;

  public AddProductUseCase_Factory(Provider<InventoryRepository> inventoryRepositoryProvider,
      Provider<SyncOrchestrator> syncOrchestratorProvider) {
    this.inventoryRepositoryProvider = inventoryRepositoryProvider;
    this.syncOrchestratorProvider = syncOrchestratorProvider;
  }

  @Override
  public AddProductUseCase get() {
    return newInstance(inventoryRepositoryProvider.get(), syncOrchestratorProvider.get());
  }

  public static AddProductUseCase_Factory create(
      Provider<InventoryRepository> inventoryRepositoryProvider,
      Provider<SyncOrchestrator> syncOrchestratorProvider) {
    return new AddProductUseCase_Factory(inventoryRepositoryProvider, syncOrchestratorProvider);
  }

  public static AddProductUseCase newInstance(InventoryRepository inventoryRepository,
      SyncOrchestrator syncOrchestrator) {
    return new AddProductUseCase(inventoryRepository, syncOrchestrator);
  }
}
