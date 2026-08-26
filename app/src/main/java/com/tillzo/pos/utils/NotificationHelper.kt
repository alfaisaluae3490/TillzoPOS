package com.tillzo.pos.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val LOW_STOCK_CHANNEL    = "low_stock_channel"
        const val OUT_OF_STOCK_CHANNEL = "out_of_stock_channel"
        const val EXPIRY_CHANNEL       = "expiry_channel"
        const val VENDOR_PAYMENT_CHANNEL = "vendor_payment_channel"
    }

    init {
        createChannels()
    }

    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(LOW_STOCK_CHANNEL, "Low Stock Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Alerts when product stock falls below reorder threshold"
                }
            )
            notificationManager.createNotificationChannel(
                NotificationChannel(OUT_OF_STOCK_CHANNEL, "Out of Stock Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Alerts when a product is completely out of stock"
                }
            )
            notificationManager.createNotificationChannel(
                NotificationChannel(EXPIRY_CHANNEL, "Expiry Alerts", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Alerts for products nearing or past their expiry date"
                }
            )
            notificationManager.createNotificationChannel(
                NotificationChannel(VENDOR_PAYMENT_CHANNEL, "Vendor Payment Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Alerts for pending vendor payments and due credit bills"
                }
            )
        }
    }

    private fun canNotify(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return notificationManager.areNotificationsEnabled()
        }
        return true
    }

    /**
     * PLAY POLICY (2026-08-24, T4): Runtime POST_NOTIFICATIONS request.
     * Android 13+ (API 33) requires an explicit runtime grant — the manifest
     * declaration alone is not enough. Call this ONCE from a resumed Activity
     * (e.g. MainActivity.onCreate / onboarding flow) BEFORE any alerts are
     * scheduled by WorkManager.
     *
     * Returns true when permission is already granted, or on < Android 13
     * where no runtime request is needed. The Activity result callback must
     * be wired by the caller using registerForActivityResult(
     * RequestPermissionContract()).
     */
    fun needsRuntimePermissionRequest(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    /** The exact permission string for the runtime launcher. */
    val runtimePermission: String
        get() = android.Manifest.permission.POST_NOTIFICATIONS

    private fun send(channelId: String, title: String, body: String, notificationId: Int) {
        if (!canNotify()) return
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(
                if (channelId == EXPIRY_CHANNEL) NotificationCompat.PRIORITY_DEFAULT
                else NotificationCompat.PRIORITY_HIGH
            )
            .setAutoCancel(true)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    fun lowStockAlert(productName: String, qty: Double, unit: String) {
        send(
            LOW_STOCK_CHANNEL,
            "⚠️ Low Stock: $productName",
            "Only $qty $unit remaining. Time to reorder.",
            productName.hashCode()
        )
    }

    fun outOfStockAlert(productName: String) {
        send(
            OUT_OF_STOCK_CHANNEL,
            "🚫 Out of Stock: $productName",
            "$productName is completely out of stock.",
            productName.hashCode() + 1
        )
    }

    fun expiryNotification(productName: String, expiryDate: String, daysLeft: Int) {
        val title = if (daysLeft <= 0) "❌ EXPIRED: $productName" else "⏰ Expiring Soon: $productName"
        val body  = if (daysLeft <= 0) "Expired on $expiryDate" else "Expires in $daysLeft days ($expiryDate)"
        send(EXPIRY_CHANNEL, title, body, productName.hashCode() + 2)
    }

    fun vendorPaymentDueAlert(
        vendorName: String,
        amountDue: Double,
        dueDate: String,
        isOverdue: Boolean,
        currencySymbol: String
    ) {
        val title = if (isOverdue) "🚨 OVERDUE Vendor Bill: $vendorName" else "⏰ Vendor Payment Due: $vendorName"
        val body = if (isOverdue) {
            "Payment of $currencySymbol%.2f was due on $dueDate. Balance is still pending.".format(amountDue)
        } else {
            "Payment of $currencySymbol%.2f is due on $dueDate.".format(amountDue)
        }
        send(VENDOR_PAYMENT_CHANNEL, title, body, (vendorName + dueDate).hashCode())
    }

    // Legacy alias used by older call sites — delegates to new function
    fun nearExpiryAlert(productName: String, expiryDate: String) = expiryNotification(productName, expiryDate, 7)
    fun expiredAlert(productName: String, expiryDate: String)    = expiryNotification(productName, expiryDate, 0)
}
