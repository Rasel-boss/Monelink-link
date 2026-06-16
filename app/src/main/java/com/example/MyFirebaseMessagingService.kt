package com.example

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.data.AppDatabase
import com.example.data.NotificationEntity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM Token: $token")
        
        // Save the token to local storage so the app can retrieve and display it
        val sharedPrefs = getSharedPreferences("monelink_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("fcm_token", token).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        val url = remoteMessage.data["url"] 
            ?: remoteMessage.data["link"] 
            ?: remoteMessage.data["click_action"]
            ?: remoteMessage.data["uri"]
            ?: remoteMessage.data["href"]
            ?: remoteMessage.data["action_url"]

        // Handle notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Notification Message Body: ${it.body}")
            val title = it.title ?: "Monelink Notification"
            val body = it.body ?: ""
            saveNotificationToDatabase(title, body, url)
            sendNotification(title, body, url)
            return
        }

        // Handle data payload (if any)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            
            val title = remoteMessage.data["title"]
                ?: remoteMessage.data["subject"]
                ?: remoteMessage.data["header"]
                ?: "Monelink Notification"
                
            val body = remoteMessage.data["body"]
                ?: remoteMessage.data["message"]
                ?: remoteMessage.data["alert"]
                ?: remoteMessage.data["text"]
                ?: remoteMessage.data["description"]
                ?: ""
                
            saveNotificationToDatabase(title, body, url)
            sendNotification(title, body, url)
        }
    }

    private fun saveNotificationToDatabase(title: String, body: String, url: String? = null) {
        try {
            val database = AppDatabase.getDatabase(applicationContext)
            kotlinx.coroutines.runBlocking {
                try {
                    val recent = database.notificationDao().getRecentNotifications()
                    val isDuplicate = recent.any { 
                        it.title == title && 
                        it.body == body && 
                        it.url == url && 
                        Math.abs(it.timestamp - System.currentTimeMillis()) < 60000 
                    }
                    if (!isDuplicate) {
                        val notification = NotificationEntity(title = title, body = body, url = url)
                        database.notificationDao().insertNotification(notification)
                        Log.d(TAG, "Saved Notification to DB: $title, URL: $url")
                    } else {
                        Log.d(TAG, "Duplicate notification detected in Service, skipping DB save.")
                    }
                } catch (dbError: Exception) {
                    Log.e(TAG, "Room Database Insert Error", dbError)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save notification to database", e)
        }
    }

    private fun sendNotification(title: String, messageBody: String, url: String? = null) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            url?.let { putExtra("url", it) }
            putExtra("title", title)
            putExtra("body", messageBody)
        }
        
        val notificationId = System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(
            this, 
            notificationId, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "monelink_fcm_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Standard clean material style fallback icon
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since Oreo (API 26), notification channel is mandatory
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Monelink Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel used for Firebase Cloud Messaging push notifications."
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MonelinkFCM"
    }
}
