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
public final class DisasterWorker_AssistedFactory_Impl implements DisasterWorker_AssistedFactory {
  private final DisasterWorker_Factory delegateFactory;

  DisasterWorker_AssistedFactory_Impl(DisasterWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public DisasterWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<DisasterWorker_AssistedFactory> create(
      DisasterWorker_Factory delegateFactory) {
    return InstanceFactory.create(new DisasterWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<DisasterWorker_AssistedFactory> createFactoryProvider(
      DisasterWorker_Factory delegateFactory) {
    return InstanceFactory.create(new DisasterWorker_AssistedFactory_Impl(delegateFactory));
  }
}
