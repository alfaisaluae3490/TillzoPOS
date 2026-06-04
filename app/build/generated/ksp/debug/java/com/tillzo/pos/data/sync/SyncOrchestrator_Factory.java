package com.tillzo.pos.data.sync;

import android.content.Context;
import com.tillzo.pos.data.sync.options.delta.DeltaSyncManager;
import com.tillzo.pos.domain.sync.usecase.SchemaGuardUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SyncOrchestrator_Factory implements Factory<SyncOrchestrator> {
  private final Provider<Context> contextProvider;

  private final Provider<DeltaSyncManager> deltaSyncManagerProvider;

  private final Provider<SchemaGuardUseCase> schemaGuardUseCaseProvider;

  public SyncOrchestrator_Factory(Provider<Context> contextProvider,
      Provider<DeltaSyncManager> deltaSyncManagerProvider,
      Provider<SchemaGuardUseCase> schemaGuardUseCaseProvider) {
    this.contextProvider = contextProvider;
    this.deltaSyncManagerProvider = deltaSyncManagerProvider;
    this.schemaGuardUseCaseProvider = schemaGuardUseCaseProvider;
  }

  @Override
  public SyncOrchestrator get() {
    return newInstance(contextProvider.get(), deltaSyncManagerProvider.get(), schemaGuardUseCaseProvider.get());
  }

  public static SyncOrchestrator_Factory create(Provider<Context> contextProvider,
      Provider<DeltaSyncManager> deltaSyncManagerProvider,
      Provider<SchemaGuardUseCase> schemaGuardUseCaseProvider) {
    return new SyncOrchestrator_Factory(contextProvider, deltaSyncManagerProvider, schemaGuardUseCaseProvider);
  }

  public static SyncOrchestrator newInstance(Context context, DeltaSyncManager deltaSyncManager,
      SchemaGuardUseCase schemaGuardUseCase) {
    return new SyncOrchestrator(context, deltaSyncManager, schemaGuardUseCase);
  }
}
