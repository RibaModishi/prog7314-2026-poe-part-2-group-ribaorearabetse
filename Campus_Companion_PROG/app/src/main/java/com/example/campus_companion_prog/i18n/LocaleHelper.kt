package com.example.campus_companion_prog.i18n

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    fun applyAppLocale(languageCode: String) {
        val tag = when (languageCode.lowercase()) {
            "zu" -> "zu"
            "af" -> "af"
            else -> "en"
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }

    fun wrap(context: Context, languageCode: String): Context {
        val locale = localeFor(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocales(LocaleList(locale))
        return context.createConfigurationContext(config)
    }

    fun localeFor(code: String): Locale = when (code.lowercase()) {
        "zu" -> Locale.forLanguageTag("zu")
        "af" -> Locale.forLanguageTag("af")
        else -> Locale.ENGLISH
    }
}
