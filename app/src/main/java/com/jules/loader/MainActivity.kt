package com.jules.loader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.jules.loader.data.JulesRepository
import com.jules.loader.data.model.Session
import com.jules.loader.databinding.ActivityMainBinding
import com.jules.loader.ui.BaseActivity
import com.jules.loader.ui.OnboardingActivity
import com.jules.loader.ui.SessionAdapter
import com.jules.loader.ui.TaskDetailActivity
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: JulesRepository
    private lateinit var adapter: SessionAdapter
    private var allSessions: List<Session> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = JulesRepository.getInstance(applicationContext)

        if (repository.getApiKey().isNullOrEmpty()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.sessions_title)

        adapter = SessionAdapter()
        binding.sessionsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.sessionsRecyclerView.adapter = adapter

        binding.sessionsRecyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    binding.fab.shrink()
                } else if (dy < 0) {
                    binding.fab.extend()
                }
            }
        })

        binding.fab.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM) // Haptic
            val intent = Intent(this, com.jules.loader.ui.CreateTaskActivity::class.java)
            val options = android.app.ActivityOptions.makeSceneTransitionAnimation(
                this, binding.fab, "shared_element_container"
            )
            startActivity(intent, options.toBundle())
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadSessions(forceRefresh = true)
        }

        setupFilters()
        loadSessions()
    }

    private fun setupFilters() {
        binding.filterCategoriesGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.contains(R.id.chipStatus)) {
                binding.filterValuesScroll.visibility = View.VISIBLE
            } else {
                binding.filterValuesScroll.visibility = View.GONE
            }
            applyFilters()
        }

        binding.filterValuesGroup.setOnCheckedStateChangeListener { _, _ ->
            applyFilters()
        }
    }

    private fun applyFilters() {
        val selectedCategoryIds = binding.filterCategoriesGroup.checkedChipIds
        if (selectedCategoryIds.isEmpty()) {
             adapter.submitList(allSessions)
             return
        }

        var filteredList = allSessions

        if (selectedCategoryIds.contains(R.id.chipStatus)) {
            val selectedValueIds = binding.filterValuesGroup.checkedChipIds
            if (selectedValueIds.isNotEmpty()) {
                filteredList = filteredList.filter { session ->
                    val status = session.status
                    when {
                        selectedValueIds.contains(R.id.chipCompleted) -> status == "COMPLETED"
                        selectedValueIds.contains(R.id.chipInProgress) -> status == TaskDetailActivity.STATUS_PR_OPEN || status == TaskDetailActivity.STATUS_EXECUTING_TESTS
                        selectedValueIds.contains(R.id.chipPending) -> status == "Initialising" || status == null || status == "Idle"
                        selectedValueIds.contains(R.id.chipBlocked) -> status == "BLOCKED"
                        else -> true
                    }
                }
            }
        }

        adapter.submitList(filteredList)
    }


    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, com.jules.loader.ui.SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, com.jules.loader.ui.AboutActivity::class.java))
                true
            }
            R.id.action_gift -> {
                val url = "https://jules.google/docs/changelog"
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(this, Uri.parse(url))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadSessions(forceRefresh: Boolean = false) {
        val isFirstLoad = !forceRefresh && !repository.hasCachedSessions()
        if (isFirstLoad) {
            binding.skeletonLayout.visibility = View.VISIBLE
            binding.errorText.visibility = View.GONE
            binding.sessionsRecyclerView.visibility = View.GONE
        }

        lifecycleScope.launch {
            try {
                allSessions = repository.getSessions(forceRefresh)

                // If sessions are empty, show error/empty text immediately
                if (allSessions.isEmpty()) {
                    binding.errorText.text = getString(R.string.no_sessions)
                    binding.errorText.visibility = View.VISIBLE
                    adapter.submitList(emptyList()) // clear adapter
                } else {
                    binding.errorText.visibility = View.GONE
                    binding.sessionsRecyclerView.visibility = View.VISIBLE
                    applyFilters() // Apply filters to non-empty list
                    if (isFirstLoad) {
                        binding.sessionsRecyclerView.scheduleLayoutAnimation()
                    }
                }
            } catch (e: java.io.IOException) {
                binding.errorText.text = getString(R.string.error_loading_sessions, e.localizedMessage)
                binding.errorText.visibility = View.VISIBLE
                android.util.Log.e("MainActivity", "Error loading sessions", e)
            } catch (e: retrofit2.HttpException) {
                binding.errorText.text = getString(R.string.error_loading_sessions, e.message())
                binding.errorText.visibility = View.VISIBLE
                android.util.Log.e("MainActivity", "Error loading sessions", e)
            } finally {
                binding.skeletonLayout.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}
