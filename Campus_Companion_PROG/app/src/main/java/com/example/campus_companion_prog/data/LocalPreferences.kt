package com.example.campus_companion_prog.data

import android.content.Context
import android.util.Log

/**
 * Device-local preference flags that are not yet backed by API fields
 * (notifications master toggle, biometric opt-in). Language is stored on the API.
 */
class LocalPreferences(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var pushNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_PUSH, true)
        set(value) {
            prefs.edit().putBoolean(KEY_PUSH, value).apply()
            Log.d(TAG, "pushNotificationsEnabled=$value")
        }

    var biometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC, false)
        set(value) {
            prefs.edit().putBoolean(KEY_BIOMETRIC, value).apply()
            Log.d(TAG, "biometricEnabled=$value")
        }

    var preferredLanguage: String
        get() = prefs.getString(KEY_LANG, "en") ?: "en"
        set(value) {
            prefs.edit().putString(KEY_LANG, value).apply()
            Log.d(TAG, "preferredLanguage=$value")
        }

    var fcmToken: String?
        get() = prefs.getString(KEY_FCM, null)
        set(value) {
            prefs.edit().putString(KEY_FCM, value).apply()
        }

    fun clearLocalCache() {
        val lang = preferredLanguage
        prefs.edit().clear().apply()
        preferredLanguage = lang
        Log.i(TAG, "Local preference cache cleared")
    }

    companion object {
        private const val TAG = "LocalPreferences"
        private const val PREFS_NAME = "campus_companion_prefs"
        private const val KEY_PUSH = "push_notifications_enabled"
        private const val KEY_BIOMETRIC = "biometric_enabled"
        private const val KEY_LANG = "preferred_language"
        private const val KEY_FCM = "fcm_token"
    }
}
