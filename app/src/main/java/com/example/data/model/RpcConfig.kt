package com.example.data.model

enum class SolanaNetwork(val displayName: String, val defaultRpc: String) {
    MAINNET("Solana Mainnet-Beta", "https://mainnet.helius-rpc.com/?api-key=0f5056b5-a8f0-4c80-b768-e2baaff5073f"),
    DEVNET("Solana Devnet", "https://api.devnet.solana.com")
}

data class RpcConfig(
    val customApiKey: String = "",
    val customEndpoint: String = "",
    val useDeveloperRpc: Boolean = true,
    val network: SolanaNetwork = SolanaNetwork.MAINNET,
    val latencyMs: Long? = null,
    val isConnected: Boolean = true,
    val currentSlot: Long? = null,
    val apsPerUsd: Double = DEFAULT_APS_PER_USD,
    val isRealtimePriceLoaded: Boolean = false,
    val lastPriceUpdateTimestamp: Long? = null
) {
    companion object {
        const val DEVELOPER_RPC_URL = "https://mainnet.helius-rpc.com/?api-key=0f5056b5-a8f0-4c80-b768-e2baaff5073f"
        const val APS_MINT_ADDRESS = "5Jbt6zLztKuRF9hc7C8y2VemxMguP86ibDoJzetkpump"
        const val TREASURY_WALLET_ADDRESS = "HNKC9TVBJSKG1Mzu6PE9Yag83y9iZv6yhr6z1UdK92hq"
        const val ORACLE_FEE_USD = 10.0
        const val DEFAULT_APS_PER_USD = 344850.3 // 1 USD = 344,850.3 APS -> 10 USD = 3,448,503 APS
    }

    val developerFeeAps: Double
        get() = ORACLE_FEE_USD * apsPerUsd // 10 USD * 338,174.39 = 3,381,743.9 APS

    val activeRpcUrl: String
        get() = when {
            network == SolanaNetwork.DEVNET -> SolanaNetwork.DEVNET.defaultRpc
            !useDeveloperRpc && customEndpoint.isNotBlank() -> customEndpoint
            !useDeveloperRpc && customApiKey.isNotBlank() -> "https://mainnet.helius-rpc.com/?api-key=${customApiKey.trim()}"
            else -> DEVELOPER_RPC_URL
        }
}

