package com.iamfiro.clari.core.repository

import com.iamfiro.clari.core.network.ApiClient
import com.iamfiro.clari.core.network.dto.CancelSessionRequest
import com.iamfiro.clari.core.network.dto.CreateSessionRequest
import com.iamfiro.clari.core.network.dto.RecordingUrlResponse
import com.iamfiro.clari.core.network.dto.SessionResponse
import com.iamfiro.clari.core.network.dto.StopSessionRequest
import com.iamfiro.clari.core.network.dto.StopSessionResponse

class RecordingRepository {
    
    companion object {
        @Volatile
        private var INSTANCE: RecordingRepository? = null
        
        fun getInstance(): RecordingRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RecordingRepository().also { INSTANCE = it }
            }
        }
    }
    
    /**
     * 녹음 세션 생성
     */
    suspend fun createSession(
        title: String? = null,
        languageCode: String = "ko",
        keywordPackIds: List<String> = emptyList(),
        externalResourceIds: List<String> = emptyList()
    ): Result<SessionResponse> {
        return try {
            val request = CreateSessionRequest(
                title = title,
                languageCode = languageCode,
                keywordPackIds = keywordPackIds,
                externalResourceIds = externalResourceIds
            )
            val response = ApiClient.recordingApi.createSession(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 녹음 세션 중지 (트랜스크립션 시작)
     */
    suspend fun stopSession(sessionId: String): Result<StopSessionResponse> {
        return try {
            val request = StopSessionRequest(sessionId = sessionId)
            val response = ApiClient.recordingApi.stopSession(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 녹음 세션 취소 (노트 삭제)
     */
    suspend fun cancelSession(sessionId: String): Result<Unit> {
        return try {
            val request = CancelSessionRequest(sessionId = sessionId)
            ApiClient.recordingApi.cancelSession(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 녹음 파일 URL 조회
     */
    suspend fun getRecordingUrl(noteId: String): Result<RecordingUrlResponse> {
        return try {
            val response = ApiClient.recordingApi.getRecordingUrl(noteId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

