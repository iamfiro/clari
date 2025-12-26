package com.iamfiro.clari.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectPacksResponse(
    val packs: List<ProjectDto>
)

@Serializable
data class ProjectPackResponse(
    val pack: ProjectDto
)

@Serializable
data class ProjectDto(
    val id: String,
    val name: String,
    val words: List<ProjectWordDTO> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
    val isPublic: Boolean,
    val previewImageUrl: String? = null
)

@Serializable
data class ProjectWordDTO(
    val name: String,
    val description: String
)

@Serializable
data class CreateKeywordPackRequest(
    val name: String,
    val keywords: List<ProjectWordDTO> = emptyList(),
    val isPublic: Boolean = false
)

@Serializable
data class AddProjectWordRequest(
    val name: String,
    val description: String
)

@Serializable
data class UpdateProjectRequest(
    val name: String? = null,
    val keywords: List<ProjectWordDTO>? = null,
    val isPublic: Boolean? = null,
    val previewImageUrl: String? = null
)

// "Lambda"라는 단어를 서버에 보냈을때 "AWS의 서버리스 컴퓨팅 서비스입니다." 와 같은 단어 설명을 여러개 받음
@Serializable
data class AutocompleteRequest(
    val name: String
)

@Serializable
data class AutocompleteResponse(
    val suggestions: List<String>
)

// "AWS 관련 단어 찾아줘" 요청하면 단어 n개를 보내줌
@Serializable
data class AutofillRequest(
    val query: String,
    val count: Int = 50
)

@Serializable
data class AutofillResponse(
    val keywords: List<ProjectWordDTO>
)


