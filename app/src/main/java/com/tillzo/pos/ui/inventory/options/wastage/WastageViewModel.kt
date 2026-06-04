package com.tillzo.pos.ui.inventory.options.wastage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import android.content.Context
import com.tillzo.pos.data.local.dao.InventoryDao
import com.tillzo.pos.data.local.dao.ProductBatchDao
import com.tillzo.pos.data.local.dao.WastageDao
import com.tillzo.pos.data.local.entity.InventoryEntity
import com.tillzo.pos.data.local.entity.WastageEntity
import com.tillzo.pos.data.sync.options.worker.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

enum class WastageReason(val label: String) {
    EXPIRED("EXPIRED"),
    DAMAGED("DAMAGED"),
    THEFT("THEFT"),
    OTHER("OTHER")
}

@HiltViewModel
class WastageViewModel @Inject constructor(
    private val wastageDao: WastageDao,
    private val inventoryDao: InventoryDao,
    private val productBatchDao: ProductBatchDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)

    // All wastage entries (unfiltered)
    val allWastage: StateFlow<List<WastageEntity>> = wastageDao.getAllWastage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Loss totals
    val totalLossToday: StateFlow<Double> = wastageDao.getTotalLossToday(dateFormat.format(Date()))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalLossMonth: StateFlow<Double> = wastageDao.getTotalLossThisMonth(monthFormat.format(Date()))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Filter state
    private val _selectedFilter = MutableStateFlow<WastageReason?>(null)
    val selectedFilter: StateFlow<WastageReason?> = _selectedFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredWastage: StateFlow<List<WastageEntity>> = _selectedFilter
        .flatMapLatest { reason ->
            if (reason == null) wastageDao.getAllWastage()
            else wastageDao.getWastageByReason(reason.label)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Product search for the log wastage dialog
    private val _productSearchResults = MutableStateFlow<List<InventoryEntity>>(emptyList())
    val productSearchResults: StateFlow<List<InventoryEntity>> = _productSearchResults.asStateFlow()

    fun setFilter(reason: WastageReason?) {
        _selectedFilter.value = reason
    }

    fun searchProducts(query: String) {
        if (query.isBlank()) {
            _productSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            // Collect first emission of search flow
            inventoryDao.searchItems(query).collect { results ->
                _productSearchResults.value = results.take(15)
                return@collect
            }
        }
    }

    /**
     * Log a wastage event:
     *   1. Create WastageEntity → insert into Room
     *   2. Deduct from InventoryEntity.current_stock
     *   3. Deduct from batch if batchId specified
     *   4. If EXPIRED → deactivate that batch
     *   5. Trigger OneTimeWorkRequest for sync
     */
    fun logWastage(
        productId: String,
        productName: String,
        batchId: String?,
        quantity: Double,
        unit: String,
        costPrice: Double,
        reason: WastageReason,
        notes: String,
        loggedBy: String,
        posTerminalId: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val today = dateFormat.format(Date())
            val wastage = WastageEntity(
                wastageId     = UUID.randomUUID().toString(),
                productId     = productId,
                productName   = productName,
                batchId       = batchId ?: "",
                batchNumber   = if (batchId != null) {
                    productBatchDao.getBatchById(batchId)?.batchNumber ?: ""
                } else "",
                quantity      = quantity,
                unit          = unit,
                costPrice     = costPrice,
                totalLoss     = quantity * costPrice,
                reason        = reason.label,
                notes         = notes,
                loggedBy      = loggedBy,
                wastageDate   = today,
                posTerminalId = posTerminalId
            )
            wastageDao.insertWastage(wastage)

            // Deduct from inventory
            val item = inventoryDao.getItemById(productId)
            if (item != null) {
                val newStock = maxOf(0.0, item.current_stock - quantity)
                inventoryDao.updateStock(productId, newStock)

                // Deduct from specific batch if provided
                if (!batchId.isNullOrBlank()) {
                    val batch = productBatchDao.getBatchById(batchId)
                    if (batch != null) {
                        val newBatchQty = maxOf(0.0, batch.stockQty - quantity)
                        val now = System.currentTimeMillis()
                        if (reason == WastageReason.EXPIRED || newBatchQty <= 0.0) {
                            productBatchDao.deactivateBatch(batchId, now)
                        } else {
                            productBatchDao.updateBatchStock(batchId, newBatchQty, now)
                        }
                        // Recalculate total stock
                        val allBatches = productBatchDao.getAllBatchesForProduct(productId)
                        val total = allBatches.filter { it.isActive && !it.isDeleted }.sumOf { it.stockQty }
                        inventoryDao.updateTotalStock(productId, total)
                    }
                }
            }

            // Trigger sync
            try {
                val request = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "WASTAGE_SYNC",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
            } catch (e: Exception) { /* non-fatal */ }
        }
    }
}
