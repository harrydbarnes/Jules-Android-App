package com.jules.loader.util

import android.content.Context
import androidx.core.content.edit

object PreferenceUtils {
    private const val PREFS_NAME = "jules_settings"
    private const val KEY_SHORTEN_REPO_NAMES = "shorten_repo_names"

    private fun getPrefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isShortenRepoNamesEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SHORTEN_REPO_NAMES, true)
    }

    fun getDisplayRepoName(context: Context, fullName: String): String {
        return if (isShortenRepoNamesEnabled(context)) {
            fullName.substringAfterLast('/')
        } else {
            fullName
        }
    }

    fun setShortenRepoNamesEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_SHORTEN_REPO_NAMES, enabled) }
    }
}
