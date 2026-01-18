package com.jules.loader

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

object RepoManager {
    private const val PREFS_NAME = "jules_prefs"
    private const val KEY_RECENT_REPOS = "recent_repos"

    private val mutex = Mutex()

    @Volatile
    private var cachedRepos: List<String>? = null

    // VisibleForTesting
    suspend fun resetCache() {
        mutex.withLock {
            cachedRepos = null
        }
    }

    suspend fun getRecentRepos(context: Context): List<String> {
        return withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            getRecentRepos(prefs)
        }
    }

    suspend fun getRecentRepos(prefs: SharedPreferences): List<String> {
        cachedRepos?.let { return it }

        return mutex.withLock {
            cachedRepos?.let { return@withLock it }

            val loaded = loadFromPrefs(prefs)
            cachedRepos = loaded
            loaded
        }
    }

    suspend fun addRepo(context: Context, repo: String) {
        withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            addRepo(prefs, repo)
        }
    }

    suspend fun addRepo(prefs: SharedPreferences, repo: String) {
        val jsonToSave = mutex.withLock {
            if (cachedRepos == null) {
                cachedRepos = loadFromPrefs(prefs)
            }
            val currentRepos = cachedRepos!!

            if (currentRepos.isNotEmpty() && currentRepos[0] == repo) {
                return@withLock null
            }

            val repos = currentRepos.toMutableList()
            repos.remove(repo)
            repos.add(0, repo)
            if (repos.size > 10) {
                repos.removeAt(repos.size - 1)
            }
            val newRepos = repos.toList()
            cachedRepos = newRepos

            val jsonArray = JSONArray()
            for (r in newRepos) {
                jsonArray.put(r)
            }
            jsonArray.toString()
        }

        if (jsonToSave != null) {
            prefs.edit().putString(KEY_RECENT_REPOS, jsonToSave).apply()
        }
    }

    private fun loadFromPrefs(prefs: SharedPreferences): List<String> {
        val reposString = prefs.getString(KEY_RECENT_REPOS, "")
        if (reposString.isNullOrEmpty()) {
            return emptyList()
        }
        return try {
            val jsonArray = JSONArray(reposString)
            List(jsonArray.length()) { i -> jsonArray.getString(i) }
        } catch (e: JSONException) {
            reposString.split(",").filter { it.isNotEmpty() }
        }
    }
}
