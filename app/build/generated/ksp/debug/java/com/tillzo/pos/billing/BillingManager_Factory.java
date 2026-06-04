package com.tillzo.pos.billing;

import android.content.Context;
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
public final class BillingManager_Factory implements Factory<BillingManager> {
  private final Provider<Context> contextProvider;

  public BillingManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public BillingManager get() {
    return newInstance(contextProvider.get());
  }

  public static BillingManager_Factory create(Provider<Context> contextProvider) {
    return new BillingManager_Factory(contextProvider);
  }

  public static BillingManager newInstance(Context context) {
    return new BillingManager(context);
  }
}
