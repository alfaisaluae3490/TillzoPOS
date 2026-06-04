package com.tillzo.pos.ui.inventory.module_c.viewmodel;

import com.tillzo.pos.data.local.dao.PurchaseOrderDao;
import com.tillzo.pos.domain.repository.GrnRepository;
import com.tillzo.pos.domain.usecase.grn.ConfirmGrnUseCase;
import com.tillzo.pos.domain.usecase.grn.SaveGrnDraftUseCase;
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
public final class CreateGrnViewModel_Factory implements Factory<CreateGrnViewModel> {
  private final Provider<PurchaseOrderDao> poDaoProvider;

  private final Provider<GrnRepository> grnRepositoryProvider;

  private final Provider<SaveGrnDraftUseCase> saveGrnDraftUseCaseProvider;

  private final Provider<ConfirmGrnUseCase> confirmGrnUseCaseProvider;

  public CreateGrnViewModel_Factory(Provider<PurchaseOrderDao> poDaoProvider,
      Provider<GrnRepository> grnRepositoryProvider,
      Provider<SaveGrnDraftUseCase> saveGrnDraftUseCaseProvider,
      Provider<ConfirmGrnUseCase> confirmGrnUseCaseProvider) {
    this.poDaoProvider = poDaoProvider;
    this.grnRepositoryProvider = grnRepositoryProvider;
    this.saveGrnDraftUseCaseProvider = saveGrnDraftUseCaseProvider;
    this.confirmGrnUseCaseProvider = confirmGrnUseCaseProvider;
  }

  @Override
  public CreateGrnViewModel get() {
    return newInstance(poDaoProvider.get(), grnRepositoryProvider.get(), saveGrnDraftUseCaseProvider.get(), confirmGrnUseCaseProvider.get());
  }

  public static CreateGrnViewModel_Factory create(Provider<PurchaseOrderDao> poDaoProvider,
      Provider<GrnRepository> grnRepositoryProvider,
      Provider<SaveGrnDraftUseCase> saveGrnDraftUseCaseProvider,
      Provider<ConfirmGrnUseCase> confirmGrnUseCaseProvider) {
    return new CreateGrnViewModel_Factory(poDaoProvider, grnRepositoryProvider, saveGrnDraftUseCaseProvider, confirmGrnUseCaseProvider);
  }

  public static CreateGrnViewModel newInstance(PurchaseOrderDao poDao, GrnRepository grnRepository,
      SaveGrnDraftUseCase saveGrnDraftUseCase, ConfirmGrnUseCase confirmGrnUseCase) {
    return new CreateGrnViewModel(poDao, grnRepository, saveGrnDraftUseCase, confirmGrnUseCase);
  }
}
