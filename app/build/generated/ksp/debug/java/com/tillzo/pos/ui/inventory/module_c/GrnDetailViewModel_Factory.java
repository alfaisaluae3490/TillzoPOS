package com.tillzo.pos.ui.inventory.module_c;

import androidx.lifecycle.SavedStateHandle;
import com.tillzo.pos.data.local.dao.GrnDao;
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
public final class GrnDetailViewModel_Factory implements Factory<GrnDetailViewModel> {
  private final Provider<GrnDao> grnDaoProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public GrnDetailViewModel_Factory(Provider<GrnDao> grnDaoProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.grnDaoProvider = grnDaoProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public GrnDetailViewModel get() {
    return newInstance(grnDaoProvider.get(), savedStateHandleProvider.get());
  }

  public static GrnDetailViewModel_Factory create(Provider<GrnDao> grnDaoProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new GrnDetailViewModel_Factory(grnDaoProvider, savedStateHandleProvider);
  }

  public static GrnDetailViewModel newInstance(GrnDao grnDao, SavedStateHandle savedStateHandle) {
    return new GrnDetailViewModel(grnDao, savedStateHandle);
  }
}
