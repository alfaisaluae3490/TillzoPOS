package com.tillzo.pos.ui.inventory.module_b;

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
public final class VendorManagementViewModel_Factory implements Factory<VendorManagementViewModel> {
  private final Provider<VendorDao> vendorDaoProvider;

  public VendorManagementViewModel_Factory(Provider<VendorDao> vendorDaoProvider) {
    this.vendorDaoProvider = vendorDaoProvider;
  }

  @Override
  public VendorManagementViewModel get() {
    return newInstance(vendorDaoProvider.get());
  }

  public static VendorManagementViewModel_Factory create(Provider<VendorDao> vendorDaoProvider) {
    return new VendorManagementViewModel_Factory(vendorDaoProvider);
  }

  public static VendorManagementViewModel newInstance(VendorDao vendorDao) {
    return new VendorManagementViewModel(vendorDao);
  }
}
