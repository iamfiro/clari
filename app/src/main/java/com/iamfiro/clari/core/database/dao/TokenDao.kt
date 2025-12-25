package com.iamfiro.clari.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iamfiro.clari.core.database.entity.TokenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenDao {
    @Query("SELECT * FROM token WHERE id = 1")
    fun getToken(): Flow<TokenEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveToken(token: TokenEntity)

    @Query("SELECT * FROM token WHERE id = 1")
    suspend fun getTokenOnce(): TokenEntity?

    @Query("DELETE FROM token WHERE id = 1")
    suspend fun deleteToken()
}

