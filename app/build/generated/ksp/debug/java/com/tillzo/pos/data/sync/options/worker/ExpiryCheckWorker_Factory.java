package com.tillzo.pos.data.sync.options.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.tillzo.pos.data.local.AppDatabase;
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
public final class ExpiryCheckWorker_Factory {
  private final Provider<AppDatabase> appDatabaseProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  public ExpiryCheckWorker_Factory(Provider<AppDatabase> appDatabaseProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    this.appDatabaseProvider = appDatabaseProvider;
    this.notificationHelperProvider = notificationHelperProvider;
  }

  public ExpiryCheckWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, appDatabaseProvider.get(), notificationHelperProvider.get());
  }

  public static ExpiryCheckWorker_Factory create(Provider<AppDatabase> appDatabaseProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    return new ExpiryCheckWorker_Factory(appDatabaseProvider, notificationHelperProvider);
  }

  public static ExpiryCheckWorker newInstance(Context context, WorkerParameters params,
      AppDatabase appDatabase, NotificationHelper notificationHelper) {
    return new ExpiryCheckWorker(context, params, appDatabase, notificationHelper);
  }
}
