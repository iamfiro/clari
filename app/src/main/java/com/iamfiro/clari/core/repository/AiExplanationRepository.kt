package com.iamfiro.clari.core.repository

import kotlinx.coroutines.delay

class AiExplanationRepository private constructor() {
    
    // TODO: Replace with real AI API
    suspend fun explainTranscript(text: String): String {
        // 네트워크 대기 시뮬레이션
        delay(2000)
        
        // Mock AI 설명 데이터 반환
        return """
            이 발화는 화자가 특정 주제에 대해 설명하거나 의견을 제시하는 내용입니다.
            문맥상 주요 핵심은 다음과 같습니다:
            
            • 발화의 핵심 메시지를 이해하고
            • 화자의 의도와 감정을 파악하며
            • 전체 대화 흐름에서의 역할을 분석합니다
            
            실제 API 연동 시 이 부분이 GPT/Claude 등의 응답으로 대체됩니다.
        """.trimIndent()
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
