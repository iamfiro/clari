package com.iamfiro.clari.core.repository

import android.util.Log
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

private const val TAG = "ProjectRepository"

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

    suspend fun getKeywordPackById(packId: String): Result<Project> {
        return try {
            Log.d(TAG, "프로젝트 조회 시작: packId=$packId")
            val response = ApiClient.projectAPI.getProject(packId)
            Log.d(TAG, "API 응답 수신: ${response.pack.name}, words=${response.pack.words.size}")
            val pack = KeywordPackMapper.fromDto(response.pack)
            Log.d(TAG, "매핑 완료: ${pack.name}, words=${pack.word.size}")
            Result.success(pack)
        } catch (e: Exception) {
            Log.e(TAG, "프로젝트 조회 실패: packId=$packId", e)
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
                description = word.meaning,
                koreanPronunciation = word.koreanPronunciation,
                synonyms = word.synonyms
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
            updateProject(
                packId = packId,
                name = currentPack.name,
                keywords = updatedWords,
                isPublic = currentPack.isPublic,
                previewImageUrl = currentPack.thumbnail
            )
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
    
    suspend fun cloudSave(packId: String): Result<String> {
        return try {
            Log.d(TAG, "Cloud Save 시작: packId=$packId")
            val response = ApiClient.projectAPI.cloudSave(packId)
            Log.d(TAG, "Cloud Save 성공: ${response.message}")
            Result.success(response.message)
        } catch (e: Exception) {
            Log.e(TAG, "Cloud Save 실패: packId=$packId", e)
            Result.failure(e)
        }
    }
    
    suspend fun cloudUnsave(packId: String): Result<String> {
        return try {
            Log.d(TAG, "Cloud Unsave 시작: packId=$packId")
            val response = ApiClient.projectAPI.cloudUnsave(packId)
            Log.d(TAG, "Cloud Unsave 성공: ${response.message}")
            Result.success(response.message)
        } catch (e: Exception) {
            Log.e(TAG, "Cloud Unsave 실패: packId=$packId", e)
            Result.failure(e)
        }
    }
}


