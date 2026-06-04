package com.tillzo.pos.ui.inventory;

import com.tillzo.pos.data.local.dao.CategoryDao;
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
public final class CategoryManagementViewModel_Factory implements Factory<CategoryManagementViewModel> {
  private final Provider<CategoryDao> categoryDaoProvider;

  public CategoryManagementViewModel_Factory(Provider<CategoryDao> categoryDaoProvider) {
    this.categoryDaoProvider = categoryDaoProvider;
  }

  @Override
  public CategoryManagementViewModel get() {
    return newInstance(categoryDaoProvider.get());
  }

  public static CategoryManagementViewModel_Factory create(
      Provider<CategoryDao> categoryDaoProvider) {
    return new CategoryManagementViewModel_Factory(categoryDaoProvider);
  }

  public static CategoryManagementViewModel newInstance(CategoryDao categoryDao) {
    return new CategoryManagementViewModel(categoryDao);
  }
}
