package com.tillzo.pos.utils;

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
public final class LabelPrintHelper_Factory implements Factory<LabelPrintHelper> {
  @Override
  public LabelPrintHelper get() {
    return newInstance();
  }

  public static LabelPrintHelper_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LabelPrintHelper newInstance() {
    return new LabelPrintHelper();
  }

  private static final class InstanceHolder {
    private static final LabelPrintHelper_Factory INSTANCE = new LabelPrintHelper_Factory();
  }
}
