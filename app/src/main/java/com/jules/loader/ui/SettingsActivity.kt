package com.jules.loader.ui

import android.os.Bundle
import android.view.View
import com.jules.loader.R
import com.jules.loader.databinding.ActivitySettingsBinding
import com.jules.loader.util.ThemeUtils

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.menu_settings)

        setupDynamicColours()
        setupThemeSelection()
    }

    private fun setupDynamicColours() {
        val isDynamic = ThemeUtils.isDynamicColorsEnabled(this)
        binding.switchDynamicColours.isChecked = isDynamic
        binding.textDynamicWarning.visibility = if (isDynamic) View.VISIBLE else View.GONE

        // Disable theme selection if dynamic colors are enabled
        enableThemeSelection(!isDynamic)

        binding.switchDynamicColours.setOnCheckedChangeListener { _, isChecked ->
            ThemeUtils.setDynamicColorsEnabled(this, isChecked)
            binding.textDynamicWarning.visibility = if (isChecked) View.VISIBLE else View.GONE
            enableThemeSelection(!isChecked)
            recreate()
        }
    }

    private fun setupThemeSelection() {
        val currentTheme = ThemeUtils.getSelectedTheme(this)
        if (currentTheme == ThemeUtils.THEME_SQUID) {
            binding.radioSquid.isChecked = true
        } else {
            binding.radioOctopus.isChecked = true
        }

        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val newTheme = when (checkedId) {
                R.id.radio_squid -> ThemeUtils.THEME_SQUID
                else -> ThemeUtils.THEME_OCTOPUS
            }
            // Only recreate if changed to avoid loop (though RadioGroup listener usually fires only on change)
            if (newTheme != ThemeUtils.getSelectedTheme(this)) {
                ThemeUtils.setSelectedTheme(this, newTheme)
                recreate()
            }
        }
    }

    private fun enableThemeSelection(enabled: Boolean) {
        binding.radioGroupTheme.isEnabled = enabled
        for (i in 0 until binding.radioGroupTheme.childCount) {
            binding.radioGroupTheme.getChildAt(i).isEnabled = enabled
        }
        binding.radioGroupTheme.alpha = if (enabled) 1.0f else 0.5f
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
