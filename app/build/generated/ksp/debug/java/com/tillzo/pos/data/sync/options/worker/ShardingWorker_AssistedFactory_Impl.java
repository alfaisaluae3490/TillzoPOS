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
public final class ShardingWorker_AssistedFactory_Impl implements ShardingWorker_AssistedFactory {
  private final ShardingWorker_Factory delegateFactory;

  ShardingWorker_AssistedFactory_Impl(ShardingWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public ShardingWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<ShardingWorker_AssistedFactory> create(
      ShardingWorker_Factory delegateFactory) {
    return InstanceFactory.create(new ShardingWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<ShardingWorker_AssistedFactory> createFactoryProvider(
      ShardingWorker_Factory delegateFactory) {
    return InstanceFactory.create(new ShardingWorker_AssistedFactory_Impl(delegateFactory));
  }
}
