package com.iamfiro.clari.screen.recording.project

import com.iamfiro.clari.feature.project.model.Project

data class RecordingProjectSelectionUiState(
    val filteredProjects: List<Project> = emptyList(),
    val selectedProject: Project? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)