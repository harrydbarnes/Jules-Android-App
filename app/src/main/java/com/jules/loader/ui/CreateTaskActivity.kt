package com.jules.loader.ui

import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.jules.loader.data.JulesRepository
import com.jules.loader.databinding.ActivityCreateTaskBinding
import kotlinx.coroutines.launch

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTaskBinding
    private lateinit var repository: JulesRepository

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

        lifecycleScope.launch {
            try {
                repository.createSession(prompt)
                Toast.makeText(this@CreateTaskActivity, "Task started successfully", Toast.LENGTH_SHORT).show()
                finish() // Return to MainActivity where the list will refresh
            } catch (e: Exception) {
                binding.btnStartTask.isEnabled = true
                binding.btnStartTask.text = "Start Octopus"
                Toast.makeText(this@CreateTaskActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
