package com.tillzo.pos.data.sync.options.worker;

import androidx.hilt.work.WorkerAssistedFactory;
import androidx.work.ListenableWorker;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.annotation.processing.Generated;

@Generated("androidx.hilt.AndroidXHiltProcessor")
@Module
@InstallIn(SingletonComponent.class)
@OriginatingElement(
    topLevelClass = ShardingWorker.class
)
public interface ShardingWorker_HiltModule {
  @Binds
  @IntoMap
  @StringKey("com.tillzo.pos.data.sync.options.worker.ShardingWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind(ShardingWorker_AssistedFactory factory);
}
