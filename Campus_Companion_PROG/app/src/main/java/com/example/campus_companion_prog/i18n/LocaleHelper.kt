package com.example.campus_companion_prog.i18n

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    fun wrap(context: Context, languageCode: String): Context {
        val locale = localeFor(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocales(LocaleList(locale))
        return context.createConfigurationContext(config)
    }

    fun localeFor(code: String): Locale = when (code.lowercase()) {
        "zu" -> Locale("zu")
        "af" -> Locale("af")
        else -> Locale.ENGLISH
    }
}
