package com.jules.loader.util

import android.content.Context
import androidx.core.content.edit

object PreferenceUtils {
    private const val PREFS_NAME = "jules_settings"
    private const val KEY_SHORTEN_REPO_NAMES = "shorten_repo_names"

    fun isShortenRepoNamesEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SHORTEN_REPO_NAMES, true)
    }

    fun setShortenRepoNamesEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_SHORTEN_REPO_NAMES, enabled) }
    }
}
