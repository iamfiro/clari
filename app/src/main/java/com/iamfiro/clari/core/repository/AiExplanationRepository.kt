package com.iamfiro.clari.core.repository

import android.util.Log
import com.iamfiro.clari.core.network.ApiClient
import com.iamfiro.clari.core.network.dto.AiExplanationRequest
import com.iamfiro.clari.core.network.dto.AiExplanationResponse

private const val TAG = "AiExplanationRepository"

class AiExplanationRepository private constructor() {
    
    suspend fun explainTranscript(noteId: String, sentence: String): Result<AiExplanationResponse> {
        return try {
            Log.d(TAG, "AI 설명 요청 - noteId: $noteId, sentence: $sentence")
            val request = AiExplanationRequest(sentence = sentence)
            val response = ApiClient.noteApi.getAiExplanation(noteId, request)
            Log.d(TAG, "AI 설명 성공 - explanation length: ${response.explanation.length}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "AI 설명 실패", e)
            Result.failure(e)
        }
    }
    
    companion object {
        @Volatile
        private var instance: AiExplanationRepository? = null
        
        fun getInstance(): AiExplanationRepository {
            return instance ?: synchronized(this) {
                instance ?: AiExplanationRepository().also { instance = it }
            }
        }
    }
}
