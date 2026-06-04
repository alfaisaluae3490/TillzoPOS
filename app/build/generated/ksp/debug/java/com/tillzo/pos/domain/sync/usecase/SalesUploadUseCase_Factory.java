package com.tillzo.pos.domain.sync.usecase;

import com.tillzo.pos.data.local.dao.SaleDao;
import com.tillzo.pos.data.repository.SheetsRepository;
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
public final class SalesUploadUseCase_Factory implements Factory<SalesUploadUseCase> {
  private final Provider<SaleDao> saleDaoProvider;

  private final Provider<SheetsRepository> sheetsRepositoryProvider;

  public SalesUploadUseCase_Factory(Provider<SaleDao> saleDaoProvider,
      Provider<SheetsRepository> sheetsRepositoryProvider) {
    this.saleDaoProvider = saleDaoProvider;
    this.sheetsRepositoryProvider = sheetsRepositoryProvider;
  }

  @Override
  public SalesUploadUseCase get() {
    return newInstance(saleDaoProvider.get(), sheetsRepositoryProvider.get());
  }

  public static SalesUploadUseCase_Factory create(Provider<SaleDao> saleDaoProvider,
      Provider<SheetsRepository> sheetsRepositoryProvider) {
    return new SalesUploadUseCase_Factory(saleDaoProvider, sheetsRepositoryProvider);
  }

  public static SalesUploadUseCase newInstance(SaleDao saleDao, SheetsRepository sheetsRepository) {
    return new SalesUploadUseCase(saleDao, sheetsRepository);
  }
}
