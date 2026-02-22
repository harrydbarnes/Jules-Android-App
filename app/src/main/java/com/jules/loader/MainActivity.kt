package com.jules.loader

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jules.loader.data.JulesRepository
import com.jules.loader.databinding.ActivityMainBinding
import com.jules.loader.ui.BaseActivity
import com.jules.loader.ui.OnboardingActivity
import com.jules.loader.ui.SessionAdapter
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: JulesRepository
    private lateinit var adapter: SessionAdapter

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

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadSessions(forceRefresh = true, animate = false)
        }

        loadSessions(animate = savedInstanceState == null)
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadSessions(forceRefresh: Boolean = false, animate: Boolean = true) {
        val isPullToRefresh = binding.swipeRefreshLayout.isRefreshing
        if (!forceRefresh && repository.hasCachedData()) {
            binding.skeletonLayout.visibility = View.GONE
        } else if (!isPullToRefresh) {
            binding.skeletonLayout.visibility = View.VISIBLE
            binding.sessionsRecyclerView.visibility = View.GONE
        }

        binding.errorText.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val sessions = repository.getSessions(forceRefresh)
                adapter.submitList(sessions) {
                    if (animate) {
                        binding.sessionsRecyclerView.scheduleLayoutAnimation()
                    }
                }

                if (sessions.isEmpty()) {
                    binding.errorText.text = getString(R.string.no_sessions)
                    binding.errorText.visibility = View.VISIBLE
                } else {
                    binding.sessionsRecyclerView.visibility = View.VISIBLE
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
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}
