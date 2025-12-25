package com.iamfiro.clari.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.Repository.NoteRepository
import com.iamfiro.clari.feature.note.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteListViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _notes.value = noteRepository.getAllNotes()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadNotes()
    }
}

