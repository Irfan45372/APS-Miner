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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class WalletBalancePollingState(
    val walletAddress: String = "",
    val solBalance: Double = 0.0,
    val onChainApsBalance: Double = 0.0,
    val hasApsTokenAccount: Boolean = false,
    val isPolling: Boolean = false,
    val isRefreshing: Boolean = false,
    val lastPollTimestamp: Long = 0L,
    val lastError: String? = null
) {
    /**
     * Checks if the on-chain Phantom wallet holds at least the specified APS threshold
     * (e.g. 338,174 APS for Tier 1 / Level 2 hardware upgrade).
     */
    fun hasSufficientAps(requiredAps: Double): Boolean {
        return onChainApsBalance >= requiredAps
    }
}

/**
 * Dedicated service layer to poll the Helius RPC API for the user's Phantom wallet
 * token balance and verify they hold sufficient funds for the 338,174 APS upgrade.
 */
class HeliusWalletPollingService(
    private val rpcService: SolanaRpcService = SolanaRpcService(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _pollingState = MutableStateFlow(WalletBalancePollingState())
    val pollingState: StateFlow<WalletBalancePollingState> = _pollingState.asStateFlow()

    private var pollingJob: Job? = null

    /**
     * Starts continuous periodic polling (default every 8 seconds) for the given Phantom wallet.
     */
    fun startPolling(
        walletAddress: String,
        rpcUrl: String,
        apsMintAddress: String = RpcConfig.APS_MINT_ADDRESS,
        pollIntervalMs: Long = 8000L
    ) {
        if (walletAddress.isBlank() || walletAddress.length < 32) {
            stopPolling()
            _pollingState.update { WalletBalancePollingState() }
            return
        }

        // Cancel existing job if running with a different wallet
        if (pollingJob?.isActive == true && _pollingState.value.walletAddress == walletAddress) {
            return
        }
        pollingJob?.cancel()

        _pollingState.update {
            it.copy(
                walletAddress = walletAddress,
                isPolling = true,
                lastError = null
            )
        }

        pollingJob = scope.launch {
            while (isActive) {
                try {
                    _pollingState.update { it.copy(isRefreshing = true) }
                    val balances = rpcService.getWalletBalances(
                        rpcUrl = rpcUrl,
                        walletAddress = walletAddress,
                        apsMintAddress = apsMintAddress
                    )

                    _pollingState.update {
                        it.copy(
                            walletAddress = walletAddress,
                            solBalance = balances.solBalance,
                            onChainApsBalance = balances.apsTokenBalance,
                            hasApsTokenAccount = balances.hasApsTokenAccount,
                            isRefreshing = false,
                            lastPollTimestamp = System.currentTimeMillis(),
                            lastError = null
                        )
                    }
                } catch (e: Exception) {
                    Log.w("HeliusPollingService", "Error querying Helius RPC: ${e.message}")
                    _pollingState.update {
                        it.copy(
                            isRefreshing = false,
                            lastError = e.message
                        )
                    }
                }

                delay(pollIntervalMs)
            }
        }
    }

    /**
     * Manually triggers an immediate single check of the Phantom wallet balance via Helius RPC.
     */
    suspend fun checkBalanceImmediately(
        walletAddress: String,
        rpcUrl: String,
        apsMintAddress: String = RpcConfig.APS_MINT_ADDRESS
    ): WalletBalances {
        if (walletAddress.isBlank() || walletAddress.length < 32) {
            return WalletBalances(0.0, 0.0, false, null)
        }

        _pollingState.update { it.copy(isRefreshing = true) }
        val balances = rpcService.getWalletBalances(
            rpcUrl = rpcUrl,
            walletAddress = walletAddress,
            apsMintAddress = apsMintAddress
        )
        _pollingState.update {
            it.copy(
                walletAddress = walletAddress,
                solBalance = balances.solBalance,
                onChainApsBalance = balances.apsTokenBalance,
                hasApsTokenAccount = balances.hasApsTokenAccount,
                isRefreshing = false,
                lastPollTimestamp = System.currentTimeMillis(),
                lastError = null
            )
        }
        return balances
    }

    /**
     * Stops the background polling loop.
     */
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        _pollingState.update { it.copy(isPolling = false, isRefreshing = false) }
    }
}
