package com.jules.loader

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONException
import kotlin.jvm.Synchronized

object RepoManager {
    private const val PREFS_NAME = "jules_prefs"
    private const val KEY_RECENT_REPOS = "recent_repos"

    @Volatile
    private var cachedRepos: List<String>? = null

    // VisibleForTesting
    @Synchronized
    fun resetCache() {
        cachedRepos = null
    }

    fun getRecentRepos(context: Context): List<String> {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return getRecentRepos(prefs)
    }

    fun getRecentRepos(prefs: SharedPreferences): List<String> {
        // First check (no lock)
        cachedRepos?.let { return it }

        // Perform I/O and parsing outside the lock
        val reposString = prefs.getString(KEY_RECENT_REPOS, "")
        val loadedRepos = if (reposString.isNullOrEmpty()) {
            emptyList<String>()
        } else {
            try {
                val jsonArray = JSONArray(reposString)
                // This is a more concise way to create the list from JSONArray
                List(jsonArray.length()) { i -> jsonArray.getString(i) }
            } catch (e: JSONException) {
                // The fallback logic is preserved
                reposString.split(",").filter { it.isNotEmpty() }
            }
        }

        return synchronized(this) {
            // Second check (inside lock)
            if (cachedRepos == null) {
                cachedRepos = loadedRepos
            }
            cachedRepos!!
        }
    }

    fun addRepo(context: Context, repo: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        addRepo(prefs, repo)
    }

    fun addRepo(prefs: SharedPreferences, repo: String) {
        val jsonToSave: String
        synchronized(this) {
            val repos = getRecentRepos(prefs).toMutableList()
            repos.remove(repo)
            repos.add(0, repo) // Add to top
            if (repos.size > 10) {
                repos.removeAt(repos.size - 1)
            }
            val newRepos = repos.toList()
            cachedRepos = newRepos // This is the critical write

            val jsonArray = JSONArray()
            for (r in newRepos) {
                jsonArray.put(r)
            }
            jsonToSave = jsonArray.toString()
        }
        // I/O is now outside the lock
        prefs.edit().putString(KEY_RECENT_REPOS, jsonToSave).apply()
    }
}
