package com.tillzo.pos.domain.sync.usecase;

import com.tillzo.pos.data.local.dao.KhataEventDao;
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
public final class KhataEventUseCase_Factory implements Factory<KhataEventUseCase> {
  private final Provider<KhataEventDao> khataEventDaoProvider;

  private final Provider<SheetsRepository> sheetsRepositoryProvider;

  public KhataEventUseCase_Factory(Provider<KhataEventDao> khataEventDaoProvider,
      Provider<SheetsRepository> sheetsRepositoryProvider) {
    this.khataEventDaoProvider = khataEventDaoProvider;
    this.sheetsRepositoryProvider = sheetsRepositoryProvider;
  }

  @Override
  public KhataEventUseCase get() {
    return newInstance(khataEventDaoProvider.get(), sheetsRepositoryProvider.get());
  }

  public static KhataEventUseCase_Factory create(Provider<KhataEventDao> khataEventDaoProvider,
      Provider<SheetsRepository> sheetsRepositoryProvider) {
    return new KhataEventUseCase_Factory(khataEventDaoProvider, sheetsRepositoryProvider);
  }

  public static KhataEventUseCase newInstance(KhataEventDao khataEventDao,
      SheetsRepository sheetsRepository) {
    return new KhataEventUseCase(khataEventDao, sheetsRepository);
  }
}
