package com.iamfiro.clari.core.repository

import com.iamfiro.clari.core.mapper.KeywordPackMapper
import com.iamfiro.clari.core.network.ApiClient
import com.iamfiro.clari.core.network.dto.AddProjectWordRequest
import com.iamfiro.clari.core.network.dto.AutocompleteRequest
import com.iamfiro.clari.core.network.dto.AutofillRequest
import com.iamfiro.clari.core.network.dto.CreateKeywordPackRequest
import com.iamfiro.clari.core.network.dto.UpdateProjectRequest
import com.iamfiro.clari.feature.project.model.Project
import com.iamfiro.clari.feature.project.model.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProjectRepository {
    companion object {
        @Volatile
        private var INSTANCE: ProjectRepository? = null
        
        fun getInstance(): ProjectRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProjectRepository().also { INSTANCE = it }
            }
        }
    }

    suspend fun getProjects(limit: Int = 100): Result<List<Project>> {
        return try {
            val response = ApiClient.projectAPI.getProjects(limit = limit)
            val packs = response.packs.map { KeywordPackMapper.fromDto(it) }
            Result.success(packs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getProjectsFlow(limit: Int = 100): Flow<List<Project>> = flow {
        try {
            val response = ApiClient.projectAPI.getProjects(limit = limit)
            val packs = response.packs.map { KeywordPackMapper.fromDto(it) }
            emit(packs)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * 키워드팩 상세 조회
     */
    suspend fun getKeywordPackById(packId: String): Result<Project> {
        return try {
            val response = ApiClient.projectAPI.getProject(packId)
            val pack = KeywordPackMapper.fromDto(response.pack)
            Result.success(pack)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProject(
        name: String,
        keywords: List<Word> = emptyList(),
        isPublic: Boolean = false
    ): Result<Project> {
        return try {
            val keywordDtos = KeywordPackMapper.wordsToKeywords(keywords)
            val request = CreateKeywordPackRequest(
                name = name,
                keywords = keywordDtos,
                isPublic = isPublic
            )
            val response = ApiClient.projectAPI.createProject(request)
            val pack = KeywordPackMapper.fromDto(response.pack)
            Result.success(pack)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addProjectWord(packId: String, word: Word): Result<Project> {
        return try {
            val request = AddProjectWordRequest(
                name = word.name,
                description = word.meaning
            )
            val response = ApiClient.projectAPI.addProjectWord(packId, request)
            val pack = KeywordPackMapper.fromDto(response.pack)
            Result.success(pack)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProject(
        packId: String,
        name: String? = null,
        keywords: List<Word>? = null,
        isPublic: Boolean? = null,
        previewImageUrl: String? = null
    ): Result<Project> {
        return try {
            val keywordDtos = keywords?.let { KeywordPackMapper.wordsToKeywords(it) }
            val request = UpdateProjectRequest(
                name = name,
                keywords = keywordDtos,
                isPublic = isPublic,
                previewImageUrl = previewImageUrl
            )
            val response = ApiClient.projectAPI.updateProject(packId, request)
            val pack = KeywordPackMapper.fromDto(response.pack)
            Result.success(pack)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeProjectWord(packId: String, wordName: String): Result<Project> {
        return try {
            val currentPack = getKeywordPackById(packId).getOrThrow()
            val updatedWords = currentPack.word.filter { it.name != wordName }
            updateProject(packId, keywords = updatedWords)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProject(packId: String): Result<Unit> {
        return try {
            ApiClient.projectAPI.deleteProject(packId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateWordDescription(keywordName: String): Result<List<String>> {
        return try {
            val request = AutocompleteRequest(name = keywordName)
            val response = ApiClient.projectAPI.generateWordDescription(request)
            Result.success(response.suggestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateAutofill(query: String, count: Int = 50): Result<List<Word>> {
        return try {
            val request = AutofillRequest(query = query, count = count)
            val response = ApiClient.projectAPI.generateAutofill(request)
            val words = response.keywords.map { KeywordPackMapper.keywordToWord(it) }
            Result.success(words)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


