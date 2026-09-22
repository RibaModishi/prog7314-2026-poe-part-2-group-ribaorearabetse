package com.example.campus_companion_prog

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.example.campus_companion_prog.data.LocalPreferences
import com.example.campus_companion_prog.i18n.LocaleHelper
import com.example.campus_companion_prog.notifications.CampusMessagingService
import com.google.firebase.messaging.FirebaseMessaging

class CampusCompanionApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Application onCreate")
        LocaleHelper.applyAppLocale(LocalPreferences(this).preferredLanguage)
        ensureNotificationChannel()
        refreshFcmToken()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CampusMessagingService.CHANNEL_ID,
                getString(R.string.alerts),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    private fun refreshFcmToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.i(TAG, "FCM token ready")
                    LocalPreferences(this).fcmToken = token
                } else {
                    Log.w(TAG, "FCM token failed", task.exception)
                }
            }
        } catch (ex: Exception) {
            Log.w(TAG, "FCM not ready yet (google-services?)", ex)
        }
    }

    companion object {
        private const val TAG = "CampusCompanionApp"
    }
}
