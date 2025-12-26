package com.iamfiro.clari.screen.project.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.repository.ProjectRepository
import com.iamfiro.clari.feature.project.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "ProjectCreateViewModel"

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

    fun createProject(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        if (_projectName.value.isBlank()) {
            onError("프로젝트 이름을 입력해주세요.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            
            projectRepository.createProject(name = _projectName.value)
                .onSuccess { project ->
                    Log.d(TAG, "프로젝트 생성 완료: ${project.name}")
                    _createdProject.value = project
                    onSuccess(project.id)
                }
                .onFailure { e ->
                    Log.e(TAG, "프로젝트 생성 실패", e)
                    onError("프로젝트 생성에 실패했습니다: ${e.message}")
                }
            
            _isLoading.value = false
        }
    }
}
