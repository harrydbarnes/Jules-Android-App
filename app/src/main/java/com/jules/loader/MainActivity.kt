package com.jules.loader

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jules.loader.data.JulesRepository
import com.jules.loader.databinding.ActivityMainBinding
import com.jules.loader.ui.OnboardingActivity
import com.jules.loader.ui.SessionAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: JulesRepository
    private lateinit var adapter: SessionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = JulesRepository(this)

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

        binding.fab.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM) // Haptic
            val intent = Intent(this, com.jules.loader.ui.CreateTaskActivity::class.java)
            val options = android.app.ActivityOptions.makeSceneTransitionAnimation(
                this, binding.fab, "shared_element_container"
            )
            startActivity(intent, options.toBundle())
        }

        loadSessions()
    }

    private fun loadSessions() {
        binding.skeletonLayout.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE
        binding.sessionsRecyclerView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val sessions = repository.getSessions()
                adapter.submitList(sessions)

                if (sessions.isEmpty()) {
                    binding.errorText.text = getString(R.string.no_sessions)
                    binding.errorText.visibility = View.VISIBLE
                } else {
                    binding.sessionsRecyclerView.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.errorText.text = getString(R.string.error_loading_sessions, e.localizedMessage)
                binding.errorText.visibility = View.VISIBLE
                android.util.Log.e("MainActivity", "Error loading sessions", e)
            } finally {
                binding.skeletonLayout.visibility = View.GONE
            }
        }
    }
}
