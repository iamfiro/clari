package com.iamfiro.clari.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.repository.ProjectRepository
import com.iamfiro.clari.core.repository.NoteRepository
import com.iamfiro.clari.feature.note.model.Note
import com.iamfiro.clari.feature.project.model.Project
import com.iamfiro.clari.feature.project.model.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

data class HomeUiState(
    val notes: List<Note> = emptyList(),
    val keywordPacks: List<Project> = emptyList(),
    val words: List<Word> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val noteRepository = NoteRepository.getInstance()
    private val projectRepository = ProjectRepository.getInstance()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                noteRepository.getAllNotes(limit = 10, sort = "recent_used")
                    .onSuccess { notes ->
                        _uiState.value = _uiState.value.copy(notes = notes)
                        Log.d(TAG, "노트 ${notes.size}개 로드 완료")
                    }
                    .onFailure { e ->
                        Log.e(TAG, "노트 로드 실패", e)
                        _uiState.value = _uiState.value.copy(error = "노트를 불러오는데 실패했습니다")
                    }
                
                projectRepository.getProjects(limit = 10)
                    .onSuccess { packs ->
                        val allWords = packs.flatMap { it.word }.take(6)
                        _uiState.value = _uiState.value.copy(
                            keywordPacks = packs,
                            words = allWords
                        )
                        Log.d(TAG, "키워드팩 ${packs.size}개 로드 완료")
                    }
                    .onFailure { e ->
                        Log.e(TAG, "키워드팩 로드 실패", e)
                    }
                    
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun refresh() {
        loadData()
    }
}
