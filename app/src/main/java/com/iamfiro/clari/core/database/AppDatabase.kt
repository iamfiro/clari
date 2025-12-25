package com.iamfiro.clari.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iamfiro.clari.core.database.dao.LanguagePreferenceDao
import com.iamfiro.clari.core.database.dao.TokenDao
import com.iamfiro.clari.core.database.entity.LanguagePreferenceEntity
import com.iamfiro.clari.core.database.entity.TokenEntity

@Database(
    entities = [LanguagePreferenceEntity::class, TokenEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun languagePreferenceDao(): LanguagePreferenceDao
    abstract fun tokenDao(): TokenDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clari_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

