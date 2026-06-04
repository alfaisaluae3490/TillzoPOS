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
public final class DeleteProductUseCase_Factory implements Factory<DeleteProductUseCase> {
  private final Provider<InventoryRepository> inventoryRepositoryProvider;

  private final Provider<SyncOrchestrator> syncOrchestratorProvider;

  public DeleteProductUseCase_Factory(Provider<InventoryRepository> inventoryRepositoryProvider,
      Provider<SyncOrchestrator> syncOrchestratorProvider) {
    this.inventoryRepositoryProvider = inventoryRepositoryProvider;
    this.syncOrchestratorProvider = syncOrchestratorProvider;
  }

  @Override
  public DeleteProductUseCase get() {
    return newInstance(inventoryRepositoryProvider.get(), syncOrchestratorProvider.get());
  }

  public static DeleteProductUseCase_Factory create(
      Provider<InventoryRepository> inventoryRepositoryProvider,
      Provider<SyncOrchestrator> syncOrchestratorProvider) {
    return new DeleteProductUseCase_Factory(inventoryRepositoryProvider, syncOrchestratorProvider);
  }

  public static DeleteProductUseCase newInstance(InventoryRepository inventoryRepository,
      SyncOrchestrator syncOrchestrator) {
    return new DeleteProductUseCase(inventoryRepository, syncOrchestrator);
  }
}
