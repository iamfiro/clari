package com.iamfiro.clari.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "language_preference")
data class LanguagePreferenceEntity(
    @PrimaryKey val id: Int = 1,
    val languageCode: String
)

