package com.iamfiro.clari.screen.project

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.repository.KeywordPackRepository
import com.iamfiro.clari.feature.project.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "ProjectListViewModel"

class ProjectListViewModel(
    private val keywordPackRepository: KeywordPackRepository
) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            keywordPackRepository.getAllKeywordPacks()
                .onSuccess { packs ->
                    _projects.value = packs
                    Log.d(TAG, "키워드팩 ${packs.size}개 로드 완료")
                }
                .onFailure { e ->
                    Log.e(TAG, "키워드팩 로드 실패", e)
                    _error.value = "프로젝트를 불러오는데 실패했습니다: ${e.message}"
                }
            
            _isLoading.value = false
        }
    }

    fun refresh() {
        loadProjects()
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            keywordPackRepository.deleteKeywordPack(projectId)
                .onSuccess {
                    Log.d(TAG, "프로젝트 삭제 완료: $projectId")
                    _projects.value = _projects.value.filter { it.id != projectId }
                }
                .onFailure { e ->
                    Log.e(TAG, "프로젝트 삭제 실패", e)
                    _error.value = "프로젝트 삭제에 실패했습니다"
                }
        }
    }
}
