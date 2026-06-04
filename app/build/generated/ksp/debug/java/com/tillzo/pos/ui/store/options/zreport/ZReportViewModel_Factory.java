package com.tillzo.pos.ui.store.options.zreport;

import com.tillzo.pos.data.local.dao.SyncLogDao;
import com.tillzo.pos.data.local.dao.TillSessionDao;
import com.tillzo.pos.domain.repository.SaleRepository;
import com.tillzo.pos.domain.repository.StoreRepository;
import com.tillzo.pos.utils.printer.TsplPrinter;
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
public final class ZReportViewModel_Factory implements Factory<ZReportViewModel> {
  private final Provider<SaleRepository> saleRepositoryProvider;

  private final Provider<StoreRepository> storeRepositoryProvider;

  private final Provider<SyncLogDao> syncLogDaoProvider;

  private final Provider<TillSessionDao> tillSessionDaoProvider;

  private final Provider<TsplPrinter> tsplPrinterProvider;

  public ZReportViewModel_Factory(Provider<SaleRepository> saleRepositoryProvider,
      Provider<StoreRepository> storeRepositoryProvider, Provider<SyncLogDao> syncLogDaoProvider,
      Provider<TillSessionDao> tillSessionDaoProvider, Provider<TsplPrinter> tsplPrinterProvider) {
    this.saleRepositoryProvider = saleRepositoryProvider;
    this.storeRepositoryProvider = storeRepositoryProvider;
    this.syncLogDaoProvider = syncLogDaoProvider;
    this.tillSessionDaoProvider = tillSessionDaoProvider;
    this.tsplPrinterProvider = tsplPrinterProvider;
  }

  @Override
  public ZReportViewModel get() {
    return newInstance(saleRepositoryProvider.get(), storeRepositoryProvider.get(), syncLogDaoProvider.get(), tillSessionDaoProvider.get(), tsplPrinterProvider.get());
  }

  public static ZReportViewModel_Factory create(Provider<SaleRepository> saleRepositoryProvider,
      Provider<StoreRepository> storeRepositoryProvider, Provider<SyncLogDao> syncLogDaoProvider,
      Provider<TillSessionDao> tillSessionDaoProvider, Provider<TsplPrinter> tsplPrinterProvider) {
    return new ZReportViewModel_Factory(saleRepositoryProvider, storeRepositoryProvider, syncLogDaoProvider, tillSessionDaoProvider, tsplPrinterProvider);
  }

  public static ZReportViewModel newInstance(SaleRepository saleRepository,
      StoreRepository storeRepository, SyncLogDao syncLogDao, TillSessionDao tillSessionDao,
      TsplPrinter tsplPrinter) {
    return new ZReportViewModel(saleRepository, storeRepository, syncLogDao, tillSessionDao, tsplPrinter);
  }
}
