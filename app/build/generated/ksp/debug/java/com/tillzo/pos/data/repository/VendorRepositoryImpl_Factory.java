package com.tillzo.pos.data.repository;

import com.tillzo.pos.data.local.dao.VendorDao;
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
public final class VendorRepositoryImpl_Factory implements Factory<VendorRepositoryImpl> {
  private final Provider<VendorDao> vendorDaoProvider;

  public VendorRepositoryImpl_Factory(Provider<VendorDao> vendorDaoProvider) {
    this.vendorDaoProvider = vendorDaoProvider;
  }

  @Override
  public VendorRepositoryImpl get() {
    return newInstance(vendorDaoProvider.get());
  }

  public static VendorRepositoryImpl_Factory create(Provider<VendorDao> vendorDaoProvider) {
    return new VendorRepositoryImpl_Factory(vendorDaoProvider);
  }

  public static VendorRepositoryImpl newInstance(VendorDao vendorDao) {
    return new VendorRepositoryImpl(vendorDao);
  }
}
