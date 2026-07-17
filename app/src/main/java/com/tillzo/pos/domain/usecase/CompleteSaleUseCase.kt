package com.tillzo.pos.domain.usecase

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.tillzo.pos.data.local.dao.CustomerDao
import com.tillzo.pos.data.local.dao.KhataEventDao
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

/**
 * M4 CompleteSaleUseCase
 *
 * Steps:
 *   1. Build SaleEntity from cart + payment data
 *   2. Insert SaleEntity to Room (OFFLINE FIRST, syncStatus = "pending")
 *   3. If udhaar component → insert KhataEventEntity to Room
 *   4. Trigger OneTimeWorkRequest so sync happens ASAP
 *   5. Return the saved SaleEntity (for receipt display)
 *
 * Architecture Law: NEVER checks stock (Blind Selling rule).
 * Architecture Law: NEVER marks syncStatus = "synced" here — only SyncWorker + HTTP 200 does.
 */
class CompleteSaleUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val saleDao: SaleDao,
    private val khataEventDao: KhataEventDao,
    private val customerDao: CustomerDao,
    private val tillSessionDao: TillSessionDao,
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
            payment_split_json = if (paymentMethod == "SPLIT") gson.toJson(paymentDetails) else null,
            reference_id = null
        )

        // Step 2: Save to Room FIRST (offline-first rule)
        saleDao.insertSale(saleEntity)
        Log.d(TAG, "Sale saved to Room: $invoiceUuid")

        // Step 2b: Record sale in active till session (non-fatal — no open session is OK)
        try {
            val posId = posId
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
     * Creates a new customer record in Room and returns their ID.
     * Used for the "Add New Customer" flow in the Udhaar payment section.
     */
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
