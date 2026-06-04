package com.tillzo.pos.ui.settings.options.billing

import androidx.lifecycle.ViewModel
import com.tillzo.pos.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    val billingManager: BillingManager
) : ViewModel() {
    
    val subscriptionStatus = billingManager.subscriptionStatus
    val billingError = billingManager.billingError

}
