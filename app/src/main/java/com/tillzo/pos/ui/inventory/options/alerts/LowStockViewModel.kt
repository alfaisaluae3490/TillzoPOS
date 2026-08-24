package com.tillzo.pos.ui.inventory.options.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * M6.6 - Low Stock Alerts
 * Observes the database for items below threshold, out of stock, and near/past expiry.
 */
@HiltViewModel
class LowStockViewModel @Inject constructor(
    inventoryRepository: InventoryRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val today30Days = dateFormat.format(
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }.time
    )
    private val today = dateFormat.format(Date())

    val lowStockItems: StateFlow<List<InventoryEntity>> = inventoryRepository.getLowStockItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val outOfStockItems: StateFlow<List<InventoryEntity>> = inventoryRepository.getOutOfStockItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nearExpiryItems: StateFlow<List<InventoryEntity>> = inventoryRepository.getNearExpiryItems(today30Days)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expiredItems: StateFlow<List<InventoryEntity>> = inventoryRepository.getExpiredItems(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Count StateFlows for badge display in InventoryCrudScreen
    val lowStockCount: StateFlow<Int> = lowStockItems.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val outOfStockCount: StateFlow<Int> = outOfStockItems.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // FIX (2026-08-23, DEF-66): expiringCount ab combine() use karta hai —
    // pehle `expiredItems.value` ka STALE snapshot read hota tha (map body
    // execute hone ke waqt ka), isliye badge count expiry update ke baad bhi
    // purana reh sakta tha. Dono flows ab reactively merge hote hain.
    val expiringCount: StateFlow<Int> = combine(nearExpiryItems, expiredItems) { near, expired ->
        near.size + expired.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}

