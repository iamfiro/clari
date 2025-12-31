package com.iamfiro.clari.screen.project.detail

import com.iamfiro.clari.feature.project.model.Project

data class ProjectDetailUiState(
    val project: Project? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val aiSuggestions: List<String> = emptyList(),
    val isAiLoading: Boolean = false,
    val isAddingConnector: Boolean = false,
    val aiGeneratedWords: List<com.iamfiro.clari.feature.project.model.Word> = emptyList(),
    val isTogglingPublic: Boolean = false,
    val isSaving: Boolean = false,
    val isUnsaving: Boolean = false
) {
    val isOwned: Boolean
        get() = project?.isOwned == true
    
    val isSaved: Boolean
        get() = project?.isSaved == true
    
    val isPublic: Boolean
        get() = project?.isPublic == true
    
    val canEdit: Boolean
        get() = isOwned
}
