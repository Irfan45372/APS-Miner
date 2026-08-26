package com.example.data.remote

import android.util.Log
import com.example.data.model.RpcConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Real-time Price Quote data model returned by the DEX Oracle.
 */
data class ApsPriceQuote(
    val priceUsdPerToken: Double,       // USD price per 1 APS token (e.g. $0.0000029)
    val apsPerUsd: Double,              // Number of APS tokens per 1 USD (e.g. 344,850.3 APS)
    val dexSource: String,              // Source of price feed: "Jupiter v2", "DexScreener", "GeckoTerminal/Birdeye", or "Fallback Parity"
    val timestamp: Long = System.currentTimeMillis(),
    val isLive: Boolean = true
) {
    /**
     * Calculates the exact dynamic APS token amount required to cover a specified USD fee.
     *
     * Example:
     *  - If 1 APS = $0.0000028998 ($1 = 344,850.3 APS) -> $10 fee = 3,448,503 APS
     *  - If 1 APS rises to $2.00 ($1 = 0.5 APS) -> $10 fee = 5 APS
     *  - If 1 APS rises to $5.00 ($1 = 0.2 APS) -> $10 fee = 2 APS
     */
    fun calculateApsForFeeUsd(feeUsd: Double = 10.0): Double {
        if (priceUsdPerToken > 0.0) {
            return feeUsd / priceUsdPerToken
        }
        if (apsPerUsd > 0.0) {
            return feeUsd * apsPerUsd
        }
        return feeUsd * RpcConfig.DEFAULT_APS_PER_USD
    }

    /**
     * Calculates the USD equivalent for a given APS token balance.
     */
    fun calculateUsdForAps(apsAmount: Double): Double {
        if (priceUsdPerToken > 0.0) {
            return apsAmount * priceUsdPerToken
        }
        if (apsPerUsd > 0.0) {
            return apsAmount / apsPerUsd
        }
        return apsAmount / RpcConfig.DEFAULT_APS_PER_USD
    }

    /**
     * Generates a clear, user-facing summary of the fee calculation and exchange rate.
     */
    fun getFormattedFeeExplanation(feeUsd: Double = 10.0): String {
        val requiredAps = calculateApsForFeeUsd(feeUsd)
        return "Fee: $${String.format(Locale.US, "%.2f", feeUsd)} USD = ${String.format(Locale.US, "%,.2f", requiredAps)} APS (Oracle: 1 APS = $${String.format(Locale.US, "%.8f", priceUsdPerToken)} USD | Source: $dexSource)"
    }
}

/**
 * High-performance DEX Price Oracle Service for the APS Token.
 *
 * Connects directly to premier Solana DEX aggregates (Jupiter API v2, DexScreener, GeckoTerminal/Birdeye)
 * and dynamically calculates the exact token amount required for developer RPC access, rig upgrades,
 * and USD parity conversions.
 */
class ApsPriceOracleService(
    private val externalScope: CoroutineScope? = null
) {
    companion object {
        private const val TAG = "ApsPriceOracleService"
        
        // Fallback default: 1 USD = 344,850.3 APS -> 10 USD = 3,448,503 APS ($0.0000028998 per APS)
        const val DEFAULT_APS_PER_USD = 344850.3
        const val DEFAULT_PRICE_USD_PER_APS = 1.0 / DEFAULT_APS_PER_USD
        const val DEFAULT_DEVELOPER_FEE_USD = 10.0
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    // Default initial quote
    private val _latestQuote = MutableStateFlow(
        ApsPriceQuote(
            priceUsdPerToken = DEFAULT_PRICE_USD_PER_APS,
            apsPerUsd = DEFAULT_APS_PER_USD,
            dexSource = "Fallback Parity ($10 = 3,448,503 APS)",
            isLive = false
        )
    )
    val latestQuote: StateFlow<ApsPriceQuote> = _latestQuote.asStateFlow()

    private var pollingJob: Job? = null

    /**
     * Primary entry point: Queries DEX APIs to fetch the current live APS price and returns an [ApsPriceQuote].
     *
     * Tries in priority:
     * 1. Jupiter Price API v2
     * 2. DexScreener Solana Pair API
     * 3. GeckoTerminal / Birdeye Solana Token API
     * 4. Reliable Default Fallback
     */
    suspend fun fetchCurrentPriceQuote(
        mintAddress: String = RpcConfig.APS_MINT_ADDRESS
    ): ApsPriceQuote = withContext(Dispatchers.IO) {

        // 1. Attempt Jupiter Price API v2
        try {
            val jupUrl = "https://api.jup.ag/price/v2?ids=$mintAddress"
            val request = Request.Builder()
                .url(jupUrl)
                .get()
                .header("User-Agent", "APS-Node-Oracle/2.0")
                .header("Accept", "application/json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val data = json.optJSONObject("data")
                    val tokenData = data?.optJSONObject(mintAddress)
                    val priceStr = tokenData?.optString("price")
                    val price = priceStr?.toDoubleOrNull()

                    if (price != null && price > 0.0) {
                        val apsPerUsd = 1.0 / price
                        val quote = ApsPriceQuote(
                            priceUsdPerToken = price,
                            apsPerUsd = apsPerUsd,
                            dexSource = "Jupiter v2 DEX",
                            timestamp = System.currentTimeMillis(),
                            isLive = true
                        )
                        Log.i(TAG, "Jupiter DEX Oracle updated: 1 APS = $$price USD ($apsPerUsd APS/USD)")
                        _latestQuote.value = quote
                        return@withContext quote
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Jupiter DEX oracle query failed: ${e.message}")
        }

        // 2. Attempt DexScreener DEX Aggregator API
        try {
            val dexUrl = "https://api.dexscreener.com/latest/dex/tokens/$mintAddress"
            val request = Request.Builder()
                .url(dexUrl)
                .get()
                .header("User-Agent", "APS-Node-Oracle/2.0")
                .header("Accept", "application/json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val pairs = json.optJSONArray("pairs")
                    if (pairs != null && pairs.length() > 0) {
                        val firstPair = pairs.getJSONObject(0)
                        val priceUsdStr = firstPair.optString("priceUsd", "")
                        val priceUsd = priceUsdStr.toDoubleOrNull()

                        if (priceUsd != null && priceUsd > 0.0) {
                            val apsPerUsd = 1.0 / priceUsd
                            val quote = ApsPriceQuote(
                                priceUsdPerToken = priceUsd,
                                apsPerUsd = apsPerUsd,
                                dexSource = "DexScreener",
                                timestamp = System.currentTimeMillis(),
                                isLive = true
                            )
                            Log.i(TAG, "DexScreener Oracle updated: 1 APS = $$priceUsd USD ($apsPerUsd APS/USD)")
                            _latestQuote.value = quote
                            return@withContext quote
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "DexScreener oracle query failed: ${e.message}")
        }

        // 3. Attempt GeckoTerminal Solana Token API
        try {
            val geckoUrl = "https://api.geckoterminal.com/api/v2/networks/solana/tokens/$mintAddress"
            val request = Request.Builder()
                .url(geckoUrl)
                .get()
                .header("User-Agent", "APS-Node-Oracle/2.0")
                .header("Accept", "application/json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val data = json.optJSONObject("data")
                    val attributes = data?.optJSONObject("attributes")
                    val priceUsdStr = attributes?.optString("price_usd")
                    val priceUsd = priceUsdStr?.toDoubleOrNull()

                    if (priceUsd != null && priceUsd > 0.0) {
                        val apsPerUsd = 1.0 / priceUsd
                        val quote = ApsPriceQuote(
                            priceUsdPerToken = priceUsd,
                            apsPerUsd = apsPerUsd,
                            dexSource = "GeckoTerminal/Birdeye",
                            timestamp = System.currentTimeMillis(),
                            isLive = true
                        )
                        Log.i(TAG, "GeckoTerminal Oracle updated: 1 APS = $$priceUsd USD ($apsPerUsd APS/USD)")
                        _latestQuote.value = quote
                        return@withContext quote
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "GeckoTerminal oracle query failed: ${e.message}")
        }

        // 4. Fallback to calibrated parity ($10 = 3,448,503 APS)
        val fallbackQuote = ApsPriceQuote(
            priceUsdPerToken = DEFAULT_PRICE_USD_PER_APS,
            apsPerUsd = DEFAULT_APS_PER_USD,
            dexSource = "Fallback Baseline Parity",
            timestamp = System.currentTimeMillis(),
            isLive = false
        )
        _latestQuote.value = fallbackQuote
        return@withContext fallbackQuote
    }

    /**
     * Calculates the required APS amount dynamically for a given USD fee (defaults to $10.00).
     *
     * Dynamically adjusts as the price changes:
     * - If 1 APS = $0.0000028998 -> $10 fee = 3,448,503 APS
     * - If 1 APS = $2.00 -> $10 fee = 5 APS
     * - If 1 APS = $5.00 -> $10 fee = 2 APS
     *
     * @param feeUsd The USD fee amount (default: 10.0)
     * @param mintAddress The SPL token mint address (defaults to APS mint)
     * @return The required amount of APS tokens
     */
    suspend fun calculateApsForUsdFee(
        feeUsd: Double = DEFAULT_DEVELOPER_FEE_USD,
        mintAddress: String = RpcConfig.APS_MINT_ADDRESS
    ): Double {
        val quote = fetchCurrentPriceQuote(mintAddress)
        return quote.calculateApsForFeeUsd(feeUsd)
    }

    /**
     * Synchronous pure calculation utility based on an already known APS/USD exchange rate.
     */
    fun calculateApsForFee(feeUsd: Double, apsPerUsd: Double): Double {
        val safeRate = if (apsPerUsd > 0.0) apsPerUsd else DEFAULT_APS_PER_USD
        return feeUsd * safeRate
    }

    /**
     * Synchronous pure calculation utility based on an already known USD price per 1 APS token.
     */
    fun calculateApsForFeeWithTokenPrice(feeUsd: Double, priceUsdPerToken: Double): Double {
        return if (priceUsdPerToken > 0.0) {
            feeUsd / priceUsdPerToken
        } else {
            feeUsd * DEFAULT_APS_PER_USD
        }
    }

    /**
     * Starts continuous periodic background polling to keep the DEX price oracle updated.
     *
     * @param intervalMillis Polling interval in milliseconds (default: 30 seconds)
     * @param mintAddress The SPL token mint address
     */
    fun startPricePolling(
        intervalMillis: Long = 30_000L,
        mintAddress: String = RpcConfig.APS_MINT_ADDRESS
    ) {
        val scope = externalScope ?: return
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                try {
                    fetchCurrentPriceQuote(mintAddress)
                } catch (e: Exception) {
                    Log.w(TAG, "Error in price polling cycle: ${e.message}")
                }
                delay(intervalMillis)
            }
        }
    }

    /**
     * Stops continuous periodic price polling.
     */
    fun stopPricePolling() {
        pollingJob?.cancel()
        pollingJob = null
    }
}
