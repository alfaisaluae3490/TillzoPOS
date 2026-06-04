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
public final class BarcodeHelper_Factory implements Factory<BarcodeHelper> {
  @Override
  public BarcodeHelper get() {
    return newInstance();
  }

  public static BarcodeHelper_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static BarcodeHelper newInstance() {
    return new BarcodeHelper();
  }

  private static final class InstanceHolder {
    private static final BarcodeHelper_Factory INSTANCE = new BarcodeHelper_Factory();
  }
}
