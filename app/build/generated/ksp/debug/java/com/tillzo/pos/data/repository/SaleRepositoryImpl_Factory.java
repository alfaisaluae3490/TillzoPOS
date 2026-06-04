package com.tillzo.pos.data.repository;

import com.google.gson.Gson;
import com.tillzo.pos.data.local.dao.SaleDao;
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
public final class SaleRepositoryImpl_Factory implements Factory<SaleRepositoryImpl> {
  private final Provider<SaleDao> saleDaoProvider;

  private final Provider<Gson> gsonProvider;

  public SaleRepositoryImpl_Factory(Provider<SaleDao> saleDaoProvider,
      Provider<Gson> gsonProvider) {
    this.saleDaoProvider = saleDaoProvider;
    this.gsonProvider = gsonProvider;
  }

  @Override
  public SaleRepositoryImpl get() {
    return newInstance(saleDaoProvider.get(), gsonProvider.get());
  }

  public static SaleRepositoryImpl_Factory create(Provider<SaleDao> saleDaoProvider,
      Provider<Gson> gsonProvider) {
    return new SaleRepositoryImpl_Factory(saleDaoProvider, gsonProvider);
  }

  public static SaleRepositoryImpl newInstance(SaleDao saleDao, Gson gson) {
    return new SaleRepositoryImpl(saleDao, gson);
  }
}
