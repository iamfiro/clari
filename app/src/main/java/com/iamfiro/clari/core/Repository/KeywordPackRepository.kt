package com.iamfiro.clari.core.repository

import com.iamfiro.clari.core.mapper.KeywordPackMapper
import com.iamfiro.clari.core.network.ApiClient
import com.iamfiro.clari.core.network.dto.AddKeywordRequest
import com.iamfiro.clari.core.network.dto.AutocompleteRequest
import com.iamfiro.clari.core.network.dto.AutofillRequest
import com.iamfiro.clari.core.network.dto.CreateKeywordPackRequest
import com.iamfiro.clari.core.network.dto.KeywordDto
import com.iamfiro.clari.core.network.dto.UpdateKeywordPackRequest
import com.iamfiro.clari.feature.project.model.Project
import com.iamfiro.clari.feature.project.model.Word

class KeywordPackRepository {
    
    companion object {
        @Volatile
        private var INSTANCE: KeywordPackRepository? = null
        
        fun getInstance(): KeywordPackRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: KeywordPackRepository().also { INSTANCE = it }
            }
        }
    }
    
    /**
     * 키워드팩 목록 조회
     */
    suspend fun getAllKeywordPacks(limit: Int = 50): Result<List<Project>> {
        return try {
            val response = ApiClient.keywordPackApi.getKeywordPacks(limit = limit)
            val packs = response.packs.map { KeywordPackMapper.fromDto(it) }
            Result.success(packs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 키워드팩 상세 조회
     */
    suspend fun getKeywordPackById(packId: String): Result<Project> {
        return try {
            val response = ApiClient.keywordPackApi.getKeywordPack(packId)
            val pack = KeywordPackMapper.fromDto(response.pack)
            Result.success(pack)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 키워드팩 생성
     */
    suspend fun createKeywordPack(
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
            val response = ApiClient.keywordPackApi.createKeywordPack(request)
            val pack = KeywordPackMapper.fromDto(response.pack)
            Result.success(pack)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 키워드 추가
     */
    suspend fun addKeyword(packId: String, word: Word): Result<Project> {
        return try {
            val request = AddKeywordRequest(
                name = word.name,
                description = word.meaning
            )
            val response = ApiClient.keywordPackApi.addKeyword(packId, request)
            val pack = KeywordPackMapper.fromDto(response.pack)
            Result.success(pack)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 키워드팩 수정 (이름, 키워드 목록, 공개 여부)
     */
    suspend fun updateKeywordPack(
        packId: String,
        name: String? = null,
        keywords: List<Word>? = null,
        isPublic: Boolean? = null,
        previewImageUrl: String? = null
    ): Result<Project> {
        return try {
            val keywordDtos = keywords?.let { KeywordPackMapper.wordsToKeywords(it) }
            val request = UpdateKeywordPackRequest(
                name = name,
                keywords = keywordDtos,
                isPublic = isPublic,
                previewImageUrl = previewImageUrl
            )
            val response = ApiClient.keywordPackApi.updateKeywordPack(packId, request)
            val pack = KeywordPackMapper.fromDto(response.pack)
            Result.success(pack)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 키워드 삭제 (전체 키워드 목록에서 제거 후 업데이트)
     */
    suspend fun removeKeyword(packId: String, wordName: String): Result<Project> {
        return try {
            // 먼저 현재 팩 조회
            val currentPack = getKeywordPackById(packId).getOrThrow()
            // 해당 키워드 제거
            val updatedWords = currentPack.word.filter { it.name != wordName }
            // 업데이트
            updateKeywordPack(packId, keywords = updatedWords)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 키워드팩 삭제
     */
    suspend fun deleteKeywordPack(packId: String): Result<Unit> {
        return try {
            ApiClient.keywordPackApi.deleteKeywordPack(packId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // AI Features
    
    /**
     * AI 자동완성 - 키워드 이름으로 설명 5개 제안
     */
    suspend fun aiAutocomplete(keywordName: String): Result<List<String>> {
        return try {
            val request = AutocompleteRequest(name = keywordName)
            val response = ApiClient.keywordPackApi.aiAutocomplete(request)
            Result.success(response.suggestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * AI 자동채우기 - 쿼리로 키워드 목록 생성
     */
    suspend fun aiAutofill(query: String, count: Int = 50): Result<List<Word>> {
        return try {
            val request = AutofillRequest(query = query, count = count)
            val response = ApiClient.keywordPackApi.aiAutofill(request)
            val words = response.keywords.map { KeywordPackMapper.keywordToWord(it) }
            Result.success(words)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

