package com.tillzo.pos.data.repository;

import com.tillzo.pos.data.local.dao.CustomerDao;
import com.tillzo.pos.data.local.dao.ExpenseDao;
import com.tillzo.pos.data.local.dao.KhataEventDao;
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
public final class StoreRepositoryImpl_Factory implements Factory<StoreRepositoryImpl> {
  private final Provider<CustomerDao> customerDaoProvider;

  private final Provider<KhataEventDao> khataEventDaoProvider;

  private final Provider<ExpenseDao> expenseDaoProvider;

  public StoreRepositoryImpl_Factory(Provider<CustomerDao> customerDaoProvider,
      Provider<KhataEventDao> khataEventDaoProvider, Provider<ExpenseDao> expenseDaoProvider) {
    this.customerDaoProvider = customerDaoProvider;
    this.khataEventDaoProvider = khataEventDaoProvider;
    this.expenseDaoProvider = expenseDaoProvider;
  }

  @Override
  public StoreRepositoryImpl get() {
    return newInstance(customerDaoProvider.get(), khataEventDaoProvider.get(), expenseDaoProvider.get());
  }

  public static StoreRepositoryImpl_Factory create(Provider<CustomerDao> customerDaoProvider,
      Provider<KhataEventDao> khataEventDaoProvider, Provider<ExpenseDao> expenseDaoProvider) {
    return new StoreRepositoryImpl_Factory(customerDaoProvider, khataEventDaoProvider, expenseDaoProvider);
  }

  public static StoreRepositoryImpl newInstance(CustomerDao customerDao,
      KhataEventDao khataEventDao, ExpenseDao expenseDao) {
    return new StoreRepositoryImpl(customerDao, khataEventDao, expenseDao);
  }
}
