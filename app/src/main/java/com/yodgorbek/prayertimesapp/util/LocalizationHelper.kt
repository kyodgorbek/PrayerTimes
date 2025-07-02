package com.yodgorbek.prayertimesapp.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocalizationHelper {
    fun setLocale(context: Context, languageCode: String?): Context {
        val code = when (languageCode?.toLowerCase(Locale.ROOT)) {
            "ru" -> "ru"
            "uz" -> "uz"
            "de" -> "de"
            else -> "en"
        }
        val locale = Locale(code)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun getLocalizedTimeFormat(context: Context): String {
        val language = context.resources.configuration.locales.get(0).language
        return when (language) {
            "ru", "de" -> "HH:mm"
            else -> "hh:mm a"
        }
    }
}
