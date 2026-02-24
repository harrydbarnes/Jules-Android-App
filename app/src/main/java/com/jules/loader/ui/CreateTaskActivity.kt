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

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.contract.ActivityResultContracts

class CreateTaskActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateTaskBinding
    private lateinit var viewModel: CreateTaskViewModel

    private val speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (spokenText != null) {
                binding.taskInput.append(if (binding.taskInput.text.isNullOrEmpty()) spokenText else " $spokenText")
            }
        }
    }

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

        binding.taskInputLayout.setEndIconOnClickListener {
            startVoiceInput()
        }

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
                        val sourceNames = sources.map { it.source }.distinct()
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
                    viewModel.errorEvent.collect { errorResId ->
                        Toast.makeText(this@CreateTaskActivity, errorResId, Toast.LENGTH_LONG).show()
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
            val matchingSources = viewModel.availableSources.value.filter { it.source == selectedSourceName }
            val branches = matchingSources.mapNotNull { it.githubRepoContext?.startingBranch }.distinct()

            val branchAdapter = ArrayAdapter(
                this@CreateTaskActivity,
                android.R.layout.simple_dropdown_item_1line,
                branches
            )
            binding.branchInput.setAdapter(branchAdapter)

            if (branches.isNotEmpty()) {
                binding.branchInput.setText(branches.first(), false)
            } else {
                binding.branchInput.setText("")
            }
        }

        binding.repoInput.addTextChangedListener {
            if (it.isNullOrBlank()) {
                binding.branchInput.setText("")
            }
        }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe the task...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Voice input not supported", Toast.LENGTH_SHORT).show()
        }
    }
}
