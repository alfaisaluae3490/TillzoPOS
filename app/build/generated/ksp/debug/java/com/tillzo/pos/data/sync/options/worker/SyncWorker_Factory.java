package com.tillzo.pos.data.sync.options.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.tillzo.pos.data.local.AppDatabase;
import com.tillzo.pos.data.repository.SheetsRepository;
import com.tillzo.pos.domain.sync.usecase.InventoryUpsertUseCase;
import com.tillzo.pos.domain.sync.usecase.KhataEventUseCase;
import com.tillzo.pos.domain.sync.usecase.SalesUploadUseCase;
import com.tillzo.pos.domain.sync.usecase.SchemaGuardUseCase;
import com.tillzo.pos.utils.NotificationHelper;
import dagger.internal.DaggerGenerated;
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
public final class SyncWorker_Factory {
  private final Provider<AppDatabase> appDatabaseProvider;

  private final Provider<SalesUploadUseCase> salesUploadUseCaseProvider;

  private final Provider<InventoryUpsertUseCase> inventoryUpsertUseCaseProvider;

  private final Provider<KhataEventUseCase> khataEventUseCaseProvider;

  private final Provider<SchemaGuardUseCase> schemaGuardUseCaseProvider;

  private final Provider<SheetsRepository> sheetsRepositoryProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  public SyncWorker_Factory(Provider<AppDatabase> appDatabaseProvider,
      Provider<SalesUploadUseCase> salesUploadUseCaseProvider,
      Provider<InventoryUpsertUseCase> inventoryUpsertUseCaseProvider,
      Provider<KhataEventUseCase> khataEventUseCaseProvider,
      Provider<SchemaGuardUseCase> schemaGuardUseCaseProvider,
      Provider<SheetsRepository> sheetsRepositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    this.appDatabaseProvider = appDatabaseProvider;
    this.salesUploadUseCaseProvider = salesUploadUseCaseProvider;
    this.inventoryUpsertUseCaseProvider = inventoryUpsertUseCaseProvider;
    this.khataEventUseCaseProvider = khataEventUseCaseProvider;
    this.schemaGuardUseCaseProvider = schemaGuardUseCaseProvider;
    this.sheetsRepositoryProvider = sheetsRepositoryProvider;
    this.notificationHelperProvider = notificationHelperProvider;
  }

  public SyncWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, appDatabaseProvider.get(), salesUploadUseCaseProvider.get(), inventoryUpsertUseCaseProvider.get(), khataEventUseCaseProvider.get(), schemaGuardUseCaseProvider.get(), sheetsRepositoryProvider.get(), notificationHelperProvider.get());
  }

  public static SyncWorker_Factory create(Provider<AppDatabase> appDatabaseProvider,
      Provider<SalesUploadUseCase> salesUploadUseCaseProvider,
      Provider<InventoryUpsertUseCase> inventoryUpsertUseCaseProvider,
      Provider<KhataEventUseCase> khataEventUseCaseProvider,
      Provider<SchemaGuardUseCase> schemaGuardUseCaseProvider,
      Provider<SheetsRepository> sheetsRepositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    return new SyncWorker_Factory(appDatabaseProvider, salesUploadUseCaseProvider, inventoryUpsertUseCaseProvider, khataEventUseCaseProvider, schemaGuardUseCaseProvider, sheetsRepositoryProvider, notificationHelperProvider);
  }

  public static SyncWorker newInstance(Context context, WorkerParameters params,
      AppDatabase appDatabase, SalesUploadUseCase salesUploadUseCase,
      InventoryUpsertUseCase inventoryUpsertUseCase, KhataEventUseCase khataEventUseCase,
      SchemaGuardUseCase schemaGuardUseCase, SheetsRepository sheetsRepository,
      NotificationHelper notificationHelper) {
    return new SyncWorker(context, params, appDatabase, salesUploadUseCase, inventoryUpsertUseCase, khataEventUseCase, schemaGuardUseCase, sheetsRepository, notificationHelper);
  }
}
