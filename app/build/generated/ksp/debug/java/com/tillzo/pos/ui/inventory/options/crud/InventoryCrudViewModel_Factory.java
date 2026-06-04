package com.tillzo.pos.ui.inventory.options.crud;

import com.tillzo.pos.data.local.dao.CategoryDao;
import com.tillzo.pos.data.local.dao.InventoryDao;
import com.tillzo.pos.data.local.dao.ProductBatchDao;
import com.tillzo.pos.data.local.dao.ProductUnitDao;
import com.tillzo.pos.domain.repository.AuthRepository;
import com.tillzo.pos.domain.usecase.inventory.AddProductUseCase;
import com.tillzo.pos.domain.usecase.inventory.DeleteProductUseCase;
import com.tillzo.pos.domain.usecase.inventory.GetProductsUseCase;
import com.tillzo.pos.domain.usecase.inventory.UpdateProductUseCase;
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
public final class InventoryCrudViewModel_Factory implements Factory<InventoryCrudViewModel> {
  private final Provider<GetProductsUseCase> getProductsUseCaseProvider;

  private final Provider<AddProductUseCase> addProductUseCaseProvider;

  private final Provider<UpdateProductUseCase> updateProductUseCaseProvider;

  private final Provider<DeleteProductUseCase> deleteProductUseCaseProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<CategoryDao> categoryDaoProvider;

  private final Provider<ProductUnitDao> productUnitDaoProvider;

  private final Provider<ProductBatchDao> productBatchDaoProvider;

  private final Provider<InventoryDao> inventoryDaoProvider;

  public InventoryCrudViewModel_Factory(Provider<GetProductsUseCase> getProductsUseCaseProvider,
      Provider<AddProductUseCase> addProductUseCaseProvider,
      Provider<UpdateProductUseCase> updateProductUseCaseProvider,
      Provider<DeleteProductUseCase> deleteProductUseCaseProvider,
      Provider<AuthRepository> authRepositoryProvider, Provider<CategoryDao> categoryDaoProvider,
      Provider<ProductUnitDao> productUnitDaoProvider,
      Provider<ProductBatchDao> productBatchDaoProvider,
      Provider<InventoryDao> inventoryDaoProvider) {
    this.getProductsUseCaseProvider = getProductsUseCaseProvider;
    this.addProductUseCaseProvider = addProductUseCaseProvider;
    this.updateProductUseCaseProvider = updateProductUseCaseProvider;
    this.deleteProductUseCaseProvider = deleteProductUseCaseProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.categoryDaoProvider = categoryDaoProvider;
    this.productUnitDaoProvider = productUnitDaoProvider;
    this.productBatchDaoProvider = productBatchDaoProvider;
    this.inventoryDaoProvider = inventoryDaoProvider;
  }

  @Override
  public InventoryCrudViewModel get() {
    return newInstance(getProductsUseCaseProvider.get(), addProductUseCaseProvider.get(), updateProductUseCaseProvider.get(), deleteProductUseCaseProvider.get(), authRepositoryProvider.get(), categoryDaoProvider.get(), productUnitDaoProvider.get(), productBatchDaoProvider.get(), inventoryDaoProvider.get());
  }

  public static InventoryCrudViewModel_Factory create(
      Provider<GetProductsUseCase> getProductsUseCaseProvider,
      Provider<AddProductUseCase> addProductUseCaseProvider,
      Provider<UpdateProductUseCase> updateProductUseCaseProvider,
      Provider<DeleteProductUseCase> deleteProductUseCaseProvider,
      Provider<AuthRepository> authRepositoryProvider, Provider<CategoryDao> categoryDaoProvider,
      Provider<ProductUnitDao> productUnitDaoProvider,
      Provider<ProductBatchDao> productBatchDaoProvider,
      Provider<InventoryDao> inventoryDaoProvider) {
    return new InventoryCrudViewModel_Factory(getProductsUseCaseProvider, addProductUseCaseProvider, updateProductUseCaseProvider, deleteProductUseCaseProvider, authRepositoryProvider, categoryDaoProvider, productUnitDaoProvider, productBatchDaoProvider, inventoryDaoProvider);
  }

  public static InventoryCrudViewModel newInstance(GetProductsUseCase getProductsUseCase,
      AddProductUseCase addProductUseCase, UpdateProductUseCase updateProductUseCase,
      DeleteProductUseCase deleteProductUseCase, AuthRepository authRepository,
      CategoryDao categoryDao, ProductUnitDao productUnitDao, ProductBatchDao productBatchDao,
      InventoryDao inventoryDao) {
    return new InventoryCrudViewModel(getProductsUseCase, addProductUseCase, updateProductUseCase, deleteProductUseCase, authRepository, categoryDao, productUnitDao, productBatchDao, inventoryDao);
  }
}
