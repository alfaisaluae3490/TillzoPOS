package com.tillzo.pos.domain.provisioning

/**
 * ProvisioningState — what the UI shows during auto-provisioning.
 * Emitted step by step as provisioning progresses.
 */
sealed class ProvisioningState {
    object Idle : ProvisioningState()
    object Checking : ProvisioningState()
    object CreatingSheet : ProvisioningState()
    data class Done(val spreadsheetId: String) : ProvisioningState()
    data class Failed(val reason: String) : ProvisioningState()
}
