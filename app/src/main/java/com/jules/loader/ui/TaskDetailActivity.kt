package com.jules.loader.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.jules.loader.R
import com.jules.loader.data.JulesRepository
import com.jules.loader.data.model.ActivityLog
import com.jules.loader.util.PreferenceUtils
import com.jules.loader.databinding.ActivityTaskDetailBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TaskDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityTaskDetailBinding
    private lateinit var repository: JulesRepository
    private lateinit var logAdapter: LogAdapter
    private var sessionId: String? = null

    companion object {
        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        const val EXTRA_SESSION_TITLE = "EXTRA_SESSION_TITLE"
        const val EXTRA_SESSION_PROMPT = "EXTRA_SESSION_PROMPT"
        const val EXTRA_SESSION_STATUS = "EXTRA_SESSION_STATUS"
        const val EXTRA_SESSION_SOURCE = "EXTRA_SESSION_SOURCE"
        const val EXTRA_SESSION_BRANCH = "EXTRA_SESSION_BRANCH"
        const val STATUS_PR_OPEN = "PR Open"
        const val STATUS_EXECUTING_TESTS = "Executing Tests"
        private const val POLLING_INTERVAL_MS = 3000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
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

        repository = JulesRepository.getInstance(applicationContext)
        sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        binding.root.transitionName = "shared_element_container_${sessionId}"

        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        populateSessionDetails()

        // Setup Log Bottom Sheet
        val behavior = BottomSheetBehavior.from(binding.logBottomSheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // Setup Log RecyclerView
        binding.logRecyclerView.layoutManager = LinearLayoutManager(this)
        logAdapter = LogAdapter()
        binding.logRecyclerView.adapter = logAdapter

        sessionId?.let { id ->
            startPollingLogs(id)
        }
    }

    private fun populateSessionDetails() {
        val title = intent.getStringExtra(EXTRA_SESSION_TITLE)
        val prompt = intent.getStringExtra(EXTRA_SESSION_PROMPT) ?: getString(R.string.no_prompt)
        val status = intent.getStringExtra(EXTRA_SESSION_STATUS) ?: "Initialising"
        val source = intent.getStringExtra(EXTRA_SESSION_SOURCE)
        val branch = intent.getStringExtra(EXTRA_SESSION_BRANCH)

        binding.detailTitle.text = title ?: getString(R.string.untitled_session)
        binding.detailPrompt.text = prompt
        binding.detailStatusChip.text = status

        when (status) {
            STATUS_PR_OPEN -> binding.detailStatusChip.setChipBackgroundColorResource(R.color.status_pr_open)
            STATUS_EXECUTING_TESTS -> binding.detailStatusChip.setChipBackgroundColorResource(R.color.status_tests_passing)
            "COMPLETED" -> binding.detailStatusChip.setChipBackgroundColorResource(R.color.status_tests_passing)
            else -> binding.detailStatusChip.setChipBackgroundColorResource(R.color.jules_purple_light)
        }

        if (source != null) {
            binding.detailSourceChip.visibility = View.VISIBLE
            val cleanSource = source.removePrefix("sources/github/")
            val displaySource = PreferenceUtils.getDisplayRepoName(this, cleanSource)
            binding.detailSourceChip.text = displaySource
        } else {
            binding.detailSourceChip.visibility = View.GONE
        }

        if (branch != null) {
            binding.detailBranchChip.visibility = View.VISIBLE
            binding.detailBranchChip.text = branch
        } else {
            binding.detailBranchChip.visibility = View.GONE
        }
    }

    private fun startPollingLogs(id: String) {
        lifecycleScope.launch {
            while (isActive) {
                try {
                    val logs = repository.getActivities(id)
                    logAdapter.submitList(logs)
                } catch (e: Exception) {
                    android.util.Log.e("TaskDetailActivity", "Error polling logs", e)
                }
                delay(POLLING_INTERVAL_MS)
            }
        }
    }

    class LogAdapter : ListAdapter<ActivityLog, LogAdapter.LogViewHolder>(LogDiffCallback()) {
        class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val typeText: TextView = view.findViewById(R.id.logType)
            val descText: TextView = view.findViewById(R.id.logDescription)
            val timeText: TextView = view.findViewById(R.id.logTimestamp)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity_log, parent, false)
            return LogViewHolder(view)
        }

        override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
            val log = getItem(position)
            holder.typeText.text = log.getResolvedType()
            holder.descText.text = log.getResolvedDescription()
            holder.timeText.text = log.timestamp ?: ""
        }

        class LogDiffCallback : DiffUtil.ItemCallback<ActivityLog>() {
            override fun areItemsTheSame(oldItem: ActivityLog, newItem: ActivityLog): Boolean {
                val oldIdentifier = oldItem.name ?: oldItem.id
                val newIdentifier = newItem.name ?: newItem.id
                return if (oldIdentifier != null && newIdentifier != null) {
                    oldIdentifier == newIdentifier
                } else {
                    oldItem === newItem
                }
            }

            override fun areContentsTheSame(oldItem: ActivityLog, newItem: ActivityLog): Boolean {
                return oldItem == newItem
            }
        }
    }
}
