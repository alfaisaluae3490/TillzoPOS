package com.tillzo.pos.data.sync.options.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ExpiryCheckWorker_AssistedFactory_Impl implements ExpiryCheckWorker_AssistedFactory {
  private final ExpiryCheckWorker_Factory delegateFactory;

  ExpiryCheckWorker_AssistedFactory_Impl(ExpiryCheckWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public ExpiryCheckWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<ExpiryCheckWorker_AssistedFactory> create(
      ExpiryCheckWorker_Factory delegateFactory) {
    return InstanceFactory.create(new ExpiryCheckWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<ExpiryCheckWorker_AssistedFactory> createFactoryProvider(
      ExpiryCheckWorker_Factory delegateFactory) {
    return InstanceFactory.create(new ExpiryCheckWorker_AssistedFactory_Impl(delegateFactory));
  }
}
