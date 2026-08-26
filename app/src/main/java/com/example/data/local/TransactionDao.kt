package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TransactionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM claim_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM claim_transactions WHERE userEmail = :userEmail OR userEmail = '' ORDER BY timestamp DESC")
    fun getTransactionsForUser(userEmail: String): Flow<List<TransactionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionRecord): Long

    @Query("SELECT COUNT(*) FROM claim_transactions")
    suspend fun getTransactionCount(): Int

    @Query("DELETE FROM claim_transactions WHERE userEmail = :userEmail")
    suspend fun clearUserTransactions(userEmail: String)

    @Query("DELETE FROM claim_transactions")
    suspend fun clearAll()
}
