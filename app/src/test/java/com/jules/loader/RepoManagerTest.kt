package com.jules.loader

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RepoManagerTest {

    @Before
    fun setup() {
        RepoManager.resetCache()
    }

    @Test
    fun testCaching() {
        val fakePrefs = FakeSharedPreferences()
        // Setup initial data: A JSON array of 2 repos
        // mimics ["https://google.com","https://jules.com"]
        fakePrefs.data["recent_repos"] = "[\"https://google.com\",\"https://jules.com\"]"

        // 1. First call: Should read from prefs
        val repos1 = RepoManager.getRecentRepos(fakePrefs)
        assertEquals(2, repos1.size)
        assertEquals("https://google.com", repos1[0])
        assertEquals(1, fakePrefs.getStringCallCount)

        // 2. Second call: Should use cache (no extra read)
        val repos2 = RepoManager.getRecentRepos(fakePrefs)
        assertEquals(2, repos2.size)
        assertEquals(1, fakePrefs.getStringCallCount) // Count should NOT increase
    }

    @Test
    fun testAddRepoUpdatesCache() {
        val fakePrefs = FakeSharedPreferences()
        fakePrefs.data["recent_repos"] = "[\"https://google.com\"]"

        // Initial fetch to populate cache
        RepoManager.getRecentRepos(fakePrefs)
        assertEquals(1, fakePrefs.getStringCallCount)

        // Add a new repo
        RepoManager.addRepo(fakePrefs, "https://example.com")

        // Verify it was written to prefs (serialized)
        val stored = fakePrefs.data["recent_repos"] as String
        assertTrue("Stored JSON should contain new repo", stored.contains("https://example.com"))

        // Verify next fetch uses cache
        val repos = RepoManager.getRecentRepos(fakePrefs)
        assertEquals(2, repos.size)
        assertEquals("https://example.com", repos[0])
        assertEquals(1, fakePrefs.getStringCallCount) // Still 1, did not read again
    }

    @Test
    fun testAddRepoSkipWriteIfAtTop() {
        val fakePrefs = FakeSharedPreferences()
        fakePrefs.data["recent_repos"] = "[\"https://top.com\",\"https://other.com\"]"

        // Initial fetch
        RepoManager.getRecentRepos(fakePrefs)
        val initialWrites = fakePrefs.applyCount

        // Add the top repo again
        RepoManager.addRepo(fakePrefs, "https://top.com")

        // Assert writes did NOT increase
        assertEquals("Should skip write if already at top", initialWrites, fakePrefs.applyCount)
    }

    // Fake implementation
    class FakeSharedPreferences : SharedPreferences {
        val data = mutableMapOf<String, Any?>()
        var getStringCallCount = 0
        var applyCount = 0

        override fun getAll(): MutableMap<String, *> = data
        override fun getString(key: String?, defValue: String?): String? {
            getStringCallCount++
            return data[key] as? String ?: defValue
        }

        override fun edit(): SharedPreferences.Editor = FakeEditor(this)

        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = null
        override fun getInt(key: String?, defValue: Int): Int = 0
        override fun getLong(key: String?, defValue: Long): Long = 0
        override fun getFloat(key: String?, defValue: Float): Float = 0f
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = false
        override fun contains(key: String?): Boolean = data.containsKey(key)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        class FakeEditor(private val prefs: FakeSharedPreferences) : SharedPreferences.Editor {
            private val changes = mutableMapOf<String, Any?>()
            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                key?.let { changes[it] = value }
                return this
            }
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor = this
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor = this
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = this
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = this
            override fun remove(key: String?): SharedPreferences.Editor = this
            override fun clear(): SharedPreferences.Editor = this
            override fun commit(): Boolean { apply(); return true }
            override fun apply() {
                prefs.applyCount++
                for ((k, v) in changes) {
                    prefs.data[k] = v
                }
            }
        }
    }
}
