package com.jules.loader

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.jules.loader.data.JulesRepository
import com.jules.loader.ui.OnboardingActivity
import com.jules.loader.ui.SessionAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var repository: JulesRepository
    private lateinit var adapter: SessionAdapter
    private lateinit var skeletonLayout: View
    private lateinit var errorText: TextView
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = JulesRepository(this)

        if (repository.getApiKey().isNullOrEmpty()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.sessions_title)

        recyclerView = findViewById(R.id.sessionsRecyclerView)
        skeletonLayout = findViewById(R.id.skeletonLayout)
        errorText = findViewById(R.id.errorText)

        adapter = SessionAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val fab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM) // Haptic
            val intent = Intent(this, com.jules.loader.ui.CreateTaskActivity::class.java)
            val options = android.app.ActivityOptions.makeSceneTransitionAnimation(
                this, fab, "shared_element_container"
            )
            startActivity(intent, options.toBundle())
        }

        loadSessions()
    }

    private fun loadSessions() {
        skeletonLayout.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        recyclerView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val sessions = repository.getSessions()
                adapter.submitList(sessions)

                if (sessions.isEmpty()) {
                    errorText.text = getString(R.string.no_sessions)
                    errorText.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                errorText.text = getString(R.string.error_loading_sessions, e.localizedMessage)
                errorText.visibility = View.VISIBLE
                android.util.Log.e("MainActivity", "Error loading sessions", e)
            } finally {
                skeletonLayout.visibility = View.GONE
            }
        }
    }
}
