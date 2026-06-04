package com.tillzo.pos.domain.usecase.po;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class SharePurchaseOrderUseCase_Factory implements Factory<SharePurchaseOrderUseCase> {
  @Override
  public SharePurchaseOrderUseCase get() {
    return newInstance();
  }

  public static SharePurchaseOrderUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SharePurchaseOrderUseCase newInstance() {
    return new SharePurchaseOrderUseCase();
  }

  private static final class InstanceHolder {
    private static final SharePurchaseOrderUseCase_Factory INSTANCE = new SharePurchaseOrderUseCase_Factory();
  }
}
