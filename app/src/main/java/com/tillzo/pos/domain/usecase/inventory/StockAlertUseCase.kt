package com.tillzo.pos.domain.usecase.inventory

import com.tillzo.pos.domain.repository.InventoryRepository
import com.tillzo.pos.utils.NotificationHelper
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class StockAlertUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val notificationHelper: NotificationHelper
) {
    suspend operator fun invoke() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = dateFormat.format(Date())
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        val nearExpiryDate = dateFormat.format(Date(System.currentTimeMillis() + thirtyDaysInMillis))

        // Trigger Low Stock Alerts
        val lowStockFlow = inventoryRepository.getLowStockItems().first()
        for (item in lowStockFlow) {
            if (item.current_stock > 0) {
                notificationHelper.lowStockAlert(item.item_name, item.current_stock, item.unit)
            }
        }

        // Trigger Out of Stock Alerts
        val outOfStockItems = inventoryRepository.getOutOfStockItems().first()
        for (item in outOfStockItems) {
            notificationHelper.outOfStockAlert(item.item_name)
        }

        // Trigger Near Expiry Alerts
        val nearExpiryItems = inventoryRepository.getNearExpiryItems(nearExpiryDate).first()
        for (item in nearExpiryItems) {
            if (item.expiry_date >= today) {
                notificationHelper.nearExpiryAlert(item.item_name, item.expiry_date)
            }
        }

        // Trigger Expired Alerts
        val expiredItems = inventoryRepository.getExpiredItems(today).first()
        for (item in expiredItems) {
            notificationHelper.expiredAlert(item.item_name, item.expiry_date)
        }
    }
}
