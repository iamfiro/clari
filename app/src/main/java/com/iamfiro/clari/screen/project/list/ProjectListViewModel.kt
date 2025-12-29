package com.iamfiro.clari.screen.project.list

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.repository.ProjectRepository
import com.iamfiro.clari.core.ui.BaseViewModel
import com.iamfiro.clari.core.ui.Screen
import com.iamfiro.clari.feature.project.model.Project
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val TAG = "ProjectListViewModel"

class ProjectListViewModel() : BaseViewModel() {
    private val projectRepository = ProjectRepository.getInstance()

    private val _uiState = MutableStateFlow(ProjectListUiState())
    val uiState: StateFlow<ProjectListUiState> = _uiState.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)

    init {
        observeProjects()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeProjects() {
        _refreshTrigger
            .flatMapLatest {
                projectRepository.getProjectsFlow()
            }
            .onEach { projects ->
                _uiState.value = _uiState.value.copy(
                    projects = projects,
                    isLoading = false,
                    error = null
                )
                Log.d(TAG, "키워드팩 ${projects.size}개 로드 완료")
            }
            .catch { e ->
                Log.e(TAG, "키워드팩 로드 실패", e)
                _uiState.value = _uiState.value.copy(
                    error = "프로젝트를 불러오는데 실패했습니다: ${e.message}",
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            projectRepository.deleteProject(projectId)
                .onSuccess {
                    Log.d(TAG, "프로젝트 삭제 완료: $projectId")
                    val updatedProjects = _uiState.value.projects.filter { it.id != projectId }
                    _uiState.value = _uiState.value.copy(projects = updatedProjects)
                }
                .onFailure { e ->
                    Log.e(TAG, "프로젝트 삭제 실패", e)
                    _uiState.value = _uiState.value.copy(error = "프로젝트 삭제에 실패했습니다")
                }
        }
    }

    fun openProject(projectId: String) {
        navigateTo(Screen.ProjectDetail(projectId))
    }

    fun showMenuModal() {
        _uiState.value = _uiState.value.copy(showMenuModal = true)
    }

    fun hideMenuModal() {
        _uiState.value = _uiState.value.copy(showMenuModal = false)
    }

    fun showImportSheet() {
        _uiState.value = _uiState.value.copy(showImportSheet = true)
    }

    fun hideImportSheet() {
        _uiState.value = _uiState.value.copy(showImportSheet = false, importUrl = "")
    }

    fun updateImportUrl(url: String) {
        _uiState.value = _uiState.value.copy(importUrl = url)
    }
}
