package com.tillzo.pos.data.repository;

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
public final class GrnRepositoryImpl_Factory implements Factory<GrnRepositoryImpl> {
  private final Provider<GrnDao> grnDaoProvider;

  public GrnRepositoryImpl_Factory(Provider<GrnDao> grnDaoProvider) {
    this.grnDaoProvider = grnDaoProvider;
  }

  @Override
  public GrnRepositoryImpl get() {
    return newInstance(grnDaoProvider.get());
  }

  public static GrnRepositoryImpl_Factory create(Provider<GrnDao> grnDaoProvider) {
    return new GrnRepositoryImpl_Factory(grnDaoProvider);
  }

  public static GrnRepositoryImpl newInstance(GrnDao grnDao) {
    return new GrnRepositoryImpl(grnDao);
  }
}
