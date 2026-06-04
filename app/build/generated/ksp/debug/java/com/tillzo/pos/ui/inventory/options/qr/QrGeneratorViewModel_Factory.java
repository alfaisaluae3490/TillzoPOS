package com.tillzo.pos.ui.inventory.options.qr;

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
public final class QrGeneratorViewModel_Factory implements Factory<QrGeneratorViewModel> {
  private final Provider<TsplPrinter> tsplPrinterProvider;

  public QrGeneratorViewModel_Factory(Provider<TsplPrinter> tsplPrinterProvider) {
    this.tsplPrinterProvider = tsplPrinterProvider;
  }

  @Override
  public QrGeneratorViewModel get() {
    return newInstance(tsplPrinterProvider.get());
  }

  public static QrGeneratorViewModel_Factory create(Provider<TsplPrinter> tsplPrinterProvider) {
    return new QrGeneratorViewModel_Factory(tsplPrinterProvider);
  }

  public static QrGeneratorViewModel newInstance(TsplPrinter tsplPrinter) {
    return new QrGeneratorViewModel(tsplPrinter);
  }
}
