package com.jules.loader.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Window
import androidx.lifecycle.lifecycleScope
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jules.loader.R
import com.jules.loader.data.JulesRepository
import com.jules.loader.data.model.ActivityLog
import com.jules.loader.databinding.ActivityTaskDetailBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailBinding
    private lateinit var repository: JulesRepository
    private lateinit var logAdapter: LogAdapter
    private var sessionId: String? = null

    companion object {
        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        const val EXTRA_SESSION_TITLE = "EXTRA_SESSION_TITLE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

        // Configure Shared Element Transition
        setEnterSharedElementCallback(com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback())

        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 300L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 250L
        }

        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = JulesRepository(applicationContext)
        sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        binding.root.transitionName = "shared_element_container_${sessionId}"

        val title = intent.getStringExtra(EXTRA_SESSION_TITLE)
        binding.detailTitle.text = title ?: "Task Details"

        // Setup Log Bottom Sheet
        val behavior = BottomSheetBehavior.from(binding.logBottomSheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // Setup Log RecyclerView
        binding.logRecyclerView.layoutManager = LinearLayoutManager(this)
        logAdapter = LogAdapter(emptyList())
        binding.logRecyclerView.adapter = logAdapter

        sessionId?.let { id ->
            startPollingLogs(id)
        }
    }

    private fun startPollingLogs(id: String) {
        lifecycleScope.launch {
            while (isActive) { // Poll while the activity is alive
                try {
                    val logs = repository.getActivities(id)
                    logAdapter.updateLogs(logs)
                } catch (e: Exception) {
                    // Handle network error silently during polling or show a small error indicator
                    android.util.Log.e("TaskDetailActivity", "Error polling logs", e)
                }
                delay(3000) // Poll every 3 seconds
            }
        }
    }

    class LogAdapter(private var logs: List<ActivityLog>) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {
        class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text: TextView = view.findViewById(android.R.id.text1)
        }

        fun updateLogs(newLogs: List<ActivityLog>) {
            logs = newLogs
            notifyDataSetChanged() // For better performance, consider using DiffUtil here
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return LogViewHolder(view)
        }

        override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
            val log = logs[position]
            holder.text.text = "[${log.type}] ${log.description}"
        }

        override fun getItemCount() = logs.size
    }
}
