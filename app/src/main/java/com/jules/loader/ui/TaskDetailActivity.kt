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

    private var nextPageToken: String? = null
    private var lastLoadedPageToken: String? = null
    private var isLoadingMore = false
    private val allLogs = java.util.Collections.synchronizedList(java.util.ArrayList<ActivityLog>())

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

        val WORKING_TYPES = setOf("WORKING", "COMMITTING_CODE", "EXECUTING TESTS", "RUNNING TESTS")
        val TERMINAL_STATES = setOf("COMPLETED", "FAILED", "CANCELLED", "TERMINATED")
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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        populateSessionDetails()

        // Setup Log Bottom Sheet
        val behavior = BottomSheetBehavior.from(binding.logBottomSheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // Setup Log RecyclerView
        binding.logRecyclerView.layoutManager = LinearLayoutManager(this)
        logAdapter = LogAdapter()
        binding.logRecyclerView.adapter = logAdapter

        binding.logRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoadingMore && nextPageToken != null) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                    ) {
                        loadMoreLogs()
                    }
                }
            }
        })

        sessionId?.let { id ->
            startPollingLogs(id)
        }

        binding.btnSend.setOnClickListener {
            val message = binding.inputMessage.text.toString().trim()
            if (message.isNotEmpty() && sessionId != null) {
                sendMessage(sessionId!!, message)
            }
        }
    }

    private fun sendMessage(sessionId: String, message: String) {
        lifecycleScope.launch {
            try {
                binding.btnSend.isEnabled = false
                val log = repository.createActivity(sessionId, message)
                binding.inputMessage.text?.clear()

                allLogs.add(log)
                logAdapter.submitList(ArrayList(allLogs))
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@TaskDetailActivity, "Failed to send message", android.widget.Toast.LENGTH_SHORT).show()
                android.util.Log.e("TaskDetailActivity", "Error sending message", e)
            } finally {
                binding.btnSend.isEnabled = true
            }
        }
    }

    private fun cancelSession() {
        if (sessionId == null) return
        lifecycleScope.launch {
            try {
                val session = repository.cancelSession(sessionId!!)
                binding.detailStatusChip.text = session.status
                android.widget.Toast.makeText(this@TaskDetailActivity, "Task cancelled", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@TaskDetailActivity, "Failed to cancel task", android.widget.Toast.LENGTH_SHORT).show()
                android.util.Log.e("TaskDetailActivity", "Error cancelling task", e)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_task_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cancel -> {
                cancelSession()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
                    val session = repository.getSession(id)
                    binding.detailStatusChip.text = session.status

                    if (!isLoadingMore) {
                        val response = repository.getActivities(id, pageToken = null)
                        addLogs(response.activities ?: emptyList(), prepend = false)

                        // Only set the initial token for backward pagination
                        if (nextPageToken == null) {
                            nextPageToken = response.nextPageToken
                        }
                    }

                    if (session.status != null && TERMINAL_STATES.contains(session.status.uppercase(java.util.Locale.ROOT))) {
                        break
                    }
                } catch (e: Exception) {
                    android.util.Log.e("TaskDetailActivity", "Error polling logs", e)
                }
                delay(POLLING_INTERVAL_MS)
            }
        }
    }

    private fun loadMoreLogs() {
        if (isLoadingMore || nextPageToken == null || sessionId == null) return
        isLoadingMore = true
        lifecycleScope.launch {
            try {
                val token = nextPageToken
                val response = repository.getActivities(sessionId!!, pageToken = token)

                // lastLoadedPageToken is less relevant now that polling always fetches latest,
                // but kept for consistency if we wanted to track pagination cursor.
                lastLoadedPageToken = token
                nextPageToken = response.nextPageToken

                addLogs(response.activities ?: emptyList(), prepend = true)
            } catch (e: Exception) {
                android.util.Log.e("TaskDetailActivity", "Error loading more logs", e)
            } finally {
                isLoadingMore = false
            }
        }
    }

    private fun addLogs(newLogs: List<ActivityLog>, prepend: Boolean) {
        // Append new logs avoiding duplicates
        val existingIds = allLogs.mapNotNull { it.id }.toSet()
        val uniqueNewLogs = newLogs.filter { it.id == null || !existingIds.contains(it.id) }

        if (uniqueNewLogs.isNotEmpty()) {
            if (prepend) {
                allLogs.addAll(0, uniqueNewLogs)
            } else {
                allLogs.addAll(uniqueNewLogs)
            }
            logAdapter.submitList(ArrayList(allLogs))
        }
    }

    class LogAdapter : ListAdapter<ActivityLog, LogAdapter.LogViewHolder>(LogDiffCallback()) {
        class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val typeText: TextView = view.findViewById(R.id.logType)
            val progress: View = view.findViewById(R.id.logProgress)
            val descText: TextView = view.findViewById(R.id.logDescription)
            val timeText: TextView = view.findViewById(R.id.logTimestamp)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity_log, parent, false)
            return LogViewHolder(view)
        }

        override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
            val log = getItem(position)
            val type = log.getResolvedType()
            holder.typeText.text = type
            holder.descText.text = log.getResolvedDescription()
            holder.timeText.text = log.timestamp ?: ""

            if (TaskDetailActivity.WORKING_TYPES.contains(type.uppercase(java.util.Locale.ROOT))) {
                holder.progress.visibility = View.VISIBLE
            } else {
                holder.progress.visibility = View.GONE
            }

            if (type.contains("CODE") || type.contains("FILE") || holder.descText.text.contains("```")) {
                holder.descText.typeface = android.graphics.Typeface.MONOSPACE
            } else {
                holder.descText.typeface = android.graphics.Typeface.DEFAULT
            }
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
