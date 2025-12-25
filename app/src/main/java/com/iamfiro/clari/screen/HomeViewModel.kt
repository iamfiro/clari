package com.iamfiro.clari.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.Repository.NoteRepository
import com.iamfiro.clari.core.Repository.ProjectRepository
import com.iamfiro.clari.feature.note.model.Note
import com.iamfiro.clari.feature.project.model.Project
import com.iamfiro.clari.feature.project.model.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val noteRepository: NoteRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words.asStateFlow()

    init {
        _words.value = listOf(
            Word("손도현", "김도현은 손을 좋아한다"),
            Word("클라이언트", "컴퓨터 네트워크나 웹 서비스에서 정보나 서비스를 요청하고 제공받는 주체"),
            Word("안니", "컴퓨터 웹 서비스에서 정보나 서비스를 요청하고 제공받는 주체"),
        )
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _notes.value = noteRepository.getAllNotes()
                _projects.value = projectRepository.getAllProjects()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadData()
    }
}

