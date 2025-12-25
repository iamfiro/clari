package com.iamfiro.clari.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iamfiro.clari.core.database.dao.LanguagePreferenceDao
import com.iamfiro.clari.core.database.entity.LanguagePreferenceEntity

@Database(
    entities = [LanguagePreferenceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun languagePreferenceDao(): LanguagePreferenceDao

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

