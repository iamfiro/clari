package com.iamfiro.clari.screen.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.Repository.NoteRepository
import com.iamfiro.clari.feature.note.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteDetailViewModel(
    private val noteRepository: NoteRepository,
    private val noteId: String
) : ViewModel() {

    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 오디오 재생 상태
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val fetchedNote = noteRepository.getNoteById(noteId)
                if (fetchedNote != null) {
                    _note.value = fetchedNote
                } else {
                    _error.value = "노트를 찾을 수 없습니다."
                }
            } catch (e: Exception) {
                _error.value = "노트를 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadNote()
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun skipForward(seconds: Int = 5) {
        val note = _note.value ?: return
        val newPosition = (_currentPositionMs.value + seconds * 1000).coerceAtMost(note.duration)
        _currentPositionMs.value = newPosition
    }

    fun skipBackward(seconds: Int = 5) {
        val newPosition = (_currentPositionMs.value - seconds * 1000).coerceAtLeast(0)
        _currentPositionMs.value = newPosition
    }

    fun seekTo(positionMs: Long) {
        val note = _note.value ?: return
        _currentPositionMs.value = positionMs.coerceIn(0, note.duration)
    }

    fun updateNoteName(newName: String) {
        viewModelScope.launch {
            try {
                val updatedNote = noteRepository.updateNote(noteId, newName)
                if (updatedNote != null) {
                    _note.value = updatedNote
                }
            } catch (e: Exception) {
                _error.value = "노트 이름 변경에 실패했습니다: ${e.message}"
            }
        }
    }
}

