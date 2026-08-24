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
    @ApplicationContext private val context: Context,
    private val appSetupPrefs: com.tillzo.pos.data.local.prefs.AppSetupPrefs
) : ViewModel() {

    // FIX (2026-08-06): real user/terminal identity instead of hardcoded values
    fun currentUserId(): String = appSetupPrefs.userEmail.ifBlank { "user_1" }
    fun currentTerminalId(): String = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }

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

    /**
     * GAP-4 FIX (2026-08-22): Wastage entry delete UI ka backing function.
     * Soft-delete (syncStatus='deleted') — entry list/totals se turant gayab,
     * sheet par append-only audit trail intact rehta hai (sheet kabhi delete nahi).
     * Stock intentionally NOT mutated — stock correction alag flow (Stock Adjustment)
     * se hota hai, taaki double-deduction/restock race na ho.
     */
    fun deleteWastage(entry: WastageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                wastageDao.softDelete(entry.wastageId)
                android.util.Log.d("WastageVM", "Wastage entry soft-deleted: ${entry.wastageId}")
            } catch (e: Exception) {
                android.util.Log.e("WastageVM", "Delete failed: ${e.message}")
            }
        }
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
        // FIX (2026-08-23, DEF-102): quantity validation — pehle 0/negative qty
        // silently accepted thi; negative qty actually INCREASED stock
        // (maxOf(0, stock - (-5)) = stock+5) aur sheet par bogus wastage row
        // banti thi. Ab reject + log.
        if (quantity <= 0.0) {
            android.util.Log.w("WastageVM", "logWastage rejected: quantity must be > 0 (got $quantity)")
            return
        }
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
                val now = System.currentTimeMillis()
                val newStock = maxOf(0.0, item.current_stock - quantity)
                inventoryDao.updateStockAndSyncStatus(productId, newStock, now)

                // Deduct from specific batch if provided
                if (!batchId.isNullOrBlank()) {
                    val batch = productBatchDao.getBatchById(batchId)
                    if (batch != null) {
                        val newBatchQty = maxOf(0.0, batch.stockQty - quantity)
                        if (reason == WastageReason.EXPIRED || newBatchQty <= 0.0) {
                            productBatchDao.deactivateBatch(batchId, now)
                        } else {
                            productBatchDao.updateBatchStock(batchId, newBatchQty, now)
                        }
                        // Recalculate total stock
                        val allBatches = productBatchDao.getAllBatchesForProduct(productId)
                        val total = allBatches.filter { it.isActive && !it.isDeleted }.sumOf { it.stockQty }
                        inventoryDao.updateTotalStockAndSyncStatus(productId, total, now)
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
