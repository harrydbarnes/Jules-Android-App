package com.jules.loader.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jules.loader.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(com.jules.loader.R.string.menu_settings)

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)

        binding.switchUnlimitedTabs.isChecked = prefs.getBoolean("allow_unlimited_tabs", false)
        binding.switchUnlimitedTabs.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("allow_unlimited_tabs", isChecked).apply()
        }

        binding.switchDynamicColours.isChecked = prefs.getBoolean("use_dynamic_colours", true)
        binding.switchDynamicColours.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("use_dynamic_colours", isChecked).apply()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
