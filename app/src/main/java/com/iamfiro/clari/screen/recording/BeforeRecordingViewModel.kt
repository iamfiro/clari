package com.iamfiro.clari.screen.recording

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.Repository.ProjectRepository
import com.iamfiro.clari.feature.project.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BeforeRecordingViewModel(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _filteredProjects = MutableStateFlow<List<Project>>(emptyList())
    val filteredProjects: StateFlow<List<Project>> = _filteredProjects.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allProjects = projectRepository.getAllProjects()
                _projects.value = allProjects
                _filteredProjects.value = allProjects
            } finally {
                _isLoading.value = false
            }
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
}




