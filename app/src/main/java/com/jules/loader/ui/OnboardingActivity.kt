package com.jules.loader.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.jules.loader.MainActivity
import com.jules.loader.R
import com.jules.loader.data.JulesRepository

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val apiKeyInput = findViewById<TextInputEditText>(R.id.apiKeyInput)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val getKeyLink = findViewById<TextView>(R.id.getKeyLink)

        getKeyLink.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jules.google.com/settings#api"))
            startActivity(browserIntent)
        }

        saveButton.setOnClickListener {
            val key = apiKeyInput.text?.toString()?.trim()
            if (key.isNullOrEmpty()) {
                Toast.makeText(this, R.string.error_enter_api_key, Toast.LENGTH_SHORT).show()
            } else {
                val repo = JulesRepository(this)
                repo.saveApiKey(key)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}
