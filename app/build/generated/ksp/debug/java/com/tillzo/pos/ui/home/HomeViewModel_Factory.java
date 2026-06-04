package com.tillzo.pos.ui.home;

import com.tillzo.pos.data.sync.SyncOrchestrator;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<SyncOrchestrator> syncOrchestratorProvider;

  public HomeViewModel_Factory(Provider<SyncOrchestrator> syncOrchestratorProvider) {
    this.syncOrchestratorProvider = syncOrchestratorProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(syncOrchestratorProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<SyncOrchestrator> syncOrchestratorProvider) {
    return new HomeViewModel_Factory(syncOrchestratorProvider);
  }

  public static HomeViewModel newInstance(SyncOrchestrator syncOrchestrator) {
    return new HomeViewModel(syncOrchestrator);
  }
}
