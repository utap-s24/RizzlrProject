package edu.utap.firenote.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import edu.utap.firenote.R

object ThemeUtils {
    private const val PREFS_NAME = "theme_preferences"
    private const val THEME_KEY = "theme_preference"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun loadThemePreference(context: Context): String {
        return getSharedPreferences(context).getString(THEME_KEY, "AppTheme.Default") ?: "AppTheme.Default"
    }

    fun saveThemePreference(context: Context, theme: String) {
        getSharedPreferences(context).edit().putString(THEME_KEY, theme).apply()
        applyTheme(theme)
    }

    fun getThemeResId(themeName: String): Int {
        return when (themeName) {
            "AppTheme.Light" -> R.style.AppTheme_Light
            "AppTheme.Dark" -> R.style.AppTheme_Dark
            else -> R.style.AppTheme // Default theme
        }
    }

    fun applyTheme(theme: String) {
        Log.d("ThemeUtils", "Applying theme: $theme")

        val mode = when (theme) {
            "AppTheme.Light" -> AppCompatDelegate.MODE_NIGHT_NO
            "AppTheme.Dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)

    }
}
