package com.iamfiro.clari.core.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.iamfiro.clari.MainActivity
import com.iamfiro.clari.R

class ClariFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "ClariMessagingService"
        private const val CHANNEL_ID = "clari_notifications"
        private const val CHANNEL_NAME = "Clari 알림"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "========================================")
        Log.d(TAG, "새로운 FCM 토큰 생성")
        Log.d(TAG, "토큰: $token")
        Log.d(TAG, "========================================")
        
        // TODO: 서버에 토큰 전송
        sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "메시지 수신: ${message.from}")

        // 데이터 페이로드 처리
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "데이터 페이로드: ${message.data}")
            handleDataPayload(message.data)
        }

        // 알림 페이로드 처리
        message.notification?.let {
            Log.d(TAG, "알림 제목: ${it.title}, 내용: ${it.body}")
            sendNotification(it.title, it.body)
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        // 데이터 타입에 따라 다른 처리
        when (data["type"]) {
            "note_ready" -> {
                val noteId = data["noteId"]
                val title = data["title"] ?: "노트 준비 완료"
                val body = data["body"] ?: "녹음이 완료되었습니다"
                sendNotification(title, body, noteId)
            }
            "project_shared" -> {
                val projectId = data["projectId"]
                val title = data["title"] ?: "프로젝트 공유"
                val body = data["body"] ?: "새로운 프로젝트가 공유되었습니다"
                sendNotification(title, body, projectId)
            }
            else -> {
                val title = data["title"] ?: "Clari"
                val body = data["body"] ?: "새로운 알림이 도착했습니다"
                sendNotification(title, body)
            }
        }
    }

    private fun sendNotification(title: String?, body: String?, extraData: String? = null) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            extraData?.let { putExtra("data", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: 실제 아이콘으로 변경
            .setContentTitle(title ?: "Clari")
            .setContentText(body ?: "")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Clari 앱의 알림을 받습니다"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendTokenToServer(token: String) {
        // TODO: API를 통해 서버에 FCM 토큰 전송
        Log.d(TAG, "서버에 토큰 전송 필요: $token")
    }
}
