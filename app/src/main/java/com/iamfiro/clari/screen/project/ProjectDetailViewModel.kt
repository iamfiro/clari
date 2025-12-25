package com.iamfiro.clari.screen.project

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.Repository.ProjectRepository
import com.iamfiro.clari.feature.project.model.Project
import com.iamfiro.clari.feature.project.model.ProjectConnector
import com.iamfiro.clari.feature.project.model.ProjectConnectorType
import com.iamfiro.clari.feature.project.model.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectDetailViewModel(
    private val projectRepository: ProjectRepository,
    private val projectId: String
) : ViewModel() {

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val fetchedProject = projectRepository.getProjectById(projectId)
                if (fetchedProject != null) {
                    _project.value = fetchedProject
                } else {
                    _error.value = "프로젝트를 찾을 수 없습니다."
                }
            } catch (e: Exception) {
                _error.value = "프로젝트를 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadProject()
    }

    fun addWord(name: String, meaning: String) {
        viewModelScope.launch {
            _error.value = null
            try {
                val word = Word(name = name, meaning = meaning)
                val updatedProject = projectRepository.addWordToProject(projectId, word)
                if (updatedProject != null) {
                    _project.value = updatedProject
                } else {
                    _error.value = "단어 추가에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = "단어 추가에 실패했습니다: ${e.message}"
            }
        }
    }

    fun removeWord(wordName: String) {
        viewModelScope.launch {
            _error.value = null
            try {
                val updatedProject = projectRepository.removeWordFromProject(projectId, wordName)
                if (updatedProject != null) {
                    _project.value = updatedProject
                } else {
                    _error.value = "단어 삭제에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = "단어 삭제에 실패했습니다: ${e.message}"
            }
        }
    }

    fun addConnector(type: ProjectConnectorType, name: String, url: String) {
        viewModelScope.launch {
            _error.value = null
            try {
                val connector = ProjectConnector(type = type, name = name, url = url)
                val updatedProject = projectRepository.addConnectorToProject(projectId, connector)
                if (updatedProject != null) {
                    _project.value = updatedProject
                } else {
                    _error.value = "외부 연결 추가에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = "외부 연결 추가에 실패했습니다: ${e.message}"
            }
        }
    }

    fun updateConnector(oldConnector: ProjectConnector, newConnector: ProjectConnector) {
        viewModelScope.launch {
            _error.value = null
            try {
                val updatedProject = projectRepository.updateConnector(projectId, oldConnector, newConnector)
                if (updatedProject != null) {
                    _project.value = updatedProject
                } else {
                    _error.value = "외부 연결 수정에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = "외부 연결 수정에 실패했습니다: ${e.message}"
            }
        }
    }

    fun removeConnector(connector: ProjectConnector) {
        viewModelScope.launch {
            _error.value = null
            try {
                val updatedProject = projectRepository.removeConnector(projectId, connector)
                if (updatedProject != null) {
                    _project.value = updatedProject
                } else {
                    _error.value = "외부 연결 삭제에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = "외부 연결 삭제에 실패했습니다: ${e.message}"
            }
        }
    }

    fun updateBannerImage(uri: Uri) {
        viewModelScope.launch {
            _error.value = null
            try {
                // Mock: URI를 문자열로 변환하여 저장
                val imageUriString = uri.toString()
                val updatedProject = projectRepository.updateBannerImage(projectId, imageUriString)
                if (updatedProject != null) {
                    _project.value = updatedProject
                } else {
                    _error.value = "배너 이미지 업로드에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = "배너 이미지 업로드에 실패했습니다: ${e.message}"
            }
        }
    }

    fun deleteProject(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _error.value = null
            try {
                val success = projectRepository.deleteProject(projectId)
                if (success) {
                    onSuccess()
                } else {
                    _error.value = "프로젝트 삭제에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = "프로젝트 삭제에 실패했습니다: ${e.message}"
            }
        }
    }

    suspend fun getShareLink(): String {
        return projectRepository.getShareLink(projectId)
    }
}

