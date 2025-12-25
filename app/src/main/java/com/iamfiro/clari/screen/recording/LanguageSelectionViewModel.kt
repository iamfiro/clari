package com.iamfiro.clari.screen.recording

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.Repository.LanguageRepository
import com.iamfiro.clari.core.database.AppDatabase
import com.iamfiro.clari.feature.recording.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LanguageSelectionViewModel(
    private val languageRepository: LanguageRepository
) : ViewModel() {

    private val _selectedLanguage = MutableStateFlow<Language?>(null)
    val selectedLanguage: StateFlow<Language?> = _selectedLanguage.asStateFlow()

    private val _savedLanguage = MutableStateFlow<Language>(Language.KOREAN)
    val savedLanguage: StateFlow<Language> = _savedLanguage.asStateFlow()

    init {
        viewModelScope.launch {
            languageRepository.getLanguagePreference().collect { language ->
                _savedLanguage.value = language
            }
        }
    }

    fun selectLanguage(language: Language) {
        _selectedLanguage.value = language
    }

    fun saveLanguageSelection() {
        _selectedLanguage.value?.let { language ->
            viewModelScope.launch {
                languageRepository.saveLanguagePreference(language)
            }
        }
    }
}

class LanguageSelectionViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LanguageSelectionViewModel::class.java)) {
            val database = AppDatabase.getInstance(context)
            val repository = LanguageRepository(database.languagePreferenceDao())
            @Suppress("UNCHECKED_CAST")
            return LanguageSelectionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

