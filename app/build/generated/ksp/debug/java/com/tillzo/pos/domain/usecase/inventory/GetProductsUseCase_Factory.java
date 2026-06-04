package com.tillzo.pos.domain.usecase.inventory;

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
public final class GetProductsUseCase_Factory implements Factory<GetProductsUseCase> {
  private final Provider<InventoryRepository> inventoryRepositoryProvider;

  public GetProductsUseCase_Factory(Provider<InventoryRepository> inventoryRepositoryProvider) {
    this.inventoryRepositoryProvider = inventoryRepositoryProvider;
  }

  @Override
  public GetProductsUseCase get() {
    return newInstance(inventoryRepositoryProvider.get());
  }

  public static GetProductsUseCase_Factory create(
      Provider<InventoryRepository> inventoryRepositoryProvider) {
    return new GetProductsUseCase_Factory(inventoryRepositoryProvider);
  }

  public static GetProductsUseCase newInstance(InventoryRepository inventoryRepository) {
    return new GetProductsUseCase(inventoryRepository);
  }
}
