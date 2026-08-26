package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    fun getUserAccountFlow(email: String): Flow<UserAccount?>

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserAccount(email: String): UserAccount?

    @Query("SELECT * FROM user_accounts ORDER BY lastActiveTimestamp DESC")
    fun getAllUsers(): Flow<List<UserAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(account: UserAccount)

    @Update
    suspend fun update(account: UserAccount)

    @Query("DELETE FROM user_accounts WHERE email = :email")
    suspend fun deleteUser(email: String)
}
