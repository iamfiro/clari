package com.iamfiro.clari.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iamfiro.clari.core.database.entity.LanguagePreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguagePreferenceDao {
    @Query("SELECT * FROM language_preference WHERE id = 1")
    fun getLanguagePreference(): Flow<LanguagePreferenceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLanguagePreference(preference: LanguagePreferenceEntity)

    @Query("SELECT * FROM language_preference WHERE id = 1")
    suspend fun getLanguagePreferenceOnce(): LanguagePreferenceEntity?
}

