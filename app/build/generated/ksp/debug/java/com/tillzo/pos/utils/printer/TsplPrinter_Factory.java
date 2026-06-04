package com.tillzo.pos.utils.printer;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class TsplPrinter_Factory implements Factory<TsplPrinter> {
  @Override
  public TsplPrinter get() {
    return newInstance();
  }

  public static TsplPrinter_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TsplPrinter newInstance() {
    return new TsplPrinter();
  }

  private static final class InstanceHolder {
    private static final TsplPrinter_Factory INSTANCE = new TsplPrinter_Factory();
  }
}
