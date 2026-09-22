package com.davidegea.spotifystats.ui.settings

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList

object AppPreferences {
    fun store(context: Context) = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)
    fun theme(context: Context): String = store(context).getString("theme", "system") ?: "system"
    fun language(context: Context): String = if (Build.VERSION.SDK_INT >= 33) {
        context.getSystemService(LocaleManager::class.java).applicationLocales.toLanguageTags().ifBlank { "system" }
    } else store(context).getString("language", "system") ?: "system"

    fun setTheme(context: Context, value: String) { store(context).edit().putString("theme", value).apply() }

    fun setLanguage(context: Context, value: String) {
        if (Build.VERSION.SDK_INT >= 33) {
            context.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(if (value == "system") "" else value)
        } else {
            store(context).edit().putString("language", value).apply()
            var current = context
            while (current is android.content.ContextWrapper) {
                if (current is Activity) { current.recreate(); break }
                current = current.baseContext
            }
        }
    }

    fun localizedContext(context: Context): Context {
        if (Build.VERSION.SDK_INT >= 33) return context
        val language = language(context)
        if (language == "system") return context
        return context.createConfigurationContext(Configuration(context.resources.configuration).apply {
            setLocales(LocaleList.forLanguageTags(language))
        })
    }
}
