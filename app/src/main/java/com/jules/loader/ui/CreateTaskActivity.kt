package com.jules.loader.ui

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.jules.loader.R

class CreateTaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set up shared element transition
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        findViewById<android.view.View>(android.R.id.content).transitionName = "shared_element_container"
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
        setContentView(R.layout.activity_create_task)

        // Handle Share Intent
        if (intent?.action == android.content.Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(android.content.Intent.EXTRA_TEXT)
            if (sharedText != null) {
                val input = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.taskInput) // ID needs to be added to layout
                input?.setText(sharedText)
            }
        }
    }
}
