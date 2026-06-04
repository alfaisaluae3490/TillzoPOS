package com.tillzo.pos.ui.store.options.history;

import com.tillzo.pos.domain.repository.SaleRepository;
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
public final class HistoryViewModel_Factory implements Factory<HistoryViewModel> {
  private final Provider<SaleRepository> saleRepositoryProvider;

  private final Provider<TsplPrinter> tsplPrinterProvider;

  public HistoryViewModel_Factory(Provider<SaleRepository> saleRepositoryProvider,
      Provider<TsplPrinter> tsplPrinterProvider) {
    this.saleRepositoryProvider = saleRepositoryProvider;
    this.tsplPrinterProvider = tsplPrinterProvider;
  }

  @Override
  public HistoryViewModel get() {
    return newInstance(saleRepositoryProvider.get(), tsplPrinterProvider.get());
  }

  public static HistoryViewModel_Factory create(Provider<SaleRepository> saleRepositoryProvider,
      Provider<TsplPrinter> tsplPrinterProvider) {
    return new HistoryViewModel_Factory(saleRepositoryProvider, tsplPrinterProvider);
  }

  public static HistoryViewModel newInstance(SaleRepository saleRepository,
      TsplPrinter tsplPrinter) {
    return new HistoryViewModel(saleRepository, tsplPrinter);
  }
}
