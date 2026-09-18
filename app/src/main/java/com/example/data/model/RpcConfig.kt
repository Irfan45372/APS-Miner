package com.example.data.model

enum class SolanaNetwork(val displayName: String, val clusterName: String, val defaultRpcUrl: String) {
    MAINNET("Solana Mainnet-Beta", "mainnet-beta", "https://api.mainnet-beta.solana.com")
}

data class RpcConfig(
    val customApiKey: String = Companion.defaultHeliusKey,
    val customEndpoint: String = Companion.defaultHeliusEndpoint,
    val cloudflareRelayerUrl: String = DEFAULT_CLOUDFLARE_RELAYER_URL,
    val useDeveloperRpc: Boolean = false,
    val network: SolanaNetwork = SolanaNetwork.MAINNET,
    val latencyMs: Long? = null,
    val isConnected: Boolean = true,
    val currentSlot: Long? = null,
    val apsPerUsd: Double = DEFAULT_APS_PER_USD,
    val isRealtimePriceLoaded: Boolean = false,
    val lastPriceUpdateTimestamp: Long? = null
) {
    val developerFeeAps: Double
        get() = apsPerUsd * ORACLE_FEE_USD

    val activeRpcUrl: String
        get() {
            if (customApiKey.isNotBlank()) {
                return "https://mainnet.helius-rpc.com/?api-key=${customApiKey.trim()}"
            }
            if (customEndpoint.isNotBlank()) {
                return customEndpoint.trim()
            }
            if (Companion.defaultHeliusEndpoint.isNotBlank()) {
                return Companion.defaultHeliusEndpoint
            }
            return PUBLIC_MAINNET_RPC_URL
        }

    companion object {
        const val PUBLIC_MAINNET_RPC_URL = "https://api.mainnet-beta.solana.com"
        const val APS_MINT_ADDRESS = "5Jbt6zLztKuRF9hc7C8y2VemxMguP86ibDoJzetkpump"
        const val TREASURY_WALLET_ADDRESS = "HNKC9TVBJSKG1Mzu6PE9Yag83y9iZv6yhr6z1UdK92hq"
        const val PAYOUT_HOT_WALLET_ADDRESS = "5QSVq3Ea5CBTKpBiM6Wb7Z8SBXxmiVyJ9dwAhQmApQYN"
        const val DEFAULT_CLOUDFLARE_RELAYER_URL = "https://app.aiprojectsystem.com"
        const val ORACLE_FEE_USD = 10.0
        const val DEFAULT_APS_PER_USD = 344850.3

        val defaultHeliusKey: String
            get() = "0f5056b5-a8f0-4c80-b768-e2baaff5073f"

        val defaultHeliusEndpoint: String
            get() = "https://mainnet.helius-rpc.com/?api-key=0f5056b5-a8f0-4c80-b768-e2baaff5073f"
    }
}

enum class LogType {
    INFO, SUCCESS, WARN, ERROR
}

data class TerminalLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val type: LogType = LogType.INFO
)

data class OfflineEarnings(
    val elapsedSeconds: Long,
    val earnedAps: Double,
    val rigLevel: Int
)
