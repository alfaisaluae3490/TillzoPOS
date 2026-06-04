package com.tillzo.pos.domain.update

import com.tillzo.pos.BuildConfig
import com.tillzo.pos.data.local.prefs.UpdatePrefs
import com.tillzo.pos.domain.base.BaseUseCase
import com.tillzo.pos.domain.sync.DataSyncInterface
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Force Update state — returned by CheckForceUpdateUseCase.
 */
sealed class ForceUpdateState {
    /** App is up to date — proceed normally. */
    object UpToDate : ForceUpdateState()

    /** Currently outdated, within 3-day grace period. */
    data class CountdownActive(val daysRemaining: Int) : ForceUpdateState()

    /** Day 4+ — full-screen block, app unusable until updated. */
    object HardBlock : ForceUpdateState()

    /** Failed to fetch settings (network error) — allow app to proceed. */
    object FetchError : ForceUpdateState()
}

/**
 * CheckForceUpdateUseCase — v3 Blueprint M1.6
 *
 * 1. GET Settings tab via DataSyncInterface.getSettings()
 *    → RestApiSyncImpl → SheetsRepository → SheetsRemoteDataSource → Sheets REST API
 *    → Reads "min_app_version" cell from Settings!A:B
 * 2. Compare with BuildConfig.VERSION_CODE
 * 3. If outdated: record first-detection timestamp (only once) in UpdatePrefs
 * 4. Calculate days since first detection:
 *    - Days 1–3 → CountdownActive(daysRemaining)
 *    - Day 4+   → HardBlock
 * 5. If up to date: clear stored timestamp, return UpToDate
 *
 * Architecture Law: Only DataSyncInterface injected here — no Retrofit/Room directly.
 */
class CheckForceUpdateUseCase @Inject constructor(
    private val syncInterface: DataSyncInterface,
    private val updatePrefs: UpdatePrefs
) : BaseUseCase<Unit, ForceUpdateState> {


    override suspend fun invoke(input: Unit): ForceUpdateState {
        return try {
            val settings = syncInterface.getSettings()
            val minVersion = settings.minAppVersion
            val currentVersion = BuildConfig.VERSION_CODE

            if (currentVersion >= minVersion) {
                // App is up to date — clear any stored detection timestamp
                updatePrefs.clearOutdatedTimestamp()
                ForceUpdateState.UpToDate
            } else {
                // App is outdated — record first detection (only if not already recorded)
                val firstDetectedAt = updatePrefs.getOrSetOutdatedTimestamp()
                val now = System.currentTimeMillis()
                val daysElapsed = TimeUnit.MILLISECONDS.toDays(now - firstDetectedAt).toInt()

                when {
                    daysElapsed >= 3 -> ForceUpdateState.HardBlock
                    else -> ForceUpdateState.CountdownActive(daysRemaining = 3 - daysElapsed)
                }
            }
        } catch (e: Exception) {
            // Network failure — allow app to proceed (don't punish offline users)
            ForceUpdateState.FetchError
        }
    }
}
