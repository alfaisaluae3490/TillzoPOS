package com.tillzo.pos.domain.usecase.vendor

import com.tillzo.pos.data.local.dao.VendorDao
import com.tillzo.pos.data.local.entity.VendorEntity
import javax.inject.Inject

class AddVendorUseCase @Inject constructor(
    private val vendorDao: VendorDao
) {
    suspend operator fun invoke(vendor: VendorEntity) {
        vendorDao.insertVendor(vendor)
    }
}
