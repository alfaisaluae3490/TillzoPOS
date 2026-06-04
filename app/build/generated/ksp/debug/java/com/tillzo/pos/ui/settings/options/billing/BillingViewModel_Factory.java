package com.tillzo.pos.ui.settings.options.billing;

import com.tillzo.pos.billing.BillingManager;
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
public final class BillingViewModel_Factory implements Factory<BillingViewModel> {
  private final Provider<BillingManager> billingManagerProvider;

  public BillingViewModel_Factory(Provider<BillingManager> billingManagerProvider) {
    this.billingManagerProvider = billingManagerProvider;
  }

  @Override
  public BillingViewModel get() {
    return newInstance(billingManagerProvider.get());
  }

  public static BillingViewModel_Factory create(Provider<BillingManager> billingManagerProvider) {
    return new BillingViewModel_Factory(billingManagerProvider);
  }

  public static BillingViewModel newInstance(BillingManager billingManager) {
    return new BillingViewModel(billingManager);
  }
}
