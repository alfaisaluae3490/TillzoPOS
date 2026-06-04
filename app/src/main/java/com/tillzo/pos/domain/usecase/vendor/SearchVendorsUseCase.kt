package com.tillzo.pos.domain.usecase.vendor

import com.tillzo.pos.data.local.dao.VendorDao
import com.tillzo.pos.data.local.entity.VendorEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchVendorsUseCase @Inject constructor(
    private val vendorDao: VendorDao
) {
    operator fun invoke(query: String): Flow<List<VendorEntity>> {
        if (query.isBlank()) {
            return vendorDao.getAllVendors()
        }
        // searchVendors is now suspend — wrap in flow{} to preserve Flow return type
        return flow { emit(vendorDao.searchVendors(query)) }
    }
}
