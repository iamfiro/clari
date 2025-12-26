package com.iamfiro.clari.screen.note

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.repository.NoteRepository
import com.iamfiro.clari.feature.note.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "NoteListViewModel"

class NoteListViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            noteRepository.getAllNotes(limit = 50, sort = "recent_created")
                .onSuccess { notes ->
                    _notes.value = notes
                    Log.d(TAG, "노트 ${notes.size}개 로드 완료")
                }
                .onFailure { e ->
                    Log.e(TAG, "노트 로드 실패", e)
                    _error.value = "노트를 불러오는데 실패했습니다: ${e.message}"
                }
            
            _isLoading.value = false
        }
    }

    fun refresh() {
        loadNotes()
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
                .onSuccess {
                    Log.d(TAG, "노트 삭제 완료: $noteId")
                    // 목록에서 제거
                    _notes.value = _notes.value.filter { it.id != noteId }
                }
                .onFailure { e ->
                    Log.e(TAG, "노트 삭제 실패", e)
                    _error.value = "노트 삭제에 실패했습니다"
                }
        }
    }
}
