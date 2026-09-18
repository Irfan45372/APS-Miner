package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ActiveRental

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("aps_miner_prefs", Context.MODE_PRIVATE)

    var isMiningActive: Boolean
        get() = prefs.getBoolean(KEY_IS_MINING, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_MINING, value).apply()

    var rigLevel: Int
        get() = prefs.getInt(KEY_RIG_LEVEL, 1).coerceIn(1, 5)
        set(value) = prefs.edit().putInt(KEY_RIG_LEVEL, value).apply()

    var minedAps: Double
        get() = java.lang.Double.longBitsToDouble(prefs.getLong(KEY_MINED_APS, java.lang.Double.doubleToRawLongBits(0.0)))
        set(value) = prefs.edit().putLong(KEY_MINED_APS, java.lang.Double.doubleToRawLongBits(value)).apply()

    var inGameApsHolding: Double
        get() = java.lang.Double.longBitsToDouble(prefs.getLong(KEY_HOLDING_APS, java.lang.Double.doubleToRawLongBits(0.0)))
        set(value) = prefs.edit().putLong(KEY_HOLDING_APS, java.lang.Double.doubleToRawLongBits(value)).apply()

    var totalClaimedAps: Double
        get() = java.lang.Double.longBitsToDouble(prefs.getLong(KEY_TOTAL_CLAIMED, java.lang.Double.doubleToRawLongBits(0.0)))
        set(value) = prefs.edit().putLong(KEY_TOTAL_CLAIMED, java.lang.Double.doubleToRawLongBits(value)).apply()

    var walletAddress: String?
        get() = prefs.getString(KEY_WALLET_ADDRESS, null)
        set(value) = prefs.edit().putString(KEY_WALLET_ADDRESS, value).apply()

    var lastActiveTimestamp: Long
        get() = prefs.getLong(KEY_LAST_ACTIVE, System.currentTimeMillis())
        set(value) = prefs.edit().putLong(KEY_LAST_ACTIVE, value).apply()

    var customApiKey: String
        get() = prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_API_KEY, value).apply()

    var customEndpoint: String
        get() = prefs.getString(KEY_CUSTOM_ENDPOINT, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_ENDPOINT, value).apply()

    fun getActiveRental(): ActiveRental? {
        val planId = prefs.getString(KEY_RENTAL_PLAN_ID, null) ?: return null
        val planName = prefs.getString(KEY_RENTAL_PLAN_NAME, "") ?: ""
        val start = prefs.getLong(KEY_RENTAL_START, 0L)
        val duration = prefs.getLong(KEY_RENTAL_DURATION, 0L)
        val bonusHash = java.lang.Double.longBitsToDouble(prefs.getLong(KEY_RENTAL_HASH, 0L))
        val bonusYield = java.lang.Double.longBitsToDouble(prefs.getLong(KEY_RENTAL_YIELD, 0L))

        val rental = ActiveRental(planId, planName, start, duration, bonusHash, bonusYield)
        return if (rental.isExpired) {
            clearActiveRental()
            null
        } else {
            rental
        }
    }

    fun saveActiveRental(rental: ActiveRental) {
        prefs.edit()
            .putString(KEY_RENTAL_PLAN_ID, rental.planId)
            .putString(KEY_RENTAL_PLAN_NAME, rental.planName)
            .putLong(KEY_RENTAL_START, rental.startTimestamp)
            .putLong(KEY_RENTAL_DURATION, rental.durationMillis)
            .putLong(KEY_RENTAL_HASH, java.lang.Double.doubleToRawLongBits(rental.bonusHashrateMhs))
            .putLong(KEY_RENTAL_YIELD, java.lang.Double.doubleToRawLongBits(rental.bonusYieldPerSecond))
            .apply()
    }

    fun clearActiveRental() {
        prefs.edit()
            .remove(KEY_RENTAL_PLAN_ID)
            .remove(KEY_RENTAL_PLAN_NAME)
            .remove(KEY_RENTAL_START)
            .remove(KEY_RENTAL_DURATION)
            .remove(KEY_RENTAL_HASH)
            .remove(KEY_RENTAL_YIELD)
            .apply()
    }

    companion object {
        private const val KEY_IS_MINING = "key_is_mining"
        private const val KEY_RIG_LEVEL = "key_rig_level"
        private const val KEY_MINED_APS = "key_mined_aps"
        private const val KEY_HOLDING_APS = "key_holding_aps"
        private const val KEY_TOTAL_CLAIMED = "key_total_claimed"
        private const val KEY_WALLET_ADDRESS = "key_wallet_address"
        private const val KEY_LAST_ACTIVE = "key_last_active"
        private const val KEY_CUSTOM_API_KEY = "key_custom_api_key"
        private const val KEY_CUSTOM_ENDPOINT = "key_custom_endpoint"
        private const val KEY_RENTAL_PLAN_ID = "key_rental_plan_id"
        private const val KEY_RENTAL_PLAN_NAME = "key_rental_plan_name"
        private const val KEY_RENTAL_START = "key_rental_start"
        private const val KEY_RENTAL_DURATION = "key_rental_duration"
        private const val KEY_RENTAL_HASH = "key_rental_hash"
        private const val KEY_RENTAL_YIELD = "key_rental_yield"
    }
}
