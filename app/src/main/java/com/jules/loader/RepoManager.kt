package com.jules.loader

import android.content.Context
import android.content.SharedPreferences

object RepoManager {
    private const val PREFS_NAME = "jules_prefs"
    private const val KEY_RECENT_REPOS = "recent_repos"

    fun getRecentRepos(context: Context): List<String> {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val reposString = prefs.getString(KEY_RECENT_REPOS, "")
        if (reposString.isNullOrEmpty()) return emptyList()
        return reposString.split(",").filter { it.isNotEmpty() }
    }

    fun addRepo(context: Context, repo: String) {
        val repos = getRecentRepos(context).toMutableList()
        repos.remove(repo)
        repos.add(0, repo) // Add to top
        if (repos.size > 10) {
            repos.removeAt(repos.size - 1)
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_RECENT_REPOS, repos.joinToString(",")).apply()
    }
}
