package com.jules.loader.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jules.loader.MainActivity
import com.jules.loader.R
import com.jules.loader.data.JulesRepository
import com.jules.loader.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.getKeyLink.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jules.google.com/settings#api"))
            startActivity(browserIntent)
        }

        binding.saveButton.setOnClickListener {
            val key = binding.apiKeyInput.text?.toString()?.trim()
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
