package com.iamfiro.clari.core.repository

import com.iamfiro.clari.core.database.dao.LanguagePreferenceDao
import com.iamfiro.clari.core.database.entity.LanguagePreferenceEntity
import com.iamfiro.clari.feature.recording.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LanguageRepository(private val dao: LanguagePreferenceDao) {
    
    fun getLanguagePreference(): Flow<Language> {
        return dao.getLanguagePreference().map { entity ->
            entity?.let { Language.values().find { it.code == entity.languageCode } } ?: Language.KOREAN
        }
    }

    suspend fun getLanguagePreferenceOnce(): Language {
        val entity = dao.getLanguagePreferenceOnce()
        return entity?.let { Language.values().find { it.code == entity.languageCode } } ?: Language.KOREAN
    }

    suspend fun saveLanguagePreference(language: Language) {
        dao.saveLanguagePreference(
            LanguagePreferenceEntity(languageCode = language.code)
        )
    }
}

