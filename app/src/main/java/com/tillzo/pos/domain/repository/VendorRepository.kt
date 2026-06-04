package com.tillzo.pos.domain.repository

import com.tillzo.pos.data.local.entity.VendorEntity
import kotlinx.coroutines.flow.Flow

interface VendorRepository {
    suspend fun insertVendor(vendor: VendorEntity)
    suspend fun updateVendor(vendor: VendorEntity)
    fun getAllVendors(): Flow<List<VendorEntity>>
    suspend fun searchVendors(query: String): List<VendorEntity>
}
