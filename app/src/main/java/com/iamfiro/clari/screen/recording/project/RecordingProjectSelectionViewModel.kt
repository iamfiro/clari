package com.iamfiro.clari.screen.recording.project

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iamfiro.clari.core.repository.ExternalResourceRepository
import com.iamfiro.clari.core.repository.ProjectRepository
import com.iamfiro.clari.feature.externalresource.model.ExternalResource
import com.iamfiro.clari.feature.project.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "RecordingProjectSelectionViewModel"

class RecordingProjectSelectionViewModel() : ViewModel() {
    private val projectRepository =  ProjectRepository.getInstance()
    private val externalResourceRepository = ExternalResourceRepository.getInstance()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _uiState = MutableStateFlow(RecordingProjectSelectionUiState())
    val uiState: StateFlow<RecordingProjectSelectionUiState> = _uiState.asStateFlow()

    private val _externalResources = MutableStateFlow<List<ExternalResource>>(emptyList())
    val externalResources: StateFlow<List<ExternalResource>> = _externalResources.asStateFlow()

    private val _selectedResourceIds = MutableStateFlow<List<String>>(emptyList())
    val selectedResourceIds: StateFlow<List<String>> = _selectedResourceIds.asStateFlow()

    init {
        loadData()
    }

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            projectRepository.getProjects()
                .onSuccess { packs ->
                    _projects.value = packs
                    _uiState.value = _uiState.value.copy(
                        filteredProjects = packs,
                        isLoading = false
                    )
                    Log.d(TAG, "프로젝트 ${packs.size}개 로드 완료")
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    Log.e(TAG, "키워드팩 로드 실패", e)
                }

            externalResourceRepository.getAllResources()
                .onSuccess { resources ->
                    _externalResources.value = resources
                    Log.d(TAG, "외부 리소스 ${resources.size}개 로드 완료")
                }
                .onFailure { e ->
                    Log.e(TAG, "외부 리소스 로드 실패", e)
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterProjects()
    }

    private fun filterProjects() {
        val query = _uiState.value.searchQuery.lowercase()
        val filtered = if (query.isEmpty()) {
            _projects.value
        } else {
            _projects.value.filter { project ->
                project.name.lowercase().contains(query) ||
                project.description.lowercase().contains(query) ||
                project.publisherName.lowercase().contains(query)
            }
        }
        _uiState.value = _uiState.value.copy(filteredProjects = filtered)
    }

    fun selectProject(project: Project) {
        _uiState.value = _uiState.value.copy(selectedProject = project)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedProject = null)
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
        return _uiState.value.selectedProject?.let { listOf(it.id) } ?: emptyList()
    }

    fun getSelectedResourceIds(): List<String> {
        return _selectedResourceIds.value
    }

    fun startWithoutProject() {
        _uiState.value = _uiState.value.copy(selectedProject = null)
        Log.d(TAG, "프로젝트 없이 시작하기 선택")
    }
}
