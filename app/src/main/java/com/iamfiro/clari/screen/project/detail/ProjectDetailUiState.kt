package com.iamfiro.clari.screen.project.detail

import com.iamfiro.clari.feature.project.model.Project

data class ProjectDetailUiState(
    val project: Project? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val aiSuggestions: List<String> = emptyList(),
    val isAiLoading: Boolean = false,
    val isAddingConnector: Boolean = false,
    val aiGeneratedWords: List<com.iamfiro.clari.feature.project.model.Word> = emptyList()
)
