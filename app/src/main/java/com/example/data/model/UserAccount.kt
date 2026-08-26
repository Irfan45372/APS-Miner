package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey
    val email: String,
    val displayName: String = "",
    val avatarUrl: String = "",
    val rigLevel: Int = 1,
    val minedAps: Double = 0.0,
    val totalClaimedAps: Double = 0.0,
    val walletAddress: String = "",
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val totalSessionsCount: Int = 0,
    val totalMiningDurationSeconds: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayShortEmail: String
        get() = if (email.length > 22) {
            val parts = email.split("@")
            if (parts.size == 2) {
                "${parts[0].take(6)}...@${parts[1]}"
            } else {
                "${email.take(18)}..."
            }
        } else email
}
