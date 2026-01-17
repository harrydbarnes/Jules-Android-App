package com.jules.loader

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity(), NewTabFragment.OnRepoSelectedListener, WebViewFragment.OnWebStateListener {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: TabsAdapter
    private val tabs = mutableListOf<TabInfo>()

    data class TabInfo(var title: String, var url: String?, val id: Long = System.nanoTime())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.app_title)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        if (savedInstanceState == null) {
            tabs.add(TabInfo("Jules", "https://jules.google.com"))
        } else {
            // Restore state if needed, simpler to just start fresh or handle config changes naturally
            // For now, simple start
             if (tabs.isEmpty()) tabs.add(TabInfo("Jules", "https://jules.google.com"))
        }

        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)
        adapter = TabsAdapter(this, tabs)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position].title
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.add(0, 100, 0, "Add Tab").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            100 -> { // Add Tab
                val prefs = getSharedPreferences("jules_prefs", android.content.Context.MODE_PRIVATE)
                val allowUnlimited = prefs.getBoolean("allow_unlimited_tabs", false)
                if (!allowUnlimited && tabs.size >= 3) {
                     android.widget.Toast.makeText(this, "Tab limit reached (3). Enable more in Settings.", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    tabs.add(TabInfo(getString(R.string.new_tab), null))
                    adapter.notifyItemInserted(tabs.size - 1)
                    viewPager.currentItem = tabs.size - 1
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRepoSelected(url: String, name: String) {
        val position = viewPager.currentItem
        // Replace current "New Tab" with WebView
        // We create a new TabInfo to ensure ID changes if we want to force recreation, 
        // OR we keep ID and just update content. 
        // If we keep ID, FragmentStateAdapter might keep the old NewTabFragment unless we force it to invalidate.
        // It's safer to replace the object in the list with a new ID so the adapter treats it as a new item.
        
        val newTab = TabInfo(name, url) // New ID generated
        tabs[position] = newTab
        adapter.notifyItemChanged(position)
        
        // Add to recent repos
        if (!url.contains("jules.google.com")) {
             RepoManager.addRepo(this, url)
        }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentByTag("f" + adapter.getItemId(viewPager.currentItem))
        if (currentFragment is WebViewFragment && currentFragment.canGoBack()) {
            currentFragment.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onUrlChanged(url: String, title: String?) {
        val position = viewPager.currentItem
        if (position >= 0 && position < tabs.size) {
            val tab = tabs[position]
            
            // Simple check: Only update if the URL actually changed or we need to capture title
            // Also, only strictly exclude the exact home page from "Recent Repos"
            
            tab.url = url
            
            val isHome = url.contains("jules.google.com")
            var newTitle = if (isHome) "Jules" else title ?: "Repo"

            if (!isHome && url.contains("github.com")) {
                try {
                    val uri = android.net.Uri.parse(url)
                    val segments = uri.pathSegments
                    if (segments.size >= 2) {
                        val repoName = "${segments[0]}/${segments[1]}"
                        newTitle = if (repoName.length > 20) repoName.take(20) + "..." else repoName
                    }
                } catch (e: Exception) {
                    // Fallback to title
                }
            }
            
            if (tab.title != newTitle) {
                tab.title = newTitle
                adapter.notifyItemChanged(position)
            }

            if (!isHome) {
                RepoManager.addRepo(this, url)
            }
        }
    }
    
    // Inner class for Adapter
    inner class TabsAdapter(activity: AppCompatActivity, private val tabs: List<TabInfo>) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = tabs.size

        override fun createFragment(position: Int): Fragment {
            val tab = tabs[position]
            return if (tab.url == null) {
                NewTabFragment.newInstance()
            } else {
                WebViewFragment.newInstance(tab.url!!)
            }
        }

        override fun getItemId(position: Int): Long {
            return tabs[position].id
        }
        
        override fun containsItem(itemId: Long): Boolean {
             return tabs.any { it.id == itemId }
        }
    }
}
