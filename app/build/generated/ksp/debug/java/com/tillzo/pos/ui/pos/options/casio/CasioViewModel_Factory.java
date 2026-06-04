package com.tillzo.pos.ui.pos.options.casio;

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
public final class CasioViewModel_Factory implements Factory<CasioViewModel> {
  @Override
  public CasioViewModel get() {
    return newInstance();
  }

  public static CasioViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CasioViewModel newInstance() {
    return new CasioViewModel();
  }

  private static final class InstanceHolder {
    private static final CasioViewModel_Factory INSTANCE = new CasioViewModel_Factory();
  }
}
