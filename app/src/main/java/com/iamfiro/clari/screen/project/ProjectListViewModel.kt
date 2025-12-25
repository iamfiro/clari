package com.iamfiro.clari.screen.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.Repository.ProjectRepository
import com.iamfiro.clari.feature.project.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectListViewModel(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _projects.value = projectRepository.getAllProjects()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadProjects()
    }
}

