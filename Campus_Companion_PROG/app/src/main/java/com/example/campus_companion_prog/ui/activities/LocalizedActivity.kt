package com.example.campus_companion_prog.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campus_companion_prog.data.LocalPreferences
import com.example.campus_companion_prog.i18n.LocaleHelper

/** Applies the user's preferred language before each Activity inflates. */
open class LocalizedActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val lang = LocalPreferences(newBase).preferredLanguage
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }

    protected fun restartAppForLocale() {
        val intent = Intent(this, LaunchActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
