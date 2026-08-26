package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MiningSessionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MiningSessionDao {
    @Query("SELECT * FROM mining_sessions WHERE userEmail = :userEmail ORDER BY sessionStartTime DESC")
    fun getSessionsForUser(userEmail: String): Flow<List<MiningSessionRecord>>

    @Query("SELECT * FROM mining_sessions WHERE userEmail = :userEmail ORDER BY sessionStartTime DESC LIMIT :limit")
    fun getRecentSessionsForUser(userEmail: String, limit: Int): Flow<List<MiningSessionRecord>>

    @Query("SELECT * FROM mining_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): MiningSessionRecord?

    @Query("SELECT * FROM mining_sessions WHERE userEmail = :userEmail AND sessionEndTime IS NULL ORDER BY sessionStartTime DESC LIMIT 1")
    suspend fun getActiveSession(userEmail: String): MiningSessionRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: MiningSessionRecord): Long

    @Update
    suspend fun updateSession(session: MiningSessionRecord)

    @Query("SELECT SUM(durationSeconds) FROM mining_sessions WHERE userEmail = :userEmail")
    suspend fun getTotalMiningDuration(userEmail: String): Long?

    @Query("SELECT SUM(minedAps) FROM mining_sessions WHERE userEmail = :userEmail")
    suspend fun getTotalMinedAps(userEmail: String): Double?

    @Query("DELETE FROM mining_sessions WHERE userEmail = :userEmail")
    suspend fun clearSessionsForUser(userEmail: String)
}
