package com.tillzo.pos.data.sync.options.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tillzo.pos.data.local.AppDatabase
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import com.tillzo.pos.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * VendorPaymentReminderWorker — runs daily via WorkManager.
 * Checks for unpaid GRN bills where payment is due or overdue,
 * and triggers recurring loop notifications until balance is cleared.
 */
@HiltWorker
class VendorPaymentReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val appDatabase: AppDatabase,
    private val notificationHelper: NotificationHelper,
    private val appSetupPrefs: AppSetupPrefs
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "VendorPayReminderWorker"
        const val WORK_NAME = "DAILY_VENDOR_PAYMENT_REMINDER"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "VendorPaymentReminderWorker started")
        try {
            val grnDao = appDatabase.grnDao()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val todayStr = sdf.format(Date())
            val todayDate = sdf.parse(todayStr) ?: Date()
            val currencySymbol = appSetupPrefs.currencySymbol.ifBlank { "$" }

            val unpaidGrns = grnDao.getUnpaidGrnsWithDueDate()
            Log.d(TAG, "Found ${unpaidGrns.size} unpaid GRNs with due date")

            unpaidGrns.forEach { grn ->
                if (grn.reminderEnabled && grn.dueBalance > 0.0 && grn.paymentDueDate.isNotBlank()) {
                    try {
                        val dueDate = sdf.parse(grn.paymentDueDate)
                        if (dueDate != null) {
                            val diffDays = ((dueDate.time - todayDate.time) / (1000 * 60 * 60 * 24)).toInt()
                            val isOverdue = diffDays < 0
                            val isDueSoonOrOverdue = diffDays <= 3 // Alert 3 days before, on due date, and every day overdue

                            if (isDueSoonOrOverdue) {
                                Log.d(TAG, "Alerting vendor payment: ${grn.vendorName}, Due: ${grn.paymentDueDate}, Balance: ${grn.dueBalance}")
                                notificationHelper.vendorPaymentDueAlert(
                                    vendorName = grn.vendorName,
                                    amountDue = grn.dueBalance,
                                    dueDate = grn.paymentDueDate,
                                    isOverdue = isOverdue,
                                    currencySymbol = currencySymbol
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing due date for GRN ${grn.grnNumber}", e)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "VendorPaymentReminderWorker failed", e)
            Result.retry()
        }
    }
}
