package com.example.data.model

data class UserAccount(
    val email: String = "kakakkuganteng@gmail.com",
    val displayName: String = "APS Elite Miner",
    val avatarUrl: String = "",
    val rigLevel: Int = 1,
    val minedAps: Double = 0.0,
    val inGameApsHolding: Double = 0.0,
    val totalClaimedAps: Double = 0.0,
    val walletAddress: String? = null,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)
