package com.tillzo.pos.domain.usecase;

import android.content.Context;
import com.google.gson.Gson;
import com.tillzo.pos.data.local.dao.CustomerDao;
import com.tillzo.pos.data.local.dao.KhataEventDao;
import com.tillzo.pos.data.local.dao.SaleDao;
import com.tillzo.pos.data.local.dao.TillSessionDao;
import com.tillzo.pos.data.local.prefs.AppSetupPrefs;
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
public final class CompleteSaleUseCase_Factory implements Factory<CompleteSaleUseCase> {
  private final Provider<Context> contextProvider;

  private final Provider<SaleDao> saleDaoProvider;

  private final Provider<KhataEventDao> khataEventDaoProvider;

  private final Provider<CustomerDao> customerDaoProvider;

  private final Provider<TillSessionDao> tillSessionDaoProvider;

  private final Provider<AppSetupPrefs> appSetupPrefsProvider;

  private final Provider<Gson> gsonProvider;

  public CompleteSaleUseCase_Factory(Provider<Context> contextProvider,
      Provider<SaleDao> saleDaoProvider, Provider<KhataEventDao> khataEventDaoProvider,
      Provider<CustomerDao> customerDaoProvider, Provider<TillSessionDao> tillSessionDaoProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider, Provider<Gson> gsonProvider) {
    this.contextProvider = contextProvider;
    this.saleDaoProvider = saleDaoProvider;
    this.khataEventDaoProvider = khataEventDaoProvider;
    this.customerDaoProvider = customerDaoProvider;
    this.tillSessionDaoProvider = tillSessionDaoProvider;
    this.appSetupPrefsProvider = appSetupPrefsProvider;
    this.gsonProvider = gsonProvider;
  }

  @Override
  public CompleteSaleUseCase get() {
    return newInstance(contextProvider.get(), saleDaoProvider.get(), khataEventDaoProvider.get(), customerDaoProvider.get(), tillSessionDaoProvider.get(), appSetupPrefsProvider.get(), gsonProvider.get());
  }

  public static CompleteSaleUseCase_Factory create(Provider<Context> contextProvider,
      Provider<SaleDao> saleDaoProvider, Provider<KhataEventDao> khataEventDaoProvider,
      Provider<CustomerDao> customerDaoProvider, Provider<TillSessionDao> tillSessionDaoProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider, Provider<Gson> gsonProvider) {
    return new CompleteSaleUseCase_Factory(contextProvider, saleDaoProvider, khataEventDaoProvider, customerDaoProvider, tillSessionDaoProvider, appSetupPrefsProvider, gsonProvider);
  }

  public static CompleteSaleUseCase newInstance(Context context, SaleDao saleDao,
      KhataEventDao khataEventDao, CustomerDao customerDao, TillSessionDao tillSessionDao,
      AppSetupPrefs appSetupPrefs, Gson gson) {
    return new CompleteSaleUseCase(context, saleDao, khataEventDao, customerDao, tillSessionDao, appSetupPrefs, gson);
  }
}
