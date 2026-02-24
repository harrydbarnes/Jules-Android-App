package com.jules.loader.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognizerIntent: Intent
    private var isListening = false
    private var originalTextBeforeSpeech = ""

    companion object {
        private val TAG = CreateTaskActivity::class.java.simpleName
        private const val PERMISSION_REQUEST_AUDIO = 100
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
        setupVoiceInput()
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

    override fun onDestroy() {
        super.onDestroy()
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_AUDIO && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            toggleListening()
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
        binding.repoInput.setOnClickListener { binding.repoInput.showDropDown() }
        binding.branchInput.setOnClickListener { binding.branchInput.showDropDown() }

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

    private fun setupVoiceInput() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {
                isListening = true
                originalTextBeforeSpeech = binding.taskInput.text?.toString() ?: ""
                updateMicIconState(true)
                binding.taskInput.hint = "Listening..."
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
                updateMicIconState(false)
                binding.taskInput.hint = "Describe the task for Jules..."
            }
            override fun onError(error: Int) {
                isListening = false
                updateMicIconState(false)
                binding.taskInput.hint = "Describe the task for Jules..."
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    val newText = if (originalTextBeforeSpeech.isBlank()) spokenText else "$originalTextBeforeSpeech $spokenText"
                    binding.taskInput.setText(newText)
                    binding.taskInput.setSelection(newText.length)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    val newText = if (originalTextBeforeSpeech.isBlank()) spokenText else "$originalTextBeforeSpeech $spokenText"
                    binding.taskInput.setText(newText)
                    binding.taskInput.setSelection(newText.length)
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        binding.taskInputLayout.setEndIconOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_AUDIO)
            } else {
                toggleListening()
            }
        }
    }

    private fun toggleListening() {
        if (isListening) {
            speechRecognizer.stopListening()
        } else {
            speechRecognizer.startListening(speechRecognizerIntent)
        }
    }

    private fun updateMicIconState(listening: Boolean) {
        val colorAttr = if (listening) com.google.android.material.R.attr.colorPrimary else com.google.android.material.R.attr.colorOnSurfaceVariant
        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(colorAttr, typedValue, true)
        binding.taskInputLayout.setEndIconTintList(android.content.res.ColorStateList.valueOf(typedValue.data))
    }
}
