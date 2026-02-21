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

        setupThemeSelection()
    }

    private fun setupThemeSelection() {
        val currentTheme = ThemeUtils.getSelectedTheme(this)
        when (currentTheme) {
            ThemeUtils.THEME_SQUID -> binding.radioSquid.isChecked = true
            ThemeUtils.THEME_CUTTLEFISH -> binding.radioCuttlefish.isChecked = true
            else -> binding.radioOctopus.isChecked = true
        }

        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val newTheme = when (checkedId) {
                R.id.radio_squid -> ThemeUtils.THEME_SQUID
                R.id.radio_cuttlefish -> ThemeUtils.THEME_CUTTLEFISH
                else -> ThemeUtils.THEME_OCTOPUS
            }
            // Only recreate if changed to avoid loop (though RadioGroup listener usually fires only on change)
            if (newTheme != ThemeUtils.getSelectedTheme(this)) {
                ThemeUtils.setSelectedTheme(this, newTheme)
                recreate()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
