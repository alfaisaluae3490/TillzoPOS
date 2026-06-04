package com.tillzo.pos.ui.home;

import com.tillzo.pos.data.local.dao.CustomerDao;
import com.tillzo.pos.data.local.dao.InventoryDao;
import com.tillzo.pos.data.local.prefs.AppSetupPrefs;
import com.tillzo.pos.domain.usecase.CompleteSaleUseCase;
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
public final class PosViewModel_Factory implements Factory<PosViewModel> {
  private final Provider<CompleteSaleUseCase> completeSaleUseCaseProvider;

  private final Provider<InventoryDao> inventoryDaoProvider;

  private final Provider<CustomerDao> customerDaoProvider;

  private final Provider<AppSetupPrefs> appSetupPrefsProvider;

  public PosViewModel_Factory(Provider<CompleteSaleUseCase> completeSaleUseCaseProvider,
      Provider<InventoryDao> inventoryDaoProvider, Provider<CustomerDao> customerDaoProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    this.completeSaleUseCaseProvider = completeSaleUseCaseProvider;
    this.inventoryDaoProvider = inventoryDaoProvider;
    this.customerDaoProvider = customerDaoProvider;
    this.appSetupPrefsProvider = appSetupPrefsProvider;
  }

  @Override
  public PosViewModel get() {
    return newInstance(completeSaleUseCaseProvider.get(), inventoryDaoProvider.get(), customerDaoProvider.get(), appSetupPrefsProvider.get());
  }

  public static PosViewModel_Factory create(
      Provider<CompleteSaleUseCase> completeSaleUseCaseProvider,
      Provider<InventoryDao> inventoryDaoProvider, Provider<CustomerDao> customerDaoProvider,
      Provider<AppSetupPrefs> appSetupPrefsProvider) {
    return new PosViewModel_Factory(completeSaleUseCaseProvider, inventoryDaoProvider, customerDaoProvider, appSetupPrefsProvider);
  }

  public static PosViewModel newInstance(CompleteSaleUseCase completeSaleUseCase,
      InventoryDao inventoryDao, CustomerDao customerDao, AppSetupPrefs appSetupPrefs) {
    return new PosViewModel(completeSaleUseCase, inventoryDao, customerDao, appSetupPrefs);
  }
}
