package com.iamfiro.clari.screen.recording

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.repository.ExternalResourceRepository
import com.iamfiro.clari.core.repository.KeywordPackRepository
import com.iamfiro.clari.feature.externalresource.model.ExternalResource
import com.iamfiro.clari.feature.project.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "BeforeRecordingViewModel"

class BeforeRecordingViewModel(
    private val keywordPackRepository: KeywordPackRepository,
    private val externalResourceRepository: ExternalResourceRepository
) : ViewModel() {

    // 키워드팩 (프로젝트)
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _filteredProjects = MutableStateFlow<List<Project>>(emptyList())
    val filteredProjects: StateFlow<List<Project>> = _filteredProjects.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject.asStateFlow()

    // 외부 리소스
    private val _externalResources = MutableStateFlow<List<ExternalResource>>(emptyList())
    val externalResources: StateFlow<List<ExternalResource>> = _externalResources.asStateFlow()

    private val _selectedResourceIds = MutableStateFlow<List<String>>(emptyList())
    val selectedResourceIds: StateFlow<List<String>> = _selectedResourceIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // 키워드팩 로드
            keywordPackRepository.getAllKeywordPacks()
                .onSuccess { packs ->
                    _projects.value = packs
                    _filteredProjects.value = packs
                    Log.d(TAG, "키워드팩 ${packs.size}개 로드 완료")
                }
                .onFailure { e ->
                    Log.e(TAG, "키워드팩 로드 실패", e)
                }
            
            // 외부 리소스 로드
            externalResourceRepository.getAllResources()
                .onSuccess { resources ->
                    _externalResources.value = resources
                    Log.d(TAG, "외부 리소스 ${resources.size}개 로드 완료")
                }
                .onFailure { e ->
                    Log.e(TAG, "외부 리소스 로드 실패", e)
                }
            
            _isLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterProjects()
    }

    private fun filterProjects() {
        val query = _searchQuery.value.lowercase()
        _filteredProjects.value = if (query.isEmpty()) {
            _projects.value
        } else {
            _projects.value.filter { project ->
                project.name.lowercase().contains(query) ||
                project.description.lowercase().contains(query) ||
                project.publisherName.lowercase().contains(query)
            }
        }
    }

    fun selectProject(project: Project) {
        _selectedProject.value = project
    }

    fun clearSelection() {
        _selectedProject.value = null
    }

    fun toggleResourceSelection(resourceId: String) {
        val current = _selectedResourceIds.value.toMutableList()
        if (current.contains(resourceId)) {
            current.remove(resourceId)
        } else {
            current.add(resourceId)
        }
        _selectedResourceIds.value = current
    }

    fun getSelectedKeywordPackIds(): List<String> {
        return _selectedProject.value?.let { listOf(it.id) } ?: emptyList()
    }

    fun getSelectedResourceIds(): List<String> {
        return _selectedResourceIds.value
    }
}
