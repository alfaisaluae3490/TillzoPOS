package com.tillzo.pos.ui.inventory.options.ocr;

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
public final class OcrEntryViewModel_Factory implements Factory<OcrEntryViewModel> {
  @Override
  public OcrEntryViewModel get() {
    return newInstance();
  }

  public static OcrEntryViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OcrEntryViewModel newInstance() {
    return new OcrEntryViewModel();
  }

  private static final class InstanceHolder {
    private static final OcrEntryViewModel_Factory INSTANCE = new OcrEntryViewModel_Factory();
  }
}
