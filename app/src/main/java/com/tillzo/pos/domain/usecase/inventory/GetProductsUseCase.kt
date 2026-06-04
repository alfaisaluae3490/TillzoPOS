package com.tillzo.pos.domain.usecase.inventory

import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class ProductFilter {
    ALL, LOW_STOCK, OUT_OF_STOCK, NEAR_EXPIRY, EXPIRED, DAMAGED
}

class GetProductsUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) {
    operator fun invoke(filter: ProductFilter, searchQuery: String = ""): Flow<List<InventoryEntity>> {
        if (searchQuery.isNotBlank()) {
            return inventoryRepository.searchItems(searchQuery)
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = dateFormat.format(Date())
        
        // 30 days from now for near expiry
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        val nearExpiryDate = dateFormat.format(Date(System.currentTimeMillis() + thirtyDaysInMillis))

        return when (filter) {
            ProductFilter.ALL -> inventoryRepository.getAllItems()
            ProductFilter.LOW_STOCK -> inventoryRepository.getLowStockItems()
            ProductFilter.OUT_OF_STOCK -> inventoryRepository.getOutOfStockItems()
            ProductFilter.NEAR_EXPIRY -> inventoryRepository.getNearExpiryItems(nearExpiryDate)
            ProductFilter.EXPIRED -> inventoryRepository.getExpiredItems(today)
            ProductFilter.DAMAGED -> inventoryRepository.getDamagedItems()
        }
    }
}
