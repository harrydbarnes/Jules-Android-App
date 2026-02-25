package com.jules.loader.ui

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jules.loader.R
import com.jules.loader.data.JulesRepository
import com.jules.loader.databinding.ActivitySettingsBinding
import com.jules.loader.util.PreferenceUtils
import com.jules.loader.util.ThemeUtils

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val repository: JulesRepository by lazy { JulesRepository.getInstance(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.menu_settings)

        setupThemeSelection()
        setupDisplaySettings()
        setupApiKeySection()
    }

    private fun setupApiKeySection() {
        val apiKey = repository.getApiKey()
        if (apiKey.isNullOrEmpty()) {
            binding.cardApiKey.visibility = View.GONE
        } else {
            binding.cardApiKey.visibility = View.VISIBLE
            binding.btnEditApiKey.setOnClickListener {
                showEditApiKeyDialog()
            }
            binding.btnRemoveApiKey.setOnClickListener {
                showRemoveApiKeyDialog()
            }
        }
    }

    private fun showEditApiKeyDialog() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.dialog_edit_api_key)

        val inputLayout = dialog.findViewById<TextInputLayout>(R.id.apiKeyInputLayout)!!
        val input = dialog.findViewById<TextInputEditText>(R.id.apiKeyInput)!!
        val saveButton = dialog.findViewById<Button>(R.id.saveButton)!!
        val progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)!!

        val apiKey = repository.getApiKey()
        if (!apiKey.isNullOrEmpty()) {
            input.setText(apiKey)
        }

        inputLayout.setEndIconOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            try {
                if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
                    val item = clipboard.primaryClip?.getItemAt(0)
                    val text = item?.text?.toString()
                    if (!text.isNullOrEmpty()) {
                        input.setText(text)
                    }
                }
            } catch (e: Exception) {
                // Ignore clipboard errors
            }
        }

        saveButton.setOnClickListener {
            val newKey = input.text?.toString()?.trim()
            if (newKey.isNullOrEmpty()) {
                return@setOnClickListener
            }

            if (newKey.length != 53) {
                Toast.makeText(this, getString(R.string.error_api_key_length), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveButton.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            input.isEnabled = false

            lifecycleScope.launchWhenStarted {
                val isValid = repository.validateApiKey(newKey)
                if (isValid) {
                    repository.saveApiKey(newKey)
                    setupApiKeySection()
                    Toast.makeText(this@SettingsActivity, getString(R.string.message_api_key_updated), Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    saveButton.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    input.isEnabled = true
                    Toast.makeText(this@SettingsActivity, getString(R.string.error_api_key_invalid), Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun showRemoveApiKeyDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_confirm_remove_title)
            .setMessage(R.string.dialog_confirm_remove_message)
            .setPositiveButton(R.string.action_remove) { _, _ ->
                repository.clearApiKey()
                val intent = Intent(this, OnboardingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("start_page", 2)
                startActivity(intent)
                finish()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun setupDisplaySettings() {
        binding.switchShortenRepoNames.isChecked = PreferenceUtils.isShortenRepoNamesEnabled(this)
        binding.switchShortenRepoNames.setOnCheckedChangeListener { _, isChecked ->
            PreferenceUtils.setShortenRepoNamesEnabled(this, isChecked)
        }
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
