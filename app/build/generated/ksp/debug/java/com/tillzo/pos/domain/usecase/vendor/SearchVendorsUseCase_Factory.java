package com.tillzo.pos.domain.usecase.vendor;

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
public final class SearchVendorsUseCase_Factory implements Factory<SearchVendorsUseCase> {
  private final Provider<VendorDao> vendorDaoProvider;

  public SearchVendorsUseCase_Factory(Provider<VendorDao> vendorDaoProvider) {
    this.vendorDaoProvider = vendorDaoProvider;
  }

  @Override
  public SearchVendorsUseCase get() {
    return newInstance(vendorDaoProvider.get());
  }

  public static SearchVendorsUseCase_Factory create(Provider<VendorDao> vendorDaoProvider) {
    return new SearchVendorsUseCase_Factory(vendorDaoProvider);
  }

  public static SearchVendorsUseCase newInstance(VendorDao vendorDao) {
    return new SearchVendorsUseCase(vendorDao);
  }
}
