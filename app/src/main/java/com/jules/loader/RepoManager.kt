package com.jules.loader

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONException

object RepoManager {
    private const val PREFS_NAME = "jules_prefs"
    private const val KEY_RECENT_REPOS = "recent_repos"

    fun getRecentRepos(context: Context): List<String> {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val reposString = prefs.getString(KEY_RECENT_REPOS, "")
        if (reposString.isNullOrEmpty()) return emptyList()

        val list = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(reposString)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        } catch (e: JSONException) {
            // Log the exception for debugging purposes
            // Log.e("RepoManager", "Error parsing recent repos as JSON, falling back to comma-separated", e)
            return reposString.split(",").filter { it.isNotEmpty() }
        }
        return list
    }

    fun addRepo(context: Context, repo: String) {
        val repos = getRecentRepos(context).toMutableList()
        repos.remove(repo)
        repos.add(0, repo) // Add to top
        if (repos.size > 10) {
            repos.removeAt(repos.size - 1)
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        for (r in repos) {
            jsonArray.put(r)
        }
        prefs.edit().putString(KEY_RECENT_REPOS, jsonArray.toString()).apply()
    }
}
