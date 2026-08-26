package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mining_sessions",
    indices = [Index(value = ["userEmail"]), Index(value = ["sessionStartTime"])]
)
data class MiningSessionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userEmail: String,
    val sessionStartTime: Long = System.currentTimeMillis(),
    val sessionEndTime: Long? = null,
    val durationSeconds: Long = 0L,
    val minedAps: Double = 0.0,
    val averageHashrateMhs: Double = 1.25,
    val rigLevel: Int = 1,
    val rigName: String = "Rig v1",
    val endReason: String = "MANUAL_STOP" // MANUAL_STOP, APP_PAUSED, OVERCLOCK_COMPLETE, STILL_ACTIVE, OFFLINE_COLLECTED
) {
    val formattedDuration: String
        get() {
            val hours = durationSeconds / 3600
            val minutes = (durationSeconds % 3600) / 60
            val seconds = durationSeconds % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
}
