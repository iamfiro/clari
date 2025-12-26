package com.iamfiro.clari.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.repository.KeywordPackRepository
import com.iamfiro.clari.core.repository.NoteRepository
import com.iamfiro.clari.feature.note.model.Note
import com.iamfiro.clari.feature.project.model.Project
import com.iamfiro.clari.feature.project.model.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

class HomeViewModel(
    private val noteRepository: NoteRepository,
    private val keywordPackRepository: KeywordPackRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _keywordPacks = MutableStateFlow<List<Project>>(emptyList())
    val keywordPacks: StateFlow<List<Project>> = _keywordPacks.asStateFlow()

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // 노트 로드
                noteRepository.getAllNotes(limit = 10, sort = "recent_used")
                    .onSuccess { notes ->
                        _notes.value = notes
                        Log.d(TAG, "노트 ${notes.size}개 로드 완료")
                    }
                    .onFailure { e ->
                        Log.e(TAG, "노트 로드 실패", e)
                        _error.value = "노트를 불러오는데 실패했습니다"
                    }
                
                // 키워드팩 로드
                keywordPackRepository.getAllKeywordPacks(limit = 10)
                    .onSuccess { packs ->
                        _keywordPacks.value = packs
                        Log.d(TAG, "키워드팩 ${packs.size}개 로드 완료")
                        
                        // 자주 등장하는 단어 추출 (모든 키워드팩에서 상위 6개)
                        val allWords = packs.flatMap { it.word }.take(6)
                        _words.value = allWords
                    }
                    .onFailure { e ->
                        Log.e(TAG, "키워드팩 로드 실패", e)
                    }
                    
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadData()
    }
}
