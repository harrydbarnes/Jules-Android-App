package com.jules.loader.ui

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (24 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, 0)
        }

        val inputLayout = TextInputLayout(context).apply {
            hint = getString(R.string.api_key_hint)
            endIconMode = TextInputLayout.END_ICON_CUSTOM
            setEndIconDrawable(R.drawable.ic_content_paste)
            setEndIconContentDescription(R.string.paste_api_key)
        }

        val input = TextInputEditText(context).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }

        inputLayout.addView(input)
        layout.addView(inputLayout)

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

        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_api_key_title)
            .setView(layout)
            .setPositiveButton(R.string.action_save) { _, _ ->
                val newKey = input.text?.toString()?.trim()
                if (!newKey.isNullOrEmpty()) {
                    repository.saveApiKey(newKey)
                    setupApiKeySection()
                    Toast.makeText(this, getString(R.string.message_api_key_updated), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun showRemoveApiKeyDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_confirm_remove_title)
            .setMessage(R.string.dialog_confirm_remove_message)
            .setPositiveButton(R.string.action_remove) { _, _ ->
                repository.clearApiKey()
                val intent = Intent(this, OnboardingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
