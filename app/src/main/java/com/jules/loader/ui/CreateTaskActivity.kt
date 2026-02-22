package com.jules.loader.ui

import android.os.Bundle
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.jules.loader.data.JulesRepository
import com.jules.loader.data.model.SourceContext
import com.jules.loader.databinding.ActivityCreateTaskBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateTaskActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateTaskBinding
    private lateinit var viewModel: CreateTaskViewModel
    private var availableSources: List<SourceContext> = emptyList()

    companion object {
        private val TAG = CreateTaskActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set up shared element transition
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 300L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 250L
        }

        super.onCreate(savedInstanceState)
        binding = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.transitionName = "shared_element_container"

        val repository = JulesRepository.getInstance(applicationContext)
        val factory = CreateTaskViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CreateTaskViewModel::class.java]

        setupRepoSelector()
        observeViewModel()

        binding.btnStartTask.setOnClickListener {
            val prompt = binding.taskInput.text.toString().trim()
            if (prompt.isNotEmpty()) {
                val repo = binding.repoInput.text.toString().takeIf { it.isNotBlank() }
                val branch = binding.branchInput.text.toString().takeIf { it.isNotBlank() }
                viewModel.submitTask(prompt, repo, branch)
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

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.availableSources.collectLatest { sources ->
                        availableSources = sources
                        val sourceNames = sources.map { it.source }
                        val adapter = ArrayAdapter(
                            this@CreateTaskActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            sourceNames
                        )
                        binding.repoInput.setAdapter(adapter)
                    }
                }

                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.btnStartTask.isEnabled = !isLoading
                        binding.btnStartTask.text = if (isLoading) "Starting..." else "Start Octopus"
                    }
                }

                launch {
                    viewModel.errorEvent.collect { errorMessage ->
                        Toast.makeText(this@CreateTaskActivity, errorMessage, Toast.LENGTH_LONG).show()
                        android.util.Log.e(TAG, errorMessage)
                    }
                }

                launch {
                    viewModel.taskCreatedEvent.collect {
                        Toast.makeText(this@CreateTaskActivity, "Task started successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun setupRepoSelector() {
        binding.repoInput.setOnItemClickListener { parent, _, position, _ ->
            val selectedSourceName = parent.getItemAtPosition(position) as String
            val selectedSource = availableSources.find { it.source == selectedSourceName }
            selectedSource?.githubRepoContext?.startingBranch?.let { branch ->
                binding.branchInput.setText(branch)
            }
        }

        binding.repoInput.addTextChangedListener {
            if (it.isNullOrBlank()) {
                binding.branchInput.setText("")
            }
        }
    }
}
