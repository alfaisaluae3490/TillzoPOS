package com.tillzo.pos.domain.usecase

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.tillzo.pos.data.local.AppDatabase
import com.tillzo.pos.data.local.dao.CustomerDao
import com.tillzo.pos.data.local.dao.InventoryDao
import com.tillzo.pos.data.local.dao.KhataEventDao
import com.tillzo.pos.data.local.dao.ProductBatchDao
import com.tillzo.pos.data.local.dao.SaleDao
import com.tillzo.pos.data.local.dao.TillSessionDao
import com.tillzo.pos.data.local.entity.CustomerEntity
import com.tillzo.pos.data.local.entity.KhataEventEntity
import com.tillzo.pos.data.local.entity.SaleEntity
import com.tillzo.pos.data.local.entity.SyncStatus
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.data.sync.options.worker.SyncWorker
import com.tillzo.pos.domain.model.CartItem
import com.tillzo.pos.domain.model.PaymentDetails
import com.tillzo.pos.domain.model.Sale
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

class CompleteSaleUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase,
    private val saleDao: SaleDao,
    private val khataEventDao: KhataEventDao,
    private val customerDao: CustomerDao,
    private val tillSessionDao: TillSessionDao,
    private val inventoryDao: InventoryDao,
    private val productBatchDao: ProductBatchDao,
    private val appSetupPrefs: AppSetupPrefs,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "CompleteSaleUseCase"
        private const val WORK_NAME = "POST_SALE_INSTANT_SYNC"
    }

    suspend operator fun invoke(
        cartItems: List<CartItem>,
        subtotal: Double,
        tax: Double,
        discount: Double,
        total: Double,
        paymentMethod: String,
        cashAmount: Double,
        cardAmount: Double,
        walletAmount: Double,
        udhaarAmount: Double,
        selectedCustomerId: String?,
        cashierId: String
    ): SaleEntity {
        val posId = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }
        val invoiceUuid = UUID.randomUUID().toString()
        val systemRowId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val paymentDetails = PaymentDetails(
            cashAmount = cashAmount,
            cardAmount = cardAmount,
            walletAmount = walletAmount,
            udhaarAmount = udhaarAmount
        )

        val saleEntity = SaleEntity(
            system_row_id = systemRowId,
            sync_status = SyncStatus.PENDING,
            pos_terminal_id = posId,
            created_at = now,
            updated_at = now,
            sync_uuid = invoiceUuid,
            cashier_id = cashierId,
            timestamp = now,
            items_json = gson.toJson(cartItems),
            subtotal = subtotal,
            tax = tax,
            discount = discount,
            total = total,
            payment_method = paymentMethod,
            cash_amount = cashAmount,
            card_amount = cardAmount,
            wallet_amount = walletAmount,
            udhaar_amount = udhaarAmount,
            customer_id = selectedCustomerId,
            payment_split_json = if (paymentMethod == "SPLIT") gson.toJson(paymentDetails) else "{}",
            reference_id = null
        )

        // Step 2: Save to Room and deduct stock in a single atomic transaction
        appDatabase.withTransaction {
            saleDao.insertSale(saleEntity)
            deductStockForCartItems(cartItems, now)
        }
        Log.d(TAG, "Sale saved and stock deducted: $invoiceUuid")

        // Step 2c: Loyalty points (FIX 2026-08-06 — industry-standard rewards)
        if (selectedCustomerId != null && appSetupPrefs.loyaltyEnabled) {
            try {
                val customer = customerDao.getCustomerById(selectedCustomerId)
                if (customer != null) {
                    val earnRate = appSetupPrefs.loyaltyPointsPerCurrency.coerceAtLeast(0.0)
                    val pointsEarned = (total.coerceAtLeast(0.0) * earnRate)
                    customerDao.updateLoyalty(
                        id = selectedCustomerId,
                        points = customer.loyalty_points + pointsEarned,
                        spend = customer.lifetime_spend + total.coerceAtLeast(0.0),
                        ts = now
                    )
                    Log.d(TAG, "Loyalty: customer $selectedCustomerId earned $pointsEarned pts")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Loyalty update failed (non-fatal): ${e.message}")
            }
        }

        // Step 2b: Record sale in active till session (non-fatal — no open session is OK)
        try {
            val openSession = tillSessionDao.getOpenSession(posId)
            openSession?.let { session ->
                tillSessionDao.addSaleToSession(
                    sessionId = session.sessionId,
                    totalAmount = total,
                    cashIn = cashAmount,
                    cardIn = cardAmount,
                    walletIn = walletAmount,
                    udhaarIn = udhaarAmount,
                    paymentMethod = paymentMethod
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Till session update non-fatal: ${e.message}")
        }

        // Step 3: If udhaar component, save a KhataEvent
        if (udhaarAmount > 0.0 && selectedCustomerId != null) {
            val khataEvent = KhataEventEntity(
                system_row_id = UUID.randomUUID().toString(),
                sync_status = SyncStatus.PENDING,
                pos_terminal_id = posId,
                created_at = now,
                updated_at = now,
                customer_id = selectedCustomerId,
                event_type = "UDHAAR",
                amount = udhaarAmount,
                note = "Sale: ${invoiceUuid.take(8)}",
                reference_sale_id = systemRowId
            )
            khataEventDao.insert(khataEvent)
            Log.d(TAG, "KhataEvent saved for customer: $selectedCustomerId, amount: $udhaarAmount")
        }

        // Step 4: Trigger instant background sync
        triggerImmediateSync()

        return saleEntity
    }

    /**
     * Deducts sold quantities from local inventory immediately.
     * FIFO for batch-managed products. Floors stock at 0.
     * Marks affected inventory records as sync_status = 'pending'.
     */
    private suspend fun deductStockForCartItems(cartItems: List<CartItem>, now: Long) {
        for (item in cartItems) {
            try {
                val product = inventoryDao.getItemById(item.itemId) ?: continue
                val qtySold = item.quantity
                val newStock = maxOf(0.0, product.current_stock - qtySold)
                inventoryDao.updateStockAndSyncStatus(product.system_row_id, newStock, now)

                Log.d(TAG, "Stock deducted: ${item.itemId} → ${product.current_stock} - $qtySold = $newStock")

                // FIFO batch deduction if product has batches
                if (product.hasBatches) {
                    var remaining = qtySold
                    while (remaining > 0.0) {
                        val oldestBatch = productBatchDao.getOldestActiveBatch(item.itemId) ?: break
                        val deductFromBatch = minOf(remaining, oldestBatch.stockQty)
                        val newBatchQty = oldestBatch.stockQty - deductFromBatch
                        if (newBatchQty <= 0.0) {
                            productBatchDao.deactivateBatch(oldestBatch.batchId, now)
                        } else {
                            productBatchDao.updateBatchStock(oldestBatch.batchId, newBatchQty, now)
                        }
                        remaining -= deductFromBatch
                    }

                    // Recalculate totalStock from all active batches
                    val allBatches = productBatchDao.getAllBatchesForProduct(item.itemId)
                    val total = allBatches.filter { it.isActive && !it.isDeleted }.sumOf { it.stockQty }
                    inventoryDao.updateTotalStockAndSyncStatus(item.itemId, total, now)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Stock deduction failed for ${item.itemId}: ${e.message}")
            }
        }
    }

    suspend fun createNewCustomer(
        name: String,
        phone: String,
        whatsapp: String?
    ): CustomerEntity {
        val posId = appSetupPrefs.spreadsheetId.take(20).ifBlank { "TERM_1" }
        val customer = CustomerEntity(
            system_row_id = UUID.randomUUID().toString(),
            sync_status = SyncStatus.PENDING,
            pos_terminal_id = posId,
            name = name,
            phone = phone,
            whatsapp = whatsapp,
            email = null,
            address = null
        )
        customerDao.insert(customer)
        return customer
    }

    private fun triggerImmediateSync() {
        try {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
            Log.d(TAG, "Post-sale instant sync enqueued")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to enqueue instant sync (non-fatal): ${e.message}")
        }
    }
}
