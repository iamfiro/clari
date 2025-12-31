package com.iamfiro.clari.feature.project.model

enum class ProjectConnectorType {
    NOTION,
    GDRIVE
}

data class ProjectConnector (
    val type: ProjectConnectorType,
    val url: String,
    val name: String,
)

data class Project(
    val id: String,
    val name: String,
    val description: String,

    val publisherId: String,
    val publisherName: String,

    val thumbnail: String,

    val word: List<Word>,

    val isDownloaded: Boolean,

    val downloadCount: Int,

    val connector: List<ProjectConnector>? = null,
    
    val isPublic: Boolean = false,
    
    val isOwned: Boolean = true,
    val isSaved: Boolean = false
)