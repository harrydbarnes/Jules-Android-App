package com.jules.loader.util

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import com.google.android.material.color.DynamicColors
import com.jules.loader.R

object ThemeUtils {
    private const val PREFS_NAME = "jules_settings"
    private const val KEY_USE_DYNAMIC_COLOURS = "use_dynamic_colours"
    private const val KEY_THEME_PREFERENCE = "theme_preference"

    const val THEME_OCTOPUS = "octopus"
    const val THEME_SQUID = "squid"

    fun isDynamicColorsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_USE_DYNAMIC_COLOURS, true)
    }

    fun setDynamicColorsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_USE_DYNAMIC_COLOURS, enabled) }
    }

    fun getSelectedTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME_PREFERENCE, THEME_OCTOPUS)!!
    }

    fun setSelectedTheme(context: Context, theme: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_THEME_PREFERENCE, theme) }
    }

    fun applyTheme(activity: Activity) {
        val theme = getSelectedTheme(activity)
        val isDynamic = isDynamicColorsEnabled(activity)

        // Set the base theme first
        if (theme == THEME_SQUID) {
            activity.setTheme(R.style.Theme_Squid)
        } else {
            activity.setTheme(R.style.Theme_Octopus)
        }

        // Apply dynamic colors overlay if enabled
        if (isDynamic) {
            DynamicColors.applyToActivityIfAvailable(activity)
        }
    }
}
