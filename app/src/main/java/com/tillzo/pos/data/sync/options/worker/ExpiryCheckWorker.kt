package com.tillzo.pos.data.sync.options.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tillzo.pos.data.local.AppDatabase
import com.tillzo.pos.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * ExpiryCheckWorker — runs once per day via PeriodicWorkRequest.
 * Queries InventoryDao for near-expiry and already-expired items,
 * fires expiry notifications via NotificationHelper.
 *
 * Registered as: PeriodicWorkRequest(1, DAYS) in WorkerModule / Application onCreate.
 */
@HiltWorker
class ExpiryCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val appDatabase: AppDatabase,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ExpiryCheckWorker"
        const val WORK_NAME = "DAILY_EXPIRY_CHECK"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "ExpiryCheckWorker started")
        try {
            val inventoryDao = appDatabase.inventoryDao()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val in30Days = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(
                Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }.time
            )
            val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val todayDate = LocalDate.now()

            // Items that have already expired
            val expired = inventoryDao.getExpiredItemsList(today)
            expired.forEach { item ->
                if (item.expiry_date.isNotBlank()) {
                    Log.d(TAG, "EXPIRED: ${item.item_name} (${item.expiry_date})")
                    notificationHelper.expiryNotification(item.item_name, item.expiry_date, 0)
                }
            }

            // Items expiring within 30 days
            val nearExpiry = inventoryDao.getNearExpiryItemsList(today, in30Days)
            nearExpiry.forEach { item ->
                if (item.expiry_date.isNotBlank()) {
                    val daysLeft = try {
                        ChronoUnit.DAYS.between(todayDate, LocalDate.parse(item.expiry_date, dtf)).toInt()
                    } catch (e: Exception) { 0 }
                    Log.d(TAG, "EXPIRING: ${item.item_name} in $daysLeft days")
                    notificationHelper.expiryNotification(item.item_name, item.expiry_date, daysLeft)
                }
            }

            Log.d(TAG, "ExpiryCheckWorker complete. Expired: ${expired.size}, Near expiry: ${nearExpiry.size}")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "ExpiryCheckWorker error: ${e.message}", e)
            Result.retry()
        }
    }
}
