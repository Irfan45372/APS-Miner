package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionStatus {
    CONFIRMED,
    PROCESSING,
    FAILED
}

@Entity(tableName = "claim_transactions")
data class TransactionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userEmail: String = "",
    val signature: String,
    val amountAps: Double,
    val destinationWallet: String,
    val feeSol: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val status: TransactionStatus = TransactionStatus.CONFIRMED,
    val network: String = "Mainnet-Beta",
    val rpcUsed: String = "Helius Oracle"
) {
    val shortSignature: String
        get() = if (signature.length > 16) {
            "${signature.take(8)}...${signature.takeLast(8)}"
        } else signature

    val shortWallet: String
        get() = if (destinationWallet.length > 10) {
            "${destinationWallet.take(4)}...${destinationWallet.takeLast(4)}"
        } else destinationWallet

    val solscanUrl: String
        get() = if (network.contains("Devnet", ignoreCase = true)) {
            "https://solscan.io/tx/$signature?cluster=devnet"
        } else {
            "https://solscan.io/tx/$signature"
        }
}
