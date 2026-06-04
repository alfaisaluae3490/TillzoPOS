package com.tillzo.pos.domain.usecase.inventory;

import com.tillzo.pos.domain.repository.InventoryRepository;
import com.tillzo.pos.utils.NotificationHelper;
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
public final class StockAlertUseCase_Factory implements Factory<StockAlertUseCase> {
  private final Provider<InventoryRepository> inventoryRepositoryProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  public StockAlertUseCase_Factory(Provider<InventoryRepository> inventoryRepositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    this.inventoryRepositoryProvider = inventoryRepositoryProvider;
    this.notificationHelperProvider = notificationHelperProvider;
  }

  @Override
  public StockAlertUseCase get() {
    return newInstance(inventoryRepositoryProvider.get(), notificationHelperProvider.get());
  }

  public static StockAlertUseCase_Factory create(
      Provider<InventoryRepository> inventoryRepositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    return new StockAlertUseCase_Factory(inventoryRepositoryProvider, notificationHelperProvider);
  }

  public static StockAlertUseCase newInstance(InventoryRepository inventoryRepository,
      NotificationHelper notificationHelper) {
    return new StockAlertUseCase(inventoryRepository, notificationHelper);
  }
}
