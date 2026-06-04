package com.tillzo.pos.di

import com.tillzo.pos.data.repository.AuthRepositoryImpl
import com.tillzo.pos.data.repository.SaleRepositoryImpl
import com.tillzo.pos.data.repository.SheetsRepository
import com.tillzo.pos.data.repository.UserRepositoryImpl
import com.tillzo.pos.domain.repository.AuthRepository
import com.tillzo.pos.domain.repository.SaleRepository
import com.tillzo.pos.domain.repository.UserRepository
import com.tillzo.pos.data.repository.InventoryRepositoryImpl
import com.tillzo.pos.domain.repository.InventoryRepository
import com.tillzo.pos.data.repository.StoreRepositoryImpl
import com.tillzo.pos.domain.repository.StoreRepository
import com.tillzo.pos.data.repository.GrnRepositoryImpl
import com.tillzo.pos.domain.repository.GrnRepository
import com.tillzo.pos.data.repository.ProductBatchRepositoryImpl
import com.tillzo.pos.domain.repository.ProductBatchRepository
import com.tillzo.pos.data.repository.VendorRepositoryImpl
import com.tillzo.pos.domain.repository.VendorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
    
    @Binds
    @Singleton
    abstract fun bindSaleRepository(
        saleRepositoryImpl: SaleRepositoryImpl
    ): SaleRepository

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(
        inventoryRepositoryImpl: InventoryRepositoryImpl
    ): InventoryRepository

    @Binds
    @Singleton
    abstract fun bindStoreRepository(
        storeRepositoryImpl: StoreRepositoryImpl
    ): StoreRepository

    @Binds
    @Singleton
    abstract fun bindGrnRepository(
        grnRepositoryImpl: GrnRepositoryImpl
    ): GrnRepository

    @Binds
    @Singleton
    abstract fun bindProductBatchRepository(
        productBatchRepositoryImpl: ProductBatchRepositoryImpl
    ): ProductBatchRepository

    @Binds
    @Singleton
    abstract fun bindVendorRepository(
        vendorRepositoryImpl: VendorRepositoryImpl
    ): VendorRepository
}
