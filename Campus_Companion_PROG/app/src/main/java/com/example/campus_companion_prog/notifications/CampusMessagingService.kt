package com.example.campus_companion_prog.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.campus_companion_prog.R
import com.example.campus_companion_prog.data.LocalPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives FCM pushes. Token is stored locally for demo evidence;
 * server-side device registration can be extended later.
 */
class CampusMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.i(TAG, "FCM token=$token")
        LocalPreferences(this).fcmToken = token
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.i(TAG, "FCM message from=${message.from}")
        if (!LocalPreferences(this).pushNotificationsEnabled) {
            Log.i(TAG, "Push disabled in Settings — ignoring")
            return
        }
        val title = message.notification?.title ?: message.data["title"] ?: "Campus Companion"
        val body = message.notification?.body ?: message.data["body"] ?: "New campus update"
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        ensureChannel()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_vector)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Campus alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "CampusFCM"
        const val CHANNEL_ID = "campus_alerts"
    }
}
