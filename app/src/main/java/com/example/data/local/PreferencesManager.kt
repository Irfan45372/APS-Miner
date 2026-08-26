package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("aps_miner_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_MINED_APS = "key_mined_aps"
        private const val KEY_TOTAL_CLAIMED_APS = "key_total_claimed_aps"
        private const val KEY_RIG_LEVEL = "key_rig_level"
        private const val KEY_WALLET_ADDRESS = "key_wallet_address"
        private const val KEY_CUSTOM_RPC_KEY = "key_custom_rpc_key"
        private const val KEY_USE_DEV_RPC = "key_use_dev_rpc"
        private const val KEY_NETWORK_DEVNET = "key_network_devnet"
        private const val KEY_IS_MINING = "key_is_mining"
        private const val KEY_LAST_MINING_TIMESTAMP = "key_last_mining_timestamp"
        private const val KEY_CURRENT_USER_EMAIL = "key_current_user_email"
        private const val KEY_CURRENT_SESSION_START = "key_current_session_start"
        private const val KEY_CURRENT_SESSION_ID = "key_current_session_id"
        private const val KEY_IS_MUTED = "key_is_muted"
        private const val KEY_WALLET_AUTH_TOKEN = "key_wallet_auth_token"
        private const val DEFAULT_USER_EMAIL = "kakakkuganteng@gmail.com"
    }

    var walletAuthToken: String?
        get() = prefs.getString(KEY_WALLET_AUTH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_WALLET_AUTH_TOKEN, value).apply()

    var isSoundMuted: Boolean
        get() = prefs.getBoolean(KEY_IS_MUTED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_MUTED, value).apply()

    var currentUserEmail: String
        get() = prefs.getString(KEY_CURRENT_USER_EMAIL, DEFAULT_USER_EMAIL) ?: DEFAULT_USER_EMAIL
        set(value) = prefs.edit().putString(KEY_CURRENT_USER_EMAIL, value).apply()

    var currentSessionStartTime: Long
        get() = prefs.getLong(KEY_CURRENT_SESSION_START, 0L)
        set(value) = prefs.edit().putLong(KEY_CURRENT_SESSION_START, value).apply()

    var currentSessionId: Long
        get() = prefs.getLong(KEY_CURRENT_SESSION_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_CURRENT_SESSION_ID, value).apply()

    var minedAps: Double
        get() = Double.fromBits(prefs.getLong(KEY_MINED_APS, 0.0.toRawBits()))
        set(value) = prefs.edit().putLong(KEY_MINED_APS, value.toRawBits()).apply()

    var totalClaimedAps: Double
        get() = Double.fromBits(prefs.getLong(KEY_TOTAL_CLAIMED_APS, 0.0.toRawBits()))
        set(value) = prefs.edit().putLong(KEY_TOTAL_CLAIMED_APS, value.toRawBits()).apply()

    var rigLevel: Int
        get() = prefs.getInt(KEY_RIG_LEVEL, 1)
        set(value) = prefs.edit().putInt(KEY_RIG_LEVEL, value).apply()

    var walletAddress: String
        get() = prefs.getString(KEY_WALLET_ADDRESS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WALLET_ADDRESS, value).apply()

    var customRpcKey: String
        get() = prefs.getString(KEY_CUSTOM_RPC_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_RPC_KEY, value).apply()

    var useDeveloperRpc: Boolean
        get() = prefs.getBoolean(KEY_USE_DEV_RPC, true)
        set(value) = prefs.edit().putBoolean(KEY_USE_DEV_RPC, value).apply()

    var isDevnet: Boolean
        get() = prefs.getBoolean(KEY_NETWORK_DEVNET, false)
        set(value) = prefs.edit().putBoolean(KEY_NETWORK_DEVNET, value).apply()

    var isMining: Boolean
        get() = prefs.getBoolean(KEY_IS_MINING, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_MINING, value).apply()

    var lastMiningTimestamp: Long
        get() = prefs.getLong(KEY_LAST_MINING_TIMESTAMP, System.currentTimeMillis())
        set(value) = prefs.edit().putLong(KEY_LAST_MINING_TIMESTAMP, value).apply()
}
