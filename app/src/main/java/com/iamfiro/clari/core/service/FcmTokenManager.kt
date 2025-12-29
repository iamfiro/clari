package com.iamfiro.clari.core.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object FcmTokenManager {
    private const val TAG = "FcmTokenManager"

    /**
     * FCM 토큰을 가져옵니다
     */
    suspend fun getToken(): Result<String> {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "========================================")
            Log.d(TAG, "FCM 토큰 획득 성공")
            Log.d(TAG, "토큰: $token")
            Log.d(TAG, "토큰 길이: ${token.length}")
            Log.d(TAG, "========================================")
            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "FCM 토큰 획득 실패", e)
            Result.failure(e)
        }
    }

    /**
     * 특정 토픽을 구독합니다
     */
    suspend fun subscribeToTopic(topic: String): Result<Unit> {
        return try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            Log.d(TAG, "토픽 구독 성공: $topic")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "토픽 구독 실패: $topic", e)
            Result.failure(e)
        }
    }

    /**
     * 특정 토픽 구독을 해제합니다
     */
    suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        return try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
            Log.d(TAG, "토픽 구독 해제 성공: $topic")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "토픽 구독 해제 실패: $topic", e)
            Result.failure(e)
        }
    }
}
