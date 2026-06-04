package com.tillzo.pos.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class SubscriptionStatus {
    LOADING,
    ACTIVE,
    EXPIRED,
    ERROR
}

/**
 * M8.4: Play Billing Integration
 * Strict compliance with external billing rules.
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var billingClient: BillingClient

    private val _subscriptionStatus = MutableStateFlow(SubscriptionStatus.LOADING)
    val subscriptionStatus = _subscriptionStatus.asStateFlow()

    private val _billingError = MutableStateFlow<String?>(null)
    val billingError = _billingError.asStateFlow()

    // Using a sample generic product ID for documentation sake.
    private val MONTHLY_SUB_ID = "com.tillzo.pos.sub.monthly"

    init {
        initializeBillingClient()
    }

    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
            
        connectToGooglePlay()
    }

    private fun connectToGooglePlay() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                } else {
                    _billingError.value = "Billing Setup Failed: ${billingResult.debugMessage}"
                    _subscriptionStatus.value = SubscriptionStatus.ERROR
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.w("BillingManager", "Service Disconnected. Reconnecting...")
            }
        })
    }

    private fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases)
            } else {
                _billingError.value = "Failed to query purchases: ${billingResult.debugMessage}"
                _subscriptionStatus.value = SubscriptionStatus.ERROR
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            processPurchases(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _billingError.value = "Purchase was cancelled."
        } else {
            _billingError.value = "Purchase error: ${billingResult.debugMessage}"
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        var hasActiveSub = false
        
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase.purchaseToken)
                }
                
                // If it's our subscription and it is active.
                if (purchase.products.contains(MONTHLY_SUB_ID)) {
                    hasActiveSub = true
                }
            }
        }
        
        _subscriptionStatus.value = if (hasActiveSub) SubscriptionStatus.ACTIVE else SubscriptionStatus.EXPIRED
        _billingError.value = null
    }

    private fun acknowledgePurchase(purchaseToken: String) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                _billingError.value = "Acknowledge Failed: ${billingResult.debugMessage}"
            }
        }
    }

    /**
     * Called by UI to initiate the Google Play Billing bottom sheet.
     */
    fun initiatePurchaseFlow(activity: Activity) {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(MONTHLY_SUB_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()
            
        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                
                // Assuming it has only one offer (base plan) for simplicity.
                val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken ?: ""
                
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                // Launch the billing flow
                billingClient.launchBillingFlow(activity, billingFlowParams)
            } else {
                _billingError.value = "Product details not found."
            }
        }
    }
}
