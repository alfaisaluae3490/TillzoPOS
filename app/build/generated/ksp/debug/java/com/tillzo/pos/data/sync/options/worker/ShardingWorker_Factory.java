package com.tillzo.pos.data.sync.options.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.tillzo.pos.data.repository.SheetsRepository;
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
public final class ShardingWorker_Factory {
  private final Provider<SheetsRepository> sheetsRepositoryProvider;

  public ShardingWorker_Factory(Provider<SheetsRepository> sheetsRepositoryProvider) {
    this.sheetsRepositoryProvider = sheetsRepositoryProvider;
  }

  public ShardingWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, sheetsRepositoryProvider.get());
  }

  public static ShardingWorker_Factory create(Provider<SheetsRepository> sheetsRepositoryProvider) {
    return new ShardingWorker_Factory(sheetsRepositoryProvider);
  }

  public static ShardingWorker newInstance(Context context, WorkerParameters params,
      SheetsRepository sheetsRepository) {
    return new ShardingWorker(context, params, sheetsRepository);
  }
}
