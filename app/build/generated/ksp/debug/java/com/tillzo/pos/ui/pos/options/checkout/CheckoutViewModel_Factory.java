package com.tillzo.pos.ui.pos.options.checkout;

import android.content.Context;
import com.tillzo.pos.domain.repository.SaleRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class CheckoutViewModel_Factory implements Factory<CheckoutViewModel> {
  private final Provider<SaleRepository> saleRepositoryProvider;

  private final Provider<Context> contextProvider;

  public CheckoutViewModel_Factory(Provider<SaleRepository> saleRepositoryProvider,
      Provider<Context> contextProvider) {
    this.saleRepositoryProvider = saleRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public CheckoutViewModel get() {
    return newInstance(saleRepositoryProvider.get(), contextProvider.get());
  }

  public static CheckoutViewModel_Factory create(Provider<SaleRepository> saleRepositoryProvider,
      Provider<Context> contextProvider) {
    return new CheckoutViewModel_Factory(saleRepositoryProvider, contextProvider);
  }

  public static CheckoutViewModel newInstance(SaleRepository saleRepository, Context context) {
    return new CheckoutViewModel(saleRepository, context);
  }
}
