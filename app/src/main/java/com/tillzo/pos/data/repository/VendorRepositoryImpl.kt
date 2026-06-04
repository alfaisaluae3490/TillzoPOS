package com.tillzo.pos.data.repository

import com.tillzo.pos.data.local.dao.VendorDao
import com.tillzo.pos.data.local.entity.VendorEntity
import com.tillzo.pos.domain.repository.VendorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VendorRepositoryImpl @Inject constructor(
    private val vendorDao: VendorDao
) : VendorRepository {

    override suspend fun insertVendor(vendor: VendorEntity) {
        vendorDao.insertVendor(vendor)
    }

    override suspend fun updateVendor(vendor: VendorEntity) {
        vendorDao.updateVendor(vendor)
    }

    override fun getAllVendors(): Flow<List<VendorEntity>> {
        return vendorDao.getAllVendors()
    }

    override suspend fun searchVendors(query: String): List<VendorEntity> {
        return vendorDao.searchVendors(query)
    }
}
