package com.tillzo.pos.domain.usecase.grn;

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
public final class CreateGrnUseCase_Factory implements Factory<CreateGrnUseCase> {
  private final Provider<GrnDao> grnDaoProvider;

  public CreateGrnUseCase_Factory(Provider<GrnDao> grnDaoProvider) {
    this.grnDaoProvider = grnDaoProvider;
  }

  @Override
  public CreateGrnUseCase get() {
    return newInstance(grnDaoProvider.get());
  }

  public static CreateGrnUseCase_Factory create(Provider<GrnDao> grnDaoProvider) {
    return new CreateGrnUseCase_Factory(grnDaoProvider);
  }

  public static CreateGrnUseCase newInstance(GrnDao grnDao) {
    return new CreateGrnUseCase(grnDao);
  }
}
