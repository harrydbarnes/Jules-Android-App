package com.jules.loader.ui

import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import com.jules.loader.data.JulesRepository
import com.jules.loader.data.model.SourceContext
import com.jules.loader.databinding.ActivityCreateTaskBinding
import kotlinx.coroutines.launch

class CreateTaskActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateTaskBinding
    private lateinit var repository: JulesRepository
    private var availableSources: List<SourceContext> = emptyList()

    companion object {
        private val TAG = CreateTaskActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set up shared element transition
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        // Transition name is set in the XML root view which will be binding.root
        // However, we need to set the enter shared element callback before content view
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content) // We are targeting the whole content
            duration = 300L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 250L
        }

        super.onCreate(savedInstanceState)
        binding = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ensure transition name is set on the root view if not already in XML (it is in XML)
        binding.root.transitionName = "shared_element_container"

        repository = JulesRepository.getInstance(applicationContext)

        setupRepoSelector()

        binding.btnStartTask.setOnClickListener {
            val prompt = binding.taskInput.text.toString().trim()
            if (prompt.isNotEmpty()) {
                submitTask(prompt)
            } else {
                binding.taskInput.error = "Please enter a task"
            }
        }

        // Handle Share Intent
        if (intent?.action == android.content.Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(android.content.Intent.EXTRA_TEXT)
            if (sharedText != null) {
                binding.taskInput.setText(sharedText)
            }
        }
    }

    private fun submitTask(prompt: String) {
        binding.btnStartTask.isEnabled = false
        binding.btnStartTask.text = "Starting..."

        val repo = binding.repoInput.text.toString().takeIf { it.isNotBlank() }
        val branch = binding.branchInput.text.toString().takeIf { it.isNotBlank() }

        lifecycleScope.launch {
            try {
                repository.createSession(prompt, repo, branch)
                Toast.makeText(this@CreateTaskActivity, "Task started successfully", Toast.LENGTH_SHORT).show()
                finish() // Return to MainActivity where the list will refresh
            } catch (e: Exception) {
                binding.btnStartTask.isEnabled = true
                binding.btnStartTask.text = "Start Octopus"
                Toast.makeText(this@CreateTaskActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupRepoSelector() {
        lifecycleScope.launch {
            try {
                availableSources = repository.getSources()
                val sourceNames = availableSources.map { it.source }
                val adapter = ArrayAdapter(
                    this@CreateTaskActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    sourceNames
                )
                binding.repoInput.setAdapter(adapter)

                binding.repoInput.setOnItemClickListener { _, _, position, _ ->
                    val selectedSource = availableSources.getOrNull(position)
                    selectedSource?.githubRepoContext?.startingBranch?.let { branch ->
                        binding.branchInput.setText(branch)
                    }
                }

                binding.repoInput.addTextChangedListener {
                    if (it.isNullOrBlank()) {
                        binding.branchInput.setText("")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to load sources", e)
                Toast.makeText(this@CreateTaskActivity, "Failed to load repositories", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
