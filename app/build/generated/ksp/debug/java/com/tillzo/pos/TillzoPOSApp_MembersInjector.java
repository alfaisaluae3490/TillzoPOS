package com.tillzo.pos;

import androidx.hilt.work.HiltWorkerFactory;
import com.tillzo.pos.data.sync.SyncOrchestrator;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class TillzoPOSApp_MembersInjector implements MembersInjector<TillzoPOSApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  private final Provider<SyncOrchestrator> syncOrchestratorProvider;

  public TillzoPOSApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider,
      Provider<SyncOrchestrator> syncOrchestratorProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
    this.syncOrchestratorProvider = syncOrchestratorProvider;
  }

  public static MembersInjector<TillzoPOSApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider,
      Provider<SyncOrchestrator> syncOrchestratorProvider) {
    return new TillzoPOSApp_MembersInjector(workerFactoryProvider, syncOrchestratorProvider);
  }

  @Override
  public void injectMembers(TillzoPOSApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
    injectSyncOrchestrator(instance, syncOrchestratorProvider.get());
  }

  @InjectedFieldSignature("com.tillzo.pos.TillzoPOSApp.workerFactory")
  public static void injectWorkerFactory(TillzoPOSApp instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }

  @InjectedFieldSignature("com.tillzo.pos.TillzoPOSApp.syncOrchestrator")
  public static void injectSyncOrchestrator(TillzoPOSApp instance,
      SyncOrchestrator syncOrchestrator) {
    instance.syncOrchestrator = syncOrchestrator;
  }
}
