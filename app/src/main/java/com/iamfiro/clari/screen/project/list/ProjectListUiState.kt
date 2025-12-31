package com.iamfiro.clari.screen.project.list

import com.iamfiro.clari.feature.project.model.Project

data class ProjectListUiState(
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showMenuModal: Boolean = false,
    val showImportSheet: Boolean = false,
    val importUrl: String = ""
) {
    val ownedProjects: List<Project>
        get() = projects.filter { it.isOwned }
    
    val savedProjects: List<Project>
        get() = projects.filter { it.isSaved }
}
