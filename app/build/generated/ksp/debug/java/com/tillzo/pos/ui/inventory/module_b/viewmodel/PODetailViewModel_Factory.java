package com.tillzo.pos.ui.inventory.module_b.viewmodel;

import androidx.lifecycle.SavedStateHandle;
import com.tillzo.pos.data.local.dao.PurchaseOrderDao;
import com.tillzo.pos.domain.repository.GrnRepository;
import com.tillzo.pos.domain.usecase.po.SharePurchaseOrderUseCase;
import com.tillzo.pos.domain.usecase.po.UpdatePOStatusUseCase;
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
public final class PODetailViewModel_Factory implements Factory<PODetailViewModel> {
  private final Provider<PurchaseOrderDao> getPoDaoProvider;

  private final Provider<UpdatePOStatusUseCase> updateStatusUseCaseProvider;

  private final Provider<SharePurchaseOrderUseCase> shareUseCaseProvider;

  private final Provider<GrnRepository> grnRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public PODetailViewModel_Factory(Provider<PurchaseOrderDao> getPoDaoProvider,
      Provider<UpdatePOStatusUseCase> updateStatusUseCaseProvider,
      Provider<SharePurchaseOrderUseCase> shareUseCaseProvider,
      Provider<GrnRepository> grnRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.getPoDaoProvider = getPoDaoProvider;
    this.updateStatusUseCaseProvider = updateStatusUseCaseProvider;
    this.shareUseCaseProvider = shareUseCaseProvider;
    this.grnRepositoryProvider = grnRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public PODetailViewModel get() {
    return newInstance(getPoDaoProvider.get(), updateStatusUseCaseProvider.get(), shareUseCaseProvider.get(), grnRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static PODetailViewModel_Factory create(Provider<PurchaseOrderDao> getPoDaoProvider,
      Provider<UpdatePOStatusUseCase> updateStatusUseCaseProvider,
      Provider<SharePurchaseOrderUseCase> shareUseCaseProvider,
      Provider<GrnRepository> grnRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new PODetailViewModel_Factory(getPoDaoProvider, updateStatusUseCaseProvider, shareUseCaseProvider, grnRepositoryProvider, savedStateHandleProvider);
  }

  public static PODetailViewModel newInstance(PurchaseOrderDao getPoDao,
      UpdatePOStatusUseCase updateStatusUseCase, SharePurchaseOrderUseCase shareUseCase,
      GrnRepository grnRepository, SavedStateHandle savedStateHandle) {
    return new PODetailViewModel(getPoDao, updateStatusUseCase, shareUseCase, grnRepository, savedStateHandle);
  }
}
