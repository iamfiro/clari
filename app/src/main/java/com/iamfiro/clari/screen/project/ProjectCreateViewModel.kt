package com.iamfiro.clari.screen.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.Repository.ProjectRepository
import com.iamfiro.clari.feature.project.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectCreateViewModel(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _projectName = MutableStateFlow("")
    val projectName: StateFlow<String> = _projectName.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _createdProject = MutableStateFlow<Project?>(null)
    val createdProject: StateFlow<Project?> = _createdProject.asStateFlow()

    fun updateProjectName(name: String) {
        _projectName.value = name
    }

    fun createProject(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_projectName.value.isBlank()) {
            onError("프로젝트 이름을 입력해주세요.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val project = projectRepository.createProject(_projectName.value)
                _createdProject.value = project
                onSuccess()
            } catch (e: Exception) {
                onError("프로젝트 생성에 실패했습니다: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}




